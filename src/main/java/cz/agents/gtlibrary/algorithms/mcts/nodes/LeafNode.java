package cz.agents.gtlibrary.algorithms.mcts.nodes;

public interface LeafNode extends Node {
    double[] getUtilities();
}
