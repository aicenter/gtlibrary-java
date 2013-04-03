package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Random;


public class RandomGameInfo implements GameInfo {
    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);

    public static final Player[] ALL_PLAYERS = new Player[] {FIRST_PLAYER, SECOND_PLAYER};

    public static int MAX_DEPTH = 5;
    public static int MAX_BF = 2;
    public static int MAX_OBSERVATION = 3;
    public static int MAX_UTILITY = 1;
    public static boolean BINARY_UTILITY = false;
    public static boolean UTILITY_CORRELATION = true;

    public static long seed = 7;

    public static Random rnd = new Random(seed);

    public RandomGameInfo() {
        rnd = new Random(seed);
        if (UTILITY_CORRELATION) {
            if (BINARY_UTILITY)
                MAX_UTILITY = 1;
            else
                MAX_UTILITY = 2*MAX_DEPTH;
        }

    }

    @Override
    public double getMaxUtility() {
        return MAX_UTILITY;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return FIRST_PLAYER;
    }

    @Override
    public Player getOpponent(Player player) {
        if (player.equals(FIRST_PLAYER))
            return SECOND_PLAYER;
        return FIRST_PLAYER;
    }

    @Override
    public String getInfo() {
        return "Random game:\nMAX_UTILITY:" + MAX_UTILITY + ", MAX_BF:" + MAX_BF + ", MAX_OBSERVATIONS:" + MAX_OBSERVATION + ", MAX_DEPTH:" + MAX_DEPTH + ", BIN_UTIL:" + BINARY_UTILITY + ", UTIL_CORR:" + UTILITY_CORRELATION;
    }

    @Override
    public int getMaxDepth() {
        return MAX_DEPTH;
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }


}
