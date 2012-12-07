package gametree.interfaces;

public interface GameInfo {
	public double getMaxUtility();
	public Player getFirstPlayerToMove();	
	public Player getOpponent(Player player);
	public String getInfo();
	public int getMaxDepth();
}
