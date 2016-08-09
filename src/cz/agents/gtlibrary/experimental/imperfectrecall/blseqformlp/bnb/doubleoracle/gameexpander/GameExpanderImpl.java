package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.DOCandidate;
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
    public void expand(SequenceFormIRConfig config, DOCandidate candidate) {
        Queue<GameState> queue = new ArrayDeque<>();
        boolean added;

        queue.add(root);
        while (!queue.isEmpty()) {
            GameState state = queue.poll();
            added = false;

            config.addInformationSetFor(state);
            for (Action action : expander.getActions(state)) {
                GameState nextState = state.performAction(action);

                if (candidate.getMinPlayerBestResponse().getOrDefault(nextState.getSequenceFor(minPlayer).getLast(), 0d) > 1e-8) {
                    queue.add(nextState);
                    added = true;
                }
            }
            if (added)
                removeTemporaryLeaf(state, config);
            else
                addTemporaryLeafIfNotPresent(state, config, candidate);
        }
    }

    private void addTemporaryLeafIfNotPresent(GameState state, SequenceFormIRConfig config, DOCandidate candidate) {
        if(config.getTerminalStates().contains(state))
            return;
        config.getTerminalStates().add(state);
        double utility = getUtilityUB(state, candidate);

        config.setUtility(state, utility);
        sequenceCombinationUtilityContribution.put(state, utility);
    }

    /**
     * Computes an UB on the expected utility of maxPlayer in this state, nature probability included
     * @param state
     * @return
     */
    private double getUtilityUB(GameState state, DOCandidate candidate) {
        br.getBestResponseIn(state, candidate.getMinPlayerBestResponse());
        return br.getValue();
    }

    private void removeTemporaryLeaf(GameState state, SequenceFormIRConfig config) {
        if (state.isGameEnd())
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
