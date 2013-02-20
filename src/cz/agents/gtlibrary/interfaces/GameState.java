package cz.agents.gtlibrary.interfaces;

import cz.agents.gtlibrary.utils.Pair;

public interface GameState {
	public Player[] getAllPlayers();
	public Player getPlayerToMove();
	public GameState performAction(Action action);
	public History getHistory();
	public Sequence getSequenceFor(Player player);
	public Sequence getSequenceForPlayerToMove();
	public GameState copy();
	public double[] getUtilities();
	public double getProbabilityOfNatureFor(Action action);
	public boolean isGameEnd();
	public boolean isPlayerToMoveNature();
	public double getNatureProbability();
	public void performActionModifyingThisState(Action action);
	public void reverseAction();
	public Pair<Integer, Sequence> getISKeyForPlayerToMove();
	public boolean checkConsistency(Action action);
}
