package cz.agents.gtlibrary.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Sequence;

public class UtilityCalculator {

	private GameState rootState;
	private Expander<? extends InformationSet> expander;

	public UtilityCalculator(GameState rootState, Expander<? extends InformationSet> expander) {
		super();
		this.rootState = rootState;
		this.expander = expander;
	}

	public double computeUtility(Map<Sequence, Double> strategy1, Map<Sequence, Double> strategy2) {
		return computeUtility(rootState, strategy1, strategy2);
	}

	private double computeUtility(GameState state, Map<Sequence, Double> strategy1, Map<Sequence, Double> strategy2) {
		if (state.isGameEnd())
			return state.getUtilities()[0];
		if (state.isPlayerToMoveNature()) {
			return computeUtilityForNature(state, strategy1, strategy2);
		}
		return computeUtilityForRegularPlayer(state, strategy1, strategy2);
	}

	private double computeUtilityForRegularPlayer(GameState state, Map<Sequence, Double> strategy1, Map<Sequence, Double> strategy2) {
		if (state.getPlayerToMove().getId() == 0)
			return getUtilityForFirstPlayer(state, strategy1, strategy2);
		return getUtilityForSecondPlayer(state, strategy1, strategy2);
	}

	private double getUtilityForFirstPlayer(GameState state, Map<Sequence, Double> strategy1, Map<Sequence, Double> strategy2) {
		List<Sequence> contOfPureStrat = getContOfPureStrat(state, strategy1);
		double utility = 0;
		double sum = getProbabilitySumOf(contOfPureStrat, strategy1);

		if (contOfPureStrat.isEmpty())
			throw new UnsupportedOperationException("Missing sequences");
		for (Sequence sequence : contOfPureStrat) {
			if (strategy1.get(sequence) > 1e-8)
				utility += computeUtility(state.performAction(sequence.getLast()), strategy1, strategy2) * strategy1.get(sequence) / sum;
		}
		return utility;
	}

	private double getUtilityForSecondPlayer(GameState state, Map<Sequence, Double> strategy1, Map<Sequence, Double> strategy2) {
		List<Sequence> contOfPureStrat = getContOfPureStrat(state, strategy2);
		double utility = 0;
		double sum = getProbabilitySumOf(contOfPureStrat, strategy2);

		if (contOfPureStrat.isEmpty())
			throw new UnsupportedOperationException("Missing sequences");
		for (Sequence sequence : contOfPureStrat) {
			if (strategy2.get(sequence) > 1e-8)
				utility += computeUtility(state.performAction(sequence.getLast()), strategy1, strategy2) * strategy2.get(sequence) / sum;
		}
		return utility;
	}

	private double getProbabilitySumOf(List<Sequence> contOfPureStrat, Map<Sequence, Double> strategy) {
		double probabilitySum = 0;

		for (Sequence sequence : contOfPureStrat) {
			probabilitySum += strategy.get(sequence);
		}
		return probabilitySum;
	}

	private double computeUtilityForNature(GameState state, Map<Sequence, Double> strategy1, Map<Sequence, Double> strategy2) {
		double utility = 0;

		for (Action action : expander.getActions(state)) {
			utility += state.getProbabilityOfNatureFor(action) * computeUtility(state.performAction(action), strategy1, strategy2);
		}
		return utility;
	}

	private List<Sequence> getContOfPureStrat(GameState state, Map<Sequence, Double> strategy) {
		List<Sequence> contOfPureStrat = new LinkedList<Sequence>();

		for (Action action : expander.getActions(state)) {
			Sequence continuationSequence = getContinuationSequence(state, action);

			if (strategy.containsKey(continuationSequence)) {
				contOfPureStrat.add(continuationSequence);
			}
		}
		return contOfPureStrat;
	}

	private Sequence getContinuationSequence(GameState state, Action action) {
		Sequence continuationSequence = new LinkedListSequenceImpl(state.getSequenceForPlayerToMove());

		continuationSequence.addLast(action);
		return continuationSequence;
	}

}
