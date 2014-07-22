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


package cz.agents.gtlibrary.experimental.stochastic.valueiteration.alphabeta;

import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class AlphaBetaImpl implements AlphaBeta {

	protected StochasticExpander expander;
	protected GameState root;
	protected Map<GameState, Double> values;
	protected double minUtility;
	protected double maxUtility;

	public AlphaBetaImpl(StochasticExpander expander, Map<GameState, Double> values, GameState root, double minUtility, double maxUtility) {
		this.expander = expander;
		this.root = root;
		this.values = values;
		this.minUtility = minUtility;
		this.maxUtility = maxUtility;
	}

	public double getFirstLevelValue(GameState state, double alpha, double beta) {
		if (state.isPlayerToMoveNature()) {
			return getUtilityForNature(state, alpha, beta);
		} else if (state.getPlayerToMove().getId() == 0) {
			for (Action action : expander.getActions(state)) {
				alpha = Math.max(alpha, getValue(state.performAction(action), alpha, beta));
				if (beta <= alpha)
					break;
			}
			return alpha;
		} else {
			for (Action action : expander.getActions(state)) {
				beta = Math.min(beta, getValue(state.performAction(action), alpha, beta));
				if (beta <= alpha)
					break;
			}
			return beta;
		}
	}

	public double getValue(GameState state, double alpha, double beta) {
		Double value = values.get(state);

		if (value != null)
			return value;

		if (state.isPlayerToMoveNature()) {
			return getUtilityForNature(state, alpha, beta);
		} else if (state.getPlayerToMove().getId() == 0) {
			for (Action action : expander.getActions(state)) {
				alpha = Math.max(alpha, getValue(state.performAction(action), alpha, beta));
				if (beta <= alpha)
					break;
			}
			return alpha;
		} else {
			for (Action action : expander.getActions(state)) {
				beta = Math.min(beta, getValue(state.performAction(action), alpha, beta));
				if (beta <= alpha)
					break;
			}
			return beta;
		}
	}

	public double getUtilityForNature(GameState state, double alpha, double beta) {
		double utility = 0;
		List<Action> actions = expander.getActions(state);
		ListIterator<Action> iterator = actions.listIterator();

		while (iterator.hasNext()) {
			Action action = iterator.next();
			double lowerBound = minUtility;//Math.max(minUtility, getLowerBound(actions, state, alpha, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));
			double upperBound = maxUtility;//Math.min(maxUtility, getUpperBound(actions, state, beta, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));

			utility += state.getProbabilityOfNatureFor(action) * getValue(state.performAction(action), lowerBound, upperBound);
//			if (isTooFar(utility, iterator.previousIndex(), actions.size(), alpha, beta))
//				return alpha;
		}
		return utility;
	}

	private boolean isTooFar(double utility, int index, int size, double alpha, double beta) {
		if (utility < alpha)
			return (utility + (size - index) * maxUtility) < alpha;
		else if (utility < alpha)
			return (utility + (size - index) * minUtility) > beta;
		else
			return false;
	}

	private double getUpperBound(List<Action> actions, GameState state, double upperBound, double probability, double utilityValue, int index) {
		ListIterator<Action> iterator = actions.listIterator();
		double utility = utilityValue;

		while (iterator.hasNext()) {
			Action action = iterator.next();

			if (iterator.previousIndex() > index)
				utility += state.getProbabilityOfNatureFor(action) * minUtility;
		}
		return (upperBound - utility) / probability;
	}

	private double getLowerBound(List<Action> actions, GameState state, double lowerBound, double probability, double utilityValue, int index) {
		ListIterator<Action> iterator = actions.listIterator();
		double utility = utilityValue;

		while (iterator.hasNext()) {
			Action action = iterator.next();

			if (iterator.previousIndex() > index)
				utility += state.getProbabilityOfNatureFor(action) * maxUtility;
		}
		return (lowerBound - utility) / probability;
	}
}
