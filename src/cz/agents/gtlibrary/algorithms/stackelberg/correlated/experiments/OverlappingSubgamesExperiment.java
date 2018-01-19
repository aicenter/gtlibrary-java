package cz.agents.gtlibrary.algorithms.stackelberg.correlated.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.SefceRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.CompleteSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.iterative.LeaderGenerationSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.CompleteGenerationTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.LeaderGeneration2pRelevantWiseSefceLp;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.LeaderGenerationTwoPlayerSefceLP;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Solver;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Jakub Cerny on 09/11/2017.
 */
public class OverlappingSubgamesExperiment {

    protected static int LEADER = 0;

    public static void main(String[] args) {
        runFlipIt(new String[]{"F", "3", "6", "AP", "1"});
//        runGenSumRandomImproved(new String[]{"I", "9", "4", "-0.2", "10"});
//        runBPG(new String[]{"B", "3", "0"});
    }

    public static void runGenSumRandomImproved(String[] args){
        HashMap<Integer, HashMap<Integer, Integer>> issOutside = new HashMap<>();
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        double correlation = Double.parseDouble(args[3]);
        int maxseed = Integer.parseInt(args[4]);
        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        for (int seed = 0; seed < maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));

//            rootState = initGame(gameInfo, seed);
            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed, correlation);
            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();



            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);


            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            runner.generateCompleteGame();
            mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));
        }
        System.out.printf("\n\n");
        System.out.println("#seeds: " + (maxseed));
        for (Integer ddepth : issOutside.keySet())
            System.out.println("Depth = " + ddepth + ", [#ISs outside = #roots]: " + issOutside.get(ddepth).toString());
    }

    public static void runFlipIt(String[] args) {
        HashMap<Integer, HashMap<Integer, Integer>> issOutside = new HashMap<>();
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            int graphSize = Integer.parseInt(args[2]);
            String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, 1, graphFile, 1);
            FlipItGameInfo.OUTPUT_STRATEGY = false;
            if (args.length > 3) {
                String version = args[3];
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
                maxseed = Integer.parseInt(args[4]);

            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        for (int seed = 0; seed < maxseed; seed++) {

            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            rootState = initGame(gameInfo, seed);
            GenSumSequenceFormConfig algConfig = new StackelbergConfig(rootState);
            FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            runner.generateCompleteGame();

            int l1size = 0;
            for (SequenceInformationSet set : algConfig.getAllInformationSets().values())
                if (set.getPlayer().equals(gameInfo.getOpponent(gameInfo.getAllPlayers()[LEADER])) && set.getPlayersHistory().size() == 1)l1size++;
            System.out.println(l1size);

            mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));
        }
        System.out.printf("\n\n");
        System.out.println("#seeds: " + (maxseed));
        for (Integer depth : issOutside.keySet())
            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutside.get(depth).toString());
    }

    public static void runBPG(String[] args){
        HashMap<Integer, HashMap<Integer, Integer>> issOutside = new HashMap<>();
        int depth = Integer.parseInt(args[1]);
        LEADER = Integer.parseInt(args[2]);
//        int bf = Integer.parseInt(args[2]);
//        double correlation = Double.parseDouble(args[3]);
//        int maxseed = Integer.parseInt(args[4]);
//        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        BPGGameInfo gameInfo = new BPGGameInfo();
        BPGGameInfo.DEPTH = depth;
        rootState = new GenSumBPGGameState();



        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);

        double fullGameGV;
        double oracleGameGV;

        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
        runner.generateCompleteGame();

        int l1size = 0;
        for (SequenceInformationSet set : algConfig.getAllInformationSets().values())
            if (set.getPlayer().equals(gameInfo.getOpponent(gameInfo.getAllPlayers()[LEADER])) && set.getPlayersHistory().size() == 2)l1size++;
        System.out.println(l1size);

        mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));
        System.out.printf("\n\n");
//        System.out.println("#seeds: " + (maxseed));
        for (Integer ddepth : issOutside.keySet())
            System.out.println("Depth = " + ddepth + ", [#IS outside = #roots]: " + issOutside.get(ddepth).toString());
    }

    protected static void mergeISsOutsideSGStats(HashMap<Integer, HashMap<Integer, Integer>> stats, HashMap<Integer, HashMap<Integer, Integer>> result) {
        for (Integer depth : result.keySet()) {
            if (!stats.containsKey(depth)) {
                stats.put(depth, result.get(depth));
                continue;
            }
            for (Integer numberOfISs : result.get(depth).keySet()) {
                if (!stats.get(depth).containsKey(numberOfISs))
                    stats.get(depth).put(numberOfISs, result.get(depth).get(numberOfISs));
                else
                    stats.get(depth).put(numberOfISs, stats.get(depth).get(numberOfISs) + result.get(depth).get(numberOfISs));
            }
        }
    }

    public static GameState initGame(FlipItGameInfo gameInfo, int seed) {
        gameInfo.ZERO_SUM_APPROX = false;

        int rounding = 2;
        final double MAX_COST = 20, MAX_REWARD = 20;//MAX_COST;
        int numberOfNodes = Integer.parseInt(gameInfo.graphFile.substring(gameInfo.graphFile.length() - 5, gameInfo.graphFile.length() - 4));
//        System.out.println(numberOfNodes);
        HighQualityRandom random = new HighQualityRandom(seed);
        double[] costs = new double[numberOfNodes];
        double[] rewards = new double[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            costs[i] = Math.round(1 + ((int) Math.pow(10, rounding)) * MAX_COST * random.nextDouble()) / Math.pow(10, rounding);
            rewards[i] = Math.round(1 + ((int) Math.pow(10, rounding)) * MAX_REWARD * random.nextDouble()) / Math.pow(10, rounding);
        }

        gameInfo.graph = new FlipItGraph(gameInfo.graphFile, costs, rewards);

        GameState rootState = null;
        if (FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) gameInfo.calculateMinMaxBounds();

        switch (FlipItGameInfo.gameVersion) {
            case NO:
                rootState = new NoInfoFlipItGameState();
                break;
            case FULL:
                rootState = new FullInfoFlipItGameState();
                break;
            case REVEALED_ALL_POINTS:
                rootState = new AllPointsFlipItGameState();
                break;
            case REVEALED_NODE_POINTS:
                rootState = new NodePointsFlipItGameState();
                break;

        }
        return  rootState;
    }

}
