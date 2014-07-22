/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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

    public void setAlgConfig(AlgorithmConfig<I> algConfig);
}
