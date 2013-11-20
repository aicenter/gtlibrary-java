package cz.agents.gtlibrary.domain.bpg.data;

import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

public class BorderPatrollingGraph extends Graph {

	private static final long serialVersionUID = -5572263962229886222L;
	
	private Node origin;
	private Node destination;
	private Node p1Start;
	private Node p2Start;

	protected void init() {
		super.init();
		int n = dl.getNodesInOriginalGraph();

		origin = allNodes.get("ID0");
		destination = allNodes.get("ID" + (n - 3));
		p1Start = allNodes.get("ID" + (n - 2));
		p2Start = allNodes.get("ID" + (n - 1));
	}

	public Node getOrigin() {
		return origin;
	}

	public Node getDestination() {
		return destination;
	}

	public Node getP1Start() {
		return p1Start;
	}

	public Node getP2Start() {
		return p2Start;
	}
    public Node getNodeByID(int ID) {
        return allNodes.get("ID" + ID);
    }
}
