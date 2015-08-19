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


package cz.agents.gtlibrary.domain.aceofspades;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class AoSExpander<I extends InformationSet> extends ExpanderImpl<I>{

	private static final long serialVersionUID = 8770670308276388429L;

	public AoSExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		List<Action> actions = new LinkedList<Action>();
		
		if(gameState.isPlayerToMoveNature()) {
			actions.add(new NatureAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), true));
			actions.add(new NatureAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), false));
			return actions;
		}
		if(gameState.getPlayerToMove().equals(AoSGameInfo.FIRST_PLAYER)) {
			actions.add(new FirstPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), false));
			actions.add(new FirstPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), true));
			return actions;
		}
		actions.add(new SecondPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), true));
		actions.add(new SecondPlayerAoSAction(getAlgorithmConfig().getInformationSetFor(gameState), false));
		return actions;
	}

}
