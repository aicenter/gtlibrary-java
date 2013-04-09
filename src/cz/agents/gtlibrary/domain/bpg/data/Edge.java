package cz.agents.gtlibrary.domain.bpg.data;

import org.jgrapht.graph.DefaultDirectedGraph;

public class Edge {
	
	private Node source;
	private Node target;
	private String id;
	
	public Edge(String id, Node source, Node target) {
		this.id = id;
		this.source = source;
		this.target = target;
	}

	public Edge(String id, Node source,Node target, DefaultDirectedGraph<Node, Edge> graph) {
		this(id, source,target);
		boolean added = graph.addEdge(source, target,this);
		if(!added){
			System.err.println("Edge was not added!");
		}
		
		if(source!=graph.getEdgeSource(this)|| target!=graph.getEdgeTarget(this)){
			System.err.println("Inconsistent!");
		}
	}

	public Node getSource() {
		return source;
	}

	public Node getTarget() {
		return target;
	}

	public String toLongString() {
		return "E("+id+"){s=" + source + ", t="
				+ target + "}";
	}

	@Override
	public String toString() {
		return id;
	}

	public String getId() {
		return id;
	}

	public void setSource(Node source) {
		this.source = source;
	}

	public void setTarget(Node target) {
		this.target = target;
	}
	
}
