package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class LeafNode extends NodeImpl {

	private final double[] utilities;

	public LeafNode(InnerNode parent, GameState gameState, Action lastAction) {
		super(parent, lastAction, gameState);
		this.utilities = gameState.getUtilities();
	}
        
        public double[] getUtilities(){
                return utilities;   
        }
}
