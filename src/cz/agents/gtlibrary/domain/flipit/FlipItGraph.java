package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Jakub on 13/03/17.
 */
public class FlipItGraph extends Graph {

    private HashSet<Node> publicNodes;
    private HashMap<Node,Double> rewards;
    private HashMap<Node,Double> controlCosts;

    private Map<Node, Double> sortedNodes;

    private double UNIFORM_REWARD = 4.0;
    private double UNIFORM_COST = 1.5;

    private final boolean USE_UNIFORM_REWARD = false;
    private final boolean USE_UNIFORM_COST = false;

    private double[] init_costs = new double[]  {2.0, 4.0, 4.0, 6.0, 7.0};
    private double[] init_rewards = new double[]{3.0, 4.0, 6.0, 7.0, 4.0};

    private double MAX_REWARD;
    private double MIN_CONTROLCOST;

    public FlipItGraph(String graphFile) {
        super(graphFile);
        rewards = new HashMap<>();
        controlCosts = new HashMap<>();
        publicNodes = new HashSet<>();
        initFlipItGraph();

        MAX_REWARD = Double.MIN_VALUE;
        for (Double reward : rewards.values())
            if (reward > MAX_REWARD) MAX_REWARD = reward;

        MIN_CONTROLCOST = Double.MAX_VALUE;
        for (Double controlCost : controlCosts.values())
            if (controlCost < MIN_CONTROLCOST) MIN_CONTROLCOST = controlCost;

//        System.out.println("GRAPH INIT");
    }

    public double getMaxReward(){
        return MAX_REWARD;
    }

    public double getMinControlCost(){
        return MIN_CONTROLCOST;
    }

    private void initFlipItGraph(){
        Map<Node, Double> nodes = new HashMap<Node,Double>();
        for (Node node : getAllNodes().values()){
            if (USE_UNIFORM_REWARD) rewards.put(node, UNIFORM_REWARD);
            else rewards.put(node, init_rewards[node.getIntID()]);
            if (USE_UNIFORM_COST ) controlCosts.put(node, UNIFORM_COST);
            else controlCosts.put(node, init_costs[node.getIntID()]);
            if(getEdgesOf(node).isEmpty()) publicNodes.add(node);
            for (Edge edge : getEdgesOf(node)){
                if (edge.getTarget().equals(node))
                    break;
                publicNodes.add(node);
            }
            nodes.put(node, getControlCost(node)/getReward(node));
        }
        sortedNodes = sortByValue(nodes);
//        System.out.println("Public nodes size : " + publicNodes.size());
    }

    public Map<Node,Double> getSortedNodes(){
        return sortedNodes;
    }

    public HashSet<Node> getPublicNodes(){
        return publicNodes;
    }

    public double getReward(Node node){
//        System.out.println(node);
        return rewards.get(node); }

    public double getControlCost(Node node){ return controlCosts.get(node); }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(/*Collections.reverseOrder()*/))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
