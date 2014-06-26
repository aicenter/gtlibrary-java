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
