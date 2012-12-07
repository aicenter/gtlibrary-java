package gametree.interfaces;

public interface AlgorithmConfig<T extends GameState, U extends InformationSet> {
	public U getInformationSetFor(T gameState);
	public void addInformationSetFor(T gameState, U informationSet);
}
