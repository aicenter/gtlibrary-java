package gametree.interfaces;

public interface GameState {
	public Player[] getAllPlayers();
	public Player getPlayerToMove();
	public GameState performAction(Action action);
	public History getHistory();
	public GameState copy();
	public double[] getUtilities();
	public boolean isGameEnd();
	public boolean isPlayerToMoveNature();
	public void performActionModifyingThisState(Action action);
	public void reverseAction();
	public long getISEquivalenceFor(Player player);
}
