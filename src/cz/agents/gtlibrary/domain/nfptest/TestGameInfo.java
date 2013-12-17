package cz.agents.gtlibrary.domain.nfptest;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class TestGameInfo implements GameInfo {

	public static Player FIRST_PLAYER = new PlayerImpl(0);
	public static Player SECOND_PLAYER = new PlayerImpl(1);
	public static Player[] ALL_PLAYERS = new Player[] { FIRST_PLAYER, SECOND_PLAYER };

	@Override
	public double getMaxUtility() {
		return 2;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxDepth() {
		return 3;
	}

	@Override
	public Player[] getAllPlayers() {
		return ALL_PLAYERS;
	}

}
