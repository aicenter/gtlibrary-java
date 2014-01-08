package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import cz.agents.gtlibrary.interfaces.GameState;

public interface AlphaBeta {
	
	public double getUnboundedValue(GameState state);
	
	public double getValue(GameState state, double alpha, double beta);

}
