package cz.agents.gtlibrary.algorithms.mcts.nodes.br;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;

public class BRChanceNode extends BRInnerNode {

	public BRChanceNode(BRInnerNode parent, GameState gameState, Action lastAction) {
		super(parent, gameState, lastAction);
	}

	public BRChanceNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig config, Map<Sequence, Double> opponentRealizationPlan, Player opponent, long seed) {
		super(gameState, expander, config, opponentRealizationPlan, opponent, seed);
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
		Strategy pureStrategy = algConfig.getEmptyStrategy();

		for (Node child : children.values()) {
			pureStrategy.putAll(getStrategyFor(child, player, distribution));
		}
		return pureStrategy;
	}
}
