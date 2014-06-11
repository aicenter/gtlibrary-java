package cz.agents.gtlibrary.experimental.stochastic.experiment;

import cz.agents.gtlibrary.experimental.stochastic.StochasticAction;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.graph.Node;

public class AttackerAction extends StochasticAction {
	
	private static final long serialVersionUID = 2279161420258361243L;
	
	private Node node;
	
	public AttackerAction(Node node) {
		this.node = node;
	}
	
	public Node getNode() {
		return node;
	}

	@Override
	public void perform(GameState gameState) {
		((ExperimentGameState)gameState).attackNode(node);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AttackerAction other = (AttackerAction) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[AA: " + node + "]";
	}

}
