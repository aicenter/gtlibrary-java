package cz.agents.gtlibrary.domain.tron;

import java.util.Arrays;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

public class TronGameInfo implements GameInfo {

    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);

    public static final Player[] ALL_PLAYERS = {FIRST_PLAYER, SECOND_PLAYER};
    public static long seed = 1;

    public static char BOARDTYPE = 'A';    // which board type? (only one for now, empty board)
    public static int ROWS = 16;           // locations (2K+1)
    public static int COLS = 16;           // B from paper


    public TronGameInfo() {
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
        return "Tron, board " + BOARDTYPE + ", rows " + ROWS + ", cols " + COLS;
    }

    @Override
    public int getMaxDepth() {
      return ((ROWS*COLS)/2+1);
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
