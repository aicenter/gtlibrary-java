package cz.agents.gtlibrary.domain.oshizumo;

import cz.agents.gtlibrary.domain.oshizumo.ir.IROshiZumoGameState;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Sequence;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class IIOshiZumoGameState extends OshiZumoGameState {
    public IIOshiZumoGameState() {
        super();
    }

    public IIOshiZumoGameState(IIOshiZumoGameState gameState) {
        super(gameState);
    }

    @Override
    public GameState copy() {
        return new IIOshiZumoGameState(this);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (key == null) {
            Sequence sequence = getSequenceForPlayerToMove();
            Sequence opponentSequence = getSequenceFor(players[1 - getPlayerToMove().getId()]);
            List<Integer> wins = new ArrayList(sequence.size());

            for (int i = 0; i < sequence.size(); i++) {
                 wins.add((int) Math.signum(((OshiZumoAction)sequence.get(i)).compareTo((OshiZumoAction) opponentSequence.get(i))));
            }
            key = new PerfectRecallISKey(new HashCodeBuilder().append(wins).append(isGameEnd()).toHashCode(), sequence);
        }
        return key;
    }
}
