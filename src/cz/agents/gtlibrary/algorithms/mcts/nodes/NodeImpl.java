package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.strategy.Strategy;

public abstract class NodeImpl implements Node {
    
	protected InnerNode parent;
	protected GameState gameState;
	protected Action lastAction;
	protected MCTSConfig algConfig;
	protected int depth;

	public NodeImpl(InnerNode parent, Action lastAction, GameState gameState) {
		this.parent = parent;
		this.lastAction = lastAction;
		this.gameState = gameState;
		this.algConfig = parent.algConfig;
		depth = parent.depth + 1;
	}

	public NodeImpl(MCTSConfig algConfig, GameState gameState) {
		this.algConfig = algConfig;
		this.gameState = gameState;
		depth = 0;
	}

	@Override
	public InnerNode getParent() {
		return parent;
	}
	
	@Override
	public int getDepth() {
		return depth;
	}
	
    @Override
    public void setParent(InnerNode parent) {
        this.parent = parent;
    }

	@Override
	public Action getLastAction() {
		return lastAction;
	}
	
	@Override
	public GameState getGameState() {
		return gameState;
	}
	
        @Override
	public Strategy getStrategyFor(Player player, Distribution distribution) {
            return getStrategyFor(player, distribution, Integer.MAX_VALUE);
        }
        
	@Override
	public String toString() {
		return "Node: " + gameState;
	}

}
