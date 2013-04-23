package cz.agents.gtlibrary.algorithms.mcts.nodes;

import java.util.Random;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.strategy.Strategy;

public class ChanceNode extends InnerNode {
	private Random random;

	public ChanceNode(InnerNode parent, GameState gameState, Action lastAction) {
		super(parent, gameState, lastAction);
		random = new Random();
	}
	
	public ChanceNode(Expander<MCTSInformationSet> expander, MCTSConfig config, GameState gameState) {
		super(expander, config, gameState);
		random = new Random();
	}

	@Override
	public Node selectChild() {
		Node selected = null;
		Action action = getRandomAction();

		selected = children.get(action);
		if (selected == null) {
			selected = getNewChildAfter(action);
			children.put(action, selected);
		}
		return selected;
	}

	private Action getRandomAction() {
		double move = random.nextDouble();
		
		for (Action action : actions) {
			move -= gameState.getProbabilityOfNatureFor(action);
			if (move < 0) {
				return action;
			}
		}
		return actions.get(actions.size() - 1);
	}
	
	@Override
	public Strategy getStrategyFor(Player player, Distribution distribution) {
		Strategy strategy = algConfig.getEmptyStrategy();

		for (Node child : children.values()) {
			strategy.putAll(getStrategyFor(child, player, distribution));
		}
		return strategy;
	}
        
        	@Override
	public void backPropagate(Action action, double[] values) {
                for (int i=0; i < nodeStats.length; i++) nodeStats[i].onBackPropagate(values[i]);
		if (parent != null && !parent.isLocked()) {
			parent.backPropagate(lastAction, values);
		}
	}
}
