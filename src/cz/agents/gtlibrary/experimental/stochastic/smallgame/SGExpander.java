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


package cz.agents.gtlibrary.experimental.stochastic.smallgame;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class SGExpander implements StochasticExpander {
	
	public List<Action> getActions(GameState state) {
		if (state.getPlayerToMove().equals(SGGameInfo.p1))
			return getPatrollerActions();
		if (state.getPlayerToMove().equals(SGGameInfo.p2))
			return getAttackerActions();
		return getNatureActions();
	}

	public List<Action> getNatureActions() {
		List<Action> actions = new ArrayList<Action>();

		for (SGGameState gameState : SGGameInfo.gameStates) {
			actions.add(new NatureAction(0, gameState.copy(), SGGameInfo.nature));
		}
		return actions;
	}

	public List<Action> getAttackerActions() {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new SGAction(2, SGGameInfo.p2));
		actions.add(new SGAction(3, SGGameInfo.p2));
		return actions;
	}

	public List<Action> getPatrollerActions() {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new SGAction(0, SGGameInfo.p1));
		actions.add(new SGAction(1, SGGameInfo.p1));
		return actions;
	}
}
