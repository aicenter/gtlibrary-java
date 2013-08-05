package cz.agents.gtlibrary.domain.bpg;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class BPGGameInfo implements GameInfo {

	public static Player ATTACKER = new PlayerImpl(0);
	public static Player DEFENDER = new PlayerImpl(1);

	public static Player[] ALL_PLAYERS = { ATTACKER, DEFENDER };

	public static int DEPTH = 4;
	public static boolean SLOW_MOVES = false;
	public static String graphFile = "GridW3-almost-connected.txt";//"GridW4.txt";

	@Override
	public double getMaxUtility() {
		return 1;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return ATTACKER;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(ATTACKER) ? DEFENDER : ATTACKER;
	}

	@Override
	public String getInfo() {
		return "Border patrolling game \nSlow moves:" + SLOW_MOVES + "\tDepth:" + DEPTH + "\tGraph:" + graphFile;
	}

	@Override
	public int getMaxDepth() {
		return DEPTH;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

}
