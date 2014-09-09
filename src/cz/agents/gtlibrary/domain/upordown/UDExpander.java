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


package cz.agents.gtlibrary.domain.upordown;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class UDExpander<I extends InformationSet> extends ExpanderImpl<I>{

	private static final long serialVersionUID = 5404146015131595801L;

	public UDExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if(gameState.getPlayerToMove().equals(UDGameInfo.FIRST))
			return getP1Actions(gameState);
		return getP2Actions(gameState);
	}

	private List<Action> getP2Actions(GameState gameState) {
		UDGameState udState = (UDGameState) gameState;
		List<Action> actions = new ArrayList<Action>(2);
		
		if(udState.getP1Action().getType().equals("U")) {
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "l"));
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "r"));
		} else {
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "l'"));
			actions.add(new P2UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "r'"));
		}
		return actions;
	}

	private List<Action> getP1Actions(GameState gameState) {
		List<Action> actions = new ArrayList<Action>(2);
		
		actions.add(new P1UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "U"));
		actions.add(new P1UDAction(getAlgorithmConfig().getInformationSetFor(gameState), "D"));
		return actions;
	}

}
