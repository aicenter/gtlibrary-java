package cz.agents.gtlibrary.domain.upordown;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class UDGameInfo implements GameInfo {

	public static Player FIRST = new PlayerImpl(0);
	public static Player SECOND = new PlayerImpl(1);
	public static Player[] ALL_PLAYERS = new Player[] { FIRST, SECOND };

	@Override
	public double getMaxUtility() {
		return 2;
	}

	@Override
	public Player getFirstPlayerToMove() {
		return FIRST;
	}

	@Override
	public Player getOpponent(Player player) {
		return player.equals(FIRST)?SECOND:FIRST;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxDepth() {
		return 2;
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
