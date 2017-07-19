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
import cz.agents.gtlibrary.utils.DummyMap;
import cz.agents.gtlibrary.utils.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class TempLeafDoubleOracleGameExpander extends DoubleOracleGameExpander {

    long testTime = 0;
    private Map<GameState, Map<Action, GameState>> stateCache;
    private Map<Action, GameState> dummyInstance = new DummyMap<>();
//    public Map<Sequence, Set<Action>> tempHack = new HashMap<>();

    public TempLeafDoubleOracleGameExpander(Player maxPlayer, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        super(maxPlayer, root, expander, info);
        stateCache = ((OracleALossRecallBestResponse) br).getStateCache();
    }

    @Override
    public boolean expand(SequenceFormIRConfig config, OracleCandidate candidate) {
        brTime = 0;
//        tempHack = ((DoubleOracleCandidate) candidate).getContinuationMap();
        long start = mxBean.getCurrentThreadCpuTime();
        int terminalLeafCount = config.getTerminalStates().size();
        int sequenceCount = config.getAllSequences().size();
        int informationSetCount = config.getAllInformationSets().size();

        if (DoubleOracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states before expand: " + config.getTerminalStates().size());
            System.out.println("information sets before expand: " + config.getAllInformationSets().size());
            System.out.println("sequences before expand: " + config.getAllSequences().size());
        }
        Set<GameState> terminalStatesCopy = new HashSet<>(config.getTerminalStates());

        updatePendingAndTempLeafsForced(root, (DoubleOracleIRConfig) config, ((DoubleOracleCandidate) candidate).getContinuationMap());
//        Map<Action, Double> maxPlayerBestResponse = new HashMap<>(br.getBestResponse(candidate.getMinPlayerBestResponse()));
//
//        tempAddedActions = new HashSet<>();
//        expandRecursivelyDefault(root, config, maxPlayerBestResponse, candidate.getMinPlayerBestResponse());
//        addedActions.addAll(tempAddedActions);
        tempAddedActions = new HashSet<>();
        expandRecursivelyForced(root, config, candidate.getMaxPlayerStrategy(), candidate.getMinPlayerBestResponse(), ((DoubleOracleCandidate) candidate).getContinuationMap());
        addedActions.addAll(tempAddedActions);
        validateRestrictedGame(root, config, ((DoubleOracleCandidate) candidate).getContinuationMap());
        assert validAddedActions(config);
        assert validOutgoingSequences(config);
        if (DoubleOracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        updatePending(root, (DoubleOracleIRConfig) config, ((DoubleOracleCandidate) candidate).getContinuationMap());
        addPending((DoubleOracleIRConfig) config, (DoubleOracleCandidate) candidate, ((DoubleOracleCandidate) candidate).getContinuationMap());
        validateRestrictedGame(root, config, ((DoubleOracleCandidate) candidate).getContinuationMap());
        assert validAddedActions(config);
        assert validOutgoingSequences(config);
//        updatePending(root, (DoubleOracleIRConfig) config, candidate.getMinPlayerBestResponse());
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
        return isExpanded(config, terminalLeafCount, sequenceCount, informationSetCount, terminalStatesCopy);
    }

    public boolean expandByMaxPlayerOracle(SequenceFormIRConfig config, OracleCandidate candidate) {
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
        Set<GameState> terminalStatesCopy = new HashSet<>(config.getTerminalStates());

        updatePendingAndTempLeafsForced(root, (DoubleOracleIRConfig) config, ((DoubleOracleCandidate) candidate).getContinuationMap());
//        updatePending(root, (DoubleOracleIRConfig) config, ((DoubleOracleCandidate) candidate).getContinuationMap());
        addPending((DoubleOracleIRConfig) config, (DoubleOracleCandidate) candidate, ((DoubleOracleCandidate) candidate).getContinuationMap());
        validateRestrictedGame(root, config, ((DoubleOracleCandidate) candidate).getContinuationMap());
        assert validAddedActions(config);
        assert validOutgoingSequences(config);
//        updatePending(root, (DoubleOracleIRConfig) config, candidate.getMinPlayerBestResponse());
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
        return isExpanded(config, terminalLeafCount, sequenceCount, informationSetCount, terminalStatesCopy);
    }

    private boolean isExpanded(SequenceFormIRConfig config, int terminalLeafCount, int sequenceCount, int informationSetCount, Set<GameState> terminalStatesCopy) {
        return config.getTerminalStates().size() > terminalLeafCount || config.getAllSequences().size() > sequenceCount || config.getAllInformationSets().size() > informationSetCount || !terminalStatesCopy.equals(config.getTerminalStates());
    }

    public Map<Action, Double> createBestResponseCombo(Map<Action, Double> currentBR, Set<Action> possibleBRs) {
        Map<Action, Double> bestResponseCombo = new HashMap<>(currentBR);

        possibleBRs.forEach(a -> bestResponseCombo.put(a, 1d));
        return bestResponseCombo;
    }

    private boolean validOutgoingSequences(SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getOutgoingSequences().values().stream().map(sequences -> sequences.size()).collect(Collectors.toSet()).size() > 1) {
                return false;
            }
        }
        return true;
    }

    private boolean validAddedActions(SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            for (Set<Sequence> sequences : informationSet.getOutgoingSequences().values()) {
                for (Sequence sequence : sequences) {
                    for (Action action : sequence) {
                        if (!addedActions.contains(action))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    protected void expandRecursivelyForced(GameState state, SequenceFormIRConfig config, Map<Action, Double> maxPlayerBestResponse,
                                           Map<Action, Double> minPlayerBestResponse, Map<Sequence, Set<Action>> minPlayerBestResponseCombo) {
        config.addInformationSetFor(state);
        if (state.isGameEnd())
            return;
        List<Action> actions = expander.getActions(state);
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);
        if (state.getPlayerToMove().equals(minPlayer)) {
//            for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
            boolean added = false;

            for (Action action : actions) {
                if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                    tempAddedActions.add(action);
                    expandRecursivelyForced(successors.computeIfAbsent(action, a -> state.performAction(action)), config, maxPlayerBestResponse, minPlayerBestResponse, minPlayerBestResponseCombo);
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
            for (Action action : actions) {
                expandRecursivelyForced(successors.computeIfAbsent(action, a -> state.performAction(action)), config, maxPlayerBestResponse, minPlayerBestResponse, minPlayerBestResponseCombo);
            }
            removeTemporaryLeaf(state, config);
            return;
        }
//        for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
        boolean added = false;

        for (Action action : actions) {
            if (maxPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                tempAddedActions.add(action);
                expandRecursivelyForced(successors.computeIfAbsent(action, a -> state.performAction(action)), config, maxPlayerBestResponse, minPlayerBestResponse, minPlayerBestResponseCombo);
                added = true;
            }
        }
        if (added)
            removeTemporaryLeaf(state, config);
        else if (isVisited(state, maxPlayerBestResponse, minPlayerBestResponse))
            expand(state, config, minPlayerBestResponse, minPlayerBestResponseCombo);
//        }
    }

    protected void expand(GameState tempLeaf, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse, Map<Sequence, Set<Action>> minPlayerBestResponseCombo) {
        config.addInformationSetFor(tempLeaf);
        if (!config.getTerminalStates().contains(tempLeaf)) {
            addTemporaryLeafIfNotPresent(tempLeaf, config, getUtilityUBForCombo(tempLeaf, minPlayerBestResponseCombo));
            return;
        }
        Action action = null;

        for (GameState gameState : config.getInformationSetFor(tempLeaf).getAllStates()) {
            removeTemporaryLeaf(gameState, config);

            if (gameState.isPlayerToMoveNature()) {
                Map<Action, GameState> successors = stateCache.computeIfAbsent(gameState, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>() : dummyInstance);

                for (Action natureAction : expander.getActions(gameState)) {
                    expand(successors.computeIfAbsent(natureAction, a -> gameState.performAction(a)), config, minPlayerBestResponse, minPlayerBestResponseCombo);
                }
            } else if (gameState.getPlayerToMove().equals(minPlayer)) {
                addTempLeafAfterActionFrom(gameState, config, minPlayerBestResponseCombo);
            } else {
                action = addTempLeafAfterBestResponseAction(gameState, config, minPlayerBestResponseCombo, action);
            }
        }
    }

    protected Action addTempLeafAfterBestResponseAction(GameState tempLeaf, SequenceFormIRConfig config, Map<Sequence, Set<Action>> minPlayerBestResponse, Action previousAction) {
        if (tempLeaf.isGameEnd())
            return previousAction;
        Map<Action, GameState> successors = stateCache.computeIfAbsent(tempLeaf, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>() : dummyInstance);
        if (previousAction != null) {
            GameState state = successors.computeIfAbsent(previousAction, a -> tempLeaf.performAction(a));
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
            GameState state = successors.computeIfAbsent(action, a -> tempLeaf.performAction(a));
            double utility = getUtilityUBForCombo(state, minPlayerBestResponse);

            if (utility > max) {
                max = utility;
                maxAction = action;
            }
        }
        assert maxAction != null;
        tempAddedActions.add(maxAction);
        add(config, successors.computeIfAbsent(maxAction, a -> tempLeaf.performAction(a)), max);
        return maxAction;
    }

    protected void addTempLeafAfterActionFrom(GameState tempLeaf, SequenceFormIRConfig config, Map<Sequence, Set<Action>> minPlayerBestResponse) {
        double max = Double.NEGATIVE_INFINITY;
        Action maxAction = null;
        List<Action> actions = expander.getActions(tempLeaf);
        Map<Action, GameState> successors = stateCache.computeIfAbsent(tempLeaf, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);
        Set<Action> possibleActions = minPlayerBestResponse.get(tempLeaf.getSequenceFor(minPlayer));

        for (Action action : actions) {
            if (possibleActions.contains(action)) {
                GameState state = successors.computeIfAbsent(action, a -> tempLeaf.performAction(a));
                double utility = getUtilityUBForCombo(state, minPlayerBestResponse);

                if (utility > max) {
                    max = utility;
                    maxAction = action;
                }
            }
        }
        assert maxAction != null;
        GameState state = successors.computeIfAbsent(maxAction, a -> tempLeaf.performAction(a));
        config.addInformationSetFor(state);
        addTemporaryLeafIfNotPresent(state, config, max);
        tempAddedActions.add(maxAction);
    }

    private void validateRestrictedGame(GameState state, SequenceFormIRConfig config, Map<Sequence, Set<Action>> bestResponseCombo) {
        config.addInformationSetFor(state);
        if (state.isGameEnd() || config.getTerminalStates().contains(state))
            return;
        List<Action> actions = expander.getActions(state);
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);

        if (state.getPlayerToMove().equals(minPlayer)) {
            boolean added = false;

            for (Action action : actions) {
                if (addedActions.contains(action)) {
                    validateRestrictedGame(successors.computeIfAbsent(action, a -> state.performAction(a)), config, bestResponseCombo);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(state, config);
            else
                addTemporaryLeafIfNotPresent(state, config, bestResponseCombo);
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : actions) {
                validateRestrictedGame(successors.computeIfAbsent(action, a -> state.performAction(a)), config, bestResponseCombo);
            }
            return;
        }
        boolean added = false;

        for (Action action : actions) {
            if (addedActions.contains(action)) {
                validateRestrictedGame(successors.computeIfAbsent(action, a -> state.performAction(a)), config, bestResponseCombo);
                added = true;
            }
        }
        if (added)
            removeTemporaryLeaf(state, config);
        else
            addTemporaryLeafIfNotPresent(state, config, bestResponseCombo);
    }

    private void addPending(DoubleOracleIRConfig config, DoubleOracleCandidate candidate, Map<Sequence, Set<Action>> bestResponseCombo) {
//        Map.Entry<GameState, Double> bestPending = config.getAndRemoveBestPending(candidate.getPossibleBestResponses(), minPlayer);
//
//        if (bestPending == null)
//            return;
//        if (bestPending.getValue() < candidate.getUb()-1e-6)
//            return;
//        Action lastAction = bestPending.getKey().getSequenceFor(maxPlayer).getLast();
//
//        for (GameState alternativeState : config.getAllInformationSets().get(lastAction.getInformationSet().getISKey()).getAllStates()) {
//            if (config.getTerminalStates().contains(alternativeState))
//                continue;
//            removeTemporaryLeaf(alternativeState, config);
//            GameState nextState = alternativeState.performAction(lastAction);
//
//            config.addInformationSetFor(nextState);
//            if (nextState.equals(bestPending.getKey()))
//                addTemporaryLeafIfNotPresent(nextState, config, bestPending.getValue());
//            else
//                addTemporaryLeafIfNotPresent(nextState, config, candidate.getMinPlayerBestResponse());
//        }
//        addedActions.add(lastAction);
        Pair<GameState, Double> bestPending = config.getBestPending(candidate.getContinuationMap(), minPlayer);
        if (bestPending == null)
            return;
        Map<InformationSet, Pair<GameState, Double>> viablePending = config.getAndRemoveAllViablePending(expander, candidate.getMaxPlayerStrategy(), candidate.getContinuationMap(), minPlayer);
        boolean added = false;

        if (DoubleOracleBilinearSequenceFormBnB.DEBUG)
            System.out.println("viable pending size: " + viablePending.size());
        for (Pair<GameState, Double> pair : viablePending.values()) {
            Action lastAction = pair.getLeft().getSequenceFor(maxPlayer).getLast();

            for (GameState alternativeState : config.getAllInformationSets().get(lastAction.getInformationSet().getISKey()).getAllStates()) {
                if (config.getTerminalStates().contains(alternativeState))
                    continue;
                removeTemporaryLeaf(alternativeState, config);
                GameState nextState = alternativeState.performAction(lastAction);

                added = true;
                config.addInformationSetFor(nextState);
                if (nextState.equals(pair.getLeft()))
                    addTemporaryLeafIfNotPresent(nextState, config, pair.getRight());
                else
                    addTemporaryLeafIfNotPresent(nextState, config, bestResponseCombo);
            }
            addedActions.add(lastAction);
        }
//        System.out.println(added);
//        while (!added) {
//            bestpending = config.getandremovebestpending();
//            system.out.println(bestpending);
//            if (bestpending == null)
//                return;
//            if (bestpending.getright() < candidate.getlpub() - 1e-6)
//                return;
//            action lastaction = bestpending.getleft().getsequencefor(maxplayer).getlast();
//
//            for (gamestate alternativestate : config.getallinformationsets().get(lastaction.getinformationset().getiskey()).getallstates()) {
//                if (config.getterminalstates().contains(alternativestate))
//                    continue;
//                removetemporaryleaf(alternativestate, config);
//                gamestate nextstate = alternativestate.performaction(lastaction);
//
//                config.addinformationsetfor(nextstate);
//                added = true;
//                if (nextstate.equals(bestpending.getleft()))
//                    addtemporaryleafifnotpresent(nextstate, config, bestpending.getright());
//                else
//                    addtemporaryleafifnotpresent(nextstate, config, bestresponsecombo);
//            }
//            addedactions.add(lastaction);
//        }
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

    public boolean expand(SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse, Map<Sequence, Set<Action>> brWrap) {
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

        updatePending(root, (DoubleOracleIRConfig) config, brWrap);

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

    protected void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, Map<Sequence, Set<Action>> minPlayerBestResponses) {
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
            utility = (maxPlayer.getId() == 0 ? 1 : -1) * getUtilityUBForCombo(state, minPlayerBestResponses);
        }
        config.setUtility(state, utility);
        sequenceCombinationUtilityContribution.put(state, utility);
    }

    private void updatePending(GameState state, DoubleOracleIRConfig config, Map<Sequence, Set<Action>> minPlayerBestResponse) {
        if (config.getTerminalStates().contains(state) || state.isGameEnd())
            return;
        List<Action> actions = expander.getActions(state);
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);

        if (state.getPlayerToMove().equals(maxPlayer)) {
            for (Action action : actions) {
                GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));

                if (!config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove()))) {
                    if (!config.isInPending(nextState))
                        config.addPending(nextState, state, getUtilityUBForCombo(nextState, minPlayerBestResponse));
                } else {
                    updatePending(nextState, config, minPlayerBestResponse);
                }
            }
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : actions) {
                updatePending(successors.computeIfAbsent(action, a -> state.performAction(a)), config, minPlayerBestResponse);
            }
            return;
        }
        for (Action action : actions) {
            GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));

            if (config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove())))
                updatePending(nextState, config, minPlayerBestResponse);
        }
    }

    public void updatePendingAndTempLeafsForced(GameState state, DoubleOracleIRConfig config, Map<Sequence, Set<Action>> minPlayerBestResponse) {
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
            return;
        }
        List<Action> actions = expander.getActions(state);
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);

        if (state.getPlayerToMove().equals(maxPlayer)) {
            for (Action action : actions) {
                GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));

                if (!config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove()))) {
                    config.addPending(nextState, state, getUtilityUBForCombo(nextState, minPlayerBestResponse));
                } else {
                    updatePendingAndTempLeafsForced(nextState, config, minPlayerBestResponse);
                }
            }
            return;
        }
        if (state.isPlayerToMoveNature()) {
            for (Action action : actions) {
                updatePendingAndTempLeafsForced(successors.computeIfAbsent(action, a -> state.performAction(a)), config, minPlayerBestResponse);
            }
            return;
        }
        for (Action action : actions) {
            GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));

            if (config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove())))
                updatePendingAndTempLeafsForced(nextState, config, minPlayerBestResponse);
        }
    }

    public boolean isResolveNeeded(GameState root, DoubleOracleIRConfig config, Map<Action, Double> maxPlayerStrategy, Map<Sequence, Set<Action>> possibleBestResponses) {
        Deque<GameState> queue = new ArrayDeque<>();

        queue.add(root);
        while (!queue.isEmpty()) {
            GameState state = queue.removeFirst();
            List<Action> actions = expander.getActions(state);
            Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);

            if (DoubleOracleBilinearSequenceFormBnB.USE_CORRECT_ALGORITHM && config.getTerminalStates().contains(state) && !state.isGameEnd()) {
                double utilityUB = (maxPlayer.getId() == 0 ? 1 : -1) * getUtilityUBForCombo(state, possibleBestResponses);
                double storedUtility = config.getActualNonzeroUtilityValues(state);

//                assert utilityUB >= storedUtility;
                if (utilityUB > storedUtility + 1e-4)
                    return true;
                continue;
            }  else if (state.isGameEnd() || config.getTerminalStates().contains(state))
                continue;
            if (state.getPlayerToMove().equals(maxPlayer)) {
                for (Action action : actions) {
                    GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));

                    if (!config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove()))) {
                        double toBeat = config.getValue(state, expander, maxPlayerStrategy, possibleBestResponses, maxPlayer);

                        if (ubHigherThan(nextState, possibleBestResponses, toBeat))
                            return true;
                    } else {
                        queue.addLast(nextState);
                    }
                }
                continue;
            }
            if (state.isPlayerToMoveNature()) {
                for (Action action : actions) {
                    queue.addLast(successors.computeIfAbsent(action, a -> state.performAction(a)));
                }
                continue;
            }
            Set<Action> possibleActions = possibleBestResponses.get(state.getSequenceForPlayerToMove());

            if (possibleActions == null)
                continue;
            for (Action action : actions) {
                if (!possibleActions.contains(action))
                    continue;
                GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));

                if (config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove())))
                    queue.addLast(nextState);
            }
        }
        return false;
    }

//    private boolean pendingAvailable(GameState root, DoubleOracleIRConfig config, Map<Action, Double> maxPlayerStrategy, Map<Sequence, Set<Action>> possibleBestResponses) {
//        Deque<GameState> queue = new ArrayDeque<>();
//
//        queue.add(root);
//        while (!queue.isEmpty()) {
//            GameState state = queue.removeFirst();
//            List<Action> actions = expander.getActions(state);
//            Map<Action, GameState> successors = stateCache.computeIfAbsent(state, s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);
//
//            if (state.isGameEnd() || config.getTerminalStates().contains(state))
//                continue;
//            if (state.getPlayerToMove().equals(maxPlayer)) {
//                for (Action action : actions) {
//                    GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));
//
//                    if (!config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove()))) {
//                        double toBeat = config.getValue(state, expander, maxPlayerStrategy, possibleBestResponses, maxPlayer);
//
//                        if (ubHigherThan(nextState, possibleBestResponses, toBeat))
//                            return true;
//                    } else {
//                        queue.addLast(nextState);
//                    }
//                }
//                continue;
//            }
//            if (state.isPlayerToMoveNature()) {
//                for (Action action : actions) {
//                    queue.addLast(successors.computeIfAbsent(action, a -> state.performAction(a)));
//                }
//                continue;
//            }
//            Set<Action> possibleActions = possibleBestResponses.get(state.getSequenceForPlayerToMove());
//
//            if(possibleActions == null)
//                continue;
//            for (Action action : actions) {
//                if(!possibleActions.contains(action))
//                    continue;
//                GameState nextState = successors.computeIfAbsent(action, a -> state.performAction(a));
//
//                if (config.getSequencesFor(state.getPlayerToMove()).contains(nextState.getSequenceFor(state.getPlayerToMove())))
//                    queue.addLast(nextState);
//            }
//        }
//        return false;
//    }

    private List<Integer> updateStrategyUse(boolean[] stratUse, Action action, List<Map<Action, Double>> possibleBestResponses) {
        List<Integer> updatedIndices = new ArrayList<>(stratUse.length);

        for (int i = 0; i < stratUse.length; i++) {
            if (stratUse[i]) {
                if (!(stratUse[i] &= possibleBestResponses.get(i).containsKey(action)))
                    updatedIndices.add(i);
            }
        }
        return updatedIndices;
    }

    private boolean isPlayed(boolean[] stratUse) {
        for (boolean b : stratUse) {
            if (b)
                return true;
        }
        return false;
    }

//    private double getUtilityUBForCombo(GameState state, List<Map<Action, Double>> minPlayerBestResponses) {
//        long testStart = mxBean.getCurrentThreadCpuTime();
//        double reward = minPlayerBestResponses.stream().mapToDouble(bestResponse -> {
//            if (bestResponse.isEmpty())
//                return Double.NEGATIVE_INFINITY;
//            long start = mxBean.getCurrentThreadCpuTime();
//
//            br.getBestResponseIn(state, bestResponse);
//            brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
//            return br.getValue();
//        }).max().getAsDouble();
//
////        if(tempHack != null) {
//        double value1 = getUtilityUBForCombo(state, tempHack);
//
////            if(Math.abs(reward - value1) > 1e-8) {
////                getUtilityUBForCombo(state, tempHack);
////                assert false;
////            }
////        }
////        double reward = Double.NEGATIVE_INFINITY;
////        for (Map<Action, Double> bestResponse : minPlayerBestResponses) {
////            long start = mxBean.getCurrentThreadCpuTime();
////
////            br.getBestResponseIn(state, bestResponse);
////            brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
////            if (br.getValue() > reward)
////                reward = br.getValue();
////        }
//        testTime += (mxBean.getCurrentThreadCpuTime() - testStart) / 1e6;
//        return value1;
//    }

    private double getUtilityUBForCombo(GameState state, Map<Sequence, Set<Action>> minPlayerBestResponses) {
        if (state.isGameEnd())
            return state.getUtilities()[maxPlayer.getId()];
        List<Action> actions = expander.getActions(state);
        assert !state.isPlayerToMoveNature() || actions.size() == 1;
        Map<Action, GameState> successors = stateCache.computeIfAbsent(state,
                s -> DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE ? new HashMap<>(actions.size()) : dummyInstance);

        if (state.getPlayerToMove().equals(maxPlayer) || state.isPlayerToMoveNature())
            return expander.getActions(state).stream()
                    .mapToDouble(action -> getUtilityUBForCombo(successors.computeIfAbsent(action, a -> state.performAction(a)), minPlayerBestResponses)).max().getAsDouble();
        Set<Action> possibleContinuations = minPlayerBestResponses.getOrDefault(state.getSequenceFor(minPlayer), Collections.emptySet());

        OptionalDouble max = expander.getActions(state).stream()
                .filter(a -> possibleContinuations.contains(a))
                .mapToDouble(action -> getUtilityUBForCombo(successors.computeIfAbsent(action, a -> state.performAction(a)), minPlayerBestResponses)).max();

        return max.orElseGet(() -> getUtilityUBForCombo(successors.computeIfAbsent(actions.get(0), a -> state.performAction(a)), minPlayerBestResponses));
    }

    private boolean ubHigherThan(GameState state, List<Map<Action, Double>> minPlayerBestResponses, double toBeat) {
        long testStart = mxBean.getCurrentThreadCpuTime();
        boolean value = minPlayerBestResponses.stream().mapToDouble(bestResponse -> {
            long start = mxBean.getCurrentThreadCpuTime();

            br.getBestResponseIn(state, bestResponse);
            brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
            return br.getValue() / state.getNatureProbability();
        }).anyMatch(v -> v > toBeat);
        testTime += (mxBean.getCurrentThreadCpuTime() - testStart) / 1e6;
        return value;
    }

    private boolean ubHigherThan(GameState state, Map<Sequence, Set<Action>> minPlayerBestResponses, double toBeat) {
        return getUtilityUBForCombo(state, minPlayerBestResponses) > toBeat;
    }

    @Override
    protected double getUtilityUB(GameState state, Map<Action, Double> minPlayerBestResponse) {
        long start = mxBean.getCurrentThreadCpuTime();

        br.getBestResponseIn(state, minPlayerBestResponse);
        brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
        return br.getValue();
    }

    public long getTestTime() {
        return testTime;
    }

    public Map<GameState, Map<Action, GameState>> getStateCache() {
        return stateCache;
    }
}
