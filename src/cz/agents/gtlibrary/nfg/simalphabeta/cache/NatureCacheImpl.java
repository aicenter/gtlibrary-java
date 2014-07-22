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

public class NatureCacheImpl implements NatureCache {
	
	private Map<GameState, Double> pesimisticValues;
	private Map<GameState, Double> optimisticValues;
	
	public NatureCacheImpl() {
		pesimisticValues = new HashMap<GameState, Double>();
		optimisticValues = new HashMap<GameState, Double>();
	}
	
	@Override
	public void updateOptimisticFor(GameState state, double optimistic) {
		optimisticValues.put(state, optimistic);
	}

	@Override
	public void updatePesimisticFor(GameState state, double pesimistic) {
		pesimisticValues.put(state, pesimistic);
	}
	
	@Override
	public void updateBothFor(GameState state, double value) {
		updateOptimisticFor(state, value);
		updatePesimisticFor(state, value);
	}

	@Override
	public Double getPesimisticFor(GameState state) {
		return pesimisticValues.get(state);
	}

	@Override
	public Double getOptimisticFor(GameState state) {
		return optimisticValues.get(state);
	}

}
