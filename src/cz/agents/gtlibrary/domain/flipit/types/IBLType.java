package cz.agents.gtlibrary.domain.flipit.types;

import cz.agents.gtlibrary.domain.flipit.NodePointsFlipItGameState;
import cz.agents.gtlibrary.utils.graph.Node;

/**
 * Created by Jakub Cerny on 22/06/2017.
 */
public class IBLType implements FollowerType {

    double d;
    double sigma;
    double prior;
    int ID;

    public IBLType(double d, double sigma, double prior, int id){
        this.d = d;
        this.sigma = sigma;
        this.prior = prior;
        this.ID = id;
    }

    @Override
    public double getPrior() {
        return prior;
    }

    @Override
    public double getReward(NodePointsFlipItGameState gameState, Node node) {
        return 0;
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public String toString() {
        return "IBL{" +
                "d=" + d +
                ", sigma=" + sigma +
                ", prior=" + prior +
                '}';
    }
}
