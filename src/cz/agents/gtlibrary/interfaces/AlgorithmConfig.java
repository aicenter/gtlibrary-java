package cz.agents.gtlibrary.interfaces;

public interface AlgorithmConfig<I extends InformationSet> {
	
	public I getInformationSetFor(GameState gameState);

	public void addInformationSetFor(GameState gameState, I informationSet);

	public I createInformationSetFor(GameState gameState);
	
}
