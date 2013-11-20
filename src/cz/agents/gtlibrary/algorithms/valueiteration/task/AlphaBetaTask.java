package cz.agents.gtlibrary.algorithms.valueiteration.task;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.valueiteration.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.algorithms.valueiteration.alphabeta.AlphaBetaImpl;
import cz.agents.gtlibrary.domain.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.GameState;

public class AlphaBetaTask implements Runnable {
	
	private GameState state;
	private Map<GameState, Double> oldValues;
	private Map<GameState, Double> newValues;
	private StochasticExpander expander;
	

	public AlphaBetaTask(StochasticExpander expander, GameState state, Map<GameState, Double> oldValues, Map<GameState, Double> newValues) {
		super();
		this.expander = expander;
		this.state = state;
		this.oldValues = oldValues;
		this.newValues = newValues;
	}


	@Override
	public void run() {
		AlphaBeta alphaBeta = new AlphaBetaImpl(expander, oldValues, state, -1, 0);
		
		newValues.put(state, alphaBeta.getFirstLevelValue(state, -1, 0));
	}

}
