package cz.agents.gtlibrary.domain.simrandomgame;

import java.util.Arrays;
import java.util.Random;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class SimRandomGameInfo implements GameInfo {

	public static final Player FIRST_PLAYER = new PlayerImpl(0);
	public static final Player SECOND_PLAYER = new PlayerImpl(1);

	public static final Player[] ALL_PLAYERS = new Player[] { FIRST_PLAYER, SECOND_PLAYER };

	public static long seed = 1;
	public static final int[] MAX_BF = { 4, 4 };
	public static final int MAX_DEPTH = 4;
	public static int MAX_UTILITY = 1;
	public static boolean BINARY_UTILITY = false;
	public static boolean UTILITY_CORRELATION = true;

	public static Random rnd = new Random(seed);

        public SimRandomGameInfo() {
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
		return player.equals(FIRST_PLAYER) ? SECOND_PLAYER : FIRST_PLAYER;
	}

	@Override
	public String getInfo() {
		return "Simultaneous Random game:\nMAX_UTILITY:" + MAX_UTILITY + ", MAX_BF:" + Arrays.toString(MAX_BF) + ", MAX_DEPTH:" + MAX_DEPTH + ", BIN_UTIL:" + BINARY_UTILITY + ", UTIL_CORR:" + UTILITY_CORRELATION;
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
