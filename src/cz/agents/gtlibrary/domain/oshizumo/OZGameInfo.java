package cz.agents.gtlibrary.domain.oshizumo;

import java.util.Arrays;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class OZGameInfo implements GameInfo {

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);

    public static final Player[] ALL_PLAYERS = {FIRST_PLAYER, SECOND_PLAYER};
    public static long seed = 1;

    // parameters taken from http://skatgame.net/mburo/ps/sumo.pdf
    public static int startingCoins = 10;        // N from paper
    public static int locK = 3;                 // locations (2K+1)
    public static int minBid = 1;               // B from paper


    public OZGameInfo() {
    }

    @Override
    public double getMaxUtility() {
        return 1;
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
        return "OshiZumo, starting coins: " + startingCoins + ", locK: " + locK + ", minBid: " + minBid;
    }

    @Override
    public int getMaxDepth() {
        if (minBid == 0)
            return startingCoins * 2;
        else
            return startingCoins;
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
