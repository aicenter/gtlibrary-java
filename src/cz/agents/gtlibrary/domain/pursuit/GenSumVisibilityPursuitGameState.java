package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.interfaces.GameState;

/**
 * Created by Jakub Cerny on 16/01/2018.
 */
public class GenSumVisibilityPursuitGameState extends VisibilityPursuitGameState {

    public GenSumVisibilityPursuitGameState(){}

    public GenSumVisibilityPursuitGameState(PursuitGameState state){ super(state);}

    @Override
    public GameState copy() {
        return new GenSumVisibilityPursuitGameState(this);
    }

    @Override
    protected double[] getEndGameUtilities() {
        if (isCaughtInNode() || isCaughtOnEdge())
            return new double[]{scaleUtility(-1), scaleUtility(2 - getSequenceFor(PursuitGameInfo.PATROLLER).size()*PursuitGameInfo.patrollerMoveCost)};
        return new double[]{scaleUtility(1), 0};
//            return new double[]{-10 + this.round, 20 - this.round*2};
//        return new double[]{5 + this.round, -20};

    }

    protected double scaleUtility(double utility){
        if(!PursuitGameInfo.SCALE_UTILITIES) return utility;
        double scaledUtility = Math.round(((int) Math.pow(10, PursuitGameInfo.ROUNDING)) * utility * PursuitGameInfo.SCALING_FACTOR)
                / Math.pow(10, PursuitGameInfo.ROUNDING);
//        if (scaledUtility > 11 || utility > 1.6) System.out.println(utility + " / " + scaledUtility);
        return scaledUtility;
    }



}
