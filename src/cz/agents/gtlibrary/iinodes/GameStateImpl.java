package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;

public abstract class GameStateImpl implements GameState {

    private static final long serialVersionUID = 7693972683606265891L;

    protected Player[] players;
    protected History history;
    protected double natureProbability;
    protected Rational exactNatureProbability;

    public GameStateImpl(Player[] players) {
        this.history = new HistoryImpl(players);
        this.natureProbability = 1;
        this.exactNatureProbability = Rational.ONE;
        this.players = players;
    }

    public GameStateImpl(GameStateImpl gameState) {
        this.history = gameState.getHistory().copy();
        this.natureProbability = gameState.getNatureProbability();
        this.exactNatureProbability = gameState.exactNatureProbability;
        this.players = gameState.getAllPlayers();
    }

    @Override
    public abstract Player getPlayerToMove();

    @Override
    public GameState performAction(Action action) {
        GameState state = this.copy();

        state.performActionModifyingThisState(action);
        return state;
    }

    @Override
    public void performActionModifyingThisState(Action action) {
        if (isPlayerToMoveNature() || checkConsistency(action)) {
            updateNatureProbabilityFor(action);
            addActionToHistory(action, getPlayerToMove());
            action.perform(this);
        } else {
            throw new IllegalStateException("Inconsistent move.");
        }
    }

    private void updateNatureProbabilityFor(Action action) {
        if (isPlayerToMoveNature()) {
            natureProbability *= getProbabilityOfNatureFor(action);
            exactNatureProbability = exactNatureProbability.multiply(getExactProbabilityOfNatureFor(action));
        }
    }

    private void addActionToHistory(Action action, Player playerToMove) {
        history.addActionOf(action, playerToMove);
    }

    @Override
    public boolean checkConsistency(Action action) {
        if (action == null || action.getInformationSet() == null)
            return false;
        return this.getISKeyForPlayerToMove().equals(new Pair<Integer, Sequence>(action.getInformationSet().hashCode(), action.getInformationSet().getPlayersHistory()));
//		return action.getInformationSet().getAllStates().contains(this);
    }

    @Override
    public double[] evaluate() {
        throw new UnsupportedOperationException("Evaluation function not implemented for this domain...");
    }

    @Override
    public Rational[] getExactUtilities() {
        throw new UnsupportedOperationException("Exact utility not implemented for this domain...");
    }

    @Override
    public Rational getExactProbabilityOfNatureFor(Action action) {
        throw new UnsupportedOperationException("Exact probability for action not implemented for this domain...");
    }

    @Override
    public History getHistory() {
        return history;
    }

    @Override
    public Player[] getAllPlayers() {
        return players;
    }

    @Override
    public Sequence getSequenceFor(Player player) {
        return history.getSequenceOf(player);
    }

    @Override
    public Sequence getSequenceForPlayerToMove() {
        return history.getSequenceOf(getPlayerToMove());
    }

    @Override
    public double getNatureProbability() {
        return natureProbability;
    }

    @Override
    public Rational getExactNatureProbability() {
        return exactNatureProbability;
    }

    @Override
    public abstract GameState copy();

    @Override
    public abstract double[] getUtilities();

    @Override
    public abstract boolean isGameEnd();

    @Override
    public abstract boolean isPlayerToMoveNature();

    @Override
    public void reverseAction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object object);
}
