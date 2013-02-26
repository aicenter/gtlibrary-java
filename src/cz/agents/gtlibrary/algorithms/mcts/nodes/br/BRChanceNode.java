package cz.agents.gtlibrary.algorithms.mcts.nodes.br;

import java.util.Map;
import java.util.Random;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class BRChanceNode extends BRInnerNode {
	
//	private Random random;
	
	public BRChanceNode(BRInnerNode parent, GameState gameState, Action lastAction) {
		super(parent, gameState, lastAction);
//		random = parent.brRandom;
	}
	
	public BRChanceNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig config, 
			Map<Sequence, Double> opponentRealizationPlan, Player opponent, long seed) {
		super(gameState, expander, config, opponentRealizationPlan, opponent, seed);
//		random = brRandom;
	}
	
	@Override
	public Node selectChild() {
		Node selected = null;
		int index = getRandomIndex();

		selected = children[index];
		if (selected == null) {
			selected = getNewChildAfter(this.actions.get(index));
			children[index] = selected;
		}
		return selected;
	}

	private int getRandomIndex() {
		double move = brRandom.nextDouble();
		int index = 0;
		
		for (Action action : actions) {
			move -= gameState.getProbabilityOfNatureFor(action);
			if (move < 0) {
				return index;
			}
			index++;
		}
		return index;
	}
}
