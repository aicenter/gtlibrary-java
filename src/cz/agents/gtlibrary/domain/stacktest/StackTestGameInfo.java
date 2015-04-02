package cz.agents.gtlibrary.domain.stacktest;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class StackTestGameInfo implements GameInfo{

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);

    public static final Player[] ALL_PLAYERS = new Player[] {FIRST_PLAYER, SECOND_PLAYER};

    @Override
    public double getMaxUtility() {
        return 6;
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
        return  null;
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
