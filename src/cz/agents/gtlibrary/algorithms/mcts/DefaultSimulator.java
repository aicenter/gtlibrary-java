package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.iinodes.GameStateImpl;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DefaultSimulator implements Simulator {
	final private Random rnd;
        final private Expander expander;

        public DefaultSimulator(Expander expander) {
		this.rnd = new HighQualityRandom();
                this.expander = expander;
	}
        
	public DefaultSimulator(Expander expander, long seed) {
		this.rnd = new HighQualityRandom(seed);
                this.expander = expander;
	}

    @Override
	public double[] simulate(GameState gameState) {
		GameStateImpl state = (GameStateImpl) gameState.copy();

		while (!state.isGameEnd()) {
			if (state.isPlayerToMoveNature()) {
				state.performActionModifyingThisState(getActionForNature(state));
			} else {
				state.performActionModifyingThisState(getActionForRegularPlayer(state));
			}
		}
		return state.getUtilities();
	}

	private Action getActionForNature(GameStateImpl state) {
		List<Action> actions = expander.getActions(new MCTSInformationSet(state));
		double move = rnd.nextDouble();

		for (Action action : actions) {
			move -= state.getProbabilityOfNatureFor(action);
			if (move < 0) {
				return action;
			}
		}
		return actions.get(actions.size() - 1);
	}

	private Action getAction(GameStateImpl state, Map<Sequence, Double> opponentRealizationPlan, Player opponent) {
		if (state.isPlayerToMoveNature())
			return getActionForNature(state);
		if (state.getPlayerToMove().equals(opponent))
			return getActionForOpponent(state, opponentRealizationPlan, opponent);
		return getActionForRegularPlayer(state);
	}

	private Action getActionForRegularPlayer(GameStateImpl state) {
		List<Action> possibleActions = expander.getActions(new MCTSInformationSet(state));

		return possibleActions.get(rnd.nextInt(possibleActions.size()));
	}

	private Action getActionForOpponent(GameStateImpl state, Map<Sequence, Double> opponentRealizationPlan, Player opponent) {
		List<Action> possibleActions = expander.getActions(new MCTSInformationSet(state));
		Double oppValueOfThisState = getValueOfThisState(state.getSequenceFor(opponent), opponentRealizationPlan);
		Map<Action, Double> contInRealPlan = getContinuationOfRP(state, possibleActions, opponentRealizationPlan, opponent);

		if (contInRealPlan.size() == 0) {
			return possibleActions.get(rnd.nextInt(possibleActions.size()));
		}
		return getRandActionFromContInRealPlan(possibleActions, oppValueOfThisState, contInRealPlan);
	}

	private Double getValueOfThisState(Sequence sequence, Map<Sequence, Double> opponentRealizationPlan) {
		if(sequence.size() == 0) 
			return 1d;
		return opponentRealizationPlan.get(sequence);
	}

	private Action getRandActionFromContInRealPlan(List<Action> possibleActions, Double oppValueOfThisState, Map<Action, Double> contInRealPlan) {
		double rndVal = rnd.nextDouble() * oppValueOfThisState;

		for (Action action : contInRealPlan.keySet()) {
			if (Double.compare(rndVal, contInRealPlan.get(action)) <= 0) {
				return action;
			}
			rndVal = rndVal - contInRealPlan.get(action);
		}
		return possibleActions.get(possibleActions.size() - 1);
	}

	public double[] simulateForRealPlan(GameState gameState, Map<Sequence, Double> opponentRealizationPlan, Player opponent, Expander<MCTSInformationSet> expander) {
		GameStateImpl state = (GameStateImpl) gameState.copy();

		while (!state.isGameEnd()) {
			state.performActionModifyingThisState(getAction(state, opponentRealizationPlan, opponent));
		}
		return state.getUtilities();
	}

	private Map<Action, Double> getContinuationOfRP(GameStateImpl state, List<Action> possibleActions, Map<Sequence, Double> opponentRealizationPlan, Player opponent) {
		Map<Action, Double> contInRealPlan = new HashMap<Action, Double>();

		for (Action action : possibleActions) {
			Double contNodeOppRealValue = getContValFor(state, opponentRealizationPlan, opponent, action);

			if (contNodeOppRealValue != null && contNodeOppRealValue > 0)
				contInRealPlan.put(action, contNodeOppRealValue);
		}
		return contInRealPlan;
	}

	private Double getContValFor(GameStateImpl state, Map<Sequence, Double> opponentRealizationPlan, Player opponent, Action action) {
		Sequence nextSequence = new LinkedListSequenceImpl(state.getHistory().getSequenceOf(opponent));

		nextSequence.addLast(action);
		return opponentRealizationPlan.get(nextSequence);
	}
}