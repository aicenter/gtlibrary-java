package cz.agents.gtlibrary.domain.bpg.data;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DefaultDirectedGraph;

public class BorderPatrollingGraph {

	private DefaultDirectedGraph<Node, Edge> graph = new DefaultDirectedGraph<Node, Edge>(Edge.class);
	private Map<String, Node> allNodes = new HashMap<String, Node>();
	private Node origin = null;
	private Node destination = null;
	private Node p1Start = null;
	private Node p2Start = null;

	final protected DataLoader dl;

	public BorderPatrollingGraph() {
		dl = new DataLoader();
		init();
	}

	public BorderPatrollingGraph(String graphFile) {
		dl = new DataLoader(graphFile);
		init();
	}

	private void init() {
		double[][] nodeMatrix = dl.getOriginalGraphIncMatrix();
		int n = dl.getNodesInOriginalGraph();

		for (int i = 0; i < n; i++) {
			Node node = new Node("ID" + i, graph);
			allNodes.put(node.getId(), node);
			if (i == 0) {
				origin = node;
			} else if (i == n - 3) {
				destination = node;
			} else if (i == n - 2) {
				p1Start = node;
			} else if (i == n - 1) {
				p2Start = node;
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (nodeMatrix[i][j] == 1)
					new Edge("E-" + i + "-" + j, allNodes.get("ID" + i), allNodes.get("ID" + j), graph);
			}
		}
	}

	public DefaultDirectedGraph<Node, Edge> getGraph() {
		return graph;
	}

	public Map<String, Node> getAllNodes() {
		return allNodes;
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

	public DataLoader getDl() {
		return dl;
	}

    public Node getNodeByID(int ID) {
        return allNodes.get("ID" + ID);
    }
}
