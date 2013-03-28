package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.strategy.Strategy;

public class LeafNode extends NodeImpl {

	private final double[] utilities;
	private int nbSamples = 0;

	public LeafNode(InnerNode parent, GameState gameState, Action lastAction) {
		super(parent, lastAction, gameState);
		this.utilities = gameState.getUtilities();
	}

	public static int leafNodeUsed = 0;

	@Override
	public double[] simulate() {
		leafNodeUsed++;
		return utilities;
	}

	@Override
	public void backPropagate(double[] value) {
		++nbSamples;
		parent.backPropagateInActions(lastAction, value);
		parent.backPropagate(value);
	}

	@Override
	public GameState getGameState() {
		return gameState;
	}

	public double[] getEV() {
		return utilities;
	}

	public int getNbSamples() {
		return nbSamples;
	}
	
	@Override
	public Node selectRecursively() {
		return this;
	}

	@Override
	public void expand() {
		
	}

	@Override
	public Strategy getStrategyFor(Player player, Distribution distribution) {
		return algConfig.getEmptyStrategy();
	}
}
