package cz.agents.gtlibrary.domain.poker.kuhn;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class KPGameInfo implements GameInfo {
	public static final Player FIRST_PLAYER = new PlayerImpl(0);
	public static final Player SECOND_PLAYER = new PlayerImpl(1);
	public static final Player NATURE = new PlayerImpl(2);

	public static final Player[] ALL_PLAYERS = new Player[] {FIRST_PLAYER, SECOND_PLAYER, NATURE};
	
	public static int ANTE = 1;
	public static int BET = 1;

	@Override
	public double getMaxUtility() {
		return ANTE + BET;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return NATURE;
	}

	@Override
	public Player getOpponent(Player player) {
		if (player.equals(FIRST_PLAYER))
			return SECOND_PLAYER;
		return FIRST_PLAYER;
	}

	@Override
	public String getInfo() {
		return "Kuhn poker, ante: " + ANTE + ", bet: " + BET;
	}

	@Override
	public int getMaxDepth() {
		return 5;
	}
	
	@Override
	public Player[] getAllPlayers() {		
		return ALL_PLAYERS;
	}
}
