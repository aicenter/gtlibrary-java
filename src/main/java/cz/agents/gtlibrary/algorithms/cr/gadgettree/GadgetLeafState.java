package cz.agents.gtlibrary.algorithms.cr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

public class GadgetLeafState implements GameState {
    private final GameState originalGameState;
    private final double[] utilities;

    public GadgetLeafState(GameState parentOriginalGameState, double[] utilities) {
        this.originalGameState = parentOriginalGameState;
        this.utilities = utilities;
    }

    @Override
    public Player[] getAllPlayers() {
        return originalGameState.getAllPlayers();
    }

    @Override
    public Player getPlayerToMove() {
        return originalGameState.getPlayerToMove();
    }

    @Override
    public GameState performAction(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public History getHistory() {
        throw new NotImplementedException();
    }

    @Override
    public Sequence getSequenceFor(Player player) {
        throw new NotImplementedException();
    }

    @Override
    public Sequence getSequenceForPlayerToMove() {
        return null; // todo: throw new NotImplementedException();
    }

    @Override
    public GameState copy() {
        return new GadgetLeafState(originalGameState, utilities);
    }

    @Override
    public double[] getUtilities() {
        return utilities;
    }

    @Override
    public Rational[] getExactUtilities() {
        throw new NotImplementedException();
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public boolean isGameEnd() {
        return true;
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return false;
    }

    @Override
    public double getNatureProbability() {
        throw new NotImplementedException();
    }

    @Override
    public Rational getExactNatureProbability() {
        throw new NotImplementedException();
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public void reverseAction() {
        throw new NotImplementedException();
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        throw new NotImplementedException();
    }

    @Override
    public boolean checkConsistency(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public double[] evaluate() {
        throw new NotImplementedException();
    }
}
