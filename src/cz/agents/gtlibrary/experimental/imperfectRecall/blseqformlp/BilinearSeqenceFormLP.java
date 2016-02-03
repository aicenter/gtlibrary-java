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
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;

import java.util.*;

public class BilinearSeqenceFormLP {
    private final Player player;
    private final Player opponent;
    private BilinearTable table;
    private Expander expander;
    private GameInfo gameInfo;
    private Double finalValue = null;

    private boolean DEBUG = false;


    static public final double BILINEAR_PRECISION = 0.0001;
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

//        if (config.isPlayer2IR()) {
//            System.out.println(" Player 2 has IR ... skipping ...");
//            return;
//        }

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
            Map<Action, Double> P1Strategy = extractBehavioralStrategy(config, lpData);

            while (!sequencesToTighten.isEmpty()) {

                if (table.isFixPreviousDigits()) table.storeWValues(lpData);
                for (Object s : sequencesToTighten) {
                    table.refinePrecision(lpData, s);
                }
//                System.out.println("----------------------------------------------------------------------------------------------------------");
                lpData.getSolver().exportModel("bilinSQF.lp");
                lpData.getSolver().solve();
                System.out.println(lpData.getSolver().getObjValue());

                P1Strategy = extractBehavioralStrategy(config, lpData);
//                System.out.println("-------------------\nP1 Actions " + P1Strategy);


//                if (Math.abs(lastSolution - lpData.getSolver().getObjValue()) < BILINEAR_PRECISION) {
//                    break;
//                } else {
//                    lastSolution = lpData.getSolver().getObjValue();
//                }
                sequencesToTighten = findMostViolatedBilinearConstraints(lpData);
            }
            ImperfectRecallBestResponse br = new ImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);

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
            IRSequenceBestResponse br2 = new IRSequenceBestResponse(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);
            System.out.println(br2.getBestResponseSequence(extractRPStrategy(config, lpData)));
            br.getBestResponse(P1Strategy);
//            br.getBestResponseSequence(P1StrategySeq);
            System.out.println("-------------------\nP1 Actions " + extractRPStrategy(config, lpData));
            finalValue = -br.getValue();
            System.out.println("NEW BR = " + br2.getValue());

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

            if (!((SequenceFormIRInformationSet)sequence.getLastInformationSet()).hasIR())
                continue;

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

    private Set<Object> findMostViolatedBilinearConstraints(LPData data) throws IloException{
        HashSet<Object> result = new HashSet<>();

        for (Object productSequence : table.getBilinearVars().keySet()) {
            Object sequence = table.getBilinearVars().get(productSequence).getLeft();
            Object action = table.getBilinearVars().get(productSequence).getRight();

            if (data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)) > BILINEAR_PRECISION) {
                if (DEBUG) System.out.println("X DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaBehavioralVariables().get(action)));
                result.add(productSequence);
            }

            if (data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)) > BILINEAR_PRECISION) {
                if (DEBUG) System.out.println("SEQ DELTA " + action + " = " + data.getSolver().getValue(table.getDeltaSequenceVariables().get(productSequence)));
                result.add(productSequence);
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

    public Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException{
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> P1Strategy = new HashMap<>();
        for (Sequence s : config.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            Action a = s.getLast();
            double seqValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
            double value = 0;
            if (((SequenceFormIRInformationSet)a.getInformationSet()).hasIR()) value = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(a)]);
            else {
                Sequence subS = s.getSubSequence(s.size() - 1);
                double subSValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
                if (subSValue < 1e-10) value = 0;
                else value = seqValue / subSValue;
            }
            if (DEBUG) System.out.println(a + " = " + value);
            P1Strategy.put(a,value);
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

}
