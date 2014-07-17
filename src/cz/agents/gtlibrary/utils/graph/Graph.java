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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultDirectedGraph;


public class Graph implements Serializable {

    private static final long serialVersionUID = -578541448878434086L;

    public static void main(String[] args) throws FileNotFoundException {
        Graph.makeGrid(10, 10, new PrintStream(new FileOutputStream("pursuit_simple10x10.txt")));
    }

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
        
        public static void makeGrid(int w, int h, PrintStream output){
            output.println(w*h);
            for (int n=0; n<w*h; n++){
                int nX = n % w;
                int nY = n / h;
                for (int y=0; y<h; y++){
                    for (int x=0; x<w; x++){
                        output.print((Math.abs(nX-x)==1 && nY==y || Math.abs(nY-y)==1 && nX==x) ? "1 " : "0 ");
                    }
                }
                output.println();
            }
        }
}
