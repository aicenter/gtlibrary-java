package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.interfaces.GameState;

public class GenSumPursuitGameState extends PursuitGameState {

    public GenSumPursuitGameState() {
        super();
    }

    public GenSumPursuitGameState(GenSumPursuitGameState gameState) {
        super(gameState);
    }

    @Override
    protected double[] getEndGameUtilities() {
        if (isCaughtInNode() || isCaughtOnEdge())
            return new double[]{-1, 2 - getSequenceFor(PursuitGameInfo.PATROLLER).size()*PursuitGameInfo.patrollerMoveCost};
        return new double[]{1, 0};
    }

    @Override
    public GameState copy() {
        return new GenSumPursuitGameState(this);
    }
}
