package cz.agents.gtlibrary.domain.ir.cfrcounterexample;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

@Deprecated
public class CCGameState extends GameStateImpl {

    private Player playerToMove;

    public CCGameState() {
        super(CCGameInfo.ALL_PLAYERS);
        this.playerToMove = CCGameInfo.NATURE;
    }

    public CCGameState(CCGameState gameState) {
        super(gameState);
        this.playerToMove = gameState.getPlayerToMove();
    }

    public void checkAndChangePlayer() {
        if (playerToMove.equals(CCGameInfo.NATURE))
            playerToMove = CCGameInfo.FIRST_PLAYER;
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return isPlayerToMoveNature() ? 0.5 : 0;
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return isPlayerToMoveNature() ? new Rational(1, 2) : Rational.ZERO;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (isPlayerToMoveNature())
            return new PerfectRecallISKey(getISKeyHash(), getSequenceForPlayerToMove());
        return new PerfectRecallISKey(getISKeyHash(), new KeySequence(getSequenceForPlayerToMove()));
    }

    @Override
    public Player getPlayerToMove() {
        return playerToMove;
    }

    @Override
    public GameState copy() {
        return new CCGameState(this);
    }

    @Override
    public double[] getUtilities() {
        CCAction lastAction = (CCAction) getSequenceFor(CCGameInfo.FIRST_PLAYER).getLast();

        if (lastAction.getActionType().equals("a") && getSequenceFor(CCGameInfo.FIRST_PLAYER).size() == 1)
            return new double[]{1.2, -1.2};
        if (lastAction.getActionType().equals("t"))
            return new double[]{1.1, -0.5};
        if (lastAction.getActionType().equals("w"))
            return new double[]{1.1, -0.5};
        if (lastAction.getActionType().equals("a") && getSequenceFor(CCGameInfo.FIRST_PLAYER).size() == 2)
            return new double[]{-1, 1};
        if (lastAction.getActionType().equals("b"))
            return new double[]{1.3, -1.3};
        if (lastAction.getActionType().equals("c"))
            return new double[]{1.3, (-1.3)};
        if (lastAction.getActionType().equals("d") && getSequenceFor(CCGameInfo.FIRST_PLAYER).size() == 1)
            return new double[]{1.2, -1.2};
        return new double[]{(-1), 1};
    }

    @Override
    public boolean isGameEnd() {
        if (getSequenceFor(CCGameInfo.FIRST_PLAYER).isEmpty())
            return false;
        CCAction lastAction = (CCAction) getSequenceFor(CCGameInfo.FIRST_PLAYER).getLast();

        if (lastAction.getActionType().equals("a") || lastAction.getActionType().equals("d") || lastAction.getActionType().equals("t") || lastAction.getActionType().equals("w"))
            return true;
        return getSequenceFor(CCGameInfo.FIRST_PLAYER).size() == 3;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return playerToMove.equals(CCGameInfo.NATURE);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CCGameState))
            return false;
        CCGameState other = (CCGameState) object;

        return history.equals(other.history);
    }

    public int getISKeyHash() {
        if (isPlayerToMoveNature())
            return 0;
        if ((((CCAction) getSequenceFor(CCGameInfo.NATURE).getLast()).getActionType().equals("x") && getSequenceFor(CCGameInfo.FIRST_PLAYER).isEmpty()) ||
                (((CCAction) getSequenceFor(CCGameInfo.NATURE).getLast()).getActionType().equals("y") && !getSequenceFor(CCGameInfo.FIRST_PLAYER).isEmpty() &&
                        ((CCAction) getSequenceFor(CCGameInfo.FIRST_PLAYER).getLast()).getActionType().equals("v")))
            return 1;
        if ((((CCAction) getSequenceFor(CCGameInfo.NATURE).getLast()).getActionType().equals("y") && getSequenceFor(CCGameInfo.FIRST_PLAYER).isEmpty()) ||
                (((CCAction) getSequenceFor(CCGameInfo.NATURE).getLast()).getActionType().equals("x") && !getSequenceFor(CCGameInfo.FIRST_PLAYER).isEmpty() &&
                        ((CCAction) getSequenceFor(CCGameInfo.FIRST_PLAYER).getLast()).getActionType().equals("u")))
            return 2;
        return 3;
    }

    @Override
    public String toString() {
        return history.toString();
    }
}
