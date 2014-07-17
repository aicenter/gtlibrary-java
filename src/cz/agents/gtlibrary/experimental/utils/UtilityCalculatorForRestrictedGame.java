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


package cz.agents.gtlibrary.experimental.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;

public class UtilityCalculatorForRestrictedGame extends UtilityCalculator {

	Map<GameState, Double> utilities;

	public UtilityCalculatorForRestrictedGame(GameState rootState, Expander<? extends InformationSet> expander, Map<GameState, Double> utilities) {
		super(rootState, expander);
		this.utilities = utilities;
	}

	protected double computeUtility(GameState state, Strategy strategy1, Strategy strategy2) {
		if (state.isGameEnd())
			return state.getUtilities()[0];
		if (utilities != null) {
			Double tempUtility = utilities.get(state);

			if (tempUtility != null)
				return tempUtility;
		}
		if (state.isPlayerToMoveNature())
			return computeUtilityForNature(state, strategy1, strategy2);
		return computeUtilityForRegularPlayer(state, strategy1, strategy2);
	}
	
	@Override
	protected Map<Action, Double> getContOfStrat(GameState state, Strategy strategy) {
		Map<Action, Double> strategyContinuation = new HashMap<Action, Double>();
		
		for (Action action : expander.getActions(state)) {
			Sequence continuation = new LinkedListSequenceImpl(state.getSequenceForPlayerToMove());
			
			continuation.addLast(action);
			strategyContinuation.put(action, strategy.get(continuation));
		}
		return strategyContinuation;
	}
	
	@Override
	protected double getUtilityForFirstPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
		Map<Action, Double> contOfStrat = getContOfStrat(state, strategy1);
		double utility = 0;
		double sum = getProbabilitySumOf(contOfStrat);

		if (contOfStrat.isEmpty())
			return 0;
		if (hasOnlyZeros(contOfStrat))
			return 0;
		for (Entry<Action, Double> entry : contOfStrat.entrySet()) {
			if (entry.getValue() > 1e-8)
				utility += computeUtility(state.performAction(entry.getKey()), strategy1, strategy2) * entry.getValue() / sum;
		}
		return utility;
	}
	
	@Override
	protected double getUtilityForSecondPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
		Map<Action, Double> contOfStrat = getContOfStrat(state, strategy2);
		double utility = 0;
		double sum = getProbabilitySumOf(contOfStrat);

		if (contOfStrat.isEmpty())
			return 0;
		if (hasOnlyZeros(contOfStrat))
			return 0;
		for (Entry<Action, Double> entry : contOfStrat.entrySet()) {
			if (entry.getValue() > 1e-8)
				utility += computeUtility(state.performAction(entry.getKey()), strategy1, strategy2) * entry.getValue() / sum;
		}
		return utility;
	}

}
