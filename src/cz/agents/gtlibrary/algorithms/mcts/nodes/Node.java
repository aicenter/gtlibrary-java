package cz.agents.gtlibrary.algorithms.mcts.nodes;


import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import java.io.Serializable;

public interface Node extends Serializable {

	public InnerNode getParent();

	public void setParent(InnerNode parent);
	
	public Action getLastAction();

	public GameState getGameState();

	public int getDepth();
        
        public AlgorithmData getAlgorithmData();
}