package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;

public interface Action extends Serializable {
	public void perform(GameState gameState);

	public InformationSet getInformationSet();

	public void setInformationSet(InformationSet informationSet);
}
