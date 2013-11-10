package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.utils.graph.Node;

public class DistanceNode implements Comparable<DistanceNode>{
	
	private Node node;
	private int distance;
	
	public DistanceNode(Node node, int distance) {
		super();
		this.node = node;
		this.distance = distance;
	}

	@Override
	public int compareTo(DistanceNode o) {
		return this.distance - o.distance;
	}
	
	public int getDistance() {
		return distance;
	}
	
	public Node getNode() {
		return node;
	}
	
	@Override
	public String toString() {
		return "{" + node.toString() + ", " + distance + "}";
	}
}
