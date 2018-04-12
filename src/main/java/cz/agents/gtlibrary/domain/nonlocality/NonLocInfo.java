package cz.agents.gtlibrary.domain.nonlocality;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class NonLocInfo implements GameInfo{

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);
    public static final Player NATURE = new PlayerImpl(2);

    public static final Player[] ALL_PLAYERS = new Player[] {FIRST_PLAYER, SECOND_PLAYER, NATURE};

    @Override
    public double getMaxUtility() {
        return 3;
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
        return  null;
    }

    @Override
    public int getMaxDepth() {
        return 3;
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
