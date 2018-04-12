package cz.agents.gtlibrary.nfg.sce.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.strategy.PureStrategyImpl;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.sce.Efg2Nfg;
import cz.agents.gtlibrary.nfg.sce.SCERunner;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 24/08/2017.
 */
public class RatioExperiments {

    protected static double[][] utilitiesLeader;
    protected static double[][] utilitiesFollower;

    protected final boolean RUN_SINGLE_SEED = false;


    public static void main(String[] args) {
        runFlipIt(new String[]{"3", "2", "N", "50"});
//        runFlipIt(args);
    }

    public static void runFlipIt(String[] args){
        // parse args
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[0]);
            int graphSize = Integer.parseInt(args[1]);
            String graphFile = "flipit_empty"+graphSize+".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, 1, graphFile, 1);
            FlipItGameInfo.OUTPUT_STRATEGY = false;
            if (args.length > 3) {
                String version = args[2];
                switch (version) {
                    case "F":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.FULL;
                        break;
                    case "N":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.NO;
                        break;
                    case "NP":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_NODE_POINTS;
                        break;
                    case "AP":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_ALL_POINTS;
                        break;//
                }
                maxseed = Integer.parseInt(args[3]);
            }
        }

        // run experiments
        SCERunner runner;
        double fullGameValue;
        double iterativeValue;
        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double ratio = 0.0;
        for (int seed = 0; seed < maxseed; seed++){
            System.out.println("Seed = "+seed + ", current number of NC cases = "+notConvergedSeeds.size());
            initGame(gameInfo, seed);
            runner = new SCERunner(seed, utilitiesLeader, utilitiesFollower);
            fullGameValue = runner.runFullGame();
            runner = new SCERunner(seed, utilitiesLeader, utilitiesFollower);
            iterativeValue = runner.runIterativeWithBestResponse();
            System.out.println("Full game value = " + fullGameValue + ", iterative game value = " + iterativeValue);
            if (Math.abs(iterativeValue - fullGameValue) > 0.001) {
                notConvergedSeeds.add(seed);
            }
            ratio += runner.getRGRatio();
        }
        System.out.println("Average RG ratio = " + ratio / maxseed);
//        notConvergedSeeds.add(1); notConvergedSeeds.add(2);
        System.out.println("Number of not converged cases = " + notConvergedSeeds.size() + " / " + maxseed + "; seeds = "+ notConvergedSeeds.toString());

    }

    public static void initGame(FlipItGameInfo gameInfo, int seed){
        gameInfo.ZERO_SUM_APPROX = false;

        int rounding = 3;
        final double MAX_COST = 10, MAX_REWARD = 10;//MAX_COST;
        int numberOfNodes = Integer.parseInt(gameInfo.graphFile.substring(gameInfo.graphFile.length()-5, gameInfo.graphFile.length()-4));
//        System.out.println(numberOfNodes);
        HighQualityRandom random = new HighQualityRandom(seed);
        double[] costs = new double[numberOfNodes];
        double[] rewards = new double[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++){
            costs[i] = Math.round(((int)Math.pow(10,rounding))*MAX_COST * random.nextDouble())/Math.pow(10,rounding);
            rewards[i] = Math.round(((int)Math.pow(10,rounding))*MAX_REWARD * random.nextDouble())/Math.pow(10,rounding);
        }

        gameInfo.graph = new FlipItGraph(gameInfo.graphFile, costs, rewards);

        GameState rootState = null;
        if (FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) gameInfo.calculateMinMaxBounds();

        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;

        }

        GenSumSequenceFormConfig algConfig = new GenSumSequenceFormConfig();
        Efg2Nfg generator = new Efg2Nfg(rootState, new FlipItExpander<>(algConfig), gameInfo, algConfig);
        HashMap<ArrayList<PureStrategyImpl>, double[]> utility = generator.getUtility();
        int index = 0;
        HashMap<PureStrategyImpl, Integer> leaderActions = new HashMap<>();
        for (PureStrategyImpl leaderStrat : generator.getStrategiesOfAllPlayers().get(0)) {
            leaderActions.put(leaderStrat,index); index++;
        }
        index = 0;
        HashMap<PureStrategyImpl, Integer> followerActions = new HashMap<>();
        for (PureStrategyImpl followerStrat : generator.getStrategiesOfAllPlayers().get(1)) {
            followerActions.put(followerStrat,index); index++;
        }
//        System.out.println("Utility size : " + utility.keySet().size());
        utilitiesLeader = new double[generator.getStrategiesOfAllPlayers().get(0).size()][generator.getStrategiesOfAllPlayers().get(1).size()];
        utilitiesFollower = new double[generator.getStrategiesOfAllPlayers().get(0).size()][generator.getStrategiesOfAllPlayers().get(1).size()];
        for (ArrayList<PureStrategyImpl> profile : utility.keySet()){
            int leaderAction = leaderActions.get(profile.get(0));
            int followerAction = followerActions.get(profile.get(1));
            utilitiesLeader[leaderAction][followerAction] = utility.get(profile)[0];
            utilitiesFollower[leaderAction][followerAction] = utility.get(profile)[1];
        }

    }


}
