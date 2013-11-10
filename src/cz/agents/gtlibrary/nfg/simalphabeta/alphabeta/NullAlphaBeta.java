package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import cz.agents.gtlibrary.interfaces.GameState;


public class NullAlphaBeta implements AlphaBeta {


	@Override
	public double getValue(GameState state, double alpha, double beta) {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public double getUnboundedValue(GameState state) {
		return Double.POSITIVE_INFINITY;
	}


}
