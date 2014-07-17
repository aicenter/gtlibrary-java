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
