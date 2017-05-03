package cz.agents.gtlibrary.algorithms.crswfabstraction.testdomain;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class TestInfo implements GameInfo {

    protected static final Player NATURE = new PlayerImpl(2);
    protected static final Player PL1 = new PlayerImpl(0);
    protected static final Player PL2 = new PlayerImpl(1);


    @Override
    public double getMaxUtility() {
        return 0;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return null;
    }

    @Override
    public Player getOpponent(Player player) {
        return null;
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public int getMaxDepth() {
        return 0;
    }

    @Override
    public Player[] getAllPlayers() {
        return new Player[]{PL1, PL2, NATURE};
    }

    @Override
    public double getUtilityStabilizer() {
        return 0;
    }
}
