package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.GameState;

public interface NatureCache {
	
	public void updateOptimisticFor(GameState state, double optimistic);
	
	public void updatePesimisticFor(GameState state, double pesimistic);
	
	public void updateBothFor(GameState state, double value);
	
	public Double getPesimisticFor(GameState state);
	
	public Double getOptimisticFor(GameState state);
	

}
