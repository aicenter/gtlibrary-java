package cz.agents.gtlibrary.algorithms.stackelberg.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.SefceRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.CompleteSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.iterative.LeaderGenerationSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.LeaderGenerationTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.SumForbiddingStackelbergLP;
import cz.agents.gtlibrary.algorithms.stackelberg.oracle.DoubleOracle2pSumForbiddingLP;
import cz.agents.gtlibrary.algorithms.stackelberg.oracle.LeaderOracle2pSumForbiddingLP;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jakub Cerny on 20/09/2017.
 */
public class OracleVsCompleteBBConsistencyExperiment {

    static int LEADER = 0;
    final static int depth = 3;
//    static boolean pureStrategies = false;

    public static void main(String[] args) {
        if (args.length == 0) {
//            runGenSumRandom(new String[]{"R", "3", "3", "30"});
//            runGenSumRandomImproved(new String[]{"I", "7", "5", "8", "1", "0", "-0.2"});
//            runGenSumRandomImprovedOneSeed(new String[]{"I", "7", "6", "-0.8", "1"});
//            runBPGOneSeed(new String[]{"B", "4", "0"});
//            runGenSumRandomOneSeed(new String[]{"R", "3", "3"}, 21);
//        runGenSumRandomImproved();
//        runBPG(depth);
//        runFlipIt(args);
//            runFlipIt(new String[]{"F", "2", "5", "AP"});
        } else {
            switch (args[0]) {
                case "F":
                    runFlipIt(args);
                    break;
//                case "R":
//                    runGenSumRandom(args);
//                    break;
                case "I":
                    runGenSumRandomImproved(args);
                    break;
                case "IO":
                    runGenSumRandomImprovedOneSeed(args);
                    break;
                case "B":
                    runBPGOneSeed(args);
                    break;

            }
        }
    }

    public static void runGenSumRandomImproved(String[] args) {
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int maxseed = Integer.parseInt(args[3]);
        boolean runComplete = true;
        double correlation = -0.8;
        if (args.length > 4) runComplete = (Integer.parseInt(args[4]) == 1);
        if (args.length > 5) LEADER = Integer.parseInt((args[5]));
        if (args.length > 6) correlation = Double.parseDouble(args[6]);
        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;


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

            double fullGameGV = 0.0;
            double oracleGameGV = 0.0;

            StackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            if (runComplete) {
                runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
                runner.generate(rootState.getAllPlayers()[LEADER], new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo));

                fullGameGV = runner.getGameValue();
            }
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

            algConfig = new StackelbergConfig(rootState);
            expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
//            LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, true);
            DoubleOracle2pSumForbiddingLP oracle = new DoubleOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, true);
            runner.generate(rootState.getAllPlayers()[LEADER], oracle);
            restrictedGameRatio += oracle.getRestrictedGameRatio();
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

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

    public static void runFlipIt(String[] args) {
        LEADER = 0;
        boolean runComplete = true;
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
//                maxseed = Integer.parseInt(args[4]);

            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState = null;

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        long matrixOracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();
        ArrayList<Long> matrixOracleTimes = new ArrayList<>();

//        for (int seed = 0; seed < maxseed; seed++) {
        System.out.println();
//            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));

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
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        double fullGameGV = 0.0;
        double oracleGameGV = 0.0;
        double matrixOracleGameGV = 0.0;

        StackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        SumForbiddingStackelbergLP bnb = null;
        if (runComplete) {
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            bnb = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], bnb);
//            runner.ge
            fullGameGV = runner.getGameValue();
        }
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime);

        algConfig = new StackelbergConfig(rootState);
        expander = new FlipItExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP matrixOracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, false);
        runner.generate(rootState.getAllPlayers()[LEADER], matrixOracle);
//        restrictedGameRatio += oracle.getRestrictedGameRatio();
        matrixOracleGameGV = runner.getGameValue();
        matrixOracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        matrixOracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime + "; sequential oracle time: " + matrixOracleTime);


        algConfig = new StackelbergConfig(rootState);
        expander = new FlipItExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, true);
        runner.generate(rootState.getAllPlayers()[LEADER], oracle);
        restrictedGameRatio += oracle.getRestrictedGameRatio();
        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001 || Math.abs(matrixOracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(0);


//            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
        System.out.println("Full game time = " + fullTime + " ; sequential oracle time = " + oracleTime + " ; sequential matrix oracle time = " + matrixOracleTime);
        System.out.println("Not converged = " + (notConvergedSeeds.size() > 0));
        System.out.println("Full GV: " + fullGameGV + "; sim. matrix GV: " + oracleGameGV + "; seq. matrix GV: " + matrixOracleGameGV);
//        }
//        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());

//        ArrayList<Long> times = new ArrayList<>();
//        for (int i = 0; i < fullTimes.size(); i++)
//            times.add(fullTimes.get(i) - oracleTimes.get(i));
//        System.out.println("Min = " + Collections.min(times));
//        System.out.println("Max = " + Collections.max(times));
//        Collections.sort(times);
//        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));

        System.out.println("Cplex building time (BnB / SO / SMO): " + ((bnb!=null) ? bnb.getOverallConstraintGenerationTime()/1000000l : 0) + " / " + oracle.getOverallConstraintGenerationTime()/1000000l + " / " + matrixOracle.getOverallConstraintGenerationTime()/1000000l);
        System.out.println("Cplex solving time (BnB / SO / SMO): " + ((bnb!=null) ? bnb.getOverallConstraintLPSolvingTime()/1000000l : 0) + " / " + oracle.getOverallConstraintLPSolvingTime()/1000000l + " / " + matrixOracle.getOverallConstraintLPSolvingTime()/1000000l);
        System.out.println("Branching count (SO / SMO): " + oracle.getBnbBranchingCount() + " / " + matrixOracle.getBnbBranchingCount());
        System.out.println("RG resizing count (SO / SMO): " + oracle.getGenerationCount() + " / " + matrixOracle.getGenerationCount());
        System.out.println("RG size: " + oracle.getRestrictedGameRatio());
        System.out.println("RG generation time (SO / SMO): " + oracle.getRestrictedGameGenerationTime()/ 1000000l + " / " + matrixOracle.getRestrictedGameGenerationTime()/ 1000000l);
        System.out.println("Deviation identification time (SO / SMO): " + oracle.getDeviationIdentificationTime()/ 1000000l + " / " + matrixOracle.getDeviationIdentificationTime()/ 1000000l);
        System.out.println("Broken strategy identification time (SO / SMO): " + oracle.getBrokenStrategyIdentificationTime()/ 1000000l + " / " + matrixOracle.getBrokenStrategyIdentificationTime()/ 1000000l);
        System.out.println("Number of ISs: " + algConfig.getAllInformationSets().size());

    }

    public static void runBPGOneSeed(String[] args) {
//        boolean runComplete = true;
        LEADER = Integer.parseInt(args[2]);
        int depth = Integer.parseInt(args[1]);
        boolean runComplete = true;
        if (args.length > 4) runComplete = (Integer.parseInt(args[4]) == 1);
        GameInfo gameInfo = new BPGGameInfo();
        GameState rootState = new GenSumBPGGameState();
        ((BPGGameInfo)gameInfo).DEPTH = depth;

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        long matrixOracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();
        ArrayList<Long> matrixOracleTimes = new ArrayList<>();

//        for (int seed = 0; seed < maxseed; seed++) {
        System.out.println();
//            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));


//        gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, 10, correlation);
//        rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);

        double fullGameGV = 0.0;
        double oracleGameGV = 0.0;
        double matrixOracleGameGV = 0.0;

        StackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        SumForbiddingStackelbergLP bnb = null;
        long bnbcgtime = 0;
        long bnbstime = 0;
        if (runComplete) {
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            bnb = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], bnb);
//            runner.ge
            fullGameGV = runner.getGameValue();
            bnbcgtime = bnb.getOverallConstraintGenerationTime()/1000000l;
            bnbstime = bnb.getOverallConstraintLPSolvingTime()/1000000l;
            bnb = null;
            runner = null;
        }
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime);

        algConfig = new StackelbergConfig(rootState);
        expander = new BPGExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP matrixOracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, false);
        runner.generate(rootState.getAllPlayers()[LEADER], matrixOracle);
//        restrictedGameRatio += oracle.getRestrictedGameRatio();
        matrixOracleGameGV = runner.getGameValue();
        matrixOracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        matrixOracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime + "; sequential oracle time: " + matrixOracleTime);


        algConfig = new StackelbergConfig(rootState);
        expander = new BPGExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, true);
        runner.generate(rootState.getAllPlayers()[LEADER], oracle);
        restrictedGameRatio += oracle.getRestrictedGameRatio();
        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001 || Math.abs(matrixOracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(0);


//            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
        System.out.println("Full game time = " + fullTime + " ; sequential oracle time = " + oracleTime + " ; sequential matrix oracle time = " + matrixOracleTime);
        System.out.println("Not converged = " + (notConvergedSeeds.size() > 0));
        System.out.println("Full GV: " + fullGameGV + "; sim. matrix GV: " + oracleGameGV + "; seq. matrix GV: " + matrixOracleGameGV);
//        }
//        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());

//        ArrayList<Long> times = new ArrayList<>();
//        for (int i = 0; i < fullTimes.size(); i++)
//            times.add(fullTimes.get(i) - oracleTimes.get(i));
//        System.out.println("Min = " + Collections.min(times));
//        System.out.println("Max = " + Collections.max(times));
//        Collections.sort(times);
//        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));

        System.out.println("Cplex building time (BnB / SO / SMO): " + bnbcgtime + " / " + oracle.getOverallConstraintGenerationTime()/1000000l + " / " + matrixOracle.getOverallConstraintGenerationTime()/1000000l);
        System.out.println("Cplex solving time (BnB / SO / SMO): " + bnbstime + " / " + oracle.getOverallConstraintLPSolvingTime()/1000000l + " / " + matrixOracle.getOverallConstraintLPSolvingTime()/1000000l);
        System.out.println("Branching count (SO / SMO): " + oracle.getBnbBranchingCount() + " / " + matrixOracle.getBnbBranchingCount());
        System.out.println("RG resizing count (SO / SMO): " + oracle.getGenerationCount() + " / " + matrixOracle.getGenerationCount());
        System.out.println("RG size: " + oracle.getRestrictedGameRatio() + " / " + matrixOracle.getRestrictedGameRatio());
        System.out.println("RG generation time (SO / SMO): " + oracle.getRestrictedGameGenerationTime()/ 1000000l + " / " + matrixOracle.getRestrictedGameGenerationTime()/ 1000000l);
        System.out.println("Deviation identification time (SO / SMO): " + oracle.getDeviationIdentificationTime()/ 1000000l + " / " + matrixOracle.getDeviationIdentificationTime()/ 1000000l);
        System.out.println("Broken strategy identification time (SO / SMO): " + oracle.getBrokenStrategyIdentificationTime()/ 1000000l + " / " + matrixOracle.getBrokenStrategyIdentificationTime()/ 1000000l);
        System.out.println("Number of ISs: " + algConfig.getAllInformationSets().size());

    }

    public static void runGenSumRandomImprovedOneSeed(String[] args) {
//        boolean runComplete = true;
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        double correlation = Double.parseDouble(args[3]);
//        int maxseed = Integer.parseInt(args[3]);
        boolean runComplete = true;
        if (args.length > 4) runComplete = (Integer.parseInt(args[4]) == 1);
        if (args.length > 5) LEADER = (Integer.parseInt(args[5]));
        GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        long matrixOracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();
        ArrayList<Long> matrixOracleTimes = new ArrayList<>();

//        for (int seed = 0; seed < maxseed; seed++) {
        System.out.println();
//            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));


        gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, 10, correlation);
        rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);

        double fullGameGV = 0.0;
        double oracleGameGV = 0.0;
        double matrixOracleGameGV = 0.0;

        StackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        SumForbiddingStackelbergLP bnb = null;
        long bnbcgtime = 0;
        long bnbstime = 0;
        if (runComplete) {
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            bnb = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], bnb);
//            runner.ge
            fullGameGV = runner.getGameValue();
            bnbcgtime = bnb.getOverallConstraintGenerationTime()/1000000l;
            bnbstime = bnb.getOverallConstraintLPSolvingTime()/1000000l;
            bnb = null;
        }
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime);

        algConfig = new StackelbergConfig(rootState);
        expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP matrixOracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, false);
        runner.generate(rootState.getAllPlayers()[LEADER], matrixOracle);
//        restrictedGameRatio += oracle.getRestrictedGameRatio();
        matrixOracleGameGV = runner.getGameValue();
        matrixOracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        matrixOracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime + "; sequential oracle time: " + matrixOracleTime);


        algConfig = new StackelbergConfig(rootState);
        expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true, true);
        runner.generate(rootState.getAllPlayers()[LEADER], oracle);
        restrictedGameRatio += oracle.getRestrictedGameRatio();
        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001 || Math.abs(matrixOracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(0);


//            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
        System.out.println("Full game time = " + fullTime + " ; sequential oracle time = " + oracleTime + " ; sequential matrix oracle time = " + matrixOracleTime);
        System.out.println("Not converged = " + (notConvergedSeeds.size() > 0));
        System.out.println("Full GV: " + fullGameGV + "; sim. matrix GV: " + oracleGameGV + "; seq. matrix GV: " + matrixOracleGameGV);
//        }
//        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());

//        ArrayList<Long> times = new ArrayList<>();
//        for (int i = 0; i < fullTimes.size(); i++)
//            times.add(fullTimes.get(i) - oracleTimes.get(i));
//        System.out.println("Min = " + Collections.min(times));
//        System.out.println("Max = " + Collections.max(times));
//        Collections.sort(times);
//        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));

        System.out.println("Cplex building time (BnB / SO / SMO): " + bnbcgtime + " / " + oracle.getOverallConstraintGenerationTime()/1000000l + " / " + matrixOracle.getOverallConstraintGenerationTime()/1000000l);
        System.out.println("Cplex solving time (BnB / SO / SMO): " + bnbstime + " / " + oracle.getOverallConstraintLPSolvingTime()/1000000l + " / " + matrixOracle.getOverallConstraintLPSolvingTime()/1000000l);
        System.out.println("Branching count (SO / SMO): " + oracle.getBnbBranchingCount() + " / " + matrixOracle.getBnbBranchingCount());
        System.out.println("RG resizing count (SO / SMO): " + oracle.getGenerationCount() + " / " + matrixOracle.getGenerationCount());
        System.out.println("RG size: " + oracle.getRestrictedGameRatio() + " / " + matrixOracle.getRestrictedGameRatio());
        System.out.println("RG generation time (SO / SMO): " + oracle.getRestrictedGameGenerationTime()/ 1000000l + " / " + matrixOracle.getRestrictedGameGenerationTime()/ 1000000l);
        System.out.println("Deviation identification time (SO / SMO): " + oracle.getDeviationIdentificationTime()/ 1000000l + " / " + matrixOracle.getDeviationIdentificationTime()/ 1000000l);
        System.out.println("Broken strategy identification time (SO / SMO): " + oracle.getBrokenStrategyIdentificationTime()/ 1000000l + " / " + matrixOracle.getBrokenStrategyIdentificationTime()/ 1000000l);
        System.out.println("Number of ISs: " + algConfig.getAllInformationSets().size());

    }

    public static void runFlipItLessMemory(String[] args) {
        boolean runComplete = true;
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
//                maxseed = Integer.parseInt(args[4]);

            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState = null;

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        long matrixOracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();
        ArrayList<Long> matrixOracleTimes = new ArrayList<>();

//        for (int seed = 0; seed < maxseed; seed++) {
        System.out.println();
//            System.out.println("Running seed " + (seed) + " of " + (maxseed-1));

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
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        double fullGameGV = 0.0;
        double oracleGameGV = 0.0;
        double matrixOracleGameGV = 0.0;

        StackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        SumForbiddingStackelbergLP bnb = null;
        if (runComplete) {
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            bnb = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], bnb);
//            runner.ge
            fullGameGV = runner.getGameValue();
        }
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime);

        algConfig = new StackelbergConfig(rootState);
        expander = new FlipItExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP matrixOracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
        runner.generate(rootState.getAllPlayers()[LEADER], matrixOracle);
//        restrictedGameRatio += oracle.getRestrictedGameRatio();
        matrixOracleGameGV = runner.getGameValue();
        matrixOracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        matrixOracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        System.out.println("Full time: " + fullTime + "; sequential oracle time: " + matrixOracleTime);


        algConfig = new StackelbergConfig(rootState);
        expander = new FlipItExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
        runner.generate(rootState.getAllPlayers()[LEADER], oracle);
        restrictedGameRatio += oracle.getRestrictedGameRatio();
        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001 || Math.abs(matrixOracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(0);


//            System.out.println("Average restricted game ratio = " + restrictedGameRatio/(seed+1));
        System.out.println("Full game time = " + fullTime + " ; sequential oracle time = " + oracleTime + " ; sequential matrix oracle time = " + matrixOracleTime);
        System.out.println("Not converged = " + (notConvergedSeeds.size() > 0));
        System.out.println("Full GV: " + fullGameGV + "; sim. matrix GV: " + oracleGameGV + "; seq. matrix GV: " + matrixOracleGameGV);
//        }
//        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());

//        ArrayList<Long> times = new ArrayList<>();
//        for (int i = 0; i < fullTimes.size(); i++)
//            times.add(fullTimes.get(i) - oracleTimes.get(i));
//        System.out.println("Min = " + Collections.min(times));
//        System.out.println("Max = " + Collections.max(times));
//        Collections.sort(times);
//        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));

        System.out.println("Cplex building time (BnB / SO / SMO): " + ((bnb!=null) ? bnb.getOverallConstraintGenerationTime()/1000000l : 0) + " / " + oracle.getOverallConstraintGenerationTime()/1000000l + " / " + matrixOracle.getOverallConstraintGenerationTime()/1000000l);
        System.out.println("Cplex solving time (BnB / SO / SMO): " + ((bnb!=null) ? bnb.getOverallConstraintLPSolvingTime()/1000000l : 0) + " / " + oracle.getOverallConstraintLPSolvingTime()/1000000l + " / " + matrixOracle.getOverallConstraintLPSolvingTime()/1000000l);
        System.out.println("Branching count (SO / SMO): " + oracle.getBnbBranchingCount() + " / " + matrixOracle.getBnbBranchingCount());
        System.out.println("RG resizing count (SO / SMO): " + oracle.getGenerationCount() + " / " + matrixOracle.getGenerationCount());
        System.out.println("RG size: " + oracle.getRestrictedGameRatio());
        System.out.println("RG generation time (SO / SMO): " + oracle.getRestrictedGameGenerationTime()/ 1000000l + " / " + matrixOracle.getRestrictedGameGenerationTime()/ 1000000l);
        System.out.println("Deviation identification time (SO / SMO): " + oracle.getDeviationIdentificationTime()/ 1000000l + " / " + matrixOracle.getDeviationIdentificationTime()/ 1000000l);
        System.out.println("Broken strategy identification time (SO / SMO): " + oracle.getBrokenStrategyIdentificationTime()/ 1000000l + " / " + matrixOracle.getBrokenStrategyIdentificationTime()/ 1000000l);
        System.out.println("Number of ISs: " + algConfig.getAllInformationSets().size());

    }

    public static GameState initGame(FlipItGameInfo gameInfo, int seed) {
        gameInfo.ZERO_SUM_APPROX = false;

        int rounding = 3;
        final double MAX_COST = 10, MAX_REWARD = 10;//MAX_COST;
        int numberOfNodes = Integer.parseInt(gameInfo.graphFile.substring(gameInfo.graphFile.length() - 5, gameInfo.graphFile.length() - 4));
//        System.out.println(numberOfNodes);
        HighQualityRandom random = new HighQualityRandom(seed);
        double[] costs = new double[numberOfNodes];
        double[] rewards = new double[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            costs[i] = Math.round(((int) Math.pow(10, rounding)) * MAX_COST * random.nextDouble()) / Math.pow(10, rounding);
            rewards[i] = Math.round(((int) Math.pow(10, rounding)) * MAX_REWARD * random.nextDouble()) / Math.pow(10, rounding);
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
        return rootState;
    }


}
