package cz.agents.gtlibrary.domain.flipit;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.flipit.types.ExponentialGreedyType;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ISKey;
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
    public static final Player[] ALL_PLAYERS = new Player[] {DEFENDER, ATTACKER};//, NATURE};

    public static long seed = 11;
    public static int depth = 2;
    public static final boolean RANDOM_TIE = false;
    public static final boolean PREDETERMINED_RANDOM_TIE_WINNER = false;
    public static final Player RANDOM_TIE_WINNER = FlipItGameInfo.DEFENDER;
    public static final boolean INFORMED_ATTACKERS = true;

    public static final boolean DEFENDER_CAN_ALWAYS_ATTACK = true;
    public static final boolean ATTACKER_CAN_ALWAYS_ATTACK = true;
//    public static final boolean CALCULATE_REWARDS = false;

    private static final boolean FULLY_RATIONAL_ATTACKER = true;
    public static boolean ZERO_SUM_APPROX = true;
    protected static final boolean ENABLE_PASS = false;

    public static final double INITIAL_POINTS = 50.0;

    public static final boolean RANDOM_TERMINATION = false;
    public static final double RANDOM_TERMINATION_PROBABILITY = 0.3;


    public enum FlipItInfo {
        NO, FULL, REVEALED_ALL_POINTS, REVEALED_NODE_POINTS
    }

    public static FlipItInfo gameVersion = FlipItInfo.FULL;//REVEALED_NODE_POINTS;//FULL;//REVEALED_ALL_POINTS;//FULL;

    public static boolean PERFECT_RECALL = true;

    public static final boolean CALCULATE_UTILITY_BOUNDS = false;
    public static final boolean ENABLE_ITERATIVE_SOLVING = false;
    public static boolean OUTPUT_STRATEGY = true;
    public static final boolean USE_ISKEY_WITH_OBSERVATIONS = false;

    public static double MAX_UTILITY;

    private static double[] determinedBounds;


    public static HashMap<ISKey,HashMap<GameState,Double>> minUtility;
    public static HashMap<ISKey,HashMap<GameState,Double>> maxUtility;


    // TYPES
    public static int numTypes = 1;
    public static FollowerType[] types;
    private static double[] typesPrior = new double[] {1.0, 0.7, 0.5};
    private static double[] typesDiscounts = new double[] {1.0, 0.8, 0.6};
    private static final double MIN_DISCOUNT = 0.8;


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
        MAX_UTILITY = getMaxUtility();
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
        int rounding = 2;
        for (int i = 0; i < numTypes-1; i++){
            typesDiscounts[i] = MIN_DISCOUNT + Math.round(((int) Math.pow(10, rounding)) * (1.0 - MIN_DISCOUNT) * rnd.nextDouble()) / Math.pow(10, rounding);
            typesPrior[i] = Math.round(((int) Math.pow(10, rounding)) * 0.2 * rnd.nextDouble() * (1.0-priors)) / Math.pow(10, rounding);
            priors += typesPrior[i];
        }
        typesDiscounts[numTypes-1] = MIN_DISCOUNT + Math.round(((int) Math.pow(10, rounding)) * (1.0 - MIN_DISCOUNT) *rnd.nextDouble()) / Math.pow(10, rounding);
        typesPrior[numTypes-1] = Math.round(((int) Math.pow(10, rounding)) * (1.0-priors)) / Math.pow(10, rounding);

        if (numTypes == 1 && FULLY_RATIONAL_ATTACKER) typesDiscounts[0] = 1.0;

        types = new FollowerType[numTypes];
        for(int i = 0; i < numTypes; i++) {
            types[i] = new ExponentialGreedyType(typesPrior[i], typesDiscounts[i],i);
        }
        MAX_UTILITY = getMaxUtility();


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
        String info = "";
        info+= "Flip It Game : depth = " + depth + "; attacker types = "+getAttackerInfo() + "; graph = " +graphFile + "; initial points = " + INITIAL_POINTS + "; info = "+gameVersion + "; zero-sum = "+ZERO_SUM_APPROX;
        info+= "\n" + graph.getInfo();
        return info;
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
        GameState root = new NodePointsFlipItGameState();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);
        minUtility = new HashMap<>();
        maxUtility =  new HashMap<>();
        traverseTree(root,expander, null);
        root = new NodePointsFlipItGameState();
//        System.out.println(minUtility.get(root));
    }

    public static double[] calculateMinMaxBoundsFor(GameState state){
//        GameState root = new NodePointsFlipItGameState();
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);
//        minUtility = new HashMap<>();
//        maxUtility =  new HashMap<>();
        return traverseTree(state,expander, null);
//        root = new NodePointsFlipItGameState();
//        System.out.println(minUtility.get(root));
    }

    public static double[] determineMinMaxBoundsFor(GameState state) {
        SequenceFormConfig<SequenceInformationSet> algConfig = new SequenceFormConfig<SequenceInformationSet>();
        Expander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);
//        minUtility = new HashMap<>();
//        maxUtility =  new HashMap<>();
        traverseTree(new NodePointsFlipItGameState(),expander, state);
        return determinedBounds;
    }

    private static double[] traverseTree(GameState state, Expander<SequenceInformationSet> expander, GameState target){
        double[] bounds = new double[2];
        bounds[0] = Double.MAX_VALUE;
        bounds[1] = Double.MIN_VALUE;
        ((SequenceFormConfig)expander.getAlgorithmConfig()).addStateToSequenceForm(state);
        if (state.isGameEnd()) {
            double utility = state.getUtilities()[0];
            if (!minUtility.containsKey(state.getISKeyForPlayerToMove())){
                minUtility.put(state.getISKeyForPlayerToMove(), new HashMap<>());
                maxUtility.put(state.getISKeyForPlayerToMove(), new HashMap<>());
            }
            minUtility.get(state.getISKeyForPlayerToMove()).put(state,utility);
            maxUtility.get(state.getISKeyForPlayerToMove()).put(state,utility);
//            minUtility.put((NodePointsFlipItGameState) state, utility);
//            maxUtility.put((NodePointsFlipItGameState) state, utility);
            return new double[]{utility, utility};
        }
        else{
            double[] actionBounds;
            for (Action action : expander.getActions(state))   {
                actionBounds = traverseTree(state.performAction(action), expander, target);
                if (actionBounds[0] < bounds[0]) bounds[0] = actionBounds[0];
                if (actionBounds[1] > bounds[1]) bounds[1] = actionBounds[1];
            }
        }

        if(state.equals(target)){
            determinedBounds = bounds;
        }

        if (!minUtility.containsKey(state.getISKeyForPlayerToMove())){
            minUtility.put(state.getISKeyForPlayerToMove(), new HashMap<>());
            maxUtility.put(state.getISKeyForPlayerToMove(), new HashMap<>());
        }
        if (minUtility.get(state.getISKeyForPlayerToMove()).containsKey(state) && Math.abs(minUtility.get(state.getISKeyForPlayerToMove()).get(state) - bounds[0]) > 0.01){
            System.err.println("ERROR : " + Math.abs(minUtility.get(state.getISKeyForPlayerToMove()).get(state) - bounds[0]));
//            System.exit(0);
        }
        minUtility.get(state.getISKeyForPlayerToMove()).put(state,bounds[0]);
        maxUtility.get(state.getISKeyForPlayerToMove()).put(state,bounds[1]);
        return bounds;
    }
}
