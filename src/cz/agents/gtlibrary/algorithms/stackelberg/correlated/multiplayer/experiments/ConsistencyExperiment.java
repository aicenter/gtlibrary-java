package cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.SefceRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.CompleteSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.iterative.LeaderGenerationSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.LeaderGenerationTwoPlayerSefceLP;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jakub Cerny on 30/08/2017.
 */
public class ConsistencyExperiment {

    final static int LEADER = 0;
    final static int depth = 3;
    static boolean pureStrategies = false;

    public static void main(String[] args) {
        if (args.length == 0){
//            runGenSumRandom(new String[]{"R", "3", "3", "30"});
//            runGenSumRandomImproved(new String[]{"I", "8", "4", "10"});
//            runGenSumRandomOneSeed(new String[]{"I", "2", "2"}, 0);
//        runGenSumRandomImproved();
//        runBPG(depth);
//        runFlipIt(args);
//        runFlipIt(new String[]{"F", "3", "4", "AP", "3"});
         runFlipIt(new String[]{"F", "3", "2", "AP", "20"});
        }
        else {
            switch (args[0]) {
                case "F":
                    runFlipIt(args);
                    break;
                case "R":
                    runGenSumRandom(args);
                    break;
                case "I":
                    runGenSumRandomImproved(args);
                    break;

            }
        }
    }

    public static void runGenSumRandomOneSeed(String[] args, int seed){
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        GameState rootState = new GeneralSumRandomGameState();
        GameInfo gameInfo = new RandomGameInfo();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

            System.out.println();
            System.out.println("Running seed " + (seed));

//            rootState = initGame(gameInfo, seed);
            gameInfo = new RandomGameInfo(seed, depth, bf);
            rootState = new GeneralSumRandomGameState();



            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);

            double fullGameGV;
            double oracleGameGV;

            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

            LPTable table = null;
        CompleteTwoPlayerSefceLP solver;
            long startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies)
                runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            else {
                solver = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], solver);
                table = solver.getLpTable();
            }
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

            algConfig = new StackelbergConfig(rootState);
            runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies) {
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            }
            else {
                solver = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], solver);
                oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
//                System.out.println("------------ FIRST TEST ------------");
//                solver.getLpTable().compareConstraints(table);
//                System.out.println("------------ SECOND TEST ------------");
//                table.compareConstraints(solver.getLpTable());
                boolean sameConstraints = table.compareConstraintsSize(solver.getLpTable());
//                boolean sameConstraints = solver.getLpTable().compareConstraintsSize(table);
                System.out.println("Same number of constrains = " + sameConstraints);
            }
            oracleGameGV = runner.getGameValue();
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
            restrictedGameRatio += runner.getRestrictedGameRatio();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

//            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
            System.out.println("Full game time = " + fullTime + "; oracle time = " + oracleTime);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
    }

    public static void runGenSumRandomImproved(String[] args){
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int maxseed = Integer.parseInt(args[3]);
        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        for (int seed = 0; seed < maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));

//            rootState = initGame(gameInfo, seed);
            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();



            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);

            double fullGameGV;
            double oracleGameGV;

            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies)
                runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            else
                runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

            System.out.println();
            algConfig = new StackelbergConfig(rootState);
            runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies)
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            else
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
            restrictedGameRatio += runner.getRestrictedGameRatio();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
            System.out.println("Full game time = " + fullTime + "; oracle time = " + oracleTime);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
        }
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size()/2)));
    }


    public static void runGenSumRandom(String[] args){
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int maxseed = Integer.parseInt(args[3]);
        GameState rootState = new GeneralSumRandomGameState();
        GameInfo gameInfo = new RandomGameInfo();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        for (int seed = 0; seed < maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));

//            rootState = initGame(gameInfo, seed);
            gameInfo = new RandomGameInfo(seed, depth, bf);
            rootState = new GeneralSumRandomGameState();



            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);

            double fullGameGV;
            double oracleGameGV;

            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies)
                runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            else
                runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

            algConfig = new StackelbergConfig(rootState);
            runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies)
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            else
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
            restrictedGameRatio += runner.getRestrictedGameRatio();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
            System.out.println("Full game time = " + fullTime + "; oracle time = " + oracleTime);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
        }
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size()/2)));
    }

    public static GameState initGame(FlipItGameInfo gameInfo, int seed) {
        gameInfo.ZERO_SUM_APPROX = false;

        int rounding = 2;
        final double MAX_COST = 10, MAX_REWARD = 10;//MAX_COST;
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

    public static void runFlipIt(String[] args){
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

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        for (int seed = 0; seed < maxseed; seed++) {

            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));

                rootState = initGame(gameInfo, seed);


                StackelbergConfig algConfig = new StackelbergConfig(rootState);
                FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

                double fullGameGV;
                double oracleGameGV;

                SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

                long startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies)
                runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            else
                runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                fullGameGV = runner.getGameValue();
                fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
                fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

                algConfig = new StackelbergConfig(rootState);
                runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                startGeneration = threadBean.getCurrentThreadCpuTime();
            if (pureStrategies)
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            else
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                oracleGameGV = runner.getGameValue();
                oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
                oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
                restrictedGameRatio += runner.getRestrictedGameRatio();

                if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                    notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
            System.out.println("Full game time = " + fullTime + "; oracle time = " + oracleTime);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
        }
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size()/2)));


    }


}
