package cz.agents.gtlibrary.domain.bpg.data;

import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GenSumBorderPatrollingGraph extends BorderPatrollingGraph {

    private Map<Node, Double> utilityChange;

    public GenSumBorderPatrollingGraph(String graphFile) {
        super(graphFile);
        Random random = new Random(1);

        utilityChange = new HashMap<>();
        for (Edge edge : graph.edgesOf(destination)) {
            utilityChange.put(edge.getSource(), random.nextDouble());
        }
    }

    public double getEvaderUtilityChange(Node node) {
        return utilityChange.get(node);
    }
}
