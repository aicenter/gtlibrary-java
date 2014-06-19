package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import java.util.Random;

public class ChanceNode extends InnerNode {
	private Random random;

	public ChanceNode(InnerNode parent, GameState gameState, Action lastAction) {
		this(parent, gameState, lastAction, new Random());
	}
	
	public ChanceNode(Expander<MCTSInformationSet> expander, GameState gameState) {
		this(expander, gameState, new Random());
	}

    public ChanceNode(InnerNode parent, GameState gameState, Action lastAction, long seed) {
        this(parent, gameState, lastAction, new Random(seed));
    }

    public ChanceNode(Expander<MCTSInformationSet> expander, GameState gameState, long seed) {
        this(expander, gameState, new Random(seed));
    }

    public ChanceNode(InnerNode parent, GameState gameState, Action lastAction, Random random) {
        super(parent, gameState, lastAction);
        this.random = random;
    }

    public ChanceNode(Expander<MCTSInformationSet> expander, GameState gameState, Random random) {
        super(expander, gameState);
        this.random = random;
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
