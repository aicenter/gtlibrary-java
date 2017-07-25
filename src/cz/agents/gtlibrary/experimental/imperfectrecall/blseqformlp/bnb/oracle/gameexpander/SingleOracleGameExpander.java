package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleBilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleALossRecallBestResponse;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleImperfectRecallBestResponse;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.io.PartialGambitEFG;
import cz.agents.gtlibrary.interfaces.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

public class SingleOracleGameExpander implements GameExpander {

    protected Expander<SequenceFormIRInformationSet> expander;
    protected GameState root;
    protected Player maxPlayer;
    protected Player minPlayer;
    protected Map<GameState, Double> sequenceCombinationUtilityContribution;
    protected OracleImperfectRecallBestResponse br;
    protected ThreadMXBean mxBean;

    protected long brTime;
    protected long selfTime;

    public SingleOracleGameExpander(Player maxPlayer, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.expander = expander;
        this.root = root;
        this.maxPlayer = maxPlayer;
        this.minPlayer = info.getOpponent(maxPlayer);
        sequenceCombinationUtilityContribution = new HashMap<>();
//        br = new OracleImperfectRecallBestResponse(maxPlayer, expander, info);
        br = new OracleALossRecallBestResponse(maxPlayer, root, expander, info, false);
//        br = new LinearOracleImperfectRecallBestResponse(maxPlayer, root, expander, info);
        mxBean = ManagementFactory.getThreadMXBean();
    }

//    @Override
//    public void expand(SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
//        Queue<GameState> queue = new ArrayDeque<>();
//        boolean added;
//
//        queue.add(root);
//        System.out.println("size before expand: " + config.getTerminalStates().size());
//        while (!queue.isEmpty()) {
//            GameState state = queue.poll();
//
//            added = false;
//            config.addInformationSetFor(state);
//            if (state.isGameEnd())
//                continue;
//            for (Action action : expander.getActions(state)) {
//                GameState nextState = state.performAction(action);
//
//                if (nextState.getSequenceFor(minPlayer).isEmpty() || minPlayerBestResponse.getOrDefault(nextState.getSequenceFor(minPlayer).getLast(), 0d) > 1e-8) {
//                    queue.add(nextState);
//                    added = true;
//                }
//            }
//            if (added)
//                removeTemporaryLeaf(state, config);
//            else
//                addTemporaryLeafIfNotPresent(state, config, minPlayerBestResponse);
//        }
//        System.out.println("size after expand: " + config.getTerminalStates().size());
//        System.out.println(config.getTerminalStates());
//        config.updateP1UtilitiesReachableBySequences();
//    }

    @Override
    public boolean expand(SequenceFormIRConfig config, OracleCandidate candidate) {
        brTime = 0;
        long start = mxBean.getCurrentThreadCpuTime();
        int terminalLeafCount = config.getTerminalStates().size();

        if (OracleBilinearSequenceFormBnB.DEBUG) {
            System.out.println("terminal states before expand: " + config.getTerminalStates().size());
            System.out.println("information sets before expand: " + config.getAllInformationSets().size());
            System.out.println("sequences before expand: " + config.getAllInformationSets().size());
        }
        expandRecursively(root, config, candidate);
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
        expandRecursively(root, config, minPlayerBestResponse);
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

    protected void expandRecursively(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        config.addInformationSetFor(state);
        if (state.isGameEnd())
            return;
        if (state.getPlayerToMove().equals(minPlayer)) {
            boolean added = false;

            for (Action action : expander.getActions(state)) {
                if (minPlayerBestResponse.getOrDefault(action, 0d) > 1e-8) {
                    expandRecursively(state.performAction(action), config, minPlayerBestResponse);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(state, config);
            else
                addTemporaryLeafIfNotPresent(state, config, minPlayerBestResponse);
            return;
        }
        for (Action action : expander.getActions(state)) {
            expandRecursively(state.performAction(action), config, minPlayerBestResponse);
        }
    }

    protected void expandRecursively(GameState state, SequenceFormIRConfig config, OracleCandidate candidate) {
        expandRecursively(state, config, candidate.getMinPlayerBestResponse());
    }

    protected void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
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
        return 0;
    }
}
