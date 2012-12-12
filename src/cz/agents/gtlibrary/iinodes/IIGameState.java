package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public abstract class IIGameState implements GameState {

	protected History history;
	
	@Override
	public abstract Player[] getAllPlayers();

	@Override
	public abstract Player getPlayerToMove();

	@Override
	public GameState performAction(Action action) {
		IIGameState state = (IIGameState) this.copy();

		if (isPlayerToMoveNature() || state.checkConsistency((IIAction) action)) {
			state.performActionModifyingThisState(action);
			return state;
		}
		return null;
	}

	@Override
	public void performActionModifyingThisState(Action action) {
		if (isPlayerToMoveNature() || checkConsistency((IIAction) action)) {
			addActionToHistory(action, getPlayerToMove());
			action.perform(this);
		} else {
			throw new IllegalStateException("Inconsistent move.");
		}
	}

	private void addActionToHistory(Action action, Player playerToMove) {
		history.addActionOf(action, playerToMove);	
	}

	public boolean checkConsistency(IIAction action) {
		if (action == null)
			return false;
		return action.getISHash() == getISEquivalenceFor(getPlayerToMove());
	}

	@Override
	public History getHistory() {
		return history;
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
	public abstract long getISEquivalenceFor(Player player);
	
	@Override
	public abstract int hashCode();

	@Override
	public abstract boolean equals(Object object);

}
