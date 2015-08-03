package cz.agents.gtlibrary.domain.clairvoyance;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class ClairvoyanceInfo implements GameInfo {

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);
    public static final Player NATURE = new PlayerImpl(2);

    public static final Player[] ALL_PLAYERS = {FIRST_PLAYER, SECOND_PLAYER, NATURE};

    public static int betCount = 5;

    @Override
    public double getMaxUtility() {
        return betCount + 1;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return NATURE;
    }

    @Override
    public Player getOpponent(Player player) {
        return FIRST_PLAYER.equals(FIRST_PLAYER) ? SECOND_PLAYER : FIRST_PLAYER;
    }

    @Override
    public String getInfo() {
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

    @Override
    public double getUtilityStabilizer() {
        return 1;
    }
}
