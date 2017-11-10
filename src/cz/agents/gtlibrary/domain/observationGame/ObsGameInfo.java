package cz.agents.gtlibrary.domain.observationGame;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by bbosansky on 11/3/17.
 */
public class ObsGameInfo implements GameInfo {


    public static Player LEADER = new PlayerImpl(0);
    public static Player FOLLOWER = new PlayerImpl(1);
    public static Player[] ALL_PLAYERS = { LEADER, FOLLOWER };
    public static long seed = 8;

    public static int width = 5;
    public static int attackAfterTimeStep = 7;
    public static int maxTimeSteps = 10;

    public static int[] targetValues;
    public static int MAXUTIL = 10;

    public ObsGameInfo() {
        targetValues = new int[width];
        Random rnd = new Random(seed);
        for (int i=0;i<width; i++)
            targetValues[i] = rnd.nextInt(MAXUTIL)+1;
    }

    @Override
    public double getMaxUtility() {
        return MAXUTIL;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return LEADER;
    }

    @Override
    public Player getOpponent(Player player) {
        return player.equals(LEADER) ? FOLLOWER : LEADER;
    }

    @Override
    public String getInfo() {
        return "Observation Game; Width: " + width + " maxTimeSteps: " + maxTimeSteps + " seed: " + seed + " target values: {" + Arrays.toString(targetValues) + "}";
    }

    @Override
    public int getMaxDepth() {
        return maxTimeSteps;
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
