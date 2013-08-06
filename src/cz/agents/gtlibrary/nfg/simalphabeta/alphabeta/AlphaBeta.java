package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public interface AlphaBeta {
	
	public double getValue(GameState state, Action action, double alpha, double beta);

	public double getValue(GameState state, double alpha, double beta);

}
