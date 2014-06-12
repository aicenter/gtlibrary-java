package cz.agents.gtlibrary.iinodes;

import cz.agents.gtlibrary.interfaces.Player;

public abstract class SimultaneousGameState extends GameStateImpl {

    protected int depth;

    public SimultaneousGameState(Player[] players) {
        super(players);
        depth = Integer.MAX_VALUE;
    }

    public SimultaneousGameState(SimultaneousGameState gameState) {
        super(gameState);
        this.depth = gameState.depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public boolean isGameEnd() {
        return isDepthLimit() || isActualGameEnd();
    }

    public double[] getUtilities() {
        if (isActualGameEnd())
            return getEndGameUtilities();
        return evaluate();
    }

    protected abstract double[] getEndGameUtilities();

    protected abstract boolean isActualGameEnd();

    protected abstract boolean isDepthLimit();
}
