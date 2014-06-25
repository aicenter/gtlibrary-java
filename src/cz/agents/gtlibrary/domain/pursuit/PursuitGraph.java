package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.DistanceNode;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.PriorityQueue;

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
        if (PursuitGameInfo.randomizeStartPositions) {
            int nodes = getAllNodes().size();
            int tmp = (nodes)*(nodes-1)*(nodes-2);
            HighQualityRandom rnd = new HighQualityRandom(PursuitGameInfo.seed);
            int choice = rnd.nextInt(tmp);
            int en = choice / ((nodes-1)*(nodes-2));
            choice = choice % ((nodes-1)*(nodes-2));
            int p1n = choice / (nodes - 2);
            if (p1n == en) p1n++;
            int p2n = choice % (nodes-2);
            while (p2n == p1n || p2n == en)
                p2n++;
            evaderStart = allNodes.get("ID" + en);
            p1Start = allNodes.get("ID" + p1n);
            p2Start = allNodes.get("ID" + p2n);
            PursuitGameInfo.evaderStart = en;
            PursuitGameInfo.p1Start = p1n;
            PursuitGameInfo.p2Start = p2n;
        } else {
            evaderStart = allNodes.get("ID" + PursuitGameInfo.evaderStart);
            p1Start = allNodes.get("ID" + PursuitGameInfo.p1Start);
            p2Start = allNodes.get("ID" + PursuitGameInfo.p2Start);
        }
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

    public double getDistance(Node start, Node goal) {
        PriorityQueue<cz.agents.gtlibrary.utils.graph.DistanceNode> queue = new PriorityQueue<>();

        queue.add(new DistanceNode(start, 0));
        while(!queue.isEmpty()) {
            DistanceNode current = queue.poll();

            if(current.getNode().equals(goal))
                return current.getDistance();
            for (Edge edge : graph.edgesOf(current.getNode())) {
                if(edge.getSource().equals(current.getNode()))
                    queue.add(new DistanceNode(edge.getTarget(), current.getDistance() + 1));
            }
        }
        return Double.POSITIVE_INFINITY;
    }
}
