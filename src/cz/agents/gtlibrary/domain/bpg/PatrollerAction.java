package cz.agents.gtlibrary.domain.bpg;

import java.util.Set;

import cz.agents.gtlibrary.domain.bpg.data.Node;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class PatrollerAction extends ActionImpl {

	private Set<Node> flaggedNodes;
	private Node fromNodeP1;
	private Node fromNodeP2;
	private Node toNodeP1;
	private Node toNodeP2;
	private int hashCode = -1;

	public PatrollerAction(Node fromNodeP1, Node fromNodeP2, Node toNodeP1, Node toNodeP2, InformationSet informationSet, Set<Node> flaggedNodes) {
		super(informationSet);
		this.fromNodeP1 = fromNodeP1;
		this.fromNodeP2 = fromNodeP2;
		this.toNodeP1 = toNodeP1;
		this.toNodeP2 = toNodeP2;
		this.flaggedNodes = flaggedNodes;
	}

	public Node getFromNodeForP1() {
		return fromNodeP1;
	}

	public Node getFromNodeForP2() {
		return fromNodeP2;
	}

	public Node getToNodeForP1() {
		return toNodeP1;
	}

	public Node getToNodeForP2() {
		return toNodeP2;
	}

	@Override
	public void perform(GameState gameState) {
		((BPGGameState) gameState).executePatrollerAction(this);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("PA:");
		sb.append("[" + buildStringRepFor(fromNodeP1) + " -> " + buildStringRepFor(toNodeP1) + "]");
		sb.append("[" + buildStringRepFor(fromNodeP2) + " -> " + buildStringRepFor(toNodeP2) + "]");
		return sb.toString();
	}

	private String buildStringRepFor(Node node) {
		return node.toString() + (flaggedNodes.contains(node) ? "'" : "");
	}

	@Override
	public int hashCode() {
		if(hashCode != -1)
			return hashCode;
		final int prime = 31;
		hashCode = 1;

		hashCode = prime * hashCode + ((fromNodeP1 == null) ? 0 : fromNodeP1.hashCode());
		hashCode = prime * hashCode + ((fromNodeP2 == null) ? 0 : fromNodeP2.hashCode());
		hashCode = prime * hashCode + ((toNodeP1 == null) ? 0 : toNodeP1.hashCode());
		hashCode = prime * hashCode + ((toNodeP2 == null) ? 0 : toNodeP2.hashCode());
		hashCode = prime * hashCode + (informationSet.hashCode());

		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		PatrollerAction other = (PatrollerAction) obj;
		if (!fromNodeP1.equals(other.fromNodeP1))
			return false;
		if (!fromNodeP2.equals(other.fromNodeP2))
			return false;
		if (!toNodeP1.equals(other.toNodeP1))
			return false;
		if (!toNodeP2.equals(other.toNodeP2))
			return false;
		return true;
	}

}
