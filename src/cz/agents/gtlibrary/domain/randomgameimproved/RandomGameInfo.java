package cz.agents.gtlibrary.domain.randomgameimproved;

import cz.agents.gtlibrary.domain.randomgameimproved.centers.ModificationGenerator;
import cz.agents.gtlibrary.domain.randomgameimproved.centers.UniformModificationGenerator;
import cz.agents.gtlibrary.domain.randomgameimproved.observationvariants.ObservationsType;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.Random;


public class RandomGameInfo implements GameInfo {
    public static final Player FIRST_PLAYER = new PlayerImpl(0);
    public static final Player SECOND_PLAYER = new PlayerImpl(1);
    public static final Player NATURE = new PlayerImpl(2, "Nature");

    /**
     * Nature should always be last
     **/
    public static final Player[] ALL_PLAYERS = new Player[]{FIRST_PLAYER, SECOND_PLAYER, NATURE};

    public static double CORRELATION = -0.2;// -1 for zero sum, 1 for identical utilities
    public static int MAX_DEPTH = 5;
    public static int MAX_BF = 2;
    public static int MIN_BF = 2;
    public static int MAX_OBSERVATION = 3;
    public static int MAX_UTILITY = 100;
    public static boolean INTEGER_UTILITY = false;
    public static boolean UTILITY_CORRELATION = true;
    public static boolean MULTIPLE_PLAYER_DEPTHS = false;
    public static int MAX_CENTER_MODIFICATION = 3;
    public static boolean FIXED_SIZE_BF = false;
    public static double NATURE_STATE_PROBABILITY = 0;
    public static int[] ACTIONS;

    public static boolean IMPERFECT_RECALL = false;
    public static boolean IMPERFECT_RECALL_ONLYFORP1 = false;
    public static boolean ABSENT_MINDEDNESS = false;
    public static double EMPTY_OBSERVATION_PROBABILITY = 0.5;
    public static ObservationsType OBSERVATIONS_TYPE = ObservationsType.FORGETFUL;
    public static double FORGET_OBSERVATION_PROBABILITY = 0.5;

    public static long seed = 1;

    public static Random rnd = new HighQualityRandom(seed);
    public static ModificationGenerator modificationGenerator = new UniformModificationGenerator(MAX_CENTER_MODIFICATION);

    public RandomGameInfo() {
        rnd = new HighQualityRandom(seed);
        if (UTILITY_CORRELATION) {
            MAX_UTILITY = 2 * MAX_CENTER_MODIFICATION * MAX_DEPTH;
        }
        ACTIONS = new int[MAX_BF - 1];
        for (int i = 0; i < MAX_BF - 1; i++) {
            ACTIONS[i] = i;
        }
    }

    public RandomGameInfo(int depth, int bf, int seed) {
        this.MAX_BF = bf;
        this.MAX_DEPTH = depth;
        this.seed = seed;
        rnd = new HighQualityRandom(seed);
        if (UTILITY_CORRELATION) {
            MAX_UTILITY = 2 * MAX_CENTER_MODIFICATION * MAX_DEPTH;
        }
        ACTIONS = new int[MAX_BF - 1];
        for (int i = 0; i < MAX_BF - 1; i++) {
            ACTIONS[i] = i;
        }
    }

    public RandomGameInfo(int depth, int bf, int seed, double correlation) {
        this.CORRELATION = correlation;
        this.MAX_BF = bf;
        this.MAX_DEPTH = depth;
        this.seed = seed;
        rnd = new HighQualityRandom(seed);
        if (UTILITY_CORRELATION) {
            MAX_UTILITY = 2 * MAX_CENTER_MODIFICATION * MAX_DEPTH;
        }
        ACTIONS = new int[MAX_BF - 1];
        for (int i = 0; i < MAX_BF - 1; i++) {
            ACTIONS[i] = i;
        }
    }

    @Override
    public double getMaxUtility() {
        return MAX_UTILITY;
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
        return "Random game:\nMAX_UTILITY:" + MAX_UTILITY + ", MAX_BF:" + MAX_BF + ", MAX_OBSERVATIONS:" + MAX_OBSERVATION + ", MAX_DEPTH:" + MAX_DEPTH + ", BIN_UTIL:" + INTEGER_UTILITY + ", UTIL_CORR:" + UTILITY_CORRELATION + ", CORR:" + CORRELATION + ", SEED:" + seed;
    }

    @Override
    public int getMaxDepth() {
        return MAX_DEPTH;
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
