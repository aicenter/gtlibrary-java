package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.GameState;

public class NullAlphaBetaCache implements AlphaBetaCache{

	@Override
	public Double get(GameState state) {
		return null;
	}

	@Override
	public void put(GameState state, double value) {
	}

	@Override
	public int size() {
		return 0;
	}

}
