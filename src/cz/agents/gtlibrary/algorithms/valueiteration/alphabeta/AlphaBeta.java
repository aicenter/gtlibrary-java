package cz.agents.gtlibrary.algorithms.valueiteration.alphabeta;

import cz.agents.gtlibrary.interfaces.GameState;

public interface AlphaBeta {
	public double getValue(GameState state, double alpha, double beta);
	public double getFirstLevelValue(GameState state, double alpha, double beta);
}
