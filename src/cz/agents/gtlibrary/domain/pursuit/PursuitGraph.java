package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

public class PursuitGraph extends Graph {

	private static final long serialVersionUID = -3128524115361864271L;
	
	private Node evaderStart;
	private Node p1Start;
	private Node p2Start;
	
	public PursuitGraph(String graphFile) {
		super(graphFile);
	}

	protected void init() {
		super.init();
		evaderStart = allNodes.get("ID" + PursuitGameInfo.evaderStart);
		p1Start = allNodes.get("ID" + PursuitGameInfo.p1Start);
		p2Start = allNodes.get("ID" + PursuitGameInfo.p2Start);
	}

	public Node getEvaderStart() {
		return evaderStart;
	}

	public Node getP1Start() {
		return p1Start;
	}

	public Node getP2Start() {
		return p2Start;
	}

}
