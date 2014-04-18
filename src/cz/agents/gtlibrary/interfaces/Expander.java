package cz.agents.gtlibrary.interfaces;

import java.io.Serializable;
import java.util.List;

public interface Expander<I extends InformationSet> extends Serializable {
	
	public AlgorithmConfig<I> getAlgorithmConfig();
	
	/**
	 * 
	 * @param gameState
	 * @return list of actions available in given game state for player to move, actions must include information set
	 */
	public List<Action> getActions(GameState gameState);
	
	/**
	 * This method should be used only in situations where IS is not guaranteed to exist
	 * 
	 * @param gameState
	 * @return list of actions available in given game state for player to move
	 */
	@Deprecated
	public List<Action> getActionsForUnknownIS(GameState gameState);
	
	/**
	 * 
	 * @param informationSet
	 * @return list of actions available in given information set, actions must include information set
	 */
	public List<Action> getActions(I informationSet);
}
