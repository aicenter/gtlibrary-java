package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.HashSet;
import java.util.Set;

public class MCTSInformationSet extends InformationSetImpl {

    private Set<InnerNode> allNodes;
    private AlgorithmData algorithmData;

    public MCTSInformationSet(GameState state) {
        super(state);
        allNodes = new HashSet<InnerNode>();
    }

    public void addNode(InnerNode node) {
        allNodes.add(node);
    }

    public Set<InnerNode> getAllNodes() {
        return allNodes;
    }

    public AlgorithmData getAlgorithmData() {
        return algorithmData;
    }

    public void setAlgorithmData(AlgorithmData algorithmData) {
        this.algorithmData = algorithmData;
    }

}
