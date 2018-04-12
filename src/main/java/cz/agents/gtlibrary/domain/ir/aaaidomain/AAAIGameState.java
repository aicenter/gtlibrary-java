package cz.agents.gtlibrary.domain.ir.aaaidomain;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class AAAIGameState extends GameStateImpl {

    private Player playerToMove;

    public AAAIGameState() {
        super(AAAIGameInfo.ALL_PLAYERS);
        this.playerToMove = AAAIGameInfo.FIRST_PLAYER;
    }

    public AAAIGameState(AAAIGameState gameState) {
        super(gameState);
        this.playerToMove = gameState.playerToMove;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 0;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (playerToMove.equals(AAAIGameInfo.SECOND_PLAYER))
            return new PerfectRecallISKey(((AAAIAction) getSequenceFor(AAAIGameInfo.FIRST_PLAYER).getLast()).getActionType().hashCode(), getSequenceForPlayerToMove());
        return new PerfectRecallISKey(0, new KeySequence(getSequenceForPlayerToMove()));
    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new AAAIGameState(this);
    }

    @Override
    public double[] getUtilities() {
        AAAIAction p2Action = (AAAIAction) getSequenceFor(AAAIGameInfo.SECOND_PLAYER).getLast();

        if (p2Action.getActionType().equals("c") || p2Action.getActionType().equals("f"))
            return new double[]{-1, 1};
        AAAIAction p1Action = (AAAIAction) getSequenceFor(AAAIGameInfo.FIRST_PLAYER).getLast();
        if (p1Action.getActionType().equals("g") && p2Action.getActionType().equals("d"))
            return new double[]{0, 0};
        if (p1Action.getActionType().equals("h") && p2Action.getActionType().equals("e"))
            return new double[]{0, 0};
        return new double[]{-10, 10};
    }

    @Override
    public boolean isGameEnd() {
        if (getSequenceFor(AAAIGameInfo.SECOND_PLAYER).isEmpty())
            return false;
        AAAIAction p2Action = (AAAIAction) getSequenceFor(AAAIGameInfo.SECOND_PLAYER).getLast();

        if (p2Action.getActionType().equals("c") || p2Action.getActionType().equals("f"))
            return true;
        return getSequenceFor(AAAIGameInfo.FIRST_PLAYER).size() == 2;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof AAAIGameState))
            return false;
        AAAIGameState other = (AAAIGameState) object;

        return history.equals(other.history);
    }

    public void changePlayer() {
        playerToMove = players[1 - playerToMove.getId()];
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
