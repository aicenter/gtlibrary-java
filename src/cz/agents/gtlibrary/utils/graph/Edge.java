/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.utils.graph;

import org.jgrapht.graph.DefaultDirectedGraph;

import java.io.Serializable;

public class Edge implements Serializable {
	
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
