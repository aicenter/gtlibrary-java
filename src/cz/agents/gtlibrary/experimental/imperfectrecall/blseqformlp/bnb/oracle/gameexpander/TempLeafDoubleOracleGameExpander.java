package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleALossRecallBestResponse;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.DoubleOracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.PartialGambitEFG;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
        tempAddedActions = new HashSet<>();
        expandRecursivelyForced(root, config, candidate.getMaxPlayerStrategy(), candidate.getMinPlayerBestResponse());
        addedActions.addAll(tempAddedActions);
        validateRestrictedGame(root, config);
        if (DoubleOracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        updatePending(root, (DoubleOracleIRConfig) config);
        addPending((DoubleOracleIRConfig) config, (DoubleOracleCandidate) candidate);
        validateRestrictedGame(root, config);
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

            for (GameState alternativeState : lastAction.getInformationSet().getAllStates()) {
                removeTemporaryLeaf(alternativeState, config);
                GameState nextState = alternativeState.performAction(lastAction);

                config.addInformationSetFor(nextState);
                addTemporaryLeafIfNotPresent(nextState, config, Collections.EMPTY_MAP);
            }
            addedActions.add(lastAction);
        }
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
        updatePending(root, (DoubleOracleIRConfig) config);

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


    private void addMaxPending(DoubleOracleIRConfig config, OracleCandidate candidate) {
        Map.Entry<GameState, Double> bestPending = config.getAndRemoveBestPending();

        if (bestPending != null && bestPending.getValue() >= candidate.getUb()) {
            Action lastAction = bestPending.getKey().getSequenceFor(maxPlayer).getLast();

            for (GameState alternativeState : lastAction.getInformationSet().getAllStates()) {
                removeTemporaryLeaf(alternativeState, config);
                GameState nextState = alternativeState.performAction(lastAction);

                config.addInformationSetFor(nextState);
                addTemporaryLeafIfNotPresent(nextState, config, Collections.EMPTY_MAP);
            }
            tempAddedActions.add(lastAction);
        }
    }

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

    private void updatePending(GameState state, DoubleOracleIRConfig config) {
        if (config.getTerminalStates().contains(state) || state.isGameEnd())
            return;
        if (state.getPlayerToMove().equals(maxPlayer)) {
            for (Action action : expander.getActions(state)) {
                GameState nextState = state.performAction(action);

                if (!config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove()))) {
                    if (!config.isInPending(nextState))
                        config.addPending(nextState, getUtilityUB(nextState, Collections.EMPTY_MAP));
                } else {
                    updatePending(nextState, config);
                }
            }
            return;
        }
        for (Action action : expander.getActions(state)) {
            GameState nextState = state.performAction(action);

            if (config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove())))
                updatePending(nextState, config);
        }
    }

    @Override
    protected double getUtilityUB(GameState state, Map<Action, Double> minPlayerBestResponse) {
        long start = mxBean.getCurrentThreadCpuTime();

        ((OracleALossRecallBestResponse) br).getBestResponseIn(state, minPlayerBestResponse);
        brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
        return br.getValue();
    }
}
