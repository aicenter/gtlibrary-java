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

import java.io.Serializable;
import java.util.ArrayList;

import org.jgrapht.graph.DefaultDirectedGraph;

public class Node implements Serializable {

	public static final Node EMPTY_NODE = new Node("ID-1");
	
	final private String id;
	final private int hash;

	private int intID;
	private int layerNo = Integer.MAX_VALUE;;

	private NODE_TYPE nodeType = NODE_TYPE.NORMAL;

	public enum NODE_TYPE {
		NORMAL, ORIGIN, DESTINATION, TARGET
	};

	private ArrayList<Integer> targetsNumber = new ArrayList<Integer>();

	public Node(String id) {
		this.id = id;
		this.intID = new Integer((String) this.id.subSequence(2, this.id.length()));
		this.hash = (intID + 7) * 17;
	}

	public Node(String id, DefaultDirectedGraph<Node, Edge> graph) {
		this(id);
		graph.addVertex(this);
	}

	public String getId() {
		return id;
	}

	public NODE_TYPE getNodeType() {
		return nodeType;
	}

	public void setNodeType(NODE_TYPE nodeType) {
		this.nodeType = nodeType;
	}

	public int getLayerNo() {
		return layerNo;
	}

	public void setLayerNo(int layerNo) {
		this.layerNo = layerNo;
	}

	public ArrayList<Integer> getTargetNumbers() {
		return targetsNumber;
	}

	public void setTargetNumber(int targetNumber) {
		this.targetsNumber.add(targetNumber);
	}

	public int getIntID() {
		return intID;
	}
	
	public boolean isEmptyNode() {
		return this.equals(EMPTY_NODE);
	}
	
	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (this.nodeType != other.nodeType)
			return false;
		if (!this.id.equalsIgnoreCase(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "N" + intID;
	}
}
