/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.experimental.stochastic.smallgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.experimental.stochastic.StochasticGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolver;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;

public class SGGameState extends StochasticGameState {

	private static final long serialVersionUID = 134417344843916155L;

	private Map<SGAction, Map<SGAction, Double>> utilities;
	private Action p1Action;
	private Action p2Action;
	private Map<SGGameState, Double> distribution;
	private Player currentPlayer;

	public SGGameState(Random random) {
		super(SGGameInfo.ALL_PLAYERS);
		Map<SGAction, Double> a1Utilities = new HashMap<SGAction, Double>();
		Map<SGAction, Double> a2Utilities = new HashMap<SGAction, Double>();

		utilities = new HashMap<SGAction, Map<SGAction, Double>>();
		a1Utilities.put(new SGAction(2, SGGameInfo.p2), random.nextDouble() * 2 - 1);
		a1Utilities.put(new SGAction(3, SGGameInfo.p2), random.nextDouble() * 2 - 1);
		a2Utilities.put(new SGAction(2, SGGameInfo.p2), random.nextDouble() * 2 - 1);
		a2Utilities.put(new SGAction(3, SGGameInfo.p2), random.nextDouble() * 2 - 1);
		utilities.put(new SGAction(0, SGGameInfo.p1), a1Utilities);
		utilities.put(new SGAction(1, SGGameInfo.p1), a2Utilities);
		currentPlayer = SGGameInfo.p1;
	}

	public SGGameState(SGGameState state) {
		super(state);
		this.utilities = state.utilities;
		this.p1Action = state.p1Action;
		this.p2Action = state.p2Action;
		this.currentPlayer = state.currentPlayer;
	}
	
	public Map<SGAction, Map<SGAction, Double>> getCurrentUtilities() {
		return utilities;
	}

	public double getProbabilityOfNatureFor(Action action) {
		if (distribution == null)
			distribution = getDistribution(action);
		return distribution.get(((NatureAction) action).getGameState());
	}

	private Map<SGGameState, Double> getDistribution(Action action) {
		Random random = new Random(getSeed(action));
		Map<SGGameState, Double> distribution = new HashMap<SGGameState, Double>();

		for (SGGameState state : SGGameInfo.gameStates) {
			distribution.put(state, random.nextDouble());
		}
		normalize(distribution);
		return distribution;
	}

	private void normalize(Map<SGGameState, Double> distribution) {
		double sum = 0;

		for (Double probability : distribution.values()) {
			sum += probability;
		}
		for (Entry<SGGameState, Double> entry : distribution.entrySet()) {
			distribution.put(entry.getKey(), entry.getValue() / sum);
		}
	}

//	public Map<SGAction, Map<SGAction, Double>> getUtilities() {
//		return utilities;
//	}

	public void performActionModifyingThisState(Action action) {
		if (((SGAction) action).getPlayer().equals(SGGameInfo.p1)) {
			p1Action = action;
			currentPlayer = SGGameInfo.p2;
		} else {
			p2Action = action;
			currentPlayer = SGGameInfo.nature;
		}
	}

	public SGGameState performAction(Action action) {
		if (action instanceof NatureAction)
			return ((NatureAction) action).getGameState();
		SGGameState state = copy();

		state.performActionModifyingThisState(action);
		return state;
	}

	private long getSeed(Action natureAction) {
		HashCodeBuilder hcb = new HashCodeBuilder(17, 31);

		hcb.append(utilities);
		hcb.append(p1Action);
		hcb.append(p2Action);
		hcb.append(natureAction);
		return hcb.toHashCode();
	}

	public SGGameState copy() {
		return new SGGameState(this);
	}

	public Player getPlayerToMove() {
		return currentPlayer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currentPlayer == null) ? 0 : currentPlayer.hashCode());
		result = prime * result + ((p1Action == null) ? 0 : p1Action.hashCode());
		result = prime * result + ((p2Action == null) ? 0 : p2Action.hashCode());
		result = prime * result + ((utilities == null) ? 0 : utilities.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SGGameState other = (SGGameState) obj;
		if (currentPlayer == null) {
			if (other.currentPlayer != null)
				return false;
		} else if (!currentPlayer.equals(other.currentPlayer))
			return false;
		if (p1Action == null) {
			if (other.p1Action != null)
				return false;
		} else if (!p1Action.equals(other.p1Action))
			return false;
		if (p2Action == null) {
			if (other.p2Action != null)
				return false;
		} else if (!p2Action.equals(other.p2Action))
			return false;
		if (utilities == null) {
			if (other.utilities != null)
				return false;
		} else if (!utilities.equals(other.utilities))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Utilities: " + utilities + "\n" + p1Action + " " + p2Action;
	}

	/**
	 * Never terminal
	 */
	@Override
	public double[] getUtilities() {
		StochasticExpander expander = new SGExpander();
		List<Action> p1Actions = expander.getActions(this);
		List<Action> p2Actions = expander.getActions(this.performAction(p1Actions.get(0)));
		ZeroSumGameNESolver<ActionPureStrategy, ActionPureStrategy> solver = new ZeroSumGameNESolverImpl<ActionPureStrategy, ActionPureStrategy>(new InitialUtility(this));

		solver.addPlayerOneStrategies(wrap(p1Actions));
		solver.addPlayerTwoStrategies(wrap(p2Actions));
		solver.computeNashEquilibrium();
		return new double[] { solver.getGameValue(), -solver.getGameValue(), 0 };
	}
	
	private Iterable<ActionPureStrategy> wrap(List<Action> actions) {
		List<ActionPureStrategy> wrapedActions = new ArrayList<ActionPureStrategy>(actions.size());

		for (Action action : actions) {
			wrapedActions.add(new ActionPureStrategy(action));
		}
		return wrapedActions;
	}

	@Override
	public boolean isGameEnd() {
		return false;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return currentPlayer.equals(SGGameInfo.nature);
	}

}
