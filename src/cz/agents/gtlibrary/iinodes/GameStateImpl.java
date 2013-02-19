package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public abstract class GameStateImpl implements GameState {

	protected Player[] players;
	protected History history;
	protected double natureProbability;

	public GameStateImpl(Player[] players) {
		this.history = new HistoryImpl(players);
		this.natureProbability = 1;
		this.players = players;
	}

	public GameStateImpl(GameStateImpl gameState) {
		this.history = gameState.getHistory().copy();
		this.natureProbability = gameState.getNatureProbability();
		this.players = gameState.getAllPlayers();
	}

	@Override
	public abstract Player getPlayerToMove();

	@Override
	public GameState performAction(Action action) {
		GameStateImpl state = (GameStateImpl) this.copy();
		
		state.performActionModifyingThisState(action);
		return state;
	}

	@Override
	public void performActionModifyingThisState(Action action) {
		if (isPlayerToMoveNature() || checkConsistency((ActionImpl) action)) {
			updateNatureProbabilityFor(action);
			addActionToHistory(action, getPlayerToMove());
			action.perform(this);
		} else {
			throw new IllegalStateException("Inconsistent move.");
		}
	}

	private void updateNatureProbabilityFor(Action action) {
		if (isPlayerToMoveNature())
			natureProbability *= getProbabilityOfNatureFor(action);
	}

	private void addActionToHistory(Action action, Player playerToMove) {
		history.addActionOf(action, playerToMove);
	}

	public boolean checkConsistency(ActionImpl action) {
		if (action == null || action.getInformationSet() == null)
			return false;
		return action.getInformationSet().getAllStates().contains(this);
	}

	@Override
	public History getHistory() {
		return history;
	}

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
