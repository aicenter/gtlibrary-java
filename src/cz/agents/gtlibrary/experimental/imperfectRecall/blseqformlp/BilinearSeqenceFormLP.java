package cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponse;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.*;

public class BilinearSeqenceFormLP {
    private final Player player;
    private final Player opponent;
    private BilinearTable table;
    private Expander expander;
    private GameInfo gameInfo;
    private Double finalValue = null;


    private final double BILINEAR_PRECISION = 0.0001;
    private final double EPS = 0.000001;

    public static void main(String[] args) {
        runRandomGame();
//        runBRTest();
    }

    private static void runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);
        BilinearSeqenceFormLP solver = new BilinearSeqenceFormLP(BRTestGameInfo.FIRST_PLAYER, new RandomGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

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
        SequenceFormIRConfig config = new SequenceFormIRConfig();

        builder.build(new BRTestGameState(), config, new BRTestExpander<>(config));
        BilinearSeqenceFormLP solver = new BilinearSeqenceFormLP(BRTestGameInfo.FIRST_PLAYER, new BRTestGameInfo());

        solver.solve(config);
    }

    public BilinearSeqenceFormLP(Player player, GameInfo info) {
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

            lpData.getSolver().exportModel("bilinSQF.lp");
            lpData.getSolver().solve();
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());
            double lastSolution = lpData.getSolver().getObjValue();

            Set<Object> sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
//            Object sequenceToTighten = findMostViolatedBilinearConstraints(lpData);

            while (!sequencesToTighten.isEmpty()) {
//            while (sequenceToTighten != null) {
//                System.out.println("Refining " + sequencesToTighten);

//                for (Map.Entry<Object, IloNumVar[][]> entry : table.getwVariables().entrySet()) {
//                    System.out.println(entry.getKey());
//                    for (IloNumVar[] v : entry.getValue())
//                        for (IloNumVar w : v) {
//                            if (w == null) continue;
//                            double value = lpData.getSolver().getValue(w);
//                            if (value > 0) System.out.println(w + " = " + value);
//                        }
//                }


                if (table.isFixPreviousDigits()) table.storeWValues(lpData);
                for (Object s : sequencesToTighten) {
                    table.refinePrecision(lpData, s);
                }
//                System.out.println("----------------------------------------------------------------------------------------------------------");
                lpData.getSolver().exportModel("bilinSQF.lp");
                lpData.getSolver().solve();
                System.out.println(lpData.getSolver().getObjValue());
                if (Math.abs(lastSolution - lpData.getSolver().getObjValue()) < BILINEAR_PRECISION)
                    break;
                else
                    lastSolution = lpData.getSolver().getObjValue();
                sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
//                sequenceToTighten = findMostViolatedBilinearConstraints(lpData);
//                break;
            }
//            System.out.println("-------------------\nP1 Strategy");
            ImperfectRecallBestResponse br = new ImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);
            Map<Action, Double> P1Strategy = new HashMap<>();
            for (Sequence s : config.getSequencesFor(player)) {
                if (s.isEmpty()) continue;
                Action a = s.getLast();
//                System.out.println(s + " = " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]));
                double value = 0;
                if (((SequenceFormIRInformationSet)a.getInformationSet()).isHasIR()) value = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(a)]);
                else {
                    Sequence subS = s.getSubSequence(s.size() - 1);
                    double subSValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
                    if (subSValue < 1e-6) value = 0;
                    else value = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]) / subSValue;
                }
                P1Strategy.put(a,value);
            }
//            System.out.println("-------------------");
//            for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
//                for (Map.Entry<Sequence, Set<Sequence>> entry : i.getOutgoingSequences().entrySet()) {
//                        Sequence s = entry.getKey();
//                        Object o = new Pair<>(i, s);
//                        if (table.exists(o)) {
//                            System.out.println(i + " = " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(o)]));
//                        }
//                }
//            }
            br.getBestResponse(P1Strategy);
//            System.out.println("-------------------\nP1 Actions " + P1Strategy);
            finalValue = -br.getValue();

//            finalValue = lpData.getSolver().getObjValue();

//            for (Sequence sequence : config.getSequencesFor(player)) {
//                System.out.println(sequence + ": " + lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(sequence)]));
//            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void addBilinearConstraints(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(player)) {
            if (sequence.isEmpty())
                continue;

            if (!((SequenceFormIRInformationSet)sequence.getLastInformationSet()).isHasIR())
                continue;

//            Object eqKey = sequence;
//            Object bilinVarKey = new Pair<>(sequence.getSubSequence(sequence.size() - 1), sequence.getLast());

//            table.setConstraint(eqKey, bilinVarKey, 1);
//            table.setConstraint(eqKey, sequence, -1);
//            table.setConstraintType(eqKey, 1);
//            table.markAsBilinear(bilinVarKey, sequence.getSubSequence(sequence.size() - 1), sequence.getLast());
            table.markAsBilinear(sequence, sequence.getSubSequence(sequence.size() - 1), sequence.getLast());
        }
    }

    private void addValueConstraints(SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(opponent)) {
            Object eqKey;
            Object informationSet;
            Sequence subsequence;

            if (sequence.isEmpty()) {
                eqKey = "v_init";
                informationSet = "root";
                subsequence = new ArrayListSequenceImpl(opponent);
            } else {
                subsequence = sequence.getSubSequence(sequence.size() - 1);
                informationSet = sequence.getLastInformationSet();
                eqKey = new Triplet<>(informationSet, subsequence, sequence.getLast());
            }
            Object varKey = new Pair<>(informationSet, subsequence);

            table.setConstraint(eqKey, varKey, 1);
            table.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            for (SequenceFormIRInformationSet reachableSet : config.getReachableSets(sequence)) {
                if (!reachableSet.getActions().isEmpty() && reachableSet.getOutgoingSequences().get(sequence) != null && !reachableSet.getOutgoingSequences().get(sequence).isEmpty() && reachableSet.getPlayer().equals(opponent))
                    table.setConstraint(eqKey, new Pair<>(reachableSet, sequence), -1);
            }
            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
                Double utility = config.getUtilityFor(sequence, compatibleSequence);

                if (utility != null)
                    table.setConstraint(eqKey, compatibleSequence, -utility);
            }
        }
    }

    private void addBehaviorStrategyConstraints(SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (!informationSet.isHasIR())
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

    private Set<Object> findMostViolatedBilinearConstraints(LPData data) throws IloException{
        HashSet<Object> result = new HashSet<>();
//        double maxDifference = Double.NEGATIVE_INFINITY;

        for (Object productSequence : table.getBilinearVars().keySet()) {
            Object sequence = table.getBilinearVars().get(productSequence).getLeft();
            Object action = table.getBilinearVars().get(productSequence).getRight();

            Double prodValue = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]);
            Double seqValue = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(sequence)]);
            Double actValue = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(action)]);

//            if ((Math.abs(prodValue - seqValue*actValue) > BILINEAR_PRECISION) && (Math.abs(prodValue - seqValue*actValue) > maxDifference)) {
//                maxDifference = Math.abs(prodValue - seqValue*actValue);
            if (Math.abs(prodValue - seqValue*actValue) > BILINEAR_PRECISION) {
                result.add(productSequence);

//                System.out.println("Sequence to IS ( " + sequence + "): " + seqValue);
//                System.out.println("Behavioral action( " + action + "): " + actValue);
//                System.out.println("Sequence from IS: (" + productSequence + "): " + prodValue);
//                System.out.println("Other Sequences in that IS:");
//                IloNumVar[][] vars = table.getrHatVariables().get(productSequence);
//                for (int l=0; l<vars[0].length; l++) {
//                    for (int d=0; d<10; d++) {
//                        Double val = (vars[d][l] == null) ? null :(data.getSolver().getValue(vars[d][l]));
//                        if (val == null) continue;
//                        System.out.println("\t" + sequence + "[" + d + "][" + l + "] = " + val);
//                    }
//                }
//                vars = table.getwVariables().get(action);
//                for (int l=0; l<vars[0].length; l++) {
//                    for (int d=0; d<10; d++) {
//                        Double val = (vars[d][l] == null) ? null :(data.getSolver().getValue(vars[d][l]));
//                        System.out.println("\t" + action + "[" + d + "][" + l + "] = " + val);
//                    }
//                }
//                System.out.println("Other Behavioral in that IS:");
//                for (Sequence s : ((SequenceFormIRInformationSet)(((Action)action).getInformationSet())).getOutgoingSequencesFor((Sequence)sequence)) {
//                    System.out.println(s.getLast() + " = " + data.getSolver().getValue(data.getVariables()[table.getVariableIndex(s.getLast())]));
//                }
//
//                System.out.println();
//                break;
            }
        }

        return result;
    }

    public Expander getExpander() {
        return expander;
    }

    public void setExpander(Expander expander) {
        this.expander = expander;
    }
}
