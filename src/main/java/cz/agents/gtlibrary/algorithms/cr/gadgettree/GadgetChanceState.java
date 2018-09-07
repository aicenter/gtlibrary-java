package cz.agents.gtlibrary.algorithms.cr.gadgettree;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.Map;

public class GadgetChanceState implements GameState {
    private final GameState originalGameSomeState;
    private Map<Action, Double> chanceProbabilities;

    public GadgetChanceState(GameState originalGameSomeState) {
        this.originalGameSomeState = originalGameSomeState;
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return new GadgetISKey(1, new ArrayListSequenceImpl(this.getPlayerToMove()));
    }

    @Override
    public Player[] getAllPlayers() {
        return originalGameSomeState.getAllPlayers();
    }

    @Override
    public Player getPlayerToMove() {
        return new PlayerImpl(2);
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
        return new ArrayListSequenceImpl(player);
        //throw new NotImplementedException();
    }

    @Override
    public Sequence getSequenceForPlayerToMove() {
        return null; // todo: throw new NotImplementedException();
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
        return chanceProbabilities.get(action);
    }

    public void setChanceProbabilities(Map<Action, Double> chanceProbabilities) {
        this.chanceProbabilities = chanceProbabilities;
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
        return true;
    }

    @Override
    public double getNatureProbability() {
        return 1.0;
//        throw new NotImplementedException();
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
    public boolean checkConsistency(Action action) {
        throw new NotImplementedException();
    }

    @Override
    public double[] evaluate() {
        throw new NotImplementedException();
    }

    @Override
    public String toString() {
        return "GadgetChanceState{" +
                "originalGameSomeState=" + originalGameSomeState +
                ", chanceProbabilities=" + chanceProbabilities +
                '}';
    }
}
