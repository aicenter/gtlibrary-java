package cz.agents.gtlibrary.algorithms.mcts.nodes;

import java.util.List;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

public class InnerNode extends NodeImpl {

	protected Node[] children;
	protected List<Action> actions;
	protected Player currentPlayer;
	protected Expander<MCTSInformationSet> expander;
	protected MCTSInformationSet informationSet;

	protected boolean isLocked;

	public InnerNode(InnerNode parent, GameState gameState, Action lastAction) {
		super(parent, lastAction, gameState);
		currentPlayer = gameState.getPlayerToMove();
		isLocked = false;
		this.expander = parent.expander;

		attendInformationSet();
	}

	public InnerNode(Expander<MCTSInformationSet> expander, MCTSConfig config, GameState gameState) {
		super(config, gameState);
		currentPlayer = gameState.getPlayerToMove();
		isLocked = false;
		this.expander = expander;

		attendInformationSet();
	}

	private void attendInformationSet() {
		informationSet = algConfig.getInformationSetFor(gameState);

		if (informationSet == null) {
			informationSet = new MCTSInformationSet(gameState);
			algConfig.addInformationSetFor(gameState, informationSet);
		}
		informationSet.addNode(this);
		informationSet.addStateToIS(gameState);
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
		informationSet.addValuesToStats(values);
		if (parent != null && !parent.isLocked()) {
			parent.backPropagateInActions(lastAction, values);
			parent.backPropagate(values);
		}

	}

	public void backPropagateInActions(Action action, double[] values) {
		informationSet.updateActionStatsFor(action, values);
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
		return algConfig.getSelectionStrategy().select(informationSet.getStatsFor(playerIndex), informationSet.getActionStats());
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
		actions = expander.getActions(gameState);
		informationSet.initActionStats(actions, algConfig.getBackPropagationStrategyFactory());;
		children = new Node[actions.size()];
	}

	@Override
	public double[] getEV() {
		double[] ev = new double[gameState.getAllPlayers().length];

		for (int i = 0; i < ev.length; i++)
			ev[i] = informationSet.getStatsFor(i).getMean();
		return ev;
	}

	@Override
	public int getNbSamples() {
		return informationSet.getStatsFor(0).getNbSamples();
	}

}
