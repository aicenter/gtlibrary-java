package cz.agents.gtlibrary.algorithms.crswfabstraction;

import cz.agents.gtlibrary.iinodes.HistoryImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.*;

/**
 * Assumes that nature is always the last player
 */
public class PrunedHistory {
    private Map<Player, List<Action>> playersSequences;

    public PrunedHistory(GameState leaf, GameState parentState, Map<Action, Action> mergedActions) {
        Player[] players = leaf.getAllPlayers();
        playersSequences = new HashMap<>();
        for (int i = 0; i < players.length - 1; i++) {
            Player player = players[i];
            Sequence sequence = leaf.getSequenceFor(player);
            if (player.equals(parentState.getPlayerToMove())) {
                Sequence prefix = parentState.getSequenceForPlayerToMove();
                sequence = sequence.getSubSequence(prefix.size() + 1, sequence.size() - prefix.size() - 1);
            }
            playersSequences.put(player, getMergedList(sequence, mergedActions));
        }
    }

    private List<Action> getMergedList(Sequence sequence, Map<Action, Action> mergedActions) {
        List<Action> list = new ArrayList<>(sequence.size());
        for (Action action : sequence) {
            list.add(mergedActions.get(action));
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrunedHistory that = (PrunedHistory) o;

        return playersSequences.equals(that.playersSequences);

    }

    @Override
    public int hashCode() {
        return playersSequences.hashCode();
    }
}
