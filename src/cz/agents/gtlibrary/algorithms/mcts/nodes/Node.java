package cz.agents.gtlibrary.algorithms.mcts.nodes;


import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.strategy.Strategy;
import java.io.Serializable;

public interface Node extends Serializable {

	public Node selectRecursively();

	public void expand();
	
	public double[] simulate();

	public void backPropagate(Action action, double[] value);

	public InnerNode getParent();

	public void setParent(InnerNode parent);
	
	public Action getLastAction();

	public GameState getGameState();

	public double[] getEV();

	public int getNbSamples();
    
	public int getDepth();
	
	public Strategy getStrategyFor(Player player, Distribution distribution);
        
        public Strategy getStrategyFor(Player player, Distribution distribution, int cutOffDepth);

}