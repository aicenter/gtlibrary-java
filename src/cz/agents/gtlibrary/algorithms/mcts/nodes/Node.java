package cz.agents.gtlibrary.algorithms.mcts.nodes;

import java.util.Map;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public interface Node {

	public Node selectRecursively();

	public void expand();
	
	public double[] simulate();

	public void backPropagate(double[] value);

	public InnerNode getParent();

	public void setParent(InnerNode parent);
	
	public Action getLastAction();

	public GameState getGameState();

	public double[] getEV();

	public int getNbSamples();
    
	public int getDepth();
	
	public Map<Sequence, Double> getPureStrategyFor(Player player);

}