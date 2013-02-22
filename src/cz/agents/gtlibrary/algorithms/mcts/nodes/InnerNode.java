package cz.agents.gtlibrary.algorithms.mcts.nodes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.RunningStats;

public class InnerNode extends NodeImpl {

	protected LinkedHashMap<Action, BackPropagationStrategy> actionStats;
	protected Node[] children;
	protected RunningStats[] nodeStats;
	protected List<Action> actions;
	protected Player currentPlayer;
	protected Expander<MCTSInformationSet> expander;

	protected boolean isLocked;

	public InnerNode(InnerNode parent, GameState gameState, Action lastAction) {
		super(parent, lastAction, gameState);
		currentPlayer = gameState.getPlayerToMove();
		isLocked = false;
		this.expander = parent.expander;

		attendInformationSet();
		initNodeStats(gameState);
	}

	public InnerNode(Expander<MCTSInformationSet> expander, MCTSConfig config, GameState gameState) {
		super(config, gameState);
		currentPlayer = gameState.getPlayerToMove();
		isLocked = false;
		this.expander = expander;

		attendInformationSet();
		initNodeStats(gameState);
	}

	private void initNodeStats(GameState gameState) {
		nodeStats = new RunningStats[gameState.getAllPlayers().length];
		for (int i = 0; i < gameState.getAllPlayers().length; i++) {
			nodeStats[i] = new RunningStats();
		}
	}

	private void attendInformationSet() {
		MCTSInformationSet newIS = algConfig.getInformationSetFor(gameState);

		if (newIS == null) {
			newIS = new MCTSInformationSet(gameState);
			algConfig.addInformationSetFor(gameState, newIS);
		}
		newIS.addNode(this);
		newIS.addStateToIS(gameState);
	}

	public double[] simulate() {
		return algConfig.getSimulator().simulate(gameState, expander);
	}

	public Node selectChild() {
		return getChildFor(getIndexFromDecisionStrategy(currentPlayer.getId()));
	}

	public Node getNewChildAfter(Action action) {
		GameState nextState = gameState.performAction(action);

		if (nextState.isGameEnd()) {
			return new LeafNode(this, nextState, action);
		}
		if (nextState.isPlayerToMoveNature()) {
			return new ChanceNode(this, nextState, action);
		}
		return new InnerNode(this, nextState, action);
	}

	@Override
	public void backPropagate(double[] values) {
		assert nodeStats.length == values.length;

		for (int i = 0; i < nodeStats.length; i++)
			nodeStats[i].add(values[i]);
		if (parent != null && !parent.isLocked()) {
			parent.backPropagateInActions(lastAction, values);
			parent.backPropagate(values);
		}

	}

	public void backPropagateInActions(Action action, double[] values) {
		Set<InnerNode> otherNodesInIS = algConfig.getInformationSetFor(gameState).getAllNodes();

		otherNodesInIS.add(this);
		for (InnerNode innerNode : otherNodesInIS) {
			if (innerNode.nodeStats[0].getNbSamples() == 0)
				continue;
			BackPropagationStrategy strategy = innerNode.actionStats.get(action);

			strategy.onBackPropagate(values[currentPlayer.getId()]);
		}
	}

	protected Node getChildFor(int index) {
		Node selected = children[index];

		if (selected == null) {
			selected = createChild(index);
		}
		return selected;
	}

	protected Node createChild(int index) {
		Node child = getNewChildAfter(this.actions.get(index));

		children[index] = child;
		return child;
	}

	protected int getIndexFromDecisionStrategy(int playerIndex) {
		return algConfig.getSelectionStrategy().select(nodeStats[playerIndex], actionStats);
	}

	@Override
	public Node selectRecursively() {
		if (children == null)
			return this;
		return selectChild().selectRecursively();
	}

	public Node selectRecursively(int fixedDepth) {
		if (fixedDepth > 0)
			isLocked = true;
		if (children == null)
			return this;
		Node child = selectChild();

		return child instanceof LeafNode ? child : ((InnerNode) child).selectRecursively(fixedDepth - 1);
	}

	public boolean isLocked() {
		return isLocked;
	}

	@Override
	public int hashCode() {
		return gameState.getHistory().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof InnerNode))
			return false;
		return this.hashCode() == obj.hashCode();
	}

	@Override
	public void expand() {
		if (children != null) {
			return;
		}
		actionStats = new LinkedHashMap<Action, BackPropagationStrategy>();
		actions = expander.getActions(gameState);
		for (Action action : actions) {
			actionStats.put(action, algConfig.getBackPropagationStrategyFor(this, currentPlayer));
		}
		children = new Node[actions.size()];
	}

	@Override
	public double[] getEV() {
		double[] ev = new double[nodeStats.length];

		for (int i = 0; i < ev.length; i++)
			ev[i] = nodeStats[i].getMean();
		return ev;
	}

	@Override
	public int getNbSamples() {
		return nodeStats[0].getNbSamples();
	}

}
