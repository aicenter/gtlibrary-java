package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.GameState;

public interface AlphaBetaCache {

	public Double get(GameState state);

	public void put(GameState state, double value);

	public int size();

}
