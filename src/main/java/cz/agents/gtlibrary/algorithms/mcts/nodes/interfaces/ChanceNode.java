package cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces;

import cz.agents.gtlibrary.interfaces.Action;

public interface ChanceNode extends InnerNode {
    Action getRandomAction();
}
