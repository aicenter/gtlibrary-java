package cz.agents.gtlibrary.domain.stacktest;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public class StackTestGameState extends GameStateImpl {

    private int currentPlayerIdx;

    public StackTestGameState() {
        super(StackTestGameInfo.ALL_PLAYERS);
        currentPlayerIdx = 0;
    }

    public StackTestGameState(StackTestGameState gameState) {
        super(gameState);
        currentPlayerIdx = gameState.currentPlayerIdx;
    }

    public void switchPlayer() {
        currentPlayerIdx = 1 - currentPlayerIdx;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIdx];
    }

    @Override
    public GameState copy() {
        return new StackTestGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (((StackTestAction) getSequenceFor(players[0]).getLast()).getType().startsWith("L")
                && ((StackTestAction) getSequenceFor(players[1]).getLast()).getType().startsWith("L"))
            return new double[]{6, 2};
        else if (((StackTestAction) getSequenceFor(players[0]).getLast()).getType().startsWith("L")
                && ((StackTestAction) getSequenceFor(players[1]).getLast()).getType().startsWith("R"))
            return new double[]{1, 1};
        else if (((StackTestAction) getSequenceFor(players[0]).getLast()).getType().startsWith("R")
                && ((StackTestAction) getSequenceFor(players[1]).getLast()).getType().startsWith("L"))
            return new double[]{1, 1};
        return new double[]{2, 4};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public boolean isGameEnd() {
        return getSequenceFor(players[0]).size() == 1 && getSequenceFor(players[1]).size() == 1;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new PerfectRecallISKey(0, getSequenceForPlayerToMove());
    }

    @Override
    public int hashCode() {
        return history.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StackTestGameState)) return false;
        StackTestGameState that = (StackTestGameState) o;

        if (!history.equals(that.history))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
