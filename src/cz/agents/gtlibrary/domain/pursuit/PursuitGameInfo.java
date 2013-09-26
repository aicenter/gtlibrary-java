package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class PursuitGameInfo implements GameInfo {

	public static String graphFile = "pursuit_simple4x4.txt";
	public static Player EVADER = new PlayerImpl(0);
	public static Player PATROLLER = new PlayerImpl(1);
	public static Player[] ALL_PLAYERS = { EVADER, PATROLLER };
	public static long seed = 1;
	public static int evaderStart = 0;
	public static int p1Start = 7;
	public static int p2Start = 11;
	public static int depth = 4;
	public static boolean forceMoves = true;

	@Override
	public double getMaxUtility() {
		return 1;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return EVADER;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(EVADER) ? PATROLLER : EVADER;
	}

	@Override
	public String getInfo() {
		return "Pursuit, Evader start: " + evaderStart + ", P1 start: " + p1Start + ", P2 start: " + p2Start + ", depth: " + depth + ", graph: " + graphFile;
	}

	@Override
	public int getMaxDepth() {
		return depth;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

}