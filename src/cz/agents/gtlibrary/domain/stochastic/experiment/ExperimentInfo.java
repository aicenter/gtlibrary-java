package cz.agents.gtlibrary.domain.stochastic.experiment;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class ExperimentInfo implements GameInfo {

	public static double UTILITY = 1;
	public static Player PATROLLER = new PlayerImpl(0);
	public static Player ATTACKER = new PlayerImpl(1);
	public static Player NATURE = new PlayerImpl(2);
	public static Player[] ALL_PLAYERS = new Player[] { PATROLLER, ATTACKER, NATURE };
	public static String patrollerStartId = "0";
	public static String graphFile = "exp_small.txt";
	public static double epsilon = 0.05;
	public static int commitmentDepth = 2;

	@Override
	public double getMaxUtility() {
		return UTILITY;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return PATROLLER;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(ATTACKER) ? PATROLLER : ATTACKER;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}  

	@Override
	public int getMaxDepth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

}
