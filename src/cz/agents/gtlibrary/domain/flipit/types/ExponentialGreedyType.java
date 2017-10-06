package cz.agents.gtlibrary.domain.flipit.types;

import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.NodePointsFlipItGameState;
import cz.agents.gtlibrary.utils.graph.Node;

/**
 * Created by Jakub on 14/03/17.
 */
public class ExponentialGreedyType implements FollowerType {

    private double delta;
    private double prior;
    private int id;

    public ExponentialGreedyType(double prior, double delta, int id){
        this.prior = prior;
        this.delta = delta;
        this.id = id;
    }

    @Override
    public double getPrior() {
        return prior;
    }

    @Override
    public double getReward(NodePointsFlipItGameState gameState, Node node) {
        return Math.pow(delta,gameState.getDepth()) * FlipItGameInfo.graph.getReward(node);
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return "ExpGreedy{" +
                "delta=" + delta +
                ", prior=" + prior +
                '}';
    }
}
