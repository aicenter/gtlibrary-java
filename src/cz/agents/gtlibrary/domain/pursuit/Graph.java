package cz.agents.gtlibrary.domain.pursuit;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;

import cz.agents.gtlibrary.domain.bpg.data.DataLoader;
import cz.agents.gtlibrary.domain.bpg.data.Edge;
import cz.agents.gtlibrary.domain.bpg.data.Node;

public class Graph implements Serializable {

	private static final long serialVersionUID = -2699218462541771264L;

	private DefaultDirectedGraph<Node, Edge> graph = new DefaultDirectedGraph<Node, Edge>(Edge.class);
	private Map<String, Node> allNodes = new HashMap<String, Node>();
	private Node evaderStart = null;
	private Node p1Start = null;
	private Node p2Start = null;

	final protected DataLoader dl;

	public Graph() {
		dl = new DataLoader();
		init();
	}

	public Graph(String graphFile) {
		dl = new DataLoader(graphFile);
		init();
	}

	private void init() {
		double[][] nodeMatrix = dl.getOriginalGraphIncMatrix();
		int N = dl.getNodesInOriginalGraph();
		for (int i = 0; i < N; i++) {
			Node node = new Node("ID" + i, graph);
			allNodes.put(node.getId(), node);
			if (i == PursuitGameInfo.evaderStart) {
				evaderStart = node;
			} else if (i == PursuitGameInfo.p1Start) {
				p1Start = node;
			} else if (i == PursuitGameInfo.p2Start) {
				p2Start = node;
			}
		}
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (nodeMatrix[i][j] == 1)
					new Edge("E-" + i + "-" + j, allNodes.get("ID" + i), allNodes.get("ID" + j), graph);
			}
		}
	}

	public DefaultDirectedGraph<Node, Edge> getGraph() {
		return graph;
	}
	
	public Set<Edge> getEdgesOf(Node node) {
		return graph.edgesOf(node);
	}

	public Map<String, Node> getAllNodes() {
		return allNodes;
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

	public DataLoader getDl() {
		return dl;
	}
}
