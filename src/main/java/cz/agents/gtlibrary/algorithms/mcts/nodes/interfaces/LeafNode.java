package cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

public interface LeafNode extends Node {
    double[] getUtilities();

    default boolean isGameEnd() {
        return true;
    }

    @Override
    default double getBaselineFor(Action a, Player pl) {
        return getUtilities()[pl.getId()];
    }
}
