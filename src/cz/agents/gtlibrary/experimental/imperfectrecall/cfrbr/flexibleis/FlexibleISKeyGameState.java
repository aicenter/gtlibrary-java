package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleis;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class FlexibleISKeyGameState extends GameStateImpl {
    private GameState wrappedState;
    private ISKey isKey;

    public FlexibleISKeyGameState(GameStateImpl gameState) {
        super(gameState);
        wrappedState = gameState;
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        super.performActionModifyingThisState(action);
        wrappedState.performActionModifyingThisState(action);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return wrappedState.getProbabilityOfNatureFor(action);
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if(isKey == null)
            return wrappedState.getISKeyForPlayerToMove();
        return isKey;
    }

    public void setISKeyForPlayerToMove(ISKey isKey) {
        this.isKey = isKey;
    }

    @Override
    public Player getPlayerToMove() {
        return wrappedState.getPlayerToMove();
    }

    @Override
    public GameState copy() {
        return new FlexibleISKeyGameState(this);
    }

    @Override
    public double[] getUtilities() {
        return wrappedState.getUtilities();
    }

    @Override
    public boolean isGameEnd() {
        return wrappedState.isGameEnd();
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return wrappedState.isPlayerToMoveNature();
    }

    @Override
    public int hashCode() {
        return wrappedState.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if(!(object instanceof FlexibleISKeyGameState))
            return false;
        return wrappedState.equals(object);
    }
}
