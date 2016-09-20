package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleALossRecallBestResponse;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.DoubleOracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.io.PartialGambitEFG;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;

public class TempLeafDoubleOracleGameExpander extends DoubleOracleGameExpander {

    public TempLeafDoubleOracleGameExpander(Player maxPlayer, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        super(maxPlayer, root, expander, info);
    }

    @Override
    public boolean expand(SequenceFormIRConfig config, OracleCandidate candidate) {
        brTime = 0;
        long start = mxBean.getCurrentThreadCpuTime();
        int terminalLeafCount = config.getTerminalStates().size();
        int sequenceCount = config.getAllSequences().size();
        int informationSetCount = config.getAllInformationSets().size();

        if (DoubleOracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states before expand: " + config.getTerminalStates().size());
            System.out.println("information sets before expand: " + config.getAllInformationSets().size());
            System.out.println("sequences before expand: " + config.getAllSequences().size());
        }
        Map<Action, Double> bestResponseCombo = new HashMap<>(candidate.getMinPlayerBestResponse());

        ((DoubleOracleCandidate) candidate).getPossibleBestResponses().forEach(a -> bestResponseCombo.put(a, 1d));
        updatePendingAndTempLeafsForced(root, (DoubleOracleIRConfig) config, bestResponseCombo);
//        Map<Action, Double> maxPlayerBestResponse = new HashMap<>(br.getBestResponse(candidate.getMinPlayerBestResponse()));
//
//        tempAddedActions = new HashSet<>();
//        expandRecursivelyDefault(root, config, maxPlayerBestResponse, candidate.getMinPlayerBestResponse());
//        addedActions.addAll(tempAddedActions);
        tempAddedActions = new HashSet<>();
        expandRecursivelyForced(root, config, candidate.getMaxPlayerStrategy(), candidate.getMinPlayerBestResponse(), bestResponseCombo);
        addedActions.addAll(tempAddedActions);
        validateRestrictedGame(root, config);
        if (DoubleOracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        updatePending(root, (DoubleOracleIRConfig) config, candidate.getMinPlayerBestResponse());
        addPending((DoubleOracleIRConfig) config, (DoubleOracleCandidate) candidate);
        validateRestrictedGame(root, config);
        updatePending(root, (DoubleOracleIRConfig) config, candidate.getMinPlayerBestResponse());
//        }
        if (DoubleOracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states after expand: " + config.getTerminalStates().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getTerminalStates().size());
            System.out.println("information sets after expand: " + config.getAllInformationSets().size() + " vs " + expander.getAlgorithmConfig().getAllInformationSets().size());
            System.out.println("sequences after expand: " + config.getAllSequences().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getAllSequences().size());
        }
        if (DoubleOracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        config.updateUtilitiesReachableBySequences();
        selfTime = (long) ((mxBean.getCurrentThreadCpuTime() - start) / 1e6 - brTime);
        return config.getTerminalStates().size() > terminalLeafCount || config.getAllSequences().size() > sequenceCount || config.getAllInformationSets().size() > informationSetCount;
    }

    protected void expandRecursivelyForced(GameState state, SequenceFormIRConfig config, Map<Action, Double> maxPlayerBestResponse, Map<Action, Double> minPlayerBestResponse, Map<Action, Double> minPlayerBestResponseCombo) {
        config.addInformationSetFor(state);
        if (state.isGameEnd())
            return;
        if (state.getPlayerToMove().equals(minPlayer)) {
//            for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
            boolean added = false;

            for (Action action : expander.getActions(state)) {
                if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                    tempAddedActions.add(action);
                    expandRecursivelyForced(state.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse, minPlayerBestResponseCombo);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(state, config);
            else if (isVisited(state, maxPlayerBestResponse, minPlayerBestResponse))
                expand(state, config, minPlayerBestResponse, minPlayerBestResponseCombo);
//            }
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(state)) {
                expandRecursivelyForced(state.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse, minPlayerBestResponseCombo);
            }
            return;
        }
//        for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
        boolean added = false;

        for (Action action : expander.getActions(state)) {
            if (maxPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                tempAddedActions.add(action);
                expandRecursivelyForced(state.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse, minPlayerBestResponseCombo);
                added = true;
            }
        }
        if (added)
            removeTemporaryLeaf(state, config);
        else if (isVisited(state, maxPlayerBestResponse, minPlayerBestResponse))
            expand(state, config, minPlayerBestResponse, minPlayerBestResponseCombo);
//        }
    }

    protected void expand(GameState tempLeaf, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse, Map<Action, Double> minPlayerBestResponseCombo) {
        config.addInformationSetFor(tempLeaf);
        if (!config.getTerminalStates().contains(tempLeaf)) {
            addTemporaryLeafIfNotPresent(tempLeaf, config, getUtilityUBForCombo(tempLeaf, minPlayerBestResponseCombo));
            return;
        }
        Action action = null;
        for (GameState gameState : config.getInformationSetFor(tempLeaf).getAllStates()) {
            removeTemporaryLeaf(gameState, config);
            if (gameState.isPlayerToMoveNature())
                for (Action natureAction : expander.getActions(gameState)) {
                    expand(gameState.performAction(natureAction), config, minPlayerBestResponse, minPlayerBestResponseCombo);
                }
            else if (gameState.getPlayerToMove().equals(minPlayer))
                addTempLeafAfterActionFrom(gameState, config, minPlayerBestResponseCombo);
            else
                action = addTempLeafAfterBestResponseAction(gameState, config, minPlayerBestResponseCombo, action);
        }
    }

    protected Action addTempLeafAfterBestResponseAction(GameState tempLeaf, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse, Action previousAction) {
        if (tempLeaf.isGameEnd())
            return previousAction;
        if (previousAction != null) {
            GameState state = tempLeaf.performAction(previousAction);
            long start = mxBean.getCurrentThreadCpuTime();

            brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
            add(config, state, getUtilityUBForCombo(state, minPlayerBestResponse));
            return previousAction;
        }

//        Map<Action, Double> maxPlayerBestResponse = br.getBestResponseIn(tempLeaf, minPlayerBestResponse);
//        brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;

        double max = Double.NEGATIVE_INFINITY;
        Action maxAction = null;

        for (Action action : expander.getActions(tempLeaf)) {
//            if (maxPlayerBestResponse.getOrDefault(action, 0d) > 1e-8) {
            GameState state = tempLeaf.performAction(action);
            double utility = getUtilityUBForCombo(state, minPlayerBestResponse);

            if (utility > max) {
                max = utility;
                maxAction = action;
            }
        }
        assert maxAction != null;
        tempAddedActions.add(maxAction);
        add(config, tempLeaf.performAction(maxAction), max);
        return maxAction;
    }

    protected void addTempLeafAfterActionFrom(GameState tempLeaf, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        double max = Double.NEGATIVE_INFINITY;
        Action maxAction = null;

        for (Action action : expander.getActions(tempLeaf)) {
            if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8) {
                GameState state = tempLeaf.performAction(action);
                double utility = getUtilityUBForCombo(state, minPlayerBestResponse);

                if (utility > max) {
                    max = utility;
                    maxAction = action;
                }
            }
        }
        assert maxAction != null;
        GameState state = tempLeaf.performAction(maxAction);
        config.addInformationSetFor(state);
        addTemporaryLeafIfNotPresent(state, config, max);
        tempAddedActions.add(maxAction);
    }

    private void validateRestrictedGame(GameState state, SequenceFormIRConfig config) {
        config.addInformationSetFor(state);
        if (state.isGameEnd() || config.getTerminalStates().contains(state))
            return;
        if (state.getPlayerToMove().equals(minPlayer)) {
            boolean added = false;

            for (Action action : expander.getActions(state)) {
                if (addedActions.contains(action)) {
                    validateRestrictedGame(state.performAction(action), config);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(state, config);
            else
                addTemporaryLeafIfNotPresent(state, config, Collections.EMPTY_MAP);
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(state)) {
                validateRestrictedGame(state.performAction(action), config);
            }
            return;
        }
        boolean added = false;

        for (Action action : expander.getActions(state)) {
            if (addedActions.contains(action)) {
                validateRestrictedGame(state.performAction(action), config);
                added = true;
            }
        }
        if (added)
            removeTemporaryLeaf(state, config);
        else
            addTemporaryLeafIfNotPresent(state, config, Collections.EMPTY_MAP);
    }

    private void addPending(DoubleOracleIRConfig config, DoubleOracleCandidate candidate) {
        Map<InformationSet, Pair<GameState, Double>> viablePending = config.getAndRemoveAllViablePending(candidate.getPossibleBestResponses(), minPlayer, candidate.getUb());

        if (DoubleOracleBilinearSequenceFormBnB.DEBUG)
            System.out.println("viable pending size: " + viablePending.size());
        for (Pair<GameState, Double> pair : viablePending.values()) {
            Action lastAction = pair.getLeft().getSequenceFor(maxPlayer).getLast();

            for (GameState alternativeState : config.getAllInformationSets().get(lastAction.getInformationSet().getISKey()).getAllStates()) {
                if (config.getTerminalStates().contains(alternativeState))
                    continue;
                removeTemporaryLeaf(alternativeState, config);
                GameState nextState = alternativeState.performAction(lastAction);

                config.addInformationSetFor(nextState);
                if (nextState.equals(pair.getLeft()))
                    addTemporaryLeafIfNotPresent(nextState, config, pair.getRight());
                else
                    addTemporaryLeafIfNotPresent(nextState, config, candidate.getMinPlayerBestResponse());
            }
            addedActions.add(lastAction);
        }
    }

    protected void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, double pendingUtility) {
        if (temporaryLeafBlackList.contains(state))
            return;
        if (config.getTerminalStates().contains(state) || state.isGameEnd())
            return;
        config.getTerminalStates().add(state);
        double utility = (maxPlayer.getId() == 0 ? 1 : -1) * pendingUtility;

        config.setUtility(state, utility);
        sequenceCombinationUtilityContribution.put(state, utility);
    }

    @Override
    public boolean expand(SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        brTime = 0;
        long start = mxBean.getCurrentThreadCpuTime();
        int terminalLeafCount = config.getTerminalStates().size();

        if (DoubleOracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states before expand: " + config.getTerminalStates().size());
            System.out.println("information sets before expand: " + config.getAllInformationSets().size());
            System.out.println("sequences before expand: " + config.getAllInformationSets().size());
        }
        Map<Action, Double> maxPlayerBestResponse = new HashMap<>(br.getBestResponse(minPlayerBestResponse));

        tempAddedActions = new HashSet<>();
        expandRecursively(root, config, maxPlayerBestResponse, minPlayerBestResponse);
        addedActions.addAll(tempAddedActions);
        updatePending(root, (DoubleOracleIRConfig) config, minPlayerBestResponse);

        if (DoubleOracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states after expand: " + config.getTerminalStates().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getTerminalStates().size());
            System.out.println("information sets after expand: " + config.getAllInformationSets().size() + " vs " + expander.getAlgorithmConfig().getAllInformationSets().size());
            System.out.println("sequences after expand: " + config.getAllSequences().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getAllSequences().size());
        }
        if (DoubleOracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        config.updateUtilitiesReachableBySequences();
        selfTime = (long) ((mxBean.getCurrentThreadCpuTime() - start) / 1e6 - brTime);
        return config.getTerminalStates().size() > terminalLeafCount;
    }


//    private void addMaxPending(DoubleOracleIRConfig config, OracleCandidate candidate) {
//        Map.Entry<GameState, Double> bestPending = config.getAndRemoveBestPending();
//
//        if (bestPending != null && bestPending.getValue() >= candidate.getUb()) {
//            Action lastAction = bestPending.getKey().getSequenceFor(maxPlayer).getLast();
//
//            for (GameState alternativeState : lastAction.getInformationSet().getAllStates()) {
//                removeTemporaryLeaf(alternativeState, config);
//                GameState nextState = alternativeState.performAction(lastAction);
//
//                config.addInformationSetFor(nextState);
//                addTemporaryLeafIfNotPresent(nextState, config, Collections.EMPTY_MAP);
//            }
//            tempAddedActions.add(lastAction);
//        }
//    }

    protected void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        if (temporaryLeafBlackList.contains(state))
            return;
        if (config.getTerminalStates().contains(state) || state.isGameEnd())
            return;
        config.getTerminalStates().add(state);
        Double pendingUtility = ((DoubleOracleIRConfig) config).getPending().get(state);
        double utility;

        if (pendingUtility != null) {
            utility = (maxPlayer.getId() == 0 ? 1 : -1) * pendingUtility;
        } else {
            utility = (maxPlayer.getId() == 0 ? 1 : -1) * getUtilityUB(state, minPlayerBestResponse);
        }
        config.setUtility(state, utility);
        sequenceCombinationUtilityContribution.put(state, utility);
    }

    private void updatePending(GameState state, DoubleOracleIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        if (config.getTerminalStates().contains(state) || state.isGameEnd())
            return;
        if (state.getPlayerToMove().equals(maxPlayer)) {
            for (Action action : expander.getActions(state)) {
                GameState nextState = state.performAction(action);

                if (!config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove()))) {
                    if (!config.isInPending(nextState))
                        config.addPending(nextState, getUtilityUB(nextState, minPlayerBestResponse));
                } else {
                    updatePending(nextState, config, minPlayerBestResponse);
                }
            }
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(state)) {
                updatePending(state.performAction(action), config, minPlayerBestResponse);
            }
            return;
        }
        for (Action action : expander.getActions(state)) {
            GameState nextState = state.performAction(action);

            if (config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove())))
                updatePending(nextState, config, minPlayerBestResponse);
        }
    }

    private void updatePendingAndTempLeafsForced(GameState state, DoubleOracleIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        if (state.isGameEnd())
            return;
        if (config.getTerminalStates().contains(state)) {
            Map<Player, Sequence> seqCombination = getSequenceCombination(state);
            Double utility = config.getUtilityFor(seqCombination);
            double toSubtract = sequenceCombinationUtilityContribution.get(state);

            assert utility != null || Math.abs(toSubtract) <= 1e-8;
            if (utility != null) {
                utility -= sequenceCombinationUtilityContribution.get(state);
                config.getUtilityForSequenceCombination().put(seqCombination, utility);
                config.getActualUtilityValuesInLeafs().remove(state);
            }
            utility = (maxPlayer.getId() == 0 ? 1 : -1) * getUtilityUBForCombo(state, minPlayerBestResponse);
            config.setUtility(state, utility);
            sequenceCombinationUtilityContribution.put(state, utility);
        }

        if (state.getPlayerToMove().equals(maxPlayer)) {
            for (Action action : expander.getActions(state)) {
                GameState nextState = state.performAction(action);

                if (!config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove()))) {
                    config.addPending(nextState, getUtilityUBForCombo(nextState, minPlayerBestResponse));
                } else {
                    updatePendingAndTempLeafsForced(nextState, config, minPlayerBestResponse);
                }
            }
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(state)) {
                updatePendingAndTempLeafsForced(state.performAction(action), config, minPlayerBestResponse);
            }
            return;
        }
        for (Action action : expander.getActions(state)) {
            GameState nextState = state.performAction(action);

            if (config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove())))
                updatePendingAndTempLeafsForced(nextState, config, minPlayerBestResponse);
        }
    }

    private double getUtilityUBForCombo(GameState state, Map<Action, Double> minPlayerBestResponse) {
        if (state.isGameEnd())
            return state.getUtilities()[maxPlayer.getId()] * state.getNatureProbability();
        if (state.getPlayerToMove().equals(maxPlayer))
            return expander.getActions(state).stream()
                    .mapToDouble(a -> getUtilityUBForCombo(state.performAction(a), minPlayerBestResponse)).max().getAsDouble();
        List<Action> actions = expander.getActions(state);

        return actions
                .stream()
                .filter(a -> minPlayerBestResponse.getOrDefault(a, 0d) > 1e-8)
                .mapToDouble(a -> getUtilityUBForCombo(state.performAction(a), minPlayerBestResponse))
                .max()
                .orElse(getUtilityUBForCombo(state.performAction(actions.get(0)), minPlayerBestResponse));
    }

    @Override
    protected double getUtilityUB(GameState state, Map<Action, Double> minPlayerBestResponse) {
        long start = mxBean.getCurrentThreadCpuTime();

        ((OracleALossRecallBestResponse) br).getBestResponseIn(state, minPlayerBestResponse);
        brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
        return br.getValue();
    }
}
