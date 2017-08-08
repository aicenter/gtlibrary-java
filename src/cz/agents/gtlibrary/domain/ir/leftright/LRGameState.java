package cz.agents.gtlibrary.domain.ir.leftright;

import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.iinodes.ir.ImperfectRecallGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

@Deprecated
public class LRGameState extends ImperfectRecallGameState {

    private int currentPlayerIdx;

    public LRGameState() {
        super(LRGameInfo.ALL_PLAYERS);
        currentPlayerIdx = 0;
    }

    public LRGameState(LRGameState gameState) {
        super(gameState);
        this.currentPlayerIdx = gameState.currentPlayerIdx;
    }

    public void switchPlayers() {
        currentPlayerIdx = 1 - currentPlayerIdx;
    }

    @Override
    public Player getPlayerToMove() {
        return players[currentPlayerIdx];
    }

    @Override
    public GameState copy() {
        return new LRGameState(this);
    }

    @Override
    public double[] getUtilities() {
        if(((LRAction)getSequenceFor(LRGameInfo.SECOND_PLAYER).getLast()).getType().startsWith("A"))
            return new double[]{1, -1};
        if(((LRAction)getSequenceFor(LRGameInfo.SECOND_PLAYER).getLast()).getType().equals(getFirstActionTypeFor(LRGameInfo.FIRST_PLAYER)))
            return new double[]{-2, 2};
        return new double[]{10, -10};
    }

    private String getFirstActionTypeFor(Player player) {
        return ((LRAction)getSequenceFor(player).getFirst()).getType().substring(0, 1);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return 1;
    }

    @Override
    public boolean isGameEnd() {
        if(getSequenceFor(LRGameInfo.SECOND_PLAYER).size() > 0) {
            if(((LRAction)getSequenceFor(LRGameInfo.SECOND_PLAYER).getLast()).getType().startsWith("A"))
                return true;
            return getSequenceFor(LRGameInfo.SECOND_PLAYER).size() == 2 && getSequenceFor(LRGameInfo.FIRST_PLAYER).size() == 1;
        }
        return false;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if(getSequenceFor(LRGameInfo.SECOND_PLAYER).size() == 1)
            return new PerfectRecallISKey(0, getSequenceForPlayerToMove());
        return new PerfectRecallISKey(hashCode(), getSequenceForPlayerToMove());
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
