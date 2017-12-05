package cz.agents.gtlibrary.domain.testGame;

import cz.agents.gtlibrary.domain.testGame.gameDefs.*;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * Created by Jakub Cerny on 20/10/2017.
 */
public class TestGameInfo implements GameInfo {

    public static Player PL0 = new PlayerImpl(0);
    public static Player PL1 = new PlayerImpl(1);
    public static Player[] ALL_PLAYERS = new Player[]{PL0, PL1};
    protected static GameDefinition definition = new GambitGame("flipit_inversed_0.gbt");

    protected final static boolean USE_TL = false;

    // State -> States
    public static final HashMap<Integer, ArrayList<Integer>> successors = definition.getSuccessors();

    // State -> IS
    public static final HashMap<Integer, Integer> iss = definition.getISs();

    // IS -> Player
    public static final HashMap<Integer, Integer> players = definition.getPlayersForISs();

    // State -> utility
    public static final HashMap<Integer, Double[]> utilities = definition.getUtilities();




    @Override
    public double getMaxUtility() {
        Double[] d = new Double[0];
        for (Double[] u : utilities.values()) d = ArrayUtils.addAll(d, u);
        Arrays.sort(d);
        return d[d.length-1];
    }

    @Override
    public Player getFirstPlayerToMove() {
        return players.get(iss.get(1)).equals(0) ? PL0 : PL1;
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(PL0) ? PL1 : PL0;
    }

    @Override
    public String getInfo() {
        return "Test game with predefined game tree: " + definition.getClass().getSimpleName();
    }

    @Override
    public int getMaxDepth() {
        return successors.size();
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }

    @Override
    public double getUtilityStabilizer() {
        return 1.0;
    }
}
