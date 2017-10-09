package cz.agents.gtlibrary.domain.metroTransport;

import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 05/10/2017.
 */
public class MetroGraph extends Graph{

    private int[] rewards;
    private int[] init_rewards = new int[]{5, 2, 3};

    public MetroGraph(String graphFile) {
        super(graphFile);
        rewards = new int[allNodes.values().size()];
        for (Node node : allNodes.values()){
            rewards[node.getIntID()] = init_rewards[node.getIntID()];
        }
    }

    public int getRewardSum(){
        int sum = 0;
        for (int val : rewards) sum += val;
        return sum;
    }

    public int getDistance(Node origin, Node destination){
        // shortest path
        return 0;
    }


}
