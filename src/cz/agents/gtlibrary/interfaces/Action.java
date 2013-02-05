package cz.agents.gtlibrary.interfaces;

public interface Action {
	public void perform(GameState gameState);
	public InformationSet getInformationSet();
	public void setInformationSet(InformationSet informationSet);
}
