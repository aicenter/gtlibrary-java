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


package cz.agents.gtlibrary.experimental.stochastic.valueiteration.task;

import java.util.Map;

import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class BuildingTask implements Runnable {
	
	private GameState state;
	private StochasticExpander expander;
	private Map<GameState, Double> values;


	public BuildingTask(GameState state, StochasticExpander expander, Map<GameState, Double> values) {
		super();
		this.state = state;
		this.expander = expander;
		this.values = values;
	}

	@Override
	public void run() {
		for (Action attackerAction : expander.getActions(state)) {
			GameState natureState = state.performAction(attackerAction);

			for (Action natureAction : expander.getActions(natureState)) {
				GameState natureState1 = natureState.performAction(natureAction);
				if (natureState1.isGameEnd()) {
					values.put(natureState1, natureState1.getUtilities()[0]);
				} else {
					for (Action natureAction1 : expander.getActions(natureState1)) {
						GameState patrollerState = natureState1.performAction(natureAction1);

						values.put(patrollerState, patrollerState.getUtilities()[0]);
					}
				}
			}
		}
	}

}
