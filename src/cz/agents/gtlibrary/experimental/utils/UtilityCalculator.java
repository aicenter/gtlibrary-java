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

import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.strategy.Strategy;

public class UtilityCalculator {

	protected GameState rootState;
	protected Expander<? extends InformationSet> expander;

	public UtilityCalculator(GameState rootState, Expander<? extends InformationSet> expander) {
		super();
		this.rootState = rootState;
		this.expander = expander;
	}

	public double computeUtility(Strategy strategy1, Strategy strategy2) {
		return computeUtility(rootState, strategy1, strategy2);
	}

	protected double computeUtility(GameState state, Strategy strategy1, Strategy strategy2) {
		if (state.isGameEnd())
			return state.getUtilities()[0];
		if (state.isPlayerToMoveNature()) {
			return computeUtilityForNature(state, strategy1, strategy2);
		}
		return computeUtilityForRegularPlayer(state, strategy1, strategy2);
	}

	protected double computeUtilityForRegularPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
		if (state.getPlayerToMove().getId() == 0)
			return getUtilityForFirstPlayer(state, strategy1, strategy2);
		return getUtilityForSecondPlayer(state, strategy1, strategy2);
	}

	protected double getUtilityForFirstPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
		Map<Action, Double> contOfStrat = getContOfStrat(state, strategy1);
		double utility = 0;
		double sum = getProbabilitySumOf(contOfStrat);

		if (contOfStrat.isEmpty())
			throw new UnsupportedOperationException("Missing sequences");
		if (hasOnlyZeros(contOfStrat))
			throw new UnsupportedOperationException("Missing sequences");
		for (Entry<Action, Double> entry : contOfStrat.entrySet()) {
			if (entry.getValue() > 1e-8)
				utility += computeUtility(state.performAction(entry.getKey()), strategy1, strategy2) * entry.getValue() / sum;
		}
		return utility;
	}

	protected double getUtilityForSecondPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
		Map<Action, Double> contOfStrat = getContOfStrat(state, strategy2);
		double utility = 0;
		double sum = getProbabilitySumOf(contOfStrat);

		if (contOfStrat.isEmpty())
			throw new UnsupportedOperationException("Missing sequences");
		if (hasOnlyZeros(contOfStrat))
			throw new UnsupportedOperationException("Missing sequences");
		for (Entry<Action, Double> entry : contOfStrat.entrySet()) {
			if (entry.getValue() > 1e-8)
				utility += computeUtility(state.performAction(entry.getKey()), strategy1, strategy2) * entry.getValue() / sum;
		}
		return utility;
	}

	protected boolean hasOnlyZeros(Map<Action, Double> contOfStrat) {
		for (Double value : contOfStrat.values()) {
			if (value > 0)
				return false;
		}
		return true;
	}

	protected double getProbabilitySumOf(Map<Action, Double> contOfStrat) {
		double probabilitySum = 0;

		for (Double value : contOfStrat.values()) {
			probabilitySum += value;
		}
		return probabilitySum;
	}

	protected double computeUtilityForNature(GameState state, Strategy strategy1, Strategy strategy2) {
		double utility = 0;

		for (Action action : expander.getActions(state)) {
			utility += state.getProbabilityOfNatureFor(action) * computeUtility(state.performAction(action), strategy1, strategy2);
		}
		return utility;
	}

	protected Map<Action, Double> getContOfStrat(GameState state, Strategy strategy) {
		return strategy.getDistributionOfContinuationOf(state.getSequenceForPlayerToMove(), expander.getActionsForUnknownIS(state));
	}

}