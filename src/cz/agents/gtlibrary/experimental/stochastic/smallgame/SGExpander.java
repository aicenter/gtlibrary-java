package cz.agents.gtlibrary.experimental.stochastic.smallgame;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class SGExpander implements StochasticExpander {
	
	public List<Action> getActions(GameState state) {
		if (state.getPlayerToMove().equals(SGGameInfo.p1))
			return getPatrollerActions();
		if (state.getPlayerToMove().equals(SGGameInfo.p2))
			return getAttackerActions();
		return getNatureActions();
	}

	public List<Action> getNatureActions() {
		List<Action> actions = new ArrayList<Action>();

		for (SGGameState gameState : SGGameInfo.gameStates) {
			actions.add(new NatureAction(0, gameState.copy(), SGGameInfo.nature));
		}
		return actions;
	}

	public List<Action> getAttackerActions() {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new SGAction(2, SGGameInfo.p2));
		actions.add(new SGAction(3, SGGameInfo.p2));
		return actions;
	}

	public List<Action> getPatrollerActions() {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new SGAction(0, SGGameInfo.p1));
		actions.add(new SGAction(1, SGGameInfo.p1));
		return actions;
	}
}
