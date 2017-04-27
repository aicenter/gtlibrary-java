package cz.agents.gtlibrary.domain.flipit.types;

import cz.agents.gtlibrary.domain.flipit.FlipItGameState;
import cz.agents.gtlibrary.utils.graph.Node;

/**
 * Created by Jakub on 14/03/17.
 */
public interface FollowerType {

    public double getPrior();

    public double getReward(FlipItGameState gameState, Node node);

    public int getID();

}
