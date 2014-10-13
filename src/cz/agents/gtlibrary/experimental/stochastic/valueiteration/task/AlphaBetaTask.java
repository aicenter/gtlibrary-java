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

import cz.agents.gtlibrary.experimental.stochastic.valueiteration.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.experimental.stochastic.valueiteration.alphabeta.AlphaBetaImpl;
import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.GameState;

public class AlphaBetaTask implements Runnable {
	
	private GameState state;
	private Map<GameState, Double> oldValues;
	private Map<GameState, Double> newValues;
	private StochasticExpander expander;
	

	public AlphaBetaTask(StochasticExpander expander, GameState state, Map<GameState, Double> oldValues, Map<GameState, Double> newValues) {
		super();
		this.expander = expander;
		this.state = state;
		this.oldValues = oldValues;
		this.newValues = newValues;
	}


	@Override
	public void run() {
		AlphaBeta alphaBeta = new AlphaBetaImpl(expander, oldValues, state, -1, 0);
		
		newValues.put(state, alphaBeta.getFirstLevelValue(state, -1, 0));
	}

}
