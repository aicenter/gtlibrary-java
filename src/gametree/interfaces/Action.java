package gametree.interfaces;

public interface Action {
	public void perform(GameState gameState);
	public long getISHash();
}
