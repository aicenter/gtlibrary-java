package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jakub on 13/03/17.
 */
public class FlipItGraph extends Graph {

    private HashSet<Node> publicNodes;
    private HashMap<Node,Double> rewards;
    private HashMap<Node,Double> controlCosts;

    private double UNIFORM_REWARD = 4.0;
    private double UNIFORM_COST = 1.5;

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
    }

    public double getMaxReward(){
        return MAX_REWARD;
    }

    public double getMinControlCost(){
        return MIN_CONTROLCOST;
    }

    private void initFlipItGraph(){
        for (Node node : getAllNodes().values()){
            rewards.put(node, UNIFORM_REWARD);
            controlCosts.put(node, UNIFORM_COST);
            for (Edge edge : getEdgesOf(node)){
                if (edge.getTarget().equals(node))
                    break;
                publicNodes.add(node);
            }
        }
//        System.out.println("Public nodes size : " + publicNodes.size());
    }

    public HashSet<Node> getPublicNodes(){
        return publicNodes;
    }

    public double getReward(Node node){
//        System.out.println(node);
        return rewards.get(node); }

    public double getControlCost(Node node){ return controlCosts.get(node); }

}
