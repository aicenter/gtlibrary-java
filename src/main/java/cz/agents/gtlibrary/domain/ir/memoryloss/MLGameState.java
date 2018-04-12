package cz.agents.gtlibrary.domain.ir.memoryloss;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.ir.ImperfectRecallAction;
import cz.agents.gtlibrary.iinodes.ir.ImperfectRecallGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

@Deprecated
public class MLGameState extends ImperfectRecallGameState {

    private int round;
    private int currentPlayerIdx;

    public MLGameState() {
        super(MLGameInfo.ALL_PLAYERS);
        this.round = 0;
        currentPlayerIdx = 0;
    }

    public MLGameState(MLGameState gameState) {
        super(gameState);
        this.round = gameState.round;
        this.currentPlayerIdx = gameState.currentPlayerIdx;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIdx];
    }

    @Override
    public GameState copy() {
        return new MLGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if (!((MLAction) getSequenceFor(MLGameInfo.FIRST_PLAYER).getFirst()).getType().substring(0, 1).
                equals(((MLAction) getSequenceFor(MLGameInfo.FIRST_PLAYER).getLast()).getType().substring(0, 1)))
            return new double[]{-5, 5};
        else if (((MLAction) getSequenceFor(MLGameInfo.FIRST_PLAYER).getFirst()).getType().substring(0, 1).
                equals(((MLAction) getSequenceFor(MLGameInfo.SECOND_PLAYER).getLast()).getType().substring(0, 1)))
            return new double[]{1, -1};
        return new double[]{-1, 1};
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1;
    }

    @Override
    public boolean isGameEnd() {
        return round == 3;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new PerfectRecallISKey(round, getSequenceForPlayerToMove());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MLGameState)) return false;

        MLGameState that = (MLGameState) o;

        if (currentPlayerIdx != that.currentPlayerIdx) return false;
        if (round != that.round) return false;
        if (!history.equals(that.history)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return history.toString().hashCode();
    }

    public void increaseRound() {
        round++;
    }

    public void switchPlayer() {
        currentPlayerIdx = 1 - currentPlayerIdx;
    }

    public int getRound() {
        return round;
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
