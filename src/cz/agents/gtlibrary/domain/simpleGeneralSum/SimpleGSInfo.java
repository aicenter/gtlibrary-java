package cz.agents.gtlibrary.domain.simpleGeneralSum;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/5/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGSInfo implements GameInfo {

    final protected static int MAX_UTILITY = 10;
    final protected static int MAX_DEPTH = 1;
    final protected static int MAX_ACTIONS = 2;

    public static Player PL0 = new PlayerImpl(0);
    public static Player PL1 = new PlayerImpl(1);

    public static double[][] utilityMatrix = {{1,1},{1,2},{2,1},{0,0}};

    @Override
    public double getMaxUtility() {
        return MAX_UTILITY;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return PL0;
    }

    @Override
    public Player getOpponent(Player player) {
        if (player.equals(PL0)) return PL1;
        else return PL0;
    }

    @Override
    public String getInfo() {
        return "Simple General Sum";
    }

    @Override
    public int getMaxDepth() {
        return MAX_DEPTH;
    }

    @Override
    public Player[] getAllPlayers() {
        return new Player[] {PL0, PL1};
    }

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
