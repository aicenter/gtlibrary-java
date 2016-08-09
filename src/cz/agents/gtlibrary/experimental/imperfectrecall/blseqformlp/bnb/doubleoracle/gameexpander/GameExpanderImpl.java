package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.DOImperfectRecallBestResponse;
import cz.agents.gtlibrary.interfaces.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class GameExpanderImpl implements GameExpander {

    private Expander<SequenceFormIRInformationSet> expander;
    private GameState root;
    private Player maxPlayer;
    private Player minPlayer;
    private Map<GameState, Double> sequenceCombinationUtilityContribution;
    private DOImperfectRecallBestResponse br;

    public GameExpanderImpl(Player maxPlayer, GameState root, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.expander = expander;
        this.root = root;
        this.maxPlayer = maxPlayer;
        this.minPlayer = info.getOpponent(maxPlayer);
        sequenceCombinationUtilityContribution = new HashMap<>();
        br = new DOImperfectRecallBestResponse(maxPlayer, expander, info);
    }

    @Override
    public void expand(SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        Queue<GameState> queue = new ArrayDeque<>();
        boolean added;

        queue.add(root);
        System.out.println("size before expand: " + config.getTerminalStates().size());
        while (!queue.isEmpty()) {
            GameState state = queue.poll();
            added = false;

            config.addInformationSetFor(state);
            if (state.isGameEnd())
                continue;
            for (Action action : expander.getActions(state)) {
                GameState nextState = state.performAction(action);

                if (nextState.getSequenceFor(minPlayer).isEmpty() || minPlayerBestResponse.getOrDefault(nextState.getSequenceFor(minPlayer).getLast(), 0d) > 1e-8) {
                    queue.add(nextState);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(state, config);
            else
                addTemporaryLeafIfNotPresent(state, config, minPlayerBestResponse);
        }
        System.out.println("size after expand: " + config.getTerminalStates().size());
        System.out.println(config.getTerminalStates());
    }

    private void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse) {
        if (config.getTerminalStates().contains(state))
            return;
        config.getTerminalStates().add(state);
        double utility = getUtilityUB(state, minPlayerBestResponse);

        config.setUtility(state, utility);
        sequenceCombinationUtilityContribution.put(state, utility);
    }

    /**
     * Computes an UB on the expected utility of maxPlayer in this state, nature probability included
     *
     * @param state
     * @return
     */
    private double getUtilityUB(GameState state, Map<Action, Double> minPlayerBestResponse) {
        br.getBestResponseIn(state, minPlayerBestResponse);
        return br.getValue();
    }

    private void removeTemporaryLeaf(GameState state, SequenceFormIRConfig config) {
        if (state.isGameEnd() || !config.getTerminalStates().contains(state))
            return;
        config.getTerminalStates().remove(state);
        Map<Player, Sequence> seqCombination = getSequenceCombination(state);
        Double utility = config.getUtilityFor(seqCombination);

        utility -= sequenceCombinationUtilityContribution.get(state);
        config.getUtilityForSequenceCombination().put(seqCombination, utility);
        config.getActualNonZeroUtilityValuesInLeafs().remove(state);
    }

    private Map<Player, Sequence> getSequenceCombination(GameState state) {
        Map<Player, Sequence> sequenceCombination = new HashMap<>(2);

        sequenceCombination.put(maxPlayer, state.getSequenceFor(maxPlayer));
        sequenceCombination.put(minPlayer, state.getSequenceFor(minPlayer));
        return sequenceCombination;
    }
}
