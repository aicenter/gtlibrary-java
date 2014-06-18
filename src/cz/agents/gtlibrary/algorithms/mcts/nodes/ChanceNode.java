package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import java.util.Random;

public class ChanceNode extends InnerNode {
	private Random random;

	public ChanceNode(InnerNode parent, GameState gameState, Action lastAction) {
		super(parent, gameState, lastAction);
		random = new Random();
	}
	
	public ChanceNode(Expander<MCTSInformationSet> expander, GameState gameState) {
		super(expander, gameState);
		random = new Random();
	}

	public Action getRandomAction() {
		double move = random.nextDouble();
		
		for (Action action : actions) {
			move -= gameState.getProbabilityOfNatureFor(action);
			if (move < 0) {
				return action;
			}
		}
		return actions.get(actions.size() - 1);
	}
}
