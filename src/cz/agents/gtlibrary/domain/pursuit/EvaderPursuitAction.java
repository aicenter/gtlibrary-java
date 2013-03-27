package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.domain.bpg.data.Node;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class EvaderPursuitAction extends ActionImpl {

	private Node origin;
	private Node destination;
	private int hashCode = -1;

	public EvaderPursuitAction(Node origin, Node destination, InformationSet informationSet) {
		super(informationSet);
		this.origin = origin;
		this.destination = destination;
	}

	public Node getDestination() {
		return destination;
	}

	public Node getOrigin() {
		return origin;
	}

	@Override
	public void perform(GameState gameState) {
		((PursuitGameState) gameState).executeEvaderAction(this);
	}


	@Override
	public int hashCode() {
		if (hashCode == -1) {
			final int prime = 31;
			int result = 1;
			
			result = prime * result + ((destination == null) ? 0 : destination.hashCode());
			result = prime * result + ((informationSet == null) ? 0 : informationSet.hashCode());
			result = prime * result + ((origin == null) ? 0 : origin.hashCode());
			hashCode = result;
		}
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvaderPursuitAction other = (EvaderPursuitAction) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (origin == null) {
			if (other.origin != null)
				return false;
		} else if (!origin.equals(other.origin))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "E: [" + origin + "->" + destination + "]";
	}

}
