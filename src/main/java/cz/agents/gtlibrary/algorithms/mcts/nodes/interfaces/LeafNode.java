package cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces;

public interface LeafNode extends Node {
    double[] getUtilities();
}
