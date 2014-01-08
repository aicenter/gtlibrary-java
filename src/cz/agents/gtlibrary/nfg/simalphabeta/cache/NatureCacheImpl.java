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
