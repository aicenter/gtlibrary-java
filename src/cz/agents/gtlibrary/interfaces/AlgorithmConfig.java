package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;

public interface AlgorithmConfig<I extends InformationSet> extends Serializable {
	
	public I getInformationSetFor(GameState gameState);

	public void addInformationSetFor(GameState gameState, I informationSet);

	public I createInformationSetFor(GameState gameState);
	
}
