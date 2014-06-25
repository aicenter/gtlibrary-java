package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.GameState;

public class NullNatureCache implements NatureCache {

	@Override
	public void updateOptimisticFor(GameState state, double optimistic) {
	}

	@Override
	public void updatePesimisticFor(GameState state, double pesimistic) {
	}

	@Override
	public void updateBothFor(GameState state, double value) {
	}

	@Override
	public Double getPesimisticFor(GameState state) {
		return null;
	}

	@Override
	public Double getOptimisticFor(GameState state) {
		return null;
	}

}
