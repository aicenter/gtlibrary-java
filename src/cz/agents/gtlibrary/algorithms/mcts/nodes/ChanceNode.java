package cz.agents.gtlibrary.algorithms.mcts.nodes;

import java.util.Random;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

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
		int index = getRandomIndex();

		selected = children[index];
		if (selected == null) {
			selected = getNewChildAfter(this.actions.get(index));
			children[index] = selected;
		}
		return selected;
	}

	private int getRandomIndex() {
		double move = random.nextDouble();
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
