package cz.agents.gtlibrary.algorithms.stackelberg.correlated.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.SefceRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.CompleteSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.iterative.LeaderGenerationSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.*;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.oshizumo.*;
import cz.agents.gtlibrary.domain.randomgame.GenSumSimRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by Jakub Cerny on 30/08/2017.
 */
public class CorrelatedConsistencyExperiment {

    static int LEADER = 0;
    final static int depth = 3;
    static boolean pureStrategies = false;

    public static void main(String[] args) {
        if (args.length == 0) {
//            runGenSumRandom(new String[]{"R", "3", "3", "30"});
//            runGenSumSimRandom(new String[]{"R", "3", "3", "3"});
//            runGenSumRandomImproved(new String[]{"I", "7", "5", "-0.2", "1"});
//            runGenSumRandomOneSeed(new String[]{"I", "2", "2"}, 1);
//        runGenSumRandomImproved();
//        runBPG(new String[]{"B", "4", "0"});
//        runFlipIt(args);
//        runFlipIt(new String[]{"F", "4", "3", "AP", "1"});
            runFlipIt(new String[]{"F", "3", "3", "AP", "1"});
//            runFlipItGeneration(new String[]{"F", "2", "2", "AP", "10"});
//            runOshiZumo(args);
        } else {
            switch (args[0]) {
                case "F":
                    runFlipIt(args);
                    break;
                case "FG":
                    runFlipItGeneration(args);
                    break;
                case "B":
                    runBPG(args);
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

    public static void runGenSumRandomOneSeed(String[] args, int seed) {
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();

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
        gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
        rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
        double fullGameGV;
        double oracleGameGV;

        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

        LPTable table = null;
        CompleteTwoPlayerSefceLP solverA = null;
        CompleteTwoPlayerSefceLP solverB = null;
        long startGeneration = threadBean.getCurrentThreadCpuTime();
        if (pureStrategies)
            runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
        else {
            solverA = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                solverA = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], solverA);
            table = solverA.getLpTable();
        }
        fullGameGV = runner.getGameValue();
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        algConfig = new StackelbergConfig(rootState);
        expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        if (pureStrategies) {
            runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        } else {
            algConfig = new LeaderGenerationConfig(rootState);
            expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
//                solverB = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                solverB = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            solverB = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            runner.generate(rootState.getAllPlayers()[LEADER], solverB);
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
//                System.out.println("------------ FIRST TEST ------------");
//                solver.getLpTable().compareConstraints(table);
//                System.out.println("------------ SECOND TEST ------------");
//                table.compareConstraints(solver.getLpTable());
            boolean sameConstraints = table.compareConstraintsSize(solverB.getLpTable());
//                boolean sameConstraints = solver.getLpTable().compareConstraintsSize(table);
            System.out.println("Same number of constrains = " + sameConstraints);
        }
        oracleGameGV = runner.getGameValue();
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
        restrictedGameRatio += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(seed);

//            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
        System.out.println(solverA.getClass().getSimpleName() + " = " + fullTime + ";" + solverB.getClass().getSimpleName() + " = " + oracleTime);
        System.out.println("Number of not converged = " + notConvergedSeeds.size());
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
    }

    public static void runBPG(String[] args) {
        int depth = Integer.parseInt(args[1]);
        LEADER = Integer.parseInt(args[2]);
//        int bf = Integer.parseInt(args[2]);
//        double correlation = Double.parseDouble(args[3]);
//        int maxseed = Integer.parseInt(args[4]);
//        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioI = 0.0;
        double restrictedGameRatioII = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

//        for (int seed = 0; seed < maxseed; seed++) {
//            System.out.println();
//            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));
//
////            rootState = initGame(gameInfo, seed);
        BPGGameInfo gameInfo = new BPGGameInfo();
        BPGGameInfo.DEPTH = depth;
        rootState = new GenSumBPGGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);

        double fullGameGV;
        double oracleGameGV;

        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        Solver s1;
        if (pureStrategies) {
            s1 = new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
        } else {
//                algConfig = new LeaderGenerationConfig(rootState);
//                expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
//                runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWise(rootState.getAllPlayers()[LEADER], gameInfo);
//            s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
        }
        restrictedGameRatioI += runner.getRestrictedGameRatio();
        fullGameGV = runner.getGameValue();
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//        String s1Name =

        System.out.println();
        algConfig = new StackelbergConfig(rootState);
        expander = new BPGExpander<>(algConfig);
        runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        Solver s2;
        if (pureStrategies) {
            s2 = new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
        } else {
//            s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
        }
        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
        restrictedGameRatioII += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(0);

        System.out.println("Average restricted game ratios = " + restrictedGameRatioI + " ; " + restrictedGameRatioII);
        System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + ((s1 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s1).getFinalLpSize() : 0) +
                " ; " + s2.getClass().getSimpleName() + " : " + ((s2 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s2).getFinalLpSize() : 0));
        System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + "; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
        System.out.println("Number of not converged = " + notConvergedSeeds.size());
//        }
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
    }

    public static void runOshiZumo(String[] args) {
//        int depth = Integer.parseInt(args[1]);
//        LEADER = Integer.parseInt(args[2]);
//        int bf = Integer.parseInt(args[2]);
//        double correlation = Double.parseDouble(args[3]);
//        int maxseed = Integer.parseInt(args[4]);
//        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        OshiZumoGameState rootState;// = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioI = 0.0;
        double restrictedGameRatioII = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

//        for (int seed = 0; seed < maxseed; seed++) {
//            System.out.println();
//            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));
//
////            rootState = initGame(gameInfo, seed);
        OZGameInfo gameInfo = new OZGameInfo();
        rootState = new OshiZumoGameState();//GenSumIIOshiZumoGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new OshiZumoExpander<>(algConfig);

        double fullGameGV;
        double oracleGameGV;

        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        Solver s1;
        if (pureStrategies) {
            s1 = new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
        } else {
//                algConfig = new LeaderGenerationConfig(rootState);
//                expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
//                runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWise(rootState.getAllPlayers()[LEADER], gameInfo);
//            s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
        }
        restrictedGameRatioI += runner.getRestrictedGameRatio();
        fullGameGV = runner.getGameValue();
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//        String s1Name =

        System.out.println();
        algConfig = new StackelbergConfig(rootState);
        expander = new OshiZumoExpander<>(algConfig);
        runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        Solver s2;
        if (pureStrategies) {
            s2 = new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
        } else {
//            s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
        }
        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
        restrictedGameRatioII += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(0);

        System.out.println("Average restricted game ratios = " + restrictedGameRatioI + " ; " + restrictedGameRatioII);
        System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + ((s1 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s1).getFinalLpSize() : 0) +
                " ; " + s2.getClass().getSimpleName() + " : " + ((s2 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s2).getFinalLpSize() : 0));
        System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + "; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
        System.out.println("Number of not converged = " + notConvergedSeeds.size());
//        }
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
    }

    public static void runGenSumRandomImproved(String[] args) {
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        double correlation = Double.parseDouble(args[3]);
        int maxseed = Integer.parseInt(args[4]);
        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioI = 0.0;
        double restrictedGameRatioII = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        for (int seed = 0; seed < maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

//            rootState = initGame(gameInfo, seed);
            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed, correlation);
            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);

            double fullGameGV;
            double oracleGameGV;

            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            Solver s1;
            if (pureStrategies) {
                s1 = new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s1);
            } else {
//                algConfig = new LeaderGenerationConfig(rootState);
//                expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
//                runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWise(rootState.getAllPlayers()[LEADER], gameInfo);
                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s1);
            }
            restrictedGameRatioI += runner.getRestrictedGameRatio();
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

            System.out.println();
            algConfig = new StackelbergConfig(rootState);
            runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            Solver s2;
            if (pureStrategies) {
                s2 = new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s2);
            } else {
                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s2);
            }
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
            restrictedGameRatioII += runner.getRestrictedGameRatio();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed + 1) + " ; " + restrictedGameRatioII / (seed + 1));
            System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + ((s1 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s1).getFinalLpSize() : 0) +
                    " ; " + s2.getClass().getSimpleName() + " : " + ((s2 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s2).getFinalLpSize() : 0));
            System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + "; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
        }
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
    }

    public static void runGenSumSimRandom(String[] args) {
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int maxseed = Integer.parseInt(args[3]);
        GameState rootState = new GenSumSimRandomGameState();
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
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

//            rootState = initGame(gameInfo, seed);
            gameInfo = new RandomGameInfo(seed, depth, bf);
            rootState = new GenSumSimRandomGameState();


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
                runner.generate(rootState.getAllPlayers()[LEADER], new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
            restrictedGameRatio += runner.getRestrictedGameRatio();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratio = " + restrictedGameRatio / (seed + 1));
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
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
    }


    public static void runGenSumRandom(String[] args) {
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
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

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

            System.out.println("Average restricted game ratio = " + restrictedGameRatio / (seed + 1));
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
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
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
        gameInfo.seed = seed;

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
        return rootState;
    }

    public static void runFlipItGeneration(String[] args) {
        int startingSeed = 0; // 113, 117, 135, 157, 168
        HashMap<Integer, HashMap<Integer, Integer>> issOutside = new HashMap<>();
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = 5;
            int graphSize = 2;
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
                maxseed = 10000;

            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioII = 0.0;
        double restrictedGameRatioI = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();


        int[] setting = new int[]{1, 2, 10, 4}; // N0:r=2.0,c=1.0; N1:r=4.0,c=10.0
        final boolean FIND_SEED = false;

        for (int cost1 = 1; cost1 < 11; cost1++)
            for (int reward1 = 1; reward1 < 11; reward1++)
                for (int cost2 = 1; cost2 < 11; cost2++)
                    for (int reward2 = 1; reward2 < 11; reward2++) {

                    if (FIND_SEED && (cost1 != setting[0] || reward1 != setting[1] || cost2 != setting[2] || reward2 != setting[3]))
                        continue;

                        int seed = 1000*(cost1-1) + 100*(reward1-1) + 10*(cost2-1) + reward2 - 1;
                        System.out.println();
                        System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

//                        rootState = initGame(gameInfo, seed);
                        double[] costs = new double[]{cost1, cost2};
                        double[] rewards = new double[]{reward1, reward2};

                        gameInfo.graph = new FlipItGraph(gameInfo.graphFile, costs, rewards);
                        gameInfo.seed = seed;

                        rootState = null;
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


                        GenSumSequenceFormConfig algConfig = new StackelbergConfig(rootState);
                        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

                        double fullGameGV;
                        double oracleGameGV;

                        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

                        long startGeneration = threadBean.getCurrentThreadCpuTime();
                        Solver s1;
                        if (pureStrategies) {
                            s1 = new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                            runner.generate(rootState.getAllPlayers()[LEADER], s1);
                        } else {
//                algConfig = new LeaderGenerationConfig(rootState);
//                expander = new FlipItExpander<>(algConfig);
//                runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                            s1 = new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
                            runner.generate(rootState.getAllPlayers()[LEADER], s1);
                        }
                        restrictedGameRatioI += runner.getRestrictedGameRatio();
                        fullGameGV = runner.getGameValue();
                        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
                        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//                s1 = null;

                        algConfig = new StackelbergConfig(rootState);
                        runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                        startGeneration = threadBean.getCurrentThreadCpuTime();
//            new GambitEFG().write("testGameConsistency.gbt", rootState, expander);
                        Solver s2;
                        if (pureStrategies) {
                            s2 = new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                            runner.generate(rootState.getAllPlayers()[LEADER], s2);
                        } else {
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                            s2 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                            runner.generate(rootState.getAllPlayers()[LEADER], s2);
//                            mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));
                        }
                        oracleGameGV = runner.getGameValue();
                        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
                        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
                        restrictedGameRatioII += runner.getRestrictedGameRatio();

                        if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                            notConvergedSeeds.add(seed);

                        System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed - startingSeed + 1) + " ; " + restrictedGameRatioII / (seed - startingSeed + 1));
                        System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + ((s1 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s1).getFinalLpSize() : 0) +
                                " ; " + s2.getClass().getSimpleName() + " : " + ((s2 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s2).getFinalLpSize() : 0));
                        System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + " ; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
                        System.out.println("Number of not converged = " + notConvergedSeeds.size());
                    }
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
        System.out.println("# of seed: " + (maxseed));
        for (Integer depth : issOutside.keySet())
            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutside.get(depth).toString());
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (maxseed) + " ; " + restrictedGameRatioII / (maxseed));

    }

    public static void runFlipIt(String[] args) {
        int startingSeed = 0; // 113, 117, 135, 157, 168
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
                if(args.length > 5){
                    startingSeed = Integer.parseInt(args[5]);
                    maxseed = startingSeed;
                }

            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioII = 0.0;
        double restrictedGameRatioI = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        double minRGSize = Double.POSITIVE_INFINITY;

        for (int seed = startingSeed; seed < startingSeed + maxseed; seed++) {

            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            rootState = initGame(gameInfo, seed);


            GenSumSequenceFormConfig algConfig = new StackelbergConfig(rootState);
            FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

            double fullGameGV;
            double oracleGameGV;

            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            Solver s1;
            if (pureStrategies) {
                s1 = new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s1);
            } else {
//                algConfig = new LeaderGenerationConfig(rootState);
//                expander = new FlipItExpander<>(algConfig);
//                runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                s1 = new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s1);
            }
            double rgSize = runner.getRestrictedGameRatio();
            restrictedGameRatioI += rgSize;
            if (rgSize < minRGSize) minRGSize = rgSize;
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//                s1 = null;

            algConfig = new StackelbergConfig(rootState);
            runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
//            new GambitEFG().write("flipItConsistency.gbt", rootState, expander);
            Solver s2;
            if (pureStrategies) {
                s2 = new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s2);
            } else {
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                s2 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                runner.generate(rootState.getAllPlayers()[LEADER], s2);
//                mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));
            }
            if (checkUtilitiesInIss((StackelbergConfig) algConfig, rootState.getAllPlayers()[1-LEADER], rootState.getAllPlayers()[LEADER])) {
                System.out.println("Seed with conflicting utility: " + seed);
//                new GambitEFG().write("flipit.gbt", rootState, expander);
            }
            if (checkUtilitiesInIssII((StackelbergConfig) algConfig, rootState.getAllPlayers()[1-LEADER], rootState.getAllPlayers()[LEADER], expander,((LeaderTLSefceLP)s1).getISSizes(), ((LeaderTLSefceLP)s1).getExpanders())) {
                System.out.println("Seed with reversed utility order: " + seed);
//                System.exit(0);
//                new GambitEFG().write("flipit_inversed.gbt", rootState, expander);
            }
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
            restrictedGameRatioII += runner.getRestrictedGameRatio();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed - startingSeed + 1) + " ; " + restrictedGameRatioII / (seed - startingSeed + 1));
            System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + ((s1 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s1).getFinalLpSize() : 0) +
                    " ; " + s2.getClass().getSimpleName() + " : " + ((s2 instanceof CompleteTwoPlayerSefceLP) ? ((CompleteTwoPlayerSefceLP) s2).getFinalLpSize() : 0));
            System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + " ; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
        }
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
        System.out.println("# of seed: " + (maxseed));
        for (Integer depth : issOutside.keySet())
            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutside.get(depth).toString());
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (maxseed) + " ; " + restrictedGameRatioII / (maxseed));
        System.out.println("Min RG size = " + minRGSize);

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

    protected static boolean checkUtilitiesInIss(StackelbergConfig config, Player follower, Player leader){
        HashMap<Sequence, HashMap<Object,double[]>> utils = new HashMap<>();
        for (GameState state : config.getAllLeafs()){
            if (!utils.containsKey(state.getSequenceFor(follower))){
                utils.put(state.getSequenceFor(follower),new HashMap<>());
                utils.get(state.getSequenceFor(follower)).put(((FlipItAction)state.getSequenceFor(leader).getLast()).getControlNode(), state.getUtilities());
                continue;
            }
            if (!utils.get(state.getSequenceFor(follower)).containsKey(((FlipItAction)state.getSequenceFor(leader).getLast()).getControlNode())) {
                utils.get(state.getSequenceFor(follower)).put(((FlipItAction) state.getSequenceFor(leader).getLast()).getControlNode(), state.getUtilities());
                continue;
            }
            Object node = ((FlipItAction)state.getSequenceFor(leader).getLast()).getControlNode();
            if (Math.abs(state.getUtilities()[follower.getId()] - utils.get(state.getSequenceFor(follower)).get(node)[follower.getId()]) > 0.01) {
                System.out.println(state.getUtilities()[follower.getId()] + " " + utils.get(state.getSequenceFor(follower)).get(node)[follower.getId()]);
                return true;
            }
//            for (Object set : utils.get(state.getSequenceFor(follower)).keySet()){
//                if (!set.equals(((FlipItAction)state.getSequenceFor(leader).getLast()).getControlNode())) continue;
//                if (Math.abs(state.getUtilities()[follower.getId()] - utils.get(state.getSequenceFor(follower)).get(set)[follower.getId()]) > 0.01) {
//                    System.out.println(state.getUtilities()[follower.getId()] + " " + utils.get(state.getSequenceFor(follower)).get(set)[follower.getId()]);
//                    return true;
//                }
//            }
        }
        return  false;
    }

    protected static boolean checkUtilitiesInIssII(StackelbergConfig config, Player follower, Player leader, Expander<SequenceInformationSet> expander, HashMap<ISKey, Integer> isSizes, ArrayList<Expander<SequenceInformationSet>> expanders){
        HashSet<SequenceInformationSet> sets = new HashSet<>();
        HashMap<Object, ArrayList<Double[]>> utils;// = new HashMap<>();
        boolean hasInversedOrder = false;
        int idxx = 0;
        for (GameState leaf : config.getAllLeafs()){
            if (sets.contains(leaf.getSequenceFor(follower).getLastInformationSet())) continue;
            sets.add((SequenceInformationSet) leaf.getSequenceFor(follower).getLastInformationSet());
            utils = new HashMap<>();
            for (GameState state : leaf.getSequenceFor(follower).getLastInformationSet().getAllStates()){
                if (!utils.containsKey(((FlipItAction)state.getSequenceFor(leader).getLast()).getControlNode()))
                    utils.put(((FlipItAction)state.getSequenceFor(leader).getLast()).getControlNode(), new ArrayList<>());
                Double[] u = new Double[expander.getActions(state).size()];
                int idx = 0;
                for (Action a : expander.getActions(state)){
                    u[idx] = state.performAction(a).getUtilities()[leader.getId()];
                    idx++;
                }
                utils.get(((FlipItAction)state.getSequenceFor(leader).getLast()).getControlNode()).add(u);
            }
            // check the same order
            boolean setIsUnordered = false;
            for(Object key : utils.keySet()){
                Double[] firstUtilities = utils.get(key).get(0);
                int[] order = IntStream.range(0, firstUtilities.length)
                        .boxed().sorted((i, j) -> firstUtilities[i].compareTo(firstUtilities[j]) )
                        .mapToInt(ele -> ele).toArray();
                for (Double[] u : utils.get(key)){
                    int[] orderU = IntStream.range(0, u.length)
                            .boxed().sorted((i, j) -> u[i].compareTo(u[j]) )
                            .mapToInt(ele -> ele).toArray();
                    if (!Arrays.equals(order, orderU)) {
                        System.out.println(Arrays.toString(order) + " : " + Arrays.toString(firstUtilities));
                        System.out.println(Arrays.toString(orderU) + " : " + Arrays.toString(u));
                        int isCompleteSize = leaf.getSequenceFor(follower).getLastInformationSet().getAllStates().size();
                        System.out.println(isCompleteSize);
                        if(isSizes.containsKey(((SequenceInformationSet)leaf.getSequenceFor(follower).getLastInformationSet()).getISKey())) {
                            int isPartialSize = isSizes.get(((SequenceInformationSet) leaf.getSequenceFor(follower).getLastInformationSet()).getISKey());
                            System.out.println(isPartialSize);
                            if (isPartialSize < isCompleteSize)
                                System.out.println("Partially in RG!");

                        }
                        else {
                            System.out.println(0);
                            System.out.println("Not in RG at all!");
                        }
                        System.out.println();
                        hasInversedOrder = true;
                        setIsUnordered = true;
                        for (int i = 0; i < expanders.size(); i++) {
                            new GambitEFG().writeToReachIS("flipit_inversed_" + idxx + "_" + i + ".gbt", config.getRootState(), expanders.get(i), (SequenceInformationSet) leaf.getSequenceFor(follower).getLastInformationSet());
                        }
                        new GambitEFG().writeToReachIS("flipit_inversed_" + idxx + "_" + expanders.size() + ".gbt", config.getRootState(), expander, (SequenceInformationSet) leaf.getSequenceFor(follower).getLastInformationSet());
                        idxx++;
                        break;
                    }
                }
                if (setIsUnordered) break;
            }
        }
        return  hasInversedOrder;
    }


}
