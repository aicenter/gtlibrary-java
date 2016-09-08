package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponseImpl;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameAction;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BilinearTable;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;

import java.util.*;

public class BilinearSeqenceFormSingleOracle {
    private final Player player;
    private final Player opponent;
    private cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BilinearTable table;
    private Expander expander;
    private GameInfo gameInfo;
    private Double finalValue = null;

    private static boolean DEBUG = false;
    public static boolean SAVE_LPS = false;

    private Set<Set<Action>> bestResponses = new HashSet<>();


    static public final double BILINEAR_PRECISION = 0.0001;
    private final double EPS = 0.000001;

    public static void main(String[] args) {
        runRandomGame();
//        runBRTest();
    }

    private static void runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new RandomGameInfo());
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);

//        if (config.isPlayer2IR()) {
//            System.out.println(" Player 2 has IR ... skipping ...");
//            return;
//        }

        BilinearSeqenceFormSingleOracle solver = new BilinearSeqenceFormSingleOracle(BRTestGameInfo.FIRST_PLAYER, new RandomGameInfo());

        if (SAVE_LPS) {
            GambitEFG exporter = new GambitEFG();
            exporter.write("RG.gbt", root, expander);
        }

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        solver.solve(config);

        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);


//        System.out.println("IR SETS");
//        for (SequenceFormIRInformationSet is : config.getAllInformationSets().values()) {
//            if (is.isHasIR()) {
//                System.out.println(is.getISKey());
//            }
//        }
    }

    private static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());

        builder.build(new BRTestGameState(), config, new BRTestExpander<>(config));
        BilinearSeqenceFormSingleOracle solver = new BilinearSeqenceFormSingleOracle(BRTestGameInfo.FIRST_PLAYER, new BRTestGameInfo());

        solver.solve(config);
    }

    public BilinearSeqenceFormSingleOracle(Player player, GameInfo info) {
        this.table = new BilinearTable();
        this.player = player;
        this.opponent = info.getOpponent(player);
        this.gameInfo = info;
    }

    public void solve(SequenceFormIRConfig config) {
        addObjective();
        addRPConstraints(config);
        addBehaviorStrategyConstraints(config);
        addBilinearConstraints(config);
        addValueConstraints(config);
        try {
            LPData lpData = table.toCplex();

            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
            lpData.getSolver().solve();
//            System.out.println(lpData.getSolver().getStatus());
            System.out.println("LP Value = " + lpData.getSolver().getObjValue());
            double lastSolution = lpData.getSolver().getObjValue();

            Map<Action, Double> P1Strategy = extractBehavioralStrategy(config, lpData);

            ImperfectRecallBestResponseImpl br = new ImperfectRecallBestResponseImpl(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);
            Map<Action, Double> brActions = br.getBestResponse(P1Strategy);

            int iteration = 0;

            while (lastSolution - (-br.getValue()) > BILINEAR_PRECISION) {
                if (!addNewBR(config, brActions, iteration, lpData)) {
                   if (!tightenIntervals(lpData)) {
                       break;
                   }
                }

                if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
                lpData.getSolver().solve();
//                System.out.println(lpData.getSolver().getStatus());
                System.out.println("LP Value = " + lpData.getSolver().getObjValue());
                lastSolution = lpData.getSolver().getObjValue();

                P1Strategy = extractBehavioralStrategy(config, lpData);

                br.getBestResponse(P1Strategy);
                brActions = br.getBestResponse(P1Strategy);
                System.out.println("BR Value = " + -br.getValue());
                iteration++;
            }




            if (DEBUG) System.out.println("-------------------");
            if (DEBUG) {
                for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
                    for (Map.Entry<Sequence, Set<Sequence>> entry : i.getOutgoingSequences().entrySet()) {
                        Sequence s = entry.getKey();
                        Object o = new Pair<>(i, s);
                        if (table.exists(o)) {
                            System.out.println(i + " = " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(o)]));
                        }
                    }
                }
            }
            finalValue = -br.getValue();
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private boolean tightenIntervals(LPData lpData) throws IloException{
        Set<Action> sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
        if (sequencesToTighten.isEmpty())
            return false;
        while (!sequencesToTighten.isEmpty()) {

            if (DEBUG) System.out.println(sequencesToTighten);

            if (table.isFixPreviousDigits()) table.storeWValues(lpData);
            for (Action s : sequencesToTighten) {
                table.refinePrecision(lpData, s);
            }

            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQF.lp");
            lpData.getSolver().solve();
            System.out.println("Refining LP : " + lpData.getSolver().getObjValue());

            sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
        }
        return true;
    }

    private void addBilinearConstraints(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(player)) {
            if (sequence.isEmpty())
                continue;

            if (!((SequenceFormIRInformationSet)sequence.getLastInformationSet()).hasIR())
                continue;

            table.markAsBilinear(sequence, sequence.getSubSequence(sequence.size() - 1), sequence.getLast());
        }
    }

    private void addValueConstraints(SequenceFormIRConfig config) {
        Object eqKey = "v_init";
        Object informationSet = "root";
        Object varKey = new Pair<>(informationSet, new ArrayListSequenceImpl(opponent));
        table.setConstraint(eqKey,varKey,-1);
        table.setConstraintType(eqKey, 2);
        table.setLowerBound(varKey,Double.NEGATIVE_INFINITY);
        table.setUpperBound(varKey, Double.POSITIVE_INFINITY);
        for (GameState leaf : config.getTerminalStates()) {

            Sequence sequence = leaf.getSequenceFor(player);
            Sequence oppSequence = leaf.getSequenceFor(opponent);

            boolean first = true;
            for (Action a : oppSequence) {
                if (!((RandomGameAction)a).getValue().endsWith("_0")) {
                    first = false;
                    break;
                }
            }

            if (first) {
                Double utility = config.getUtilityFor(sequence, oppSequence);

                if (utility != null)
                    table.setConstraint(eqKey, sequence, utility);
            }

        }
    }

    private void addBehaviorStrategyConstraints(SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (!informationSet.hasIR())
                continue;
            if (informationSet.getPlayer().equals(player)) {
                for (Action action : informationSet.getActions()) {
                    table.setConstraint(informationSet, action, 1);
                    table.setLowerBound(action, 0);
                    table.setUpperBound(action, 1);
                }
                table.setConstant(informationSet, 1);
                table.setConstraintType(informationSet, 1);
            }
        }
    }

    private void addRPConstraints(SequenceFormIRConfig config) {
        table.setConstraint("rpInit", new ArrayListSequenceImpl(player), 1);
        table.setConstant("rpInit", 1);
        table.setConstraintType("rpInit", 1);
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player))
                addRPConstraint(informationSet);
        }
        addRPVarBounds(config);
    }

    private void addRPVarBounds(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(player)) {
            table.setLowerBound(sequence, 0);
            table.setUpperBound(sequence, 1);
        }
    }

    private void addRPConstraint(SequenceFormIRInformationSet informationSet) {
        for (Map.Entry<Sequence, Set<Sequence>> outgoingEntry : informationSet.getOutgoingSequences().entrySet()) {
            Object eqKey = new Pair<>(informationSet, outgoingEntry.getKey());

            table.setConstraint(eqKey, outgoingEntry.getKey(), 1);
            table.setConstraintType(eqKey, 1);
            for (Sequence sequence : outgoingEntry.getValue()) {
                table.setConstraint(eqKey, sequence, -1);
            }
        }
    }

    private void addObjective() {
        table.setObjective(new Pair<>("root", new ArrayListSequenceImpl(opponent)), 1);
    }

    private Set<Action> findMostViolatedBilinearConstraints(LPData data) throws IloException{
        HashSet<Action> result = new HashSet<>();
        HashSet<Action> result2 = new HashSet<>();

        for (Object productSequence : table.getBilinearVars().keySet()) {
            Object sequence = table.getBilinearVars().get(productSequence).getLeft();
            Object action = table.getBilinearVars().get(productSequence).getRight();

            if (data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)) > BILINEAR_PRECISION) {
                if (DEBUG) System.out.println("X DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)));
                result.add((Action)action);
            }

            if (data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)) > BILINEAR_PRECISION) {
                if (DEBUG) System.out.println("SEQ DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)));
                result.add((Action)action);
            }

        }

//        return result;

        double maxDifference = Double.NEGATIVE_INFINITY;

        for (Action a : result) {
            SequenceFormIRInformationSet is = (SequenceFormIRInformationSet)a.getInformationSet();
            double average = 0;
            ArrayList<Double> specValues = new ArrayList<>();

            for (Map.Entry<Sequence, Set<Sequence>> entry : is.getOutgoingSequences().entrySet()) {
                if (data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]) > 0) {
                    Sequence productSequence = new ArrayListSequenceImpl(entry.getKey());
                    productSequence.addLast(a);
                    double sV = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]);
                    sV =  sV / data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]);
                    average += sV;
                    specValues.add(sV);
                }
            }

            average = average / specValues.size();

            double error = 0;
            for (double d : specValues) {
                error += Math.abs(average - d);
            }

            if (error > maxDifference) {
                result2.clear();
                result2.add(a);
                maxDifference = error;
            }
        }

        return result2;
    }

    public Expander getExpander() {
        return expander;
    }

    public void setExpander(Expander expander) {
        this.expander = expander;
    }

    public Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException{
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> P1Strategy = new HashMap<>();
        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
            if (!i.getPlayer().equals(player)) continue;
            boolean allZero = true;
            for (Action a : i.getActions()) {
                double average = 0;
                int count = 0;
                for (Sequence subS : i.getOutgoingSequences().keySet()) {
                    Sequence s = new ArrayListSequenceImpl(subS);
                    s.addLast(a);

                    if (lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]) > 0) {
                        double sV = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
                        sV =  sV / lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
                        average += sV;
                        count++;
                    }
                }
                if (count == 0) average = 0;
                else average = average / count;

                if (DEBUG) System.out.println(a + " = " + average);
                P1Strategy.put(a, average);

                if (average > 0) allZero = false;
            }
            if (allZero && i.getActions().size() > 0) {
                P1Strategy.put(i.getActions().iterator().next(), 1d);
            }
        }
        return P1Strategy;

    }

    public Map<Sequence, Double> extractRPStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException{
        Map<Sequence, Double> P1StrategySeq = new HashMap<>();
        for (Sequence s : config.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            Action a = s.getLast();
            double seqValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
            if (DEBUG) System.out.println(s + " = " + seqValue);

            P1StrategySeq.put(s, lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]));
        }
        return P1StrategySeq;
    }

    private boolean addNewBR(SequenceFormIRConfig config, Map<Action, Double> brActions, int iteration, LPData lpData) throws IloException{
        Set<Action> thisBR = new HashSet<>();
        for (Map.Entry<Action, Double> e : brActions.entrySet()) {
            if (e.getValue() > 0) {
                thisBR.add(e.getKey());
            }
        }
        if (bestResponses.contains(thisBR)) {
            return false;
        }


        String eqKey = "v_init_" + iteration;
        Object informationSet = "root";
        IloNumVar rootValue = lpData.getVariables()[table.getVariableIndex(new Pair<>(informationSet, new ArrayListSequenceImpl(opponent)))];
        IloNumExpr newBRExpr = lpData.getSolver().numExpr();

        for (GameState leaf : config.getTerminalStates()) {

            Sequence sequence = leaf.getSequenceFor(player);
            Sequence oppSequence = leaf.getSequenceFor(opponent);

            boolean positive = true;
            for (Action a : oppSequence) {
                if (!thisBR.contains(a)) {
                    positive = false;
                    break;
                }
            }

            if (positive) {
                Double utility = config.getUtilityFor(sequence, oppSequence);

                if (utility != null) {
                    IloNumVar seq =  lpData.getVariables()[table.getVariableIndex(sequence)];
                    newBRExpr = lpData.getSolver().sum(lpData.getSolver().prod(utility, seq), newBRExpr);

                    table.setConstraint(eqKey, sequence, utility);
                }
            }

        }

        lpData.getSolver().addLe(rootValue, newBRExpr, eqKey);

        bestResponses.add(thisBR);
        return true;
    }

}
