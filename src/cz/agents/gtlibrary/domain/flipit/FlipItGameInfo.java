package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.flipit.types.ExponentialGreedyType;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by Jakub on 13/03/17.
 */
public class FlipItGameInfo implements GameInfo {

    // GRAPH FILE : topology, rewards and control costs
    public static String graphFile = "flipit_empty3.txt";
    public static FlipItGraph graph = new FlipItGraph(graphFile);

    // PLAYERS
    public static final Player DEFENDER = new PlayerImpl(0, "Defender");
    public static final Player ATTACKER = new PlayerImpl(1, "Attacker");
    public static final Player NATURE = new PlayerImpl(2, "Nature");
    public static final Player[] ALL_PLAYERS = new Player[] {DEFENDER, ATTACKER, NATURE};

    public static long seed = 11;
    public static int depth = 4;
    public static final boolean RANDOM_TIE = false;
    public static final boolean PREDETERMINED_RANDOM_TIE_WINNER = false;
    public static final Player RANDOM_TIE_WINNER = FlipItGameInfo.DEFENDER;
    public static final boolean INFORMED_ATTACKERS = true;

    public static final boolean DEFENDER_CAN_ALWAYS_ATTACK = true;
//    public static final boolean CALCULATE_REWARDS = false;

    private static final boolean FULLY_RATIONAL_ATTACKER = true;
    public static boolean ZERO_SUM_APPROX = true;

    public static final double INITIAL_POINTS = 5.0;

    public static final boolean NO_INFO = false;

    public static final boolean CALCULATE_UTILITY_BOUNDS = false;

    public static int hashCodeCounter = 0;


    public static HashMap<FlipItGameState,Double> minUtility;
    public static HashMap<FlipItGameState,Double> maxUtility;


    // TYPES
    public static int numTypes = 1;
    public static FollowerType[] types;
    private static double[] typesPrior = new double[] {1.0, 0.7, 0.5};
    private static double[] typesDiscounts = new double[] {1.0, 0.8, 0.6};


    // Forced optima for debugging
    public static final String[] type1optimum = new String[]{};
    public static final ArrayList<String> type1optimumSet = new ArrayList<>(Arrays.asList(type1optimum));
    public static final String[] type2optimum = new String[]{};
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
        for (Node node : graph.getAllNodes().values())
            max += graph.getReward(node);
        max = depth*max;// + INITIAL_POINTS;
//        for (int d = 1; d <= depth; d++){
//            max += Math.min(d, graph.getAllNodes().size())*graph.getMaxReward() - graph.getMinControlCost();
//        }
        if (ZERO_SUM_APPROX)
            return max;
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
        return "Flip It Game : depth = " + depth + "; attacker types = "+getAttackerInfo() + "; graph = " +graphFile + "; info = "+(NO_INFO ? "NO" : "PARTIAL") + "; zero-sum = "+ZERO_SUM_APPROX;
    }

    public String getLpExportName(){
        return "FIG_d-"+depth+"_gf-"+graphFile;
    }

    @Override
    public int getMaxDepth() {
        return 2*depth;
    }

    @Override
    public Player[] getAllPlayers() {
        return ALL_PLAYERS;
    }

    @Override
    public double getUtilityStabilizer() {
        return 1.0;
    }

    public void calculateMinMaxBounds(){
        GameState root = new FlipItGameState();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);
        minUtility = new HashMap<>();
        maxUtility =  new HashMap<>();
        traverseTree(root,expander);
        root = new FlipItGameState();
//        System.out.println(minUtility.get(root));
    }

    public static double[] calculateMinMaxBoundsFor(GameState state){
//        GameState root = new FlipItGameState();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);
//        minUtility = new HashMap<>();
//        maxUtility =  new HashMap<>();
        return traverseTree(state,expander);
//        root = new FlipItGameState();
//        System.out.println(minUtility.get(root));
    }

    private static double[] traverseTree(GameState state, Expander<SequenceInformationSet> expander){
        double[] bounds = new double[2];
        bounds[0] = Double.MAX_VALUE;
        bounds[1] = Double.MIN_VALUE;
        ((SequenceFormConfig)expander.getAlgorithmConfig()).addStateToSequenceForm(state);
        if (state.isGameEnd()) {
            double utility = state.getUtilities()[0];
            minUtility.put((FlipItGameState) state, utility);
            maxUtility.put((FlipItGameState) state, utility);
            return new double[]{utility, utility};
        }
        else{
            double[] actionBounds;
            for (Action action : expander.getActions(state))   {
                actionBounds = traverseTree(state.performAction(action), expander);
                if (actionBounds[0] < bounds[0]) bounds[0] = actionBounds[0];
                if (actionBounds[1] > bounds[1]) bounds[1] = actionBounds[1];
            }
        }
        minUtility.put((FlipItGameState) state,bounds[0]);
        maxUtility.put((FlipItGameState) state,bounds[1]);
        return bounds;
    }
}
