package cz.agents.gtlibrary.domain.pursuit;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class PursuitGameInfo implements GameInfo {

	public static String graphFile = "grid_4.txt";//"pursuit_goal_optimized.txt";
//public static String graphFile = "pursuit_goal_optimized.txt";
	public static Player EVADER = new PlayerImpl(0);
	public static Player PATROLLER = new PlayerImpl(1);
	public static Player[] ALL_PLAYERS = { EVADER, PATROLLER };
	public static long seed = 7;
//	public static int evaderStart = 23;//0;
	public static int evaderStart = 5;
//	public static int p1Start = 34;//7;
	public static int p1Start = 7;
//	public static int p2Start = 46;//13;
	public static int p2Start = 13;
//	public static int evaderGoal = 40;//10;//-1 for no goal
	public static int evaderGoal = 10;//-1 for no goal
	public static int depth = 3;
    public static double patrollerMoveCost = 0.5/depth;//0 for zero sum game
	public static boolean forceMoves = true;
    public static boolean randomizeStartPositions = false;
	public static boolean fixSecondPursuer = false;

	public static int visibility = 1;

	public static final boolean SCALE_UTILITIES = true;
	public static final double SCALING_FACTOR = 10;
	public static final int ROUNDING = 2;

	public static final Integer SAME  = 0;
	public static final Integer UP    = 1;
	public static final Integer DOWN  = 2;
	public static final Integer LEFT  = 3;
	public static final Integer RIGHT = 4;
	public static final Integer LR 	  = 5;
	public static final Integer LU 	  = 6;
	public static final Integer LD 	  = 7;
	public static final Integer RU 	  = 8;
	public static final Integer RD 	  = 9;
	public static final Integer UD 	  = 10;

	public static void initValue(boolean randomizeStartPositions, long seed, int depth){
		PursuitGameInfo.randomizeStartPositions = randomizeStartPositions;
		PursuitGameInfo.seed = seed;
		PursuitGameInfo.depth = depth;
		patrollerMoveCost = 0.5/depth;
	}

	public static void initValue(boolean randomizeStartPositions, long seed, int depth, int gridSize){
		PursuitGameInfo.randomizeStartPositions = randomizeStartPositions;
		PursuitGameInfo.seed = seed;
		PursuitGameInfo.depth = depth;
		PursuitGameInfo.graphFile = "grid_"+gridSize+".txt";
		PursuitGameInfo.patrollerMoveCost = 0.5/depth;
	}

	public static void initValue(boolean randomizeStartPositions, long seed, int depth, String gridSize, boolean fixSecondPursuer){
		PursuitGameInfo.randomizeStartPositions = randomizeStartPositions;
		PursuitGameInfo.seed = seed;
		PursuitGameInfo.depth = depth;
		PursuitGameInfo.graphFile = "grid_"+gridSize+".txt";
		PursuitGameInfo.patrollerMoveCost = 0.5/depth;
		PursuitGameInfo.fixSecondPursuer = fixSecondPursuer;
	}


    @Override
	public double getMaxUtility() {
		return SCALE_UTILITIES ? 2*SCALING_FACTOR : 2;
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
		if(!randomizeStartPositions) {
			return "Pursuit, Evader start: " + evaderStart + ", P1 start: " + p1Start + ", P2 start: " + p2Start + ", depth: " + depth + ", graph: " + graphFile;
		}
		else{
			return "Pursuit, positions randomized, depth: " + depth + ", graph: " + graphFile;
		}
	}

	@Override
	public int getMaxDepth() {
		return depth;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
