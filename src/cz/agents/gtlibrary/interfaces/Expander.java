package cz.agents.gtlibrary.interfaces;

import java.util.List;

public interface Expander<I extends InformationSet> {
	
	public AlgorithmConfig<I> getAlgorithmConfig();
	
	/**
	 * 
	 * @param gameState
	 * @return list of actions available in given game state for player to move, actions must include information set
	 */
	List<Action> getActions(GameState gameState);
	
	/**
	 * 
	 * @param gameState
	 * @return list of actions available in given information set, actions must include information set
	 */
	List<Action> getActions(I informationSet);
}
