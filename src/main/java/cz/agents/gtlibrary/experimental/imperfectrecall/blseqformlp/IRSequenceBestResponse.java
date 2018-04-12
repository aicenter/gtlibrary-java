package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.MILPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.IRInformationSetImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.*;

/**
 * Created by kail on 2/1/16.
 */
public class IRSequenceBestResponse {

    public static void main(String[] args) {
        GameState root = new BRTestGameState();
//        BRImperfectRecallAlgorithmConfig config = new BRImperfectRecallAlgorithmConfig();
//        BRTestExpander<IRInformationSetImpl> expander = new BRTestExpander<>(config);
//
//        BasicGameBuilder.build(root, config, expander);
//        ImperfectRecallBestResponse br = new ImperfectRecallBestResponse(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());
//
//        System.out.println(br.getBestResponse(getOpponentStrategy(root, expander)));
    }

    private static Map<Action, Double> getOpponentStrategy(GameState root, BRTestExpander<IRInformationSetImpl> expander) {
        Map<Action, Double> strategy = new HashMap<>(2);

        GameState state = root.performAction(expander.getActions(root).get(0));

        state = state.performAction(expander.getActions(state).get(0));
        List<Action> actions = expander.getActions(state);

        strategy.put(actions.get(0), 0.6);
        strategy.put(actions.get(1), 0.4);
        return strategy;
    }

    private Player player;
    private Player opponent;
    private SequenceFormIRConfig algConfig;
    private MILPTable milpTable;
    private GameInfo info;
    private Expander<SequenceFormIRInformationSet> expander;
    private double value;
    private double M;

    public IRSequenceBestResponse(Player player, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.player = player;
        this.opponent = info.getOpponent(player);
        this.info = info;
        this.expander = expander;
        algConfig = (SequenceFormIRConfig)(expander.getAlgorithmConfig());
        milpTable = new MILPTable();
        M = info.getMaxUtility()*2+1;
    }

    public Map<Action, Double> getBestResponseSequence(Map<Sequence, Double> opponentStrategy) {
        milpTable.clearTable();
        addYEqualities();
        addYInequalities();
        addBestActionEqualities();
        addVHatInequalities();
        addVIsSeqActionEqualities(opponentStrategy);
        addVSInequalities();
        addObjective(opponentStrategy);

        try {
            LPData lpData = milpTable.toCplex();

            lpData.getSolver().exportModel("BRMILP_new.lp");
            lpData.getSolver().solve();
            setValue(lpData.getSolver().getObjValue());

            for (Sequence s : algConfig.getSequencesFor(player)) {
                System.out.println(s + " = " + lpData.getSolver().getValue(lpData.getVariables()[milpTable.getVariableIndex(s)]));
            }

            for (Sequence s : algConfig.getSequencesFor(player)) {
                if (s.isEmpty()) continue;
                if (milpTable.exists(s.getLastInformationSet()))
                    System.out.println(s.getLastInformationSet() + " = " + lpData.getSolver().getValue(lpData.getVariables()[milpTable.getVariableIndex(s.getLastInformationSet())]));
            }

            for (Sequence s : algConfig.getSequencesFor(player)) {
                if (s.isEmpty()) continue;
                Object key = new Pair<>(s.getLastInformationSet(),s.getSubSequence(s.size() - 1));
                if (milpTable.exists(key)) {
                    System.out.println(key + " = " + lpData.getSolver().getValue(lpData.getVariables()[milpTable.getVariableIndex(key)]));
                }
                for (int i=0; i<2; i++) {
                    key = new Pair<>(new Triplet<>(s.getLastInformationSet(), s.getSubSequence(s.size() - 1), s.getLast()), i);
                    if (milpTable.exists(key))
                        System.out.println(key + " = " + lpData.getSolver().getValue(lpData.getVariables()[milpTable.getVariableIndex(key)]));
                }
            }


            return createStrategy(lpData);
        } catch (IloException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void addObjective(Map<Sequence, Double> opponentStrategy) {
        Sequence s = new ArrayListSequenceImpl(player);



        HashSet<SequenceFormIRInformationSet> topSets = new HashSet<SequenceFormIRInformationSet>();
        for (SequenceFormIRInformationSet reachableSet : algConfig.getReachableSets(s)) {
            if (!reachableSet.getActions().isEmpty() && reachableSet.getOutgoingSequences().get(s) != null && !reachableSet.getOutgoingSequences().get(s).isEmpty() && reachableSet.getPlayer().equals(player))
                topSets.add(reachableSet);
        }
        for (SequenceFormIRInformationSet i : topSets) {
            milpTable.setConstraint("v_init", i, 1);
        }
        double sumUtility = 0;
        for (Sequence ss : algConfig.getCompatibleSequencesFor(s)) {
            Double utility = algConfig.getUtilityFor(s, ss);

            if (utility != null) {
                Double strategy = opponentStrategy.get(ss);
                if (strategy == null) strategy = 0d;
                sumUtility +=  strategy * (utility);
            }
        }
        milpTable.setConstraint("v_init","V0",-1);
        milpTable.setConstant("v_init", sumUtility);
        milpTable.setConstraintType("v_init", 1);
        milpTable.setLowerBound("V0", Double.NEGATIVE_INFINITY);
        milpTable.setUpperBound("V0", Double.POSITIVE_INFINITY);
        milpTable.setObjective("V0", 1);
    }

    private Map<Action, Double> createStrategy(LPData lpData) {
        Map<Action, Double> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            try {
                strategy.put((Action) entry.getKey(), lpData.getSolver().getValue(entry.getValue()));
            } catch (IloException e) {
                e.printStackTrace();
            }
        }
        return strategy;
    }

    private void addYEqualities() {
        for (Map.Entry<ISKey, SequenceFormIRInformationSet> isKeyIEntry: algConfig.getAllInformationSets().entrySet()) {
            SequenceFormIRInformationSet infoSet = isKeyIEntry.getValue();
            if (!infoSet.getPlayer().equals(player)) continue;
            if (infoSet.getOutgoingSequences().isEmpty()) continue;

            boolean firstPass = true;

            for (Map.Entry<Sequence, Set<Sequence>> entry : infoSet.getOutgoingSequences().entrySet()) {
                milpTable.markAsBinary(entry.getKey());
                if (entry.getKey().isEmpty()) {
                    milpTable.setConstraint("root_init", entry.getKey(),1);
                    milpTable.setConstant("root_init", 1);
                    milpTable.setConstraintType("root_init", 1);
                }
                for (Sequence outSeq : entry.getValue()) {
                    Object action = outSeq.getLast();
                    milpTable.watchPrimalVariable(action,action);
                    milpTable.setConstraint(entry.getKey() + "_continuation_in_" + infoSet, outSeq, 1);
                    milpTable.markAsBinary(outSeq);
                    if (firstPass) {
                        milpTable.setConstraint(infoSet + "_action_sum", action, 1);
                        milpTable.markAsBinary(action);
                    }

                    milpTable.setConstraint(outSeq + "_action_" + action, outSeq, 1);
                    milpTable.setConstraint(outSeq + "_action_" + action, action, -1);
                    milpTable.setConstraintType(outSeq + "_action_" + action, 0);

//                    milpTable.watchPrimalVariable(outSeq,outSeq);
                }
                milpTable.setConstraint(entry.getKey() + "_continuation_in_" + infoSet, entry.getKey(), -1);
                milpTable.setConstraintType(entry.getKey() + "_continuation_in_" + infoSet, 1);
                if (firstPass) {
                    milpTable.setConstant(infoSet + "_action_sum", 1);
                    milpTable.setConstraintType(infoSet + "_action_sum", 1);
                    firstPass = false;
                }

            }
        }
    }

    private void addYInequalities() {
        for (Sequence s : algConfig.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            Action action = s.getLast();
//            milpTable.setConstraint(s + "_UB", s, 1);
//            milpTable.setConstraint(s + "_UB", action, -1);
//            milpTable.setConstraintType(s + "_UB", 0);

            Object slackAI = new Pair<>((SequenceFormIRInformationSet)action.getInformationSet(), action);
            milpTable.setConstraint(slackAI + "_UB", slackAI, 1);
            milpTable.setConstraint(slackAI + "_UB", action, M);
            milpTable.setConstant(slackAI + "_UB", M);
            milpTable.setConstraintType(slackAI + "_UB", 0);
            milpTable.setLowerBound(slackAI, 0);
            milpTable.setUpperBound(slackAI, Double.POSITIVE_INFINITY);
        }
    }

    private void addVIsSeqActionEqualities(Map<Sequence, Double> opponentStrategy) {
        for (Sequence s : algConfig.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            SequenceFormIRInformationSet infoSet = (SequenceFormIRInformationSet)s.getLastInformationSet();
            Action action = s.getLast();
            Sequence subSequence = s.getSubSequence(s.size() - 1);

            Object vSeqA = new Triplet<>(infoSet, subSequence, action);


            for (SequenceFormIRInformationSet reachableSet : algConfig.getReachableSets(s)) {
                if (!reachableSet.getActions().isEmpty() && reachableSet.getOutgoingSequences().get(s) != null && !reachableSet.getOutgoingSequences().get(s).isEmpty() && reachableSet.getPlayer().equals(player))
                    milpTable.setConstraint(vSeqA + "_exp_value", new Pair<>(reachableSet, s), -1);
            }
            double sumUtility = 0;
            Set<Sequence> sequenceSet = algConfig.getCompatibleSequencesFor(s);
            sequenceSet.add(new ArrayListSequenceImpl(opponent));
            for (Sequence compatibleSequence : sequenceSet) {
                Double utility = algConfig.getUtilityFor(s, compatibleSequence);

                if (utility != null) {
                    Double strategy = opponentStrategy.get(compatibleSequence);
                    if (strategy == null) strategy = 0d;
                     sumUtility +=  strategy * (utility);
                }
            }
            milpTable.setConstant(vSeqA + "_exp_value", sumUtility);
            milpTable.setConstraint(vSeqA + "_exp_value",vSeqA,1);
            milpTable.setConstraintType(vSeqA + "_exp_value", 1);
        }
    }

    private void addVSInequalities() {
        for (Sequence s : algConfig.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            SequenceFormIRInformationSet infoSet = (SequenceFormIRInformationSet) s.getLastInformationSet();
            Action action = s.getLast();
            Sequence subSequence = s.getSubSequence(s.size() - 1);

            Object vSeqA = new Triplet<>(infoSet, subSequence, action);
            Object vSeq = new Pair<>(infoSet, subSequence);

            milpTable.setConstraint(vSeqA + "_selected_value_1", vSeq, 1);
            milpTable.setConstraint(vSeqA + "_selected_value_1", vSeqA, -1);
            milpTable.setConstraint(vSeqA + "_selected_value_1", action, -M);
            milpTable.setConstant(vSeqA + "_selected_value_1", -M);
            milpTable.setConstraintType(vSeqA + "_selected_value_1", 2);

            milpTable.setConstraint(vSeqA + "_selected_value_2", vSeq, 1);
            milpTable.setConstraint(vSeqA + "_selected_value_2", vSeqA, -1);
            milpTable.setConstraint(vSeqA + "_selected_value_2", action, M);
            milpTable.setConstant(vSeqA + "_selected_value_2", M);
            milpTable.setConstraintType(vSeqA + "_selected_value_2", 0);

            milpTable.setLowerBound(vSeq, Double.NEGATIVE_INFINITY);
            milpTable.setUpperBound(vSeq, Double.POSITIVE_INFINITY);

            milpTable.setLowerBound(vSeqA, Double.NEGATIVE_INFINITY);
            milpTable.setUpperBound(vSeqA, Double.POSITIVE_INFINITY);

        }

        for (SequenceFormIRInformationSet i : algConfig.getAllInformationSets().values()) {
            if (i.getPlayer().equals(opponent)) continue;
            for (Sequence s : i.getOutgoingSequences().keySet())
                for (Sequence outS : i.getOutgoingSequences().get(s) ) {
                    Object vSeq = new Pair<>(i, s);
                    Object vSeqA = new Triplet<>(i, s, outS.getLast());

                    milpTable.setConstraint(vSeqA + "notselected_sigmas", vSeq, 1);
                    milpTable.setConstraint(vSeqA + "notselected_sigmas", vSeqA, -1);

                    for (Sequence ss : i.getOutgoingSequences().keySet()) {
                        milpTable.setConstraint(vSeqA + "notselected_sigmas",ss,-M*M);
                    }

                    milpTable.setConstraintType(vSeqA + "notselected_sigmas",0);
            }
        }
    }

    private void addBestActionEqualities() {
        Set<Object> addedSlacks = new HashSet<>();

        for (Sequence s : algConfig.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            SequenceFormIRInformationSet infoSet = (SequenceFormIRInformationSet) s.getLastInformationSet();

            Action action = s.getLast();
            Sequence subSequence = s.getSubSequence(s.size() - 1);
            Object slackAI = new Pair<>((SequenceFormIRInformationSet) action.getInformationSet(), action);
            Object vHat0 = new Pair<>(new Triplet<>(infoSet, subSequence, action), 0);
            Object vHat1 = new Pair<>(new Triplet<>(infoSet, subSequence, action), 1);

            if (!addedSlacks.contains(slackAI)) {
                milpTable.setConstraint(infoSet + "_best_action_" + action, infoSet, -1);
                milpTable.setConstraint(infoSet + "_best_action_" + action, slackAI, -1);
                milpTable.setConstraintType(infoSet + "_best_action_" + action, 1);
                addedSlacks.add(slackAI);
                milpTable.setLowerBound(infoSet,Double.NEGATIVE_INFINITY);
                milpTable.setUpperBound(infoSet, Double.POSITIVE_INFINITY);
            }

            milpTable.setConstraint(infoSet + "_best_action_" + action, vHat1, 1);
//            milpTable.setConstraint(infoSet + "_best_action_" + action, vHat0, 1/(info.getMaxUtility()* RandomGameInfo.MAX_BF));
        }
    }

    private void addVHatInequalities() {
        for (Sequence s : algConfig.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            SequenceFormIRInformationSet infoSet = (SequenceFormIRInformationSet)s.getLastInformationSet();
            Action action = s.getLast();
            Sequence subSequence = s.getSubSequence(s.size() - 1);
            Object vHat0 = new Pair<>(new Triplet<>(infoSet, subSequence, action), 0);
            Object vHat1 = new Pair<>(new Triplet<>(infoSet, subSequence, action), 1);
            Object vSeqA = new Triplet<>(infoSet, subSequence, action);

            milpTable.setLowerBound(vSeqA, Double.NEGATIVE_INFINITY);
            milpTable.setUpperBound(vSeqA, Double.POSITIVE_INFINITY);
            milpTable.setLowerBound(vHat0, Double.NEGATIVE_INFINITY);
            milpTable.setUpperBound(vHat0, Double.POSITIVE_INFINITY);
            milpTable.setLowerBound(vHat1, Double.NEGATIVE_INFINITY);
            milpTable.setUpperBound(vHat1, Double.POSITIVE_INFINITY);

            milpTable.setConstraint(s + "_VHAT1", vHat0, 1);
            milpTable.setConstraint(s + "_VHAT1", vHat1, 1);
            milpTable.setConstraint(s + "_VHAT1", vSeqA, -1);
            milpTable.setConstraintType(s + "_VHAT1", 1);

            milpTable.setConstraint(s + "_VHAT2_1", vHat0, 1);
            milpTable.setConstraint(s + "_VHAT2_1", subSequence, -M);
            milpTable.setConstant(s + "_VHAT2_1", -M);
            milpTable.setConstraintType(s + "_VHAT2_1",2);

            milpTable.setConstraint(s + "_VHAT2_2", vHat0, 1);
            milpTable.setConstraint(s + "_VHAT2_2", subSequence, M);
            milpTable.setConstant(s + "_VHAT2_2", M);
            milpTable.setConstraintType(s + "_VHAT2_2", 0);

            milpTable.setConstraint(s + "_VHAT3_1", vHat1, 1);
            milpTable.setConstraint(s + "_VHAT3_1", subSequence, M);
            milpTable.setConstraintType(s + "_VHAT3_1",2);

            milpTable.setConstraint(s + "_VHAT3_2", vHat1, 1);
            milpTable.setConstraint(s + "_VHAT3_2", subSequence, -M);
            milpTable.setConstraintType(s + "_VHAT3_2",0);
        }
    }


    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }


}
