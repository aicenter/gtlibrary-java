package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleALossRecallBestResponse;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleImperfectRecallBestResponse;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.io.PartialGambitEFG;
import cz.agents.gtlibrary.interfaces.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DoubleOracleGameExpander implements GameExpander {

    protected Expander<SequenceFormIRInformationSet> expander;
    protected GameState root;
    protected Player maxPlayer;
    protected Player minPlayer;
    protected Map<GameState, Double> sequenceCombinationUtilityContribution;
    protected OracleImperfectRecallBestResponse br;
    protected ThreadMXBean mxBean;

    protected long brTime;
    protected long selfTime;

    protected Set<GameState> temporaryLeafBlackList;
    protected Set<Action> addedActions;
    protected Set<Action> tempAddedActions;

    public DoubleOracleGameExpander(Player maxPlayer, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.expander = expander;
        this.root = root;
        this.maxPlayer = maxPlayer;
        this.minPlayer = info.getOpponent(maxPlayer);
        sequenceCombinationUtilityContribution = new HashMap<>();
//        br = new OracleImperfectRecallBestResponse(maxPlayer, expander, info);
        br = new OracleALossRecallBestResponse(maxPlayer, root, expander, info, DoubleOracleBilinearSequenceFormBnB.STATE_CACHE_USE);
//        br = new LinearOracleImperfectRecallBestResponse(maxPlayer, root, expander, info);
        mxBean = ManagementFactory.getThreadMXBean();
        temporaryLeafBlackList = new HashSet<>();
        addedActions = new HashSet<>();
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
        Map<Action, Double> maxPlayerBestResponse = new HashMap<>(br.getBestResponse(candidate.getMinPlayerBestResponse()));

        tempAddedActions = new HashSet<>();
        expandRecursively(root, config, maxPlayerBestResponse, candidate.getMinPlayerBestResponse());
        if (OracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        addedActions.addAll(tempAddedActions);
//        if(!(config.getTerminalStates().size() > terminalLeafCount || config.getAllSequences().size() > sequenceCount || config.getAllInformationSets().size() > informationSetCount)) {
        tempAddedActions = new HashSet<>();
        expandRecursivelyForced(root, config, candidate.getMaxPlayerStrategy(), candidate.getMinPlayerBestResponse());
        addedActions.addAll(tempAddedActions);
//        }
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
        Map<Action, Double> maxPlayerBestResponse = new HashMap<>(br.getBestResponse(minPlayerBestResponse));

        tempAddedActions = new HashSet<>();
        expandRecursively(root, config, maxPlayerBestResponse, minPlayerBestResponse);
        addedActions.addAll(tempAddedActions);
        if (OracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states after expand: " + config.getTerminalStates().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getTerminalStates().size());
            System.out.println("information sets after expand: " + config.getAllInformationSets().size() + " vs " + expander.getAlgorithmConfig().getAllInformationSets().size());
            System.out.println("sequences after expand: " + config.getAllSequences().size() + " vs " + ((SequenceFormIRConfig) expander.getAlgorithmConfig()).getAllSequences().size());
        }
        if (OracleBilinearSequenceFormBnB.EXPORT_GBT)
            new PartialGambitEFG().writeZeroSum("OracleBnBRG.gbt", root, expander, config.getActualUtilityValuesInLeafs(), config);
        config.updateUtilitiesReachableBySequences();
        selfTime = (long) ((mxBean.getCurrentThreadCpuTime() - start) / 1e6 - brTime);
        return config.getTerminalStates().size() > terminalLeafCount;
    }

    protected void expandRecursively(GameState state, SequenceFormIRConfig config, Map<Action, Double> maxPlayerBestResponse, Map<Action, Double> minPlayerBestResponse) {
        config.addInformationSetFor(state);
        if (state.isGameEnd())
            return;
        if (state.getPlayerToMove().equals(minPlayer)) {
            for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
                boolean added = false;

                for (Action action : expander.getActions(alternativeState)) {
                    if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                        tempAddedActions.add(action);
                        expandRecursively(alternativeState.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse);
                        added = true;
                    }
                }
                if (added)
                    removeTemporaryLeaf(alternativeState, config);
                else
                    addTemporaryLeafIfNotPresentForStrategy(alternativeState, config, minPlayerBestResponse);
            }
            return;
        }
        if(state.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(state)) {
                expandRecursively(state.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse);
            }
            return;
        }
        for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
            boolean added = false;

            for (Action action : expander.getActions(alternativeState)) {
                if (maxPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                    tempAddedActions.add(action);
                    expandRecursively(alternativeState.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(alternativeState, config);
            else
                addTemporaryLeafIfNotPresentForStrategy(alternativeState, config, minPlayerBestResponse);
        }
    }

    protected void expandRecursivelyForced(GameState state, SequenceFormIRConfig config, Map<Action, Double> maxPlayerBestResponse, Map<Action, Double> minPlayerBestResponse) {
        config.addInformationSetFor(state);
        if (state.isGameEnd())
            return;
        if (state.getPlayerToMove().equals(minPlayer)) {
//            for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
            boolean added = false;

            for (Action action : expander.getActions(state)) {
                if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                    tempAddedActions.add(action);
                    expandRecursivelyForced(state.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(state, config);
            else if (isVisited(state, maxPlayerBestResponse, minPlayerBestResponse))
                expand(state, config, minPlayerBestResponse);
//            }
            return;
        }
        if(state.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(state)) {
                expandRecursivelyForced(state.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse);
            }
            return;
        }
//        for (GameState alternativeState : config.getInformationSetFor(state).getAllStates()) {
        boolean added = false;

        for (Action action : expander.getActions(state)) {
            if (maxPlayerBestResponse.getOrDefault(action, 0d) > 1e-8 || addedActions.contains(action)) {
                tempAddedActions.add(action);
                expandRecursivelyForced(state.performAction(action), config, maxPlayerBestResponse, minPlayerBestResponse);
                added = true;
            }
        }
        if (added)
            removeTemporaryLeaf(state, config);
        else if (isVisited(state, maxPlayerBestResponse, minPlayerBestResponse))
            expand(state, config, minPlayerBestResponse);
//        }
    }

    protected boolean isVisited(GameState state, Map<Action, Double> maxPlayerStrategy, Map<Action, Double> minPlayerStrategy) {
        for (Action action : state.getSequenceFor(minPlayer)) {
            if (minPlayerStrategy.getOrDefault(action, 0d) < 1e-8)
                return false;
        }
        for (Action action : state.getSequenceFor(maxPlayer)) {
            if (maxPlayerStrategy.getOrDefault(action, 0d) < 1e-8)
                return false;
        }
        return true;
    }

    protected void expand(GameState tempLeaf, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        if(!config.getTerminalStates().contains(tempLeaf)) {
           addTemporaryLeafIfNotPresentForStrategy(tempLeaf, config, minPlayerBestResponse);
            return;
        }
        Action action = null;
        for (GameState gameState : config.getInformationSetFor(tempLeaf).getAllStates()) {
            removeTemporaryLeaf(gameState, config);
            if (gameState.getPlayerToMove().equals(minPlayer))
                addTempLeafAfterActionFromForStrategy(gameState, config, minPlayerBestResponse);
            else
                action = addTempLeafAfterBestResponseActionForStrategy(gameState, config, minPlayerBestResponse, action);
        }
    }

    protected Action addTempLeafAfterBestResponseActionForStrategy(GameState tempLeaf, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse, Action previousAction) {
        if (tempLeaf.isGameEnd())
            return previousAction;
        if (previousAction != null) {
            GameState state = tempLeaf.performAction(previousAction);
            long start = mxBean.getCurrentThreadCpuTime();

            br.getBestResponseIn(state, minPlayerBestResponse);
            brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
            add(config, state, br.getValue());
            return previousAction;
        }
        long start = mxBean.getCurrentThreadCpuTime();

        Map<Action, Double> maxPlayerBestResponse = br.getBestResponseIn(tempLeaf, minPlayerBestResponse);
        brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;

        for (Action action : expander.getActions(tempLeaf)) {
            if (maxPlayerBestResponse.getOrDefault(action, 0d) > 1e-8) {
                GameState state = tempLeaf.performAction(action);

                add(config, state, br.getValue());
                tempAddedActions.add(action);
                return action;
            }
        }
        assert false;
        return null;
    }

    protected void add(SequenceFormIRConfig config, GameState state, double value) {
        config.addInformationSetFor(state);
        if (temporaryLeafBlackList.contains(state))
            return;
        if (config.getTerminalStates().contains(state) || state.isGameEnd())
            return;
        config.getTerminalStates().add(state);
        double utility = (maxPlayer.getId() == 0 ? 1 : -1) * value;

        config.setUtility(state, utility);
        sequenceCombinationUtilityContribution.put(state, utility);
    }

    protected void addTempLeafAfterActionFromForStrategy(GameState tempLeaf, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        for (Action action : expander.getActions(tempLeaf)) {
            if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8) {
                GameState state = tempLeaf.performAction(action);

                config.addInformationSetFor(state);
                addTemporaryLeafIfNotPresentForStrategy(state, config, minPlayerBestResponse);
                tempAddedActions.add(action);
                return;
            }
        }
        assert false;
    }

    protected void expandRecursively(GameState state, SequenceFormIRConfig config, Map<Action, Double> maxPlayerBestResponse, OracleCandidate candidate) {
        expandRecursively(state, config, maxPlayerBestResponse, candidate.getMinPlayerBestResponse());
    }

    protected void addTemporaryLeafIfNotPresentForStrategy(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        if (temporaryLeafBlackList.contains(state))
            return;
        if (config.getTerminalStates().contains(state) || state.isGameEnd())
            return;
        config.getTerminalStates().add(state);
        double utility = (maxPlayer.getId() == 0 ? 1 : -1) * getUtilityUB(state, minPlayerBestResponse);

        config.setUtility(state, utility);
        sequenceCombinationUtilityContribution.put(state, utility);
    }

    /**
     * Computes an UB on the expected utility of maxPlayer in this state, nature probability included
     *
     * @param state
     * @return
     */
    protected double getUtilityUB(GameState state, Map<Action, Double> minPlayerBestResponse) {
        long start = mxBean.getCurrentThreadCpuTime();

        br.getBestResponseIn(state, minPlayerBestResponse);
        brTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
        return br.getValue();
    }

    protected void removeTemporaryLeaf(GameState state, SequenceFormIRConfig config) {
        temporaryLeafBlackList.add(state);
        if (state.isGameEnd() || !config.getTerminalStates().contains(state))
            return;
        config.getTerminalStates().remove(state);
        Map<Player, Sequence> seqCombination = getSequenceCombination(state);
        Double utility = config.getUtilityFor(seqCombination);
        double toSubtract = sequenceCombinationUtilityContribution.get(state);

        assert utility != null || Math.abs(toSubtract) <= 1e-8;
        if (utility != null) {
            utility -= sequenceCombinationUtilityContribution.get(state);
            config.getUtilityForSequenceCombination().put(seqCombination, utility);
            config.getActualUtilityValuesInLeafs().remove(state);
        }
    }

    protected Map<Player, Sequence> getSequenceCombination(GameState state) {
        Map<Player, Sequence> sequenceCombination = new HashMap<>(2);

        sequenceCombination.put(maxPlayer, state.getSequenceFor(maxPlayer));
        sequenceCombination.put(minPlayer, state.getSequenceFor(minPlayer));
        return sequenceCombination;
    }

    @Override
    public long getBRTime() {
        return brTime;
    }

    @Override
    public long getSelfTime() {
        return selfTime;
    }

    @Override
    public long getBRExpandedNodes() {
        return br.getExpandedNodes();
    }
}
