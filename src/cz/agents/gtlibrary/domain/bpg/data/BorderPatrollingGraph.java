package cz.agents.gtlibrary.domain.bpg.data;

import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.Map;

public class BorderPatrollingGraph extends Graph {

	protected static final long serialVersionUID = -5572263962229886222L;
	
	protected Node origin;
	protected Node destination;
	protected Node p1Start;
	protected Node p2Start;

    public BorderPatrollingGraph(String graphFile) {
        super(graphFile);
    }

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
