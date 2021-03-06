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


package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.GameState;

public class AlphaBetaCacheImpl implements AlphaBetaCache {
	
	private Map<GameState, Double> stateValues;
	
	public AlphaBetaCacheImpl() {
		stateValues = new HashMap<GameState, Double>();
	}

	@Override
	public Double get(GameState state) {
		return stateValues.get(state);
	}

	@Override
	public void put(GameState state, double value) {
		stateValues.put(state, value);
	}

	@Override
	public int size() {
		return stateValues.size();
	}

}
