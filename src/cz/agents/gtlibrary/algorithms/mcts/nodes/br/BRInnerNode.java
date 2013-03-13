package cz.agents.gtlibrary.algorithms.mcts.nodes.br;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
	protected Map<Action, Double> continuationInRP;
	protected Random random;
	protected Player opponent;

	protected long seed;

	public BRInnerNode(BRInnerNode parent, GameState gameState, Action lastAction) {
		super(parent, gameState, lastAction);
		this.opponentRealizationPlan = parent.opponentRealizationPlan;
		this.seed = parent.seed;
		this.opponent = parent.opponent;
		random = parent.random;
		continuationInRP = getContinuationInRP();
	}

	public BRInnerNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig config, Map<Sequence, Double> opponentRealizationPlan, Player opponent, long seed) {
		super(expander, config, gameState);
		this.opponentRealizationPlan = opponentRealizationPlan;
		this.seed = seed;
		this.opponent = opponent;
		random = new Random(seed);
		continuationInRP = getContinuationInRP();
	}

	@Override
	public double[] simulate() {
		return algConfig.getSimulator().simulateForRealPlan(gameState, opponentRealizationPlan, opponent, expander);
	}

	@Override
	public Node selectChild() {
		return getChildFor(getAction());
	}

	private Action getAction() {
		if (currentPlayer.equals(opponent)) {
			return getActionForOpponent();
		}
		return getActionFromDecisionStrategy(currentPlayer.getId());
	}

	private Action getActionForOpponent() {
		Double oppRealValOfThisNode = opponentRealizationPlan.get(gameState.getSequenceFor(opponent));

		if (oppRealValOfThisNode != null && oppRealValOfThisNode > 0) {
			if (continuationInRP.size() != 0) {
				return getRandomAction(oppRealValOfThisNode);
			}
		}
		return getActionFromDecisionStrategy(opponent.getId());
	}

	private Map<Action, Double> getContinuationInRP() {
		Map<Action, Double> contInRealPlan = new HashMap<Action, Double>();

		for (Action action : actions) {
			Double contNodeOppRealValue = getContValue(opponent, action);

			if (contNodeOppRealValue != null && contNodeOppRealValue > 0)
				contInRealPlan.put(action, contNodeOppRealValue);
		}
		return contInRealPlan;
	}

	private Action getRandomAction(Double oppRealValOfThisNode) {
		double rndVal = random.nextDouble() * oppRealValOfThisNode;

		for (Action action : continuationInRP.keySet()) {
			if (rndVal < continuationInRP.get(action)) {
				return action;
			}
			rndVal = rndVal - continuationInRP.get(action);
		}
		return null;
	}

	private Double getContValue(Player opponent, Action action) {
		Sequence nextSequence = new LinkedListSequenceImpl(gameState.getSequenceFor(opponent));

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

	@Override
	public Map<Sequence, Double> getPureStrategyFor(Player player) {
		if (currentPlayer.equals(opponent))
			return getPureStrategyForOpponent(player);
		return getPureStrategy(player);
	}

	private Map<Sequence, Double> getPureStrategy(Player player) {
		if (children == null)
			return null;
		Map<Sequence, Double> pureStrategy = new HashMap<Sequence, Double>();
		Action mostPlayedAction = getMostPlayedAction(currentPlayer.getId());

		pureStrategy.put(createSequenceForPureStrategy(mostPlayedAction), 1d);
		pureStrategy.putAll(getPureStrategyFor(children.get(mostPlayedAction), player));
		return pureStrategy;
	}

	private Sequence createSequenceForPureStrategy(Action action) {
		Sequence currentSequence = new LinkedListSequenceImpl(gameState.getSequenceFor(currentPlayer));

		currentSequence.addLast(action);
		return currentSequence;
	}

	private Map<Sequence, Double> getPureStrategyForOpponent(Player player) {
		Map<Sequence, Double> pureStrategy = new HashMap<Sequence, Double>();

		for (Node node : getNodesWithNonZeroRPContinuation()) {
			pureStrategy.putAll(getPureStrategyFor(node, player));
		}
		return pureStrategy;
	}

	protected Map<Sequence, Double> getPureStrategyFor(Node node, Player player) {
		if (node == null) {
			Map<Sequence, Double> pureStrategy = new HashMap<Sequence, Double>();
			
			pureStrategy.put(null, 1d);
			return pureStrategy;
		}
		return node.getPureStrategyFor(player);
	}

	private List<Node> getNodesWithNonZeroRPContinuation() {
		List<Node> nodes = new LinkedList<Node>();

		for (Entry<Action, Double> entry : continuationInRP.entrySet()) {
			if (entry.getValue() > 1e-8)
				nodes.add(children.get(entry.getKey()));
		}
		return nodes;
	}
}