package cz.agents.gtlibrary.domain.bpg.data;

import java.util.ArrayList;

import org.jgrapht.graph.DefaultDirectedGraph;

public class Node {

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
