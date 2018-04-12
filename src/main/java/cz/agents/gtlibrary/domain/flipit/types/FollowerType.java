package cz.agents.gtlibrary.domain.flipit.types;

import cz.agents.gtlibrary.domain.flipit.NodePointsFlipItGameState;
import cz.agents.gtlibrary.utils.graph.Node;

/**
 * Created by Jakub on 14/03/17.
 */
public interface FollowerType {

    public double getPrior();

    public double getReward(NodePointsFlipItGameState gameState, Node node);

    public int getID();

}
