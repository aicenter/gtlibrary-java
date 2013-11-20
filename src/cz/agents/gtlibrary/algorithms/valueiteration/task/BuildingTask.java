package cz.agents.gtlibrary.algorithms.valueiteration.task;

import java.util.Map;

import cz.agents.gtlibrary.domain.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class BuildingTask implements Runnable {
	
	private GameState state;
	private StochasticExpander expander;
	private Map<GameState, Double> values;


	public BuildingTask(GameState state, StochasticExpander expander, Map<GameState, Double> values) {
		super();
		this.state = state;
		this.expander = expander;
		this.values = values;
	}

	@Override
	public void run() {
		for (Action attackerAction : expander.getActions(state)) {
			GameState natureState = state.performAction(attackerAction);

			for (Action natureAction : expander.getActions(natureState)) {
				GameState natureState1 = natureState.performAction(natureAction);
				if (natureState1.isGameEnd()) {
					values.put(natureState1, natureState1.getUtilities()[0]);
				} else {
					for (Action natureAction1 : expander.getActions(natureState1)) {
						GameState patrollerState = natureState1.performAction(natureAction1);

						values.put(patrollerState, patrollerState.getUtilities()[0]);
					}
				}
			}
		}
	}

}
