package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleis;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

public class FlexibleISKeyGameState extends GameStateImpl {
    private GameState wrappedState;
    private ISKey isKey;

    public FlexibleISKeyGameState(GameState gameState) {
        super(gameState.getAllPlayers());
        wrappedState = gameState;
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        wrappedState.performActionModifyingThisState(action);
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return wrappedState.getProbabilityOfNatureFor(action);
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return wrappedState.getExactProbabilityOfNatureFor(action);
    }

    @Override
    public History getHistory() {
        return wrappedState.getHistory();
    }

    @Override
    public Sequence getSequenceFor(Player player) {
        return wrappedState.getSequenceFor(player);
    }

    @Override
    public Sequence getSequenceForPlayerToMove() {
        return wrappedState.getSequenceForPlayerToMove();
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        if (isKey == null)
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
    public double getNatureProbability() {
        return wrappedState.getNatureProbability();
    }

    @Override
    public GameState copy() {
        return new FlexibleISKeyGameState(wrappedState.copy());
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
        if (!(object instanceof FlexibleISKeyGameState))
            return false;
        return wrappedState.equals(((FlexibleISKeyGameState)object).getWrappedState());
    }

    public GameState getWrappedState() {
        return wrappedState;
    }

    @Override
    public String toString() {
        return wrappedState.toString();
    }
}
