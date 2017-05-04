package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.domain.flipit.types.ExponentialGreedyType;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by Jakub on 13/03/17.
 */
public class FlipItGameInfo implements GameInfo {

    // GRAPH FILE : topology, rewards and control costs
    public static String graphFile = "flipit_empty5.txt";
    public static FlipItGraph graph = new FlipItGraph(graphFile);

    // PLAYERS
    public static Player DEFENDER = new PlayerImpl(0, "Defender");
    public static Player ATTACKER = new PlayerImpl(1, "Attacker");
    public static final Player NATURE = new PlayerImpl(2, "Nature");
    public static final Player[] ALL_PLAYERS = new Player[] {DEFENDER, ATTACKER, NATURE};

    public static long seed = 11;
    public static int depth = 4;
    public static final boolean RANDOM_TIE = false;
    public static final boolean PREDETERMINED_RANDOM_TIE_WINNER = false;
    public static final Player RANDOM_TIE_WINNER = FlipItGameInfo.DEFENDER;
    public static final boolean INFORMED_PLAYERS = false;

    private static final boolean FULLY_RATIONAL_ATTACKER = true;
    public static boolean ZERO_SUM_APPROX = true;

    public static final double INITIAL_POINTS = 5.0;


    // TYPES
    public static int numTypes = 1;
    public static FollowerType[] types;
    private static double[] typesPrior = new double[] {1.0, 0.7, 0.5};
    private static double[] typesDiscounts = new double[] {1.0, 0.8, 0.6};

    public static final String[] type1optimum = new String[]{"Attacker: []",
            "Attacker: [{N0, _, _, 10893061}, {N0, _, _, 1087200722}]",
            "Attacker: [{N0, _, _, 10893061}]",
            "Attacker: [{N0, _, _, 10893061}, {_, NOOP, _, 1088511256}]"};
    public static final ArrayList<String> type1optimumSet = new ArrayList<>(Arrays.asList(type1optimum));

    public static final String[] type2optimum = new String[]{"Attacker: []",
                "Attacker: [{N0, _, _, 10893061}, {N0, _, _, 1087200722}]",
                "Attacker: [{N0, _, _, 10893061}]",
                "Attacker: [{N0, _, _, 10893061}, {_, NOOP, _, 1088511256}]"};
    public static final ArrayList<String> type2optimumSet = new ArrayList<>(Arrays.asList(type2optimum));



    public FlipItGameInfo(){
        types = new FollowerType[numTypes];
        for(int i = 0; i < numTypes; i++) {
            types[i] = new ExponentialGreedyType(typesPrior[i], typesDiscounts[i],i);
        }
    }

    public FlipItGameInfo(int depth, int numTypes, String graphFile, long seed){
        this.seed = seed;
        this.depth = depth;
        this.numTypes = numTypes;
        this.graphFile = graphFile;

        this.ZERO_SUM_APPROX = false;

        graph = new FlipItGraph(graphFile);

        Random rnd = new HighQualityRandom(seed);

        typesPrior = new double[numTypes];
        typesDiscounts = new double[numTypes];
        double priors = 0.0;
        for (int i = 0; i < numTypes-1; i++){
            typesDiscounts[i] = rnd.nextDouble();
            typesPrior[i] = rnd.nextDouble()*(1.0-priors);
            priors += typesPrior[i];
        }
        typesDiscounts[numTypes-1] = rnd.nextDouble();
        typesPrior[numTypes-1] = (1.0-priors);

        if (numTypes == 1 && FULLY_RATIONAL_ATTACKER) typesDiscounts[0] = 1.0;

        types = new FollowerType[numTypes];
        for(int i = 0; i < numTypes; i++) {
            types[i] = new ExponentialGreedyType(typesPrior[i], typesDiscounts[i],i);
        }
    }

    public FlipItGameInfo(int depth, int numTypes, String graphFile){
        this.depth = depth;
        this.numTypes = numTypes;
        this.graphFile = graphFile;

        graph = new FlipItGraph(graphFile);

        Random rnd = new HighQualityRandom(seed);

        typesPrior = new double[numTypes];
        typesDiscounts = new double[numTypes];
        double priors = 0.0;
        for (int i = 0; i < numTypes-1; i++){
            typesDiscounts[i] = rnd.nextDouble();
            typesPrior[i] = rnd.nextDouble()*(1.0-priors);
            priors += typesPrior[i];
        }
        typesDiscounts[numTypes-1] = rnd.nextDouble();
        typesPrior[numTypes-1] = (1.0-priors);

        types = new FollowerType[numTypes];
        for(int i = 0; i < numTypes; i++) {
            types[i] = new ExponentialGreedyType(typesPrior[i], typesDiscounts[i],i);
        }
    }

    public void setInfo(int depth, int numTypes, String graphFile){
        this.depth = depth;
        this.numTypes = numTypes;
        this.graphFile = graphFile;

        graph = new FlipItGraph(graphFile);

        Random rnd = new HighQualityRandom(seed);

        typesPrior = new double[numTypes];
        typesDiscounts = new double[numTypes];
        double priors = 0.0;
        for (int i = 0; i < numTypes-1; i++){
            typesDiscounts[i] = rnd.nextDouble();
            typesPrior[i] = rnd.nextDouble()*(1.0-priors);
            priors += typesPrior[i];
        }
        typesDiscounts[numTypes-1] = rnd.nextDouble();
        typesPrior[numTypes-1] = (1.0-priors);

        types = new FollowerType[numTypes];
        for(int i = 0; i < numTypes; i++) {
            types[i] = new ExponentialGreedyType(typesPrior[i], typesDiscounts[i],i);
        }
    }

    @Override
    public double getMaxUtility() {
        double max = 0.0;
        for (int d = 1; d <= depth; d++){
            max += Math.min(d, graph.getAllNodes().size())*graph.getMaxReward() - graph.getMinControlCost();
        }
        if (ZERO_SUM_APPROX)
            return 1e6;
        else
            return max;
    }

    @Override
    public Player getFirstPlayerToMove() {
        return DEFENDER;
    }

    @Override
    public Player getOpponent(Player player) {
        if (player.equals(ATTACKER)) return DEFENDER;
        else return ATTACKER;
    }

    private String getAttackerInfo(){
        String info = "";
        for (FollowerType type : types)
            info += type.toString() + ", ";
        return info;
    }

    @Override
    public String getInfo() {
        return "Flip It Game : depth = " + depth + "; attacker types = "+getAttackerInfo() + "; graph = " +graphFile;
    }

    public String getLpExportName(){
        return "FIG_d-"+depth+"_gf-"+graphFile;
    }

    @Override
    public int getMaxDepth() {
        return depth;
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
