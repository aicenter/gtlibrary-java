package cz.agents.gtlibrary.utils.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;


public class Graph implements Serializable {
	
	private static final long serialVersionUID = -578541448878434086L;
	protected DefaultDirectedGraph<Node, Edge> graph = new DefaultDirectedGraph<Node, Edge>(Edge.class);
	protected Map<String, Node> allNodes = new HashMap<String, Node>();

	final protected DataLoader dl;

	public Graph(String graphFile) {
		dl = new DataLoader(graphFile);
		init();
	}

	protected void init() {
		double[][] nodeMatrix = dl.getOriginalGraphIncMatrix();
		int N = dl.getNodesInOriginalGraph();
		for (int i = 0; i < N; i++) {
			Node node = new Node("ID" + i, graph);
			
			allNodes.put(node.getId(), node);
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
	
	public DataLoader getDataLoader() {
		return dl;
	}
        
        public static void makeGrid(int w, int h){
            for (int n=0; n<w*h; n++){
                int nX = n % w;
                int nY = n / h;
                for (int y=0; y<h; y++){
                    for (int x=0; x<w; x++){
                        System.out.print((Math.abs(nX-x)==1 && nY==y || Math.abs(nY-y)==1 && nX==x) ? "1 " : "0 ");
                    }
                }
                System.out.println();
            }
        }
}
