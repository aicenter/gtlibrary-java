package cz.agents.gtlibrary.algorithms.crswfabstraction.mergeddomain;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

public class MergedGameState implements GameState {

    private GameState state;
    private MergedGameInfo info;

    public MergedGameState(GameState state, MergedGameInfo info) {
        this.state = state;
        this.info = info;
    }

    @Override
    public Player[] getAllPlayers() {
        return state.getAllPlayers();
    }

    @Override
    public void transformInto(GameState gameState) {
        this.state = ((MergedGameState)gameState).state;
        this.info = ((MergedGameState)gameState).info;
    }

    @Override
    public Player getPlayerToMove() {
        return state.getPlayerToMove();
    }

    @Override
    public GameState performAction(Action action) {
        return new MergedGameState(state.performAction(action), info);
    }

    @Override
    public History getHistory() {
        return state.getHistory();
    }

    @Override
    public void setHistory(History history) {}

    @Override
    public Sequence getSequenceFor(Player player) {
        return state.getSequenceFor(player);
    }

    @Override
    public Sequence getSequenceForPlayerToMove() {
        return state.getSequenceForPlayerToMove();
    }

    @Override
    public GameState copy() {
        return new MergedGameState(state.copy(), info);
    }

    @Override
    public double[] getUtilities() {
        return state.getUtilities();
    }

    @Override
    public Rational[] getExactUtilities() {
        return state.getExactUtilities();
    }

    @Override
    public double getProbabilityOfNatureFor(Action action) {
        return state.getProbabilityOfNatureFor(action);
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        return state.getExactProbabilityOfNatureFor(action);
    }

    @Override
    public boolean isGameEnd() {
        return state.isGameEnd();
    }

    @Override
    public boolean isPlayerToMoveNature() {
        return state.isPlayerToMoveNature();
    }

    @Override
    public double getNatureProbability() {
        return state.getNatureProbability();
    }

    @Override
    public Rational getExactNatureProbability() {
        return state.getExactNatureProbability();
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        state.performActionModifyingThisState(action);
    }

    @Override
    public void reverseAction() {
        state.reverseAction();
    }

    @Override
    public ISKey getISKeyForPlayerToMove() {
        return info.getMergedKey(state.getISKeyForPlayerToMove());
    }

    @Override
    public boolean checkConsistency(Action action) {
        return state.checkConsistency(action);
    }

    @Override
    public double[] evaluate() {
        return state.evaluate();
    }

    protected GameState getState() {
        return state;
    }

    @Override
    public String toString() {
        return state.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MergedGameState that = (MergedGameState) o;

        return !(state != null ? !state.equals(that.state) : that.state != null);

    }

    @Override
    public int hashCode() {
        return state != null ? state.hashCode() : 0;
    }
}
