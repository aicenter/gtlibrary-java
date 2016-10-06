package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.io.PartialGambitEFG;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReducedSingleOracleGameExpander extends SingleOracleGameExpander {
    private Set<GameState> temporaryLeafBlackList;

    public ReducedSingleOracleGameExpander(Player maxPlayer, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        super(maxPlayer, root, expander, info);
        temporaryLeafBlackList = new HashSet<>();
    }

    @Override
    public boolean expand(SequenceFormIRConfig config, OracleCandidate candidate) {
        brTime = 0;
        long start = mxBean.getCurrentThreadCpuTime();
        int terminalLeafCount = config.getTerminalStates().size();
        int sequenceCount = config.getAllSequences().size();
        int informationSetCount = config.getAllInformationSets().size();

        if (OracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states before expand: " + config.getTerminalStates().size());
            System.out.println("information sets before expand: " + config.getAllInformationSets().size());
            System.out.println("sequences before expand: " + config.getAllSequences().size());
        }
        Map<Action, Double> filteredMinPlayerBestResponse = removeUnreachableActions(candidate);

        expandRecursively(root, config, filteredMinPlayerBestResponse);
        if (OracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states after expand: " + config.getTerminalStates().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getTerminalStates().size());
            System.out.println("information sets after expand: " + config.getAllInformationSets().size() + " vs " + expander.getAlgorithmConfig().getAllInformationSets().size());
            System.out.println("sequences after expand: " + config.getAllSequences().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getAllSequences().size());
        }
        if (OracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        config.updateUtilitiesReachableBySequences();
        selfTime = (long) ((mxBean.getCurrentThreadCpuTime() - start) / 1e6 - brTime);
        return config.getTerminalStates().size() > terminalLeafCount || config.getAllSequences().size() > sequenceCount || config.getAllInformationSets().size() > informationSetCount;
    }

    @Override
    public boolean expand(SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        brTime = 0;
        long start = mxBean.getCurrentThreadCpuTime();
        int terminalLeafCount = config.getTerminalStates().size();

        if (OracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states before expand: " + config.getTerminalStates().size());
            System.out.println("information sets before expand: " + config.getAllInformationSets().size());
            System.out.println("sequences before expand: " + config.getAllInformationSets().size());
        }
        Map<Action, Double> filteredMinPlayerBestResponse = removeUnreachableActions(minPlayerBestResponse);

        expandRecursively(root, config, filteredMinPlayerBestResponse);
        if (OracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states after expand: " + config.getTerminalStates().size() + " vs " + expander.getAlgorithmConfig().getAllInformationSets().size());
            System.out.println("sequences after expand: " + config.getAllSequences().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getAllSequences().size());
            System.out.println("information sets after expand: " + config.getAllInformationSets().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getTerminalStates().size());
        }
        if (OracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        config.updateUtilitiesReachableBySequences();
        selfTime = (long) ((mxBean.getCurrentThreadCpuTime() - start) / 1e6 - brTime);
        return config.getTerminalStates().size() > terminalLeafCount;
    }

    /**
     * Removes actions played in unreachable parts of the tree
     *
     * @param candidate
     * @return
     */
    private Map<Action, Double> removeUnreachableActions(OracleCandidate candidate) {
        return removeUnreachableActionsRecursively(root, candidate, new HashMap<>());
    }

    private Map<Action, Double> removeUnreachableActionsRecursively(GameState state, OracleCandidate candidate, Map<Action, Double> filteredStrategy) {
        if (state.isGameEnd())
            return filteredStrategy;
        if (state.isPlayerToMoveNature()) {
            expander.getActions(state).forEach(a -> removeUnreachableActionsRecursively(state.performAction(a), candidate, filteredStrategy));
        } else if (state.getPlayerToMove().equals(maxPlayer)) {
            for (Action action : expander.getActions(state)) {
                GameState nextState = state.performAction(action);

                if (candidate.getMaxPlayerRealPlan().getOrDefault(nextState.getSequenceFor(maxPlayer), 0d) > 1e-8)
                    removeUnreachableActionsRecursively(nextState, candidate, filteredStrategy);
            }
        } else {
            for (Action action : expander.getActions(state)) {
                double actionProbability = candidate.getMinPlayerBestResponse().getOrDefault(action, 0d);

                filteredStrategy.put(action, actionProbability);
                if (actionProbability > 1e-8)
                    removeUnreachableActionsRecursively(state.performAction(action), candidate, filteredStrategy);
            }
        }
        return filteredStrategy;
    }

    /**
     * Removes actions played in unreachable parts of the tree
     *
     * @param minPlayerBestResponse
     * @return
     */
    private Map<Action, Double> removeUnreachableActions(Map<Action, Double> minPlayerBestResponse) {
        return removeUnreachableActionsRecursively(root, minPlayerBestResponse, new HashMap<>());
    }

    private Map<Action, Double> removeUnreachableActionsRecursively(GameState state, Map<Action, Double> minPlayerBestResponse, Map<Action, Double> filteredStrategy) {
        if (state.isGameEnd())
            return filteredStrategy;
        if (state.isPlayerToMoveNature()) {
            expander.getActions(state).forEach(a -> removeUnreachableActionsRecursively(state.performAction(a), minPlayerBestResponse, filteredStrategy));
        } else if (state.getPlayerToMove().equals(maxPlayer)) {
            for (Action action : expander.getActions(state)) {
                GameState nextState = state.performAction(action);

                if ((action.equals(((SequenceFormIRInformationSet) action.getInformationSet()).getFirst())))
                    removeUnreachableActionsRecursively(nextState, minPlayerBestResponse, filteredStrategy);
            }
        } else {
            for (Action action : expander.getActions(state)) {
                double actionProbability = minPlayerBestResponse.getOrDefault(action, 0d);

                filteredStrategy.put(action, actionProbability);
                if (actionProbability > 1e-8)
                    removeUnreachableActionsRecursively(state.performAction(action), minPlayerBestResponse, filteredStrategy);
            }
        }
        return filteredStrategy;
    }

    @Override
    protected void removeTemporaryLeaf(GameState state, SequenceFormIRConfig config) {
        temporaryLeafBlackList.add(state);
        super.removeTemporaryLeaf(state, config);
    }

    @Override
    protected void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        if (temporaryLeafBlackList.contains(state))
            return;
        super.addTemporaryLeafIfNotPresent(state, config, minPlayerBestResponse);
    }

    //    protected void expandRecursively(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
//        config.addInformationSetFor(state);
//        if (state.isGameEnd())
//            return;
//        if (state.getPlayerToMove().equals(minPlayer)) {
//            boolean added = false;
//
//            for (Action action : expander.getActions(state)) {
//                if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8) {
//                    expandRecursively(state.performAction(action), config, minPlayerBestResponse);
//                    added = true;
//                }
//            }
//            if (added) {
//                removeTemporaryLeaf(state, config);
//                temporaryLeafBlackList.add(state);
//            } else {
//                addTemporaryLeafIfNotPresent(state, config, minPlayerBestResponse);
//            }
//            return;
//        }
//        for (Action action : expander.getActions(state)) {
//            GameState nextState = state.performAction(action);
//
//            if (state.isPlayerToMoveNature()) {
//                expandRecursively(nextState, config, minPlayerBestResponse);
//            } else if (action.equals(((SequenceFormIRInformationSet)action.getInformationSet()).getOutgoingSequences().values().iterator().next().iterator().next().getLast())) {
//                expandRecursively(nextState, config, minPlayerBestResponse);
//            } else {
//                expandRecursivelyNotPlayed(nextState, config, minPlayerBestResponse);
//            }
//        }
//    }
//
//    protected void expandRecursively(GameState state, SequenceFormIRConfig config, OracleCandidate candidate) {
//        config.addInformationSetFor(state);
//        if (state.isGameEnd())
//            return;
//        if (state.getPlayerToMove().equals(minPlayer)) {
//            boolean added = false;
//
//            for (Action action : expander.getActions(state)) {
//                if (candidate.getMinPlayerBestResponse().getOrDefault(action, 0d) > 1e-8) {
//                    expandRecursively(state.performAction(action), config, candidate);
//                    added = true;
//                }
//            }
//            if (added) {
//                removeTemporaryLeaf(state, config);
//                temporaryLeafBlackList.add(state);
//            } else {
//                addTemporaryLeafIfNotPresent(state, config, candidate.getMinPlayerBestResponse());
//            }
//            return;
//        }
//
//        for (Action action : expander.getActions(state)) {
//            GameState nextState = state.performAction(action);
//
//            if (state.isPlayerToMoveNature()) {
//                expandRecursively(nextState, config, candidate);
//            } else if (candidate.getMaxPlayerRealPlan().getOrDefault(nextState.getSequenceFor(maxPlayer), 0d) > 1e-8) {
//                expandRecursively(nextState, config, candidate);
//            } else {
//                expandRecursivelyNotPlayed(nextState, config, candidate.getMinPlayerBestResponse());
//            }
//        }
//    }
//
//    private void expandRecursivelyNotPlayed(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
//        config.addInformationSetFor(state);
//        if (state.getPlayerToMove().equals(minPlayer)) {
//            addTemporaryLeafIfNotPresent(state, config, minPlayerBestResponse);
//        } else {
//            for (Action action : expander.getActions(state)) {
//                expandRecursivelyNotPlayed(state.performAction(action), config, minPlayerBestResponse);
//            }
//        }
//    }
//
//    @Override
//    protected void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
//        if(temporaryLeafBlackList.contains(state))
//            return;
//        super.addTemporaryLeafIfNotPresent(state, config, minPlayerBestResponse);
//    }
}
