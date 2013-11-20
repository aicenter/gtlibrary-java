package cz.agents.gtlibrary.domain.stochastic.experiment;

import cz.agents.gtlibrary.domain.stochastic.StochasticAction;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.graph.Node;

public class NatureAction extends StochasticAction {
	
	private static final long serialVersionUID = 3254896802993668810L;
	
	private Node node;

	public NatureAction(Node node) {
		this.node = node;
	}
	
	public Node getNode() {
		return node;
	}

	@Override
	public void perform(GameState gameState) {
		((ExperimentGameState)gameState).commitTo(node);
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
		NatureAction other = (NatureAction) obj;
		
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[NA: " + node + "]";
	}

}
