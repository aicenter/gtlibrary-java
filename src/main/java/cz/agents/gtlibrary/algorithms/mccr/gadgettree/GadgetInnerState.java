package cz.agents.gtlibrary.algorithms.mccr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

public class GadgetInnerState implements GameState {
    private final GameState originalGameState;
    private final GadgetISKey isKey;
    private MCTSInformationSet informationSet;
    private final Sequence seq;

    public GadgetInnerState(GameState originalGameState, GadgetISKey isKey) {
        this.originalGameState = originalGameState;
        this.isKey = isKey;
        this.seq = isKey.getSequence();
    }

    public void setInformationSet(MCTSInformationSet informationSet) {
        this.informationSet = informationSet;
    }

    @Override
    public Player[] getAllPlayers() {
        return originalGameState.getAllPlayers();
    }

    @Override
    public Player getPlayerToMove() {
        return originalGameState.getOpponentPlayerToMove();
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
        return seq; // todo: throw new NotImplementedException();
    }

    @Override
    public GameState copy() {
        throw new NotImplementedException();
    }

    @Override
    public double[] getUtilities() {
        throw new NotImplementedException();
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
        return false;
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
        return this.isKey;
    }

    @Override
    public boolean checkConsistency(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public double[] evaluate() {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return "Gadget PL"+getPlayerToMove().getId() + " - Orig: "+originalGameState.toString();
    }
}
