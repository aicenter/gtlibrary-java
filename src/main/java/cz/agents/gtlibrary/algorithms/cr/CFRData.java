package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;

import java.util.Map;

public class CFRData {
    public java.util.Map<InnerNode, Double> reachProbs;
    public Map<InnerNode, Double> historyExpValues;

    public CFRData(Map<InnerNode, Double> reachProbs,
                   Map<InnerNode, Double> historyExpValues) {
        this.reachProbs = reachProbs;
        this.historyExpValues = historyExpValues;
    }
}
