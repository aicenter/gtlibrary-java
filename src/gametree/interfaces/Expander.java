package gametree.interfaces;

import java.util.List;

public interface Expander {
	
	/**
	 * 
	 * @param gameState
	 * @return list of actions available in given game state for player to move, actions must include isHash (0 if nature action)
	 */
	List<? extends Action> getActions(GameState gameState);
	
	/**
	 * 
	 * @param gameState
	 * @return list of actions available in given information set, actions must include isHash (0 if nature action)
	 */
	List<? extends Action> getActions(InformationSet informationSet);
}
