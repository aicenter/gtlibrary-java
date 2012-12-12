package cz.agents.gtlibrary.interfaces;

public interface Action {
	public void perform(GameState gameState);
	public long getISHash();
}
