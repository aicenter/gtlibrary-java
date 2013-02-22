package cz.agents.gtlibrary.algorithms.mcts.nodes.br;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class BRInnerNode extends InnerNode {

	protected Map<Sequence, Double> opponentRealizationPlan;
	protected Random brRandom;
	protected Player opponent;
	
	protected long seed;

	public BRInnerNode(BRInnerNode parent, GameState gameState, Action lastAction) {
		super(parent, gameState, lastAction);
		this.opponentRealizationPlan = parent.opponentRealizationPlan;
		this.seed = parent.seed;
		this.opponent = parent.opponent;
		brRandom = new Random(seed);
	}

	public BRInnerNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig config, 
			Map<Sequence, Double> opponentRealizationPlan, Player opponent, long seed) {
		super(expander, config, gameState);
		this.opponentRealizationPlan = opponentRealizationPlan;
		this.seed = seed;
		this.opponent = opponent;
		brRandom = new Random(seed);
	}

	@Override
	public double[] simulate() {
		return algConfig.getSimulator().simulateForRealPlan(gameState, opponentRealizationPlan, gameState.getAllPlayers()[opponent.getId()], expander);
	}

	@Override
	public Node selectChild() {
		return getChildFor(getIndex());
	}

	private int getIndex() {
		if (currentPlayer.equals(gameState.getAllPlayers()[opponent.getId()])) {
			return getIndexForOpponent();
		}
		return getIndexFromDecisionStrategy(currentPlayer.getId());
	}

	private int getIndexForOpponent() {
		Double oppRealValOfThisNode = opponentRealizationPlan.get(gameState.getHistory().getSequenceOf(opponent));
		Map<Action, Double> contInRealPlan = new HashMap<Action, Double>();

		if (oppRealValOfThisNode != null && oppRealValOfThisNode > 0) {
			addValOfActionsToContOfRP(opponent, contInRealPlan);
			if (contInRealPlan.size() != 0) {
				return getIndexOfAction(getRandomAction(oppRealValOfThisNode, contInRealPlan));
			}
		}
		return getIndexFromDecisionStrategy(opponent.getId());
	}

	private int getIndexOfAction(Action randomAction) {
		int index = 0;

		for (Action action : actions) {
			if (action.equals(randomAction))
				return index;
			index++;
		}
		return -1;
	}

	private Action getRandomAction(Double oppRealValOfThisNode, Map<Action, Double> contInRealPlan) {
		double rndVal = brRandom.nextDouble() * oppRealValOfThisNode;

		for (Action action : contInRealPlan.keySet()) {
			if (rndVal < contInRealPlan.get(action)) {
				return action;
			}
			rndVal = rndVal - contInRealPlan.get(action);
		}
		return null;
	}

	private void addValOfActionsToContOfRP(Player opponent, Map<Action, Double> contInRealPlan) {
		for (Action action : actions) {
			Double contNodeOppRealValue = getContValue(opponent, action);

			if (contNodeOppRealValue != null && contNodeOppRealValue > 0)
				contInRealPlan.put(action, contNodeOppRealValue);
		}
	}

	private Double getContValue(Player opponent, Action action) {
		Sequence nextSequence = new LinkedListSequenceImpl(gameState.getHistory().getSequenceOf(opponent));

		nextSequence.addLast(action);
		return opponentRealizationPlan.get(nextSequence);
	}

	@Override
	public Node getNewChildAfter(Action action) {
		GameState nextState = gameState.performAction(action);

		if (nextState.isGameEnd()) {
			return new LeafNode(this, nextState, action);
		}
		if (nextState.isPlayerToMoveNature()) {
			return new BRChanceNode(this, nextState, action);
		}
		return new BRInnerNode(this, nextState, action);
	}
}