package cz.agents.gtlibrary.algorithms.stackelberg.experiments;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.tables.GadgetLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.SumForbiddingStackelbergLP;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergMultipleLPs;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergSequenceFormMultipleLPs;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.siterator.RandomlySamplingIterator;
import cz.agents.gtlibrary.algorithms.stackelberg.oracle.GadgetOracle2pShallowestAllCplexLP;
import cz.agents.gtlibrary.algorithms.stackelberg.oracle.GadgetOracle2pShallowestBrokenCplexLP;
import cz.agents.gtlibrary.algorithms.stackelberg.oracle.GadgetOracle2pSumForbiddingLP;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.pursuit.GenSumVisibilityPursuitGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Graph;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Jakub Cerny on 20/09/2017.
 */
public class OracleVsCompleteBBExperiment {

    static int LEADER = 0;
    final static int depth = 3;
//    static boolean pureStrategies = false;

    // algorithm (O (LP / BMILP / AMILP) / F)
    // FLIP IT: FO, depth, size, version (F / AP / NP/ N), seed, algorithm, solving method
    // Pursuit: PO, gridsize, depth, seed, algorithm, solving method, discount gadgets, delta, projection, local utility
    // Random: RO, depth, bf, seed, correlation, observations, algorithm, solving method, discount, eps, delta, projection, localuti
    // BPG: BO, depth, leader (int), algorithm, solving method
    public static void main(String[] args) {
        if (args.length == 0) {
//            runGenSumRandom(new String[]{"R", "6", "2", "20"});
//            runGenSumRandomImproved(new String[]{"I", "5", "4", "1", "1"});
//            runGenSumRandomOneSeed(new String[]{"RO", "4", "4", "5","-0.7", "4", "O", "LP", "4", "0", "1e-4", "0.4", "0", "1"});
//        runGenSumRandomImproved();
//            runBPG(new String[]{"B", "4", "1"});
//            runBPGOneSeed(new String[]{"B", "3", "1", "O", "LP", ""+ IloCplex.Algorithm.Barrier, "LP", "0"});
//        runFlipIt(args);
//        runFlipIt(new String[]{"F", "4", "2", "N", "1"});
//            for (int seed = 50; seed < 80; seed++)
//                runFlipItOneSeed(new String[]{"F", "3", "3", "AP", Integer.toString(seed), "F"});
//            runFlipItOneSeed(new String[]{"F", "3", "5", "AP", "128", "O" ,"LP", "4", "0", Double.toString(1e-12)});
//            runPursuit(new String[]{"P", "3", "4", "10"});
//            runPursuitOneSeed(new String[]{"PO", "4", "3", "2", "O", "LP", "4", "1", "0.3", "1", "0", "1e-4", "0.1"});
//            runFlipItOneSeed(new String[]{"F0", "4", "4b", "AP", "0", "O", "LP", "4", "0", Double.toString(1e-6), "0.2", "1", "0"});
//            runFlipItOneSeed(new String[]{"F0", "5", "5c", "AP", "13", "O", "LP", "4", "0", Double.toString(1e-3), "0.4", "1", "0"});
//            runFlipItOneSeed(new String[]{"F0", "5", "5c", "AP", "3", "O", "LP", "4", "0", Double.toString(1e-6), "0.45", "0", "1"});
//            runFlipItOneSeed(new String[]{"F0", "5", "5c", "AP", "3", "O", "LP", "4", "1", Double.toString(1e-6), "0.45", "0", "1"});
//            runFlipItOneSeed(new String[]{"F0", "4", "4d", "AP", "1", "O", "LP", "4", "1", Double.toString(1e-5), "0.3", "0", "1", "0.01"});
//            runFlipItOneSeed(new String[]{"F0", "3", "4e", "AP", "1", "F", "4", "1", Double.toString(1e-5), "0.3", "0", "1", "0.01"});
//            runFlipItSchemataOneSeed(new String[]{"F0S", "3", "5a", "AP", "1", "8"});

//            runFlipItSchemataOneSeedEmpty(new String[]{"F0SE", "3", "2", "AP", "10", "2"});
//            runFlipItSchemataOneSeedEmpty(new String[]{"F0SE", "3", "2", "AP", "10", "2"});
//            runFlipItRandomOneSeedEmpty(new String[]{"F0SE", "3", "3", "AP", "10", "4", String.valueOf(200/70000.00), "0", "1", "1"});
//            runFlipItSchemataOneSeed(new String[]{"F0SE", "5", "2", "AP", "1", "3"});
//            runFlipItOneSeed(new String[]{"F0", "3", "4", "AP", "1", "F", "4", "1", Double.toString(1e-5), "0.3", "0", "1", "0.01"});

//            runFlipItOneSeed(new String[]{"F0", "4", "3a", "AP", "1", "F", "0"});
//            runFlipItMLPOneSeed(new String[]{"F0MLP", "3", "3", "AP", "30", "1", "1", "2"});
//            runFlipItFullOneSeed(new String[]{"F0S", "3", "3", "AP", "30", "1", "1", "2"});
//            runFlipItRandomOneSeed(new String[]{"F0S", "3", "3", "AP", "30", "1", "1", "3", "1", "8", "0.1"});
            runFlipItSchemataOneSeed(new String[]{"F0S", "4", "2", "AP", "30", "1", "1", "2", "1"});

            String[] setting = new String[6];
            setting[1] = "34"; setting[2] = "3"; setting[3] = "1"; setting[4] = "35";
            setting[5] = "2"; // schema size
//            runPursuitSchemataOneSeed(setting);
            setting[5] = "F"; // full solving
//            runPursuitOneSeed(setting);

//            runBPGSchemataOneSeed(new String[]{"BOS", "4", "0", "2"});
//            runBPGRandomOneSeed(new String[]{"BOR", "4", "0", "20", "0.2", "1", "35"});

            /*
            int numseeds = 20;
            int startseed = 30;
            double[] gvs = new double[numseeds];
            for (int seed = startseed; seed < numseeds+startseed; seed++)
                gvs[seed-startseed] = runFlipItOneSeed(new String[]{"F0", "5", "2", "AP", String.valueOf(seed), "F", "0"});

            System.out.println(Arrays.toString(gvs));
            */

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
                case "P":
                    runPursuit(args);
                    break;
                case "FO":
                    runFlipItOneSeed(args);
                    break;
                case "FOF":
                    runFlipItFullOneSeed(args);
                    break;
                case "FORE":
                    runFlipItRandomOneSeedEmpty(args);
                    break;
                case "FOR":
                    runFlipItRandomOneSeed(args);
                    break;
                case "FOMLP":
                    runFlipItMLPOneSeed(args);
                    break;
                case "FOS":
                    runFlipItSchemataOneSeed(args);
                    break;
                case "PO":
                    runPursuitOneSeed(args);
                    break;
                case "POS":
                    runPursuitSchemataOneSeed(args);
                    break;
                case "POR":
                    runPursuitRandomOneSeed(args);
                    break;
                case "RO":
                    runGenSumRandomOneSeed(args);
                    break;
                case "BO":
                    runBPGOneSeed(args);
                    break;
                case "BOS":
                    runBPGSchemataOneSeed(args);
                    break;
                case "BOR":
                    runBPGRandomOneSeed(args);
                    break;

            }
        }
    }

    public static void runBPGSchemataOneSeed(String[] args) {
        int depth = Integer.parseInt(args[1]);
        boolean useSlow = Integer.parseInt(args[2]) > 0;
        int schemaSize = Integer.parseInt(args[3]);
        LEADER = 1;

        BPGGameInfo gameInfo = new BPGGameInfo();
        BPGGameInfo.DEPTH = depth;
        BPGGameInfo.SLOW_MOVES = useSlow;
        GameState rootState = new GenSumBPGGameState();

        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        BPGExpander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        algConfig.USE_SCHEMATA = true;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.MAX_SCHEMA_SIZE = schemaSize;

        runner.generate(rootState.getAllPlayers()[LEADER], new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[LEADER], rootState.getAllPlayers()[1-LEADER], gameInfo, expander)).getLeft();
//        runner.generate(rootState.getAllPlayers()[LEADER], new StackelbergMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[LEADER], rootState.getAllPlayers()[1-LEADER], gameInfo, expander)).getLeft();
    }

    public static void runBPGRandomOneSeed(String[] args) {
        int depth = Integer.parseInt(args[1]);
        boolean useSlow = Integer.parseInt(args[2]) > 0;
        int rpCount = Integer.parseInt(args[3]);
        double samplingProb = Double.parseDouble(args[4]);
        boolean isLog = Integer.parseInt(args[5]) > 0;
        int seed = Integer.parseInt(args[6]);
        LEADER = 1;

        BPGGameInfo gameInfo = new BPGGameInfo();
        BPGGameInfo.DEPTH = depth;
        BPGGameInfo.SLOW_MOVES = useSlow;
        GameState rootState = new GenSumBPGGameState();

        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        BPGExpander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        algConfig.USE_SCHEMATA = false;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.USE_RANDOM = true;
        algConfig.MAX_RPS = rpCount;
        algConfig.RPS_PROB = samplingProb;

        RandomlySamplingIterator.USE_LOG = isLog;
        RandomlySamplingIterator.SEED = seed;

        StackelbergSequenceFormMultipleLPs mLPs = new StackelbergSequenceFormMultipleLPs(
//        StackelbergMultipleLPs mLPs = new StackelbergMultipleLPs(
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[LEADER],
                rootState.getAllPlayers()[1-LEADER], gameInfo, expander);
//        mLPs.CHECK_FEASIBILITY = false;

        RandomlySamplingIterator.USE_LOG = isLog;
        RandomlySamplingIterator.SEED = seed;
        StackelbergRunner.COUNT_PRUNED = false;

        runner.generate(rootState.getAllPlayers()[LEADER], mLPs);
    }

    public static void runBPGOneSeed(String[] args) {
        int depth = Integer.parseInt(args[1]);
        LEADER = Integer.parseInt(args[2]);
        String algVersion = args[3];

        BPGGameInfo gameInfo = new BPGGameInfo();
        BPGGameInfo.DEPTH = depth;
        GameState rootState = new GenSumBPGGameState();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        BPGExpander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        if (algVersion.equals("O")) {
            String gadgetType = "LP";
            if (args.length > 4) gadgetType = args[4];
            int lpSolvingAlg = GadgetLPTable.CPLEXALG;
            if(args.length > 5) lpSolvingAlg = Integer.parseInt(args[5]);
            GadgetOracle2pSumForbiddingLP s1 = null;
            switch (gadgetType) {
                case "LP":
                    s1 = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "BMILP":
                    s1 = new GadgetOracle2pShallowestBrokenCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "AMILP":
                    s1 = new GadgetOracle2pShallowestAllCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
            }
            if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
                algConfig = new LeaderGenerationConfig(rootState);
                expander = new BPGExpander<>(algConfig);
                runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
                s1.setLPSolvingMethod(lpSolvingAlg);
            }
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
            fullTime = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            System.out.println("Gadget LP size = " + s1.getFinalLPSize());
            System.out.println(fullTime);
        }

        if (algVersion.equals("F")) {
            int lpSolvingAlg = GadgetLPTable.CPLEXALG;
            if(args.length > 4) lpSolvingAlg = Integer.parseInt(args[4]);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            algConfig = new StackelbergConfig(rootState);
            expander = new BPGExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
//            new GambitEFG().write("flipItConsistency.gbt", rootState, expander);
            SumForbiddingStackelbergLP s2;
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2 = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2.setLPSolvingMethod(lpSolvingAlg);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
            oracleTime = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            System.out.println("Full LP size = " + s2.getFinalLpSize());
            System.out.println(oracleTime);
        }


    }

    public static void runBPG(String[] args) {
        int depth = Integer.parseInt(args[1]);
        LEADER = Integer.parseInt(args[2]);

        BPGGameInfo gameInfo = new BPGGameInfo();
        BPGGameInfo.DEPTH = depth;
        GameState rootState = new GenSumBPGGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioII = 0.0;
        double restrictedGameRatioI = 0.0;
        double averageRawISSize = 0.0;
        double averageISSizeWithoutLeaves = 0.0;
        double averageISSizeWithSingletonLeaves = 0.0;
        double averageSequencesSize = 0.0;
        double averageGadgetDepth = 0.0;
        double averageLPSize = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();
        int seed = 0, startingSeed = 0;


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        BPGExpander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);

        double fullGameGV;
        double oracleGameGV;

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        GadgetOracle2pSumForbiddingLP s1;
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new BayesianGadgetSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
        s1 = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
        if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
            algConfig = new LeaderGenerationConfig(rootState);
            expander = new BPGExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        }
        runner.generate(rootState.getAllPlayers()[LEADER], s1);
//            double rgSize = runner.getRestrictedGameRatio();
//            restrictedGameRatioI += rgSize;
//            if (rgSize < minRGSize) minRGSize = rgSize;
        fullGameGV = runner.getGameValue();
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//                s1 = null;

//            HashMap<ISKey, SequenceInformationSet> iss = algConfig.getAllInformationSets();

        int oracleRawISSize = algConfig.getAllInformationSets().size();
        int oracleISSizeWithoutLeaves = algConfig.getAllInformationSets().size();
        int oracleISSizeWithSingletonLeaves = algConfig.getAllInformationSets().size();
        if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
            oracleISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();
            oracleISSizeWithSingletonLeaves = ((GadgetSefceLP) s1).getRestrictedGameSizeWithSingletonLeaves();
        }
        int sequencesSize = (s1 instanceof GadgetSefceLP) ? ((GadgetSefceLP) s1).getNumberOfSequences() : algConfig.getAllSequences().size();
        averageGadgetDepth += (s1 instanceof GadgetSefceLP) ? ((GadgetSefceLP) s1).getExpectedGadgetDepth() : 0.0;

        int oracleFinalLPSize = 0;
        if (s1 instanceof GadgetSefceLP) {
            oracleFinalLPSize = ((GadgetSefceLP) s1).getFinalLPSize();
        }
//            if (s1 instanceof CompleteTwoPlayerSefceLP){
//                oracleFinalLPSize = ((CompleteTwoPlayerSefceLP)s1).getFinalLpSize();
//            }

        algConfig = new StackelbergConfig(rootState);
        expander = new BPGExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
//            new GambitEFG().write("flipItConsistency.gbt", rootState, expander);
        SumForbiddingStackelbergLP s2;
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
        s2 = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
        runner.generate(rootState.getAllPlayers()[LEADER], s2);
//                mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));

        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatioII += runner.getRestrictedGameRatio();

        int fullFinalLPSize = s2.getFinalLpSize();//fullFinalLPSize = ((CompleteTwoPlayerSefceLP)s2).getFinalLpSize();

//            System.out.println(oracleISSize + " / " + algConfig.getAllInformationSets().size());

        int fullISSizeWithSingletonLeaves = algConfig.getNumberOfISsWithSingletonLeaves();
        int fullISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();

        averageRawISSize += (double) oracleRawISSize / algConfig.getAllInformationSets().size();
        averageISSizeWithoutLeaves += (double) oracleISSizeWithoutLeaves / fullISSizeWithoutLeaves;
        averageISSizeWithSingletonLeaves += (double) oracleISSizeWithSingletonLeaves / fullISSizeWithSingletonLeaves;
        averageSequencesSize += (double) sequencesSize / algConfig.getAllSequences().size();
        averageLPSize += (double) oracleFinalLPSize / fullFinalLPSize;


        if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(depth);

        System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed - startingSeed + 1) + " ; " + restrictedGameRatioII / (seed - startingSeed + 1));
        System.out.println("Average IS size w/ leaves = " + (averageISSizeWithoutLeaves / (seed - startingSeed + 1)));
        System.out.println("Average IS size w 1-leaves = " + (averageISSizeWithSingletonLeaves / (seed - startingSeed + 1)));
        System.out.println("Average raw IS size = " + (averageRawISSize / (seed - startingSeed + 1)));
        System.out.println("Average # of seqs = " + (averageSequencesSize / (seed - startingSeed + 1)));
        System.out.println("Average gadget depth = " + (averageGadgetDepth / (seed - startingSeed + 1)));
        System.out.println("Average LPSize = " + (averageLPSize / (seed - startingSeed + 1)));
        System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + oracleFinalLPSize +
                " ; " + s2.getClass().getSimpleName() + " : " + fullFinalLPSize);
        System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + " ; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
        if (s1 instanceof GadgetSefceLP)
            System.out.println("Gadget making time = " + ((GadgetSefceLP) s1).getOverallGadgetMakingTime() / 1000000l);
        System.out.println("Number of not converged = " + notConvergedSeeds.size());
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
//        for (Integer depth : issOutside.keySet())
//            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutside.get(depth).toString());
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());

    }

    public static void runGenSumRandomImproved(String[] args) {
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int maxseed = Integer.parseInt(args[3]);
        boolean runComplete = true;
        if (args.length > 4) runComplete = (Integer.parseInt(args[4]) == 1);
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
            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
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

            algConfig = new LeaderGenerationConfig(rootState);//StackelbergConfig(rootState);
            expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
//            LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
            GadgetOracle2pSumForbiddingLP oracle = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
//
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

    public static void runGenSumRandomOutdated(String[] args) {
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int maxseed = Integer.parseInt(args[3]);
        boolean runComplete = true;
        if (args.length > 4) runComplete = (Integer.parseInt(args[4]) == 1);
        GameInfo gameInfo = new RandomGameInfo();
        GameState rootState = new RandomGameState();

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
            gameInfo = new RandomGameInfo(seed, depth, bf);
            rootState = new RandomGameState();//cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);

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

            algConfig = new LeaderGenerationConfig(rootState);//StackelbergConfig(rootState);
            expander = new RandomGameExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
//            LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
            GadgetOracle2pSumForbiddingLP oracle = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
//
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

    public static void runGenSumRandom(String[] args) {
        int startingSeed = 0;
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int maxseed = Integer.parseInt(args[3]);
        boolean runComplete = true;
        if (args.length > 4) runComplete = (Integer.parseInt(args[4]) == 1);
        GameInfo gameInfo = new RandomGameInfo();
        GameState rootState = new RandomGameState();

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioII = 0.0;
        double restrictedGameRatioI = 0.0;
        double averageRawISSize = 0.0;
        double averageISSizeWithoutLeaves = 0.0;
        double averageISSizeWithSingletonLeaves = 0.0;
        double averageSequencesSize = 0.0;
        double averageGadgetDepth = 0.0;
        double averageLPSize = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        double minRGSize = Double.POSITIVE_INFINITY;

        for (int seed = startingSeed; seed < startingSeed + maxseed; seed++) {

            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            gameInfo = new RandomGameInfo(seed, depth, bf);
            rootState = new RandomGameState();


            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            RandomGameExpander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);

            double fullGameGV;
            double oracleGameGV;

            StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            GadgetOracle2pSumForbiddingLP s1;
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new BayesianGadgetSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s1 = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
//            s1 = new GadgetOracle2pShallowestBrokenCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
                algConfig = new LeaderGenerationConfig(rootState);
                expander = new RandomGameExpander<>(algConfig);
                runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
                s1.setLPSolvingMethod(4);
                s1.setGadgetDiscount(0.05*gameInfo.getMaxUtility());
                s1.setHullApproximation(0.3, true, false);
            }
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
//            double rgSize = runner.getRestrictedGameRatio();
//            restrictedGameRatioI += rgSize;
//            if (rgSize < minRGSize) minRGSize = rgSize;
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//                s1 = null;

//            HashMap<ISKey, SequenceInformationSet> iss = algConfig.getAllInformationSets();

            int oracleRawISSize = algConfig.getAllInformationSets().size();
            int oracleISSizeWithoutLeaves = algConfig.getAllInformationSets().size();
            int oracleISSizeWithSingletonLeaves = algConfig.getAllInformationSets().size();
            if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
                oracleISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();
                oracleISSizeWithSingletonLeaves = ((GadgetSefceLP) s1).getRestrictedGameSizeWithSingletonLeaves();
            }
            int sequencesSize = (s1 instanceof GadgetSefceLP) ? ((GadgetSefceLP) s1).getNumberOfSequences() : algConfig.getAllSequences().size();
            averageGadgetDepth += (s1 instanceof GadgetSefceLP) ? ((GadgetSefceLP) s1).getExpectedGadgetDepth() : 0.0;

            int oracleFinalLPSize = 0;
            if (s1 instanceof GadgetSefceLP) {
                oracleFinalLPSize = ((GadgetSefceLP) s1).getFinalLPSize();
            }
//            if (s1 instanceof CompleteTwoPlayerSefceLP){
//                oracleFinalLPSize = ((CompleteTwoPlayerSefceLP)s1).getFinalLpSize();
//            }

            algConfig = new StackelbergConfig(rootState);
            expander = new RandomGameExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
//            new GambitEFG().write("flipItConsistency.gbt", rootState, expander);
            SumForbiddingStackelbergLP s2;
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2 = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);//, s1.getVariableIndices(), s1.getConstraints());
            s2.setLPSolvingMethod(4);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
//                mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));

            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatioII += runner.getRestrictedGameRatio();

            int fullFinalLPSize = s2.getFinalLpSize();//fullFinalLPSize = ((CompleteTwoPlayerSefceLP)s2).getFinalLpSize();

//            System.out.println(oracleISSize + " / " + algConfig.getAllInformationSets().size());

            int fullISSizeWithSingletonLeaves = algConfig.getNumberOfISsWithSingletonLeaves();
            int fullISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();

            averageRawISSize += (double) oracleRawISSize / algConfig.getAllInformationSets().size();
            averageISSizeWithoutLeaves += (double) oracleISSizeWithoutLeaves / fullISSizeWithoutLeaves;
            averageISSizeWithSingletonLeaves += (double) oracleISSizeWithSingletonLeaves / fullISSizeWithSingletonLeaves;
            averageSequencesSize += (double) sequencesSize / algConfig.getAllSequences().size();
            averageLPSize += (double) oracleFinalLPSize / fullFinalLPSize;

//            System.out.println(iss.size() + " / " + algConfig.getAllInformationSets().size());
//            for (ISKey key : iss.keySet())
//                if (!algConfig.getAllInformationSets().containsKey(key)) {
//                    System.out.println(key);
//                }
//            System.out.println("///");
//            for (ISKey key : algConfig.getAllInformationSets().keySet())
//                if (!iss.containsKey(key) || iss.get(key).getAllStates().size() != algConfig.getAllInformationSets().get(key).getAllStates().size() || iss.get(key).getOutgoingSequences().size() != algConfig.getAllInformationSets().get(key).getOutgoingSequences().size()) {
//                    System.out.println(key);
//                    System.out.println(iss.get(key).getAllStates().size() + " / " + algConfig.getAllInformationSets().get(key).getAllStates().size());
//                    System.out.println(iss.get(key).getOutgoingSequences().size() + " / " + algConfig.getAllInformationSets().get(key).getOutgoingSequences().size());
//                }
//            System.out.println("/////");
//
//            int size = 0;
//            for (SequenceInformationSet set : iss.values())
//                if (set.getOutgoingSequences().isEmpty()) {
////                System.out.println(set.getAllStates().size());
//                    size += Math.max(0, set.getAllStates().size() - 1);
//                }
//            System.out.println((iss.size()+size));
//
//            size = 0;
//            for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
//                if (set.getOutgoingSequences().isEmpty())
//                    size += set.getAllStates().size();
//                else size++;
//            }
//            System.out.println(size);


            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed - startingSeed + 1) + " ; " + restrictedGameRatioII / (seed - startingSeed + 1));
            System.out.println("Average IS size w/ leaves = " + (averageISSizeWithoutLeaves / (seed - startingSeed + 1)));
            System.out.println("Average IS size w 1-leaves = " + (averageISSizeWithSingletonLeaves / (seed - startingSeed + 1)));
            System.out.println("Average raw IS size = " + (averageRawISSize / (seed - startingSeed + 1)));
            System.out.println("Average # of seqs = " + (averageSequencesSize / (seed - startingSeed + 1)));
            System.out.println("Average gadget depth = " + (averageGadgetDepth / (seed - startingSeed + 1)));
            System.out.println("Average LPSize = " + (averageLPSize / (seed - startingSeed + 1)));
            System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + oracleFinalLPSize +
                    " ; " + s2.getClass().getSimpleName() + " : " + fullFinalLPSize);
            System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + " ; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
            if (s1 instanceof GadgetSefceLP)
                System.out.println("Gadget making time = " + ((GadgetSefceLP) s1).getOverallGadgetMakingTime() / 1000000l);
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
//        for (Integer depth : issOutside.keySet())
//            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutside.get(depth).toString());
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (maxseed) + " ; " + restrictedGameRatioII / (maxseed));
        System.out.println("Min RG size = " + minRGSize);

    }

    public static void runGenSumRandomOneSeed(String[] args) {
        int depth = Integer.parseInt(args[1]);
        int bf = Integer.parseInt(args[2]);
        int seed = Integer.parseInt(args[3]);
        double correlation = Double.parseDouble(args[4]);
        int observations = Integer.parseInt(args[5]);
        String algVersion = args[6];
        GameInfo gameInfo = new RandomGameInfo();
        GameState rootState = new RandomGameState();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;

        System.out.println();
        System.out.println("Running seed " + (seed));

        gameInfo = new RandomGameInfo(seed, depth, bf, correlation, observations);
        rootState = new RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        RandomGameExpander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);

        double fullGameGV;
        double oracleGameGV;

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        if (algVersion.equals("O")) {
            int lpSolvingAlg = GadgetLPTable.CPLEXALG;
            if(args.length > 8) lpSolvingAlg = Integer.parseInt(args[8]);
            int use_discounts = 0;
            if(args.length > 9) use_discounts = Integer.parseInt(args[9]);
            double eps = 1e-7;
            if(args.length > 10) eps = Double.valueOf(args[10]);
            double delta = 0.05;
            boolean useProjection = true;
            boolean useLocalUtility = false;
            if(args.length > 11){
                delta = Double.valueOf(args[11]);
                useProjection = args[12].equals("1");
                useLocalUtility = args[13].equals("1");
            }
            int rounding = 2;
            double discount = Math.round(((int) Math.pow(10, rounding)) * 0.01 * gameInfo.getMaxUtility()) / Math.pow(10, rounding);
            if(args.length > 14){
                discount = Math.round(((int) Math.pow(10, rounding)) * Double.valueOf(args[14]) * gameInfo.getMaxUtility()) / Math.pow(10, rounding);
            }
            String gadgetType = args[7];
            GadgetOracle2pSumForbiddingLP s1 = null;
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new BayesianGadgetSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            switch (gadgetType) {
                case "LP":
                    s1 = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "BMILP":
                    s1 = new GadgetOracle2pShallowestBrokenCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "AMILP":
                    s1 = new GadgetOracle2pShallowestAllCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
            }
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
                algConfig = new LeaderGenerationConfig(rootState);
                expander = new RandomGameExpander<>(algConfig);
                runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
                s1.setLPSolvingMethod(lpSolvingAlg);
                s1.setEps(eps);
                s1.setEpsilonDiscounts(use_discounts == 1);
                s1.setHullApproximation(delta, useLocalUtility, useProjection);
                s1.setGadgetDiscount(discount);

            }
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
            fullTime = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            System.out.println("Gadget LP size = " + s1.getFinalLPSize());
            System.out.println(fullTime);
        }
        if (algVersion.equals("F")) {
            int lpSolvingAlg = GadgetLPTable.CPLEXALG;
            if(args.length > 7) lpSolvingAlg = Integer.parseInt(args[7]);
            double eps = 1e-7;
            if(args.length > 9) eps = Double.valueOf(args[9]);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            algConfig = new StackelbergConfig(rootState);
            expander = new RandomGameExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            SumForbiddingStackelbergLP s2;
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2 = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2.setLPSolvingMethod(lpSolvingAlg);
            s2.setEps(eps);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
            oracleTime = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            System.out.println("Full LP size = " + s2.getFinalLpSize());
            System.out.println(oracleTime);
        }


    }

    public static void runPursuit(String[] args) {
        LEADER = 1;
        final boolean COUNT_LEAVES = true;
        int startingSeed = 0; // 113, 117, 135, 157, 168
        PursuitGameInfo gameInfo = new PursuitGameInfo();
        int depth = PursuitGameInfo.depth;
        int gridsize = 3;
        int maxseed = 10;
        if (args.length != 0) {
            gridsize = Integer.parseInt(args[1]);
            depth = Integer.parseInt(args[2]);
            maxseed = Integer.parseInt(args[3]);
        }
        GameState rootState;

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatioII = 0.0;
        double restrictedGameRatioI = 0.0;
        double averageRawISSize = 0.0;
        double averageISSizeWithoutLeaves = 0.0;
        double averageISSizeWithSingletonLeaves = 0.0;
        double averageSequencesSize = 0.0;
        double averageGadgetDepth = 0.0;
        double averageLPSize = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;
        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        double minRGSize = Double.POSITIVE_INFINITY;

        for (int seed = startingSeed; seed < startingSeed + maxseed; seed++) {

            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            gameInfo.initValue(true, seed, depth, gridsize);

            rootState = new GenSumVisibilityPursuitGameState();//GenSumPursuitGameState();


            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            PursuitExpander<SequenceInformationSet> expander = new PursuitExpander<>(algConfig);

            double fullGameGV;
            double oracleGameGV;

            StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            GadgetOracle2pSumForbiddingLP s1;
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new BayesianGadgetSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s1 = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
                algConfig = new LeaderGenerationConfig(rootState);
                expander = new PursuitExpander<>(algConfig);
                runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            }
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
//            double rgSize = runner.getRestrictedGameRatio();
//            restrictedGameRatioI += rgSize;
//            if (rgSize < minRGSize) minRGSize = rgSize;
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//                s1 = null;

//            HashMap<ISKey, SequenceInformationSet> iss = algConfig.getAllInformationSets();

            int oracleRawISSize = algConfig.getAllInformationSets().size();
            int oracleISSizeWithoutLeaves = algConfig.getAllInformationSets().size();
            int oracleISSizeWithSingletonLeaves = algConfig.getAllInformationSets().size();
            if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
                oracleISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();
                oracleISSizeWithSingletonLeaves = ((GadgetSefceLP) s1).getRestrictedGameSizeWithSingletonLeaves();
            }
            int sequencesSize = (s1 instanceof GadgetSefceLP) ? ((GadgetSefceLP) s1).getNumberOfSequences() : algConfig.getAllSequences().size();
            averageGadgetDepth += (s1 instanceof GadgetSefceLP) ? ((GadgetSefceLP) s1).getExpectedGadgetDepth() : 0.0;

            int oracleFinalLPSize = 0;
            if (s1 instanceof GadgetSefceLP) {
                oracleFinalLPSize = ((GadgetSefceLP) s1).getFinalLPSize();
            }
//            if (s1 instanceof CompleteTwoPlayerSefceLP){
//                oracleFinalLPSize = ((CompleteTwoPlayerSefceLP)s1).getFinalLpSize();
//            }

            algConfig = new StackelbergConfig(rootState);
            expander = new PursuitExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
//            new GambitEFG().write("flipItConsistency.gbt", rootState, expander);
            SumForbiddingStackelbergLP s2;
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2 = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
//                mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));

            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatioII += runner.getRestrictedGameRatio();

            int fullFinalLPSize = s2.getFinalLpSize();//fullFinalLPSize = ((CompleteTwoPlayerSefceLP)s2).getFinalLpSize();

//            System.out.println(oracleISSize + " / " + algConfig.getAllInformationSets().size());

            int fullISSizeWithSingletonLeaves = algConfig.getNumberOfISsWithSingletonLeaves();
            int fullISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();

            averageRawISSize += (double) oracleRawISSize / algConfig.getAllInformationSets().size();
            averageISSizeWithoutLeaves += (double) oracleISSizeWithoutLeaves / fullISSizeWithoutLeaves;
            averageISSizeWithSingletonLeaves += (double) oracleISSizeWithSingletonLeaves / fullISSizeWithSingletonLeaves;
            averageSequencesSize += (double) sequencesSize / algConfig.getAllSequences().size();
            averageLPSize += (double) oracleFinalLPSize / fullFinalLPSize;

//            System.out.println(iss.size() + " / " + algConfig.getAllInformationSets().size());
//            for (ISKey key : iss.keySet())
//                if (!algConfig.getAllInformationSets().containsKey(key)) {
//                    System.out.println(key);
//                }
//            System.out.println("///");
//            for (ISKey key : algConfig.getAllInformationSets().keySet())
//                if (!iss.containsKey(key) || iss.get(key).getAllStates().size() != algConfig.getAllInformationSets().get(key).getAllStates().size() || iss.get(key).getOutgoingSequences().size() != algConfig.getAllInformationSets().get(key).getOutgoingSequences().size()) {
//                    System.out.println(key);
//                    System.out.println(iss.get(key).getAllStates().size() + " / " + algConfig.getAllInformationSets().get(key).getAllStates().size());
//                    System.out.println(iss.get(key).getOutgoingSequences().size() + " / " + algConfig.getAllInformationSets().get(key).getOutgoingSequences().size());
//                }
//            System.out.println("/////");
//
//            int size = 0;
//            for (SequenceInformationSet set : iss.values())
//                if (set.getOutgoingSequences().isEmpty()) {
////                System.out.println(set.getAllStates().size());
//                    size += Math.max(0, set.getAllStates().size() - 1);
//                }
//            System.out.println((iss.size()+size));
//
//            size = 0;
//            for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
//                if (set.getOutgoingSequences().isEmpty())
//                    size += set.getAllStates().size();
//                else size++;
//            }
//            System.out.println(size);


            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed - startingSeed + 1) + " ; " + restrictedGameRatioII / (seed - startingSeed + 1));
            System.out.println("Average IS size w/ leaves = " + (averageISSizeWithoutLeaves / (seed - startingSeed + 1)));
            System.out.println("Average IS size w 1-leaves = " + (averageISSizeWithSingletonLeaves / (seed - startingSeed + 1)));
            System.out.println("Average raw IS size = " + (averageRawISSize / (seed - startingSeed + 1)));
            System.out.println("Average # of seqs = " + (averageSequencesSize / (seed - startingSeed + 1)));
            System.out.println("Average gadget depth = " + (averageGadgetDepth / (seed - startingSeed + 1)));
            System.out.println("Average LPSize = " + (averageLPSize / (seed - startingSeed + 1)));
            System.out.println("Final number of cons = " + s1.getClass().getSimpleName() + " : " + oracleFinalLPSize +
                    " ; " + s2.getClass().getSimpleName() + " : " + fullFinalLPSize);
            System.out.println(s1.getClass().getSimpleName() + " time = " + fullTime + " ; " + s2.getClass().getSimpleName() + " time = " + oracleTime);
            if (s1 instanceof GadgetSefceLP)
                System.out.println("Gadget making time = " + ((GadgetSefceLP) s1).getOverallGadgetMakingTime() / 1000000l);
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
//        for (Integer depth : issOutside.keySet())
//            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutside.get(depth).toString());
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (maxseed) + " ; " + restrictedGameRatioII / (maxseed));
        System.out.println("Min RG size = " + minRGSize);

    }

    public static void runPursuitRandomOneSeed(String[] args) {
        LEADER = 1;

        PursuitGameInfo gameInfo = new PursuitGameInfo();
        int depth = PursuitGameInfo.depth;
        int seed = 10;
        int gridsize = 3;
        int schemaSize = 2;
        gridsize = Integer.parseInt(args[1]);
        depth = Integer.parseInt(args[2]);
        seed = Integer.parseInt(args[3]);
        int rpCount = Integer.parseInt(args[4]);
        double samplingProb = Double.parseDouble(args[5]);
        boolean isLog = Integer.parseInt(args[6]) > 0;
        GameState rootState;

        System.out.println();
        System.out.println("Running seed: " + (seed));

        gameInfo.initValue(true, seed, depth, gridsize);

        rootState = new GenSumVisibilityPursuitGameState();//GenSumPursuitGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        PursuitExpander<SequenceInformationSet> expander = new PursuitExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        algConfig.USE_SCHEMATA = false;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.USE_RANDOM = true;
        algConfig.MAX_RPS = rpCount;
        algConfig.RPS_PROB = samplingProb;

        RandomlySamplingIterator.USE_LOG = isLog;

        StackelbergSequenceFormMultipleLPs mLPs = new StackelbergSequenceFormMultipleLPs(
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[LEADER],
                rootState.getAllPlayers()[1-LEADER], gameInfo, expander);

        RandomlySamplingIterator.USE_LOG = isLog;

        runner.generate(rootState.getAllPlayers()[LEADER], mLPs);
    }

    public static void runPursuitSchemataOneSeed(String[] args) {
        LEADER = 1;

        PursuitGameInfo gameInfo = new PursuitGameInfo();
        int depth = PursuitGameInfo.depth;
        int seed = 10;
        String gridsize = "3";
        int schemaSize = 3;
        boolean fixSecond = false;
        if (args.length != 0) {
            gridsize = args[1];
            depth = Integer.parseInt(args[2]);
            fixSecond = Integer.parseInt(args[3]) > 0;
            seed = Integer.parseInt(args[4]);
            schemaSize = Integer.parseInt(args[5]);
        }
        GameState rootState;

        System.out.println();
        System.out.println("Running seed: " + (seed));

        gameInfo.initValue(true, seed, depth, gridsize, fixSecond);

        rootState = new GenSumVisibilityPursuitGameState();//GenSumPursuitGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        PursuitExpander<SequenceInformationSet> expander = new PursuitExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        algConfig.USE_SCHEMATA = true;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.MAX_SCHEMA_SIZE = schemaSize;

        Double result = runner.generate(rootState.getAllPlayers()[LEADER], new StackelbergMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[LEADER], rootState.getAllPlayers()[1-LEADER], gameInfo, expander)).getLeft();

    }

    public static void runPursuitOneSeed(String[] args) {
        LEADER = 1;
        final boolean COUNT_LEAVES = true;
        int startingSeed = 0; // 113, 117, 135, 157, 168
        PursuitGameInfo gameInfo = new PursuitGameInfo();
        int depth = PursuitGameInfo.depth;
        int seed = 10;
        String gridsize = "3";
        String algVersion = "";
        boolean fixedSecond = false;
        if (args.length != 0) {
            gridsize = args[1];
            depth = Integer.parseInt(args[2]);
            seed = Integer.parseInt(args[3]);
            fixedSecond = Integer.parseInt(args[4]) > 0;
            algVersion = args[5];
        }
        GameState rootState;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;

        System.out.println();
        System.out.println("Running seed " + (seed));

        gameInfo.initValue(true, seed, depth, gridsize, fixedSecond);

        rootState = new GenSumVisibilityPursuitGameState();//GenSumPursuitGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        PursuitExpander<SequenceInformationSet> expander = new PursuitExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        if (algVersion.equals("O")) {
            String gadgetType = "LP";
            double delta = 0.05;
            boolean useProjection = true;
            boolean useLocalUtility = false;
            if(args.length > 8){
                delta = Double.valueOf(args[8]);
                useProjection = args[9].equals("1");
                useLocalUtility = args[10].equals("1");
            }
            int use_discounts = 1;
            if(args.length > 7) use_discounts = Integer.parseInt(args[7]);
            int lpSolvingAlg = 1;
            if(args.length > 6) lpSolvingAlg = Integer.parseInt(args[6]);
            if (args.length > 5) gadgetType = args[5];
            double eps = 1e-7;
            if(args.length > 11) eps = Double.valueOf(args[11]);
            int rounding = 2;
            double discount = Math.round(((int) Math.pow(10, rounding)) * 0.01 * gameInfo.getMaxUtility()) / Math.pow(10, rounding);
            if(args.length > 12){
                discount = Math.round(((int) Math.pow(10, rounding)) * Double.valueOf(args[12]) * gameInfo.getMaxUtility()) / Math.pow(10, rounding);
            }
            GadgetOracle2pSumForbiddingLP s1 = null;
//                s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new BayesianGadgetSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            switch (gadgetType) {
                case "LP":
                    s1 = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "BMILP":
                    s1 = new GadgetOracle2pShallowestBrokenCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "AMILP":
                    s1 = new GadgetOracle2pShallowestAllCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
            }
//                s1 = new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
//                s1 = new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo);
            if (s1 instanceof GadgetOracle2pSumForbiddingLP) {
                algConfig = new LeaderGenerationConfig(rootState);
                expander = new PursuitExpander<>(algConfig);
                runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
                s1.setLPSolvingMethod(lpSolvingAlg);
                s1.setEpsilonDiscounts(use_discounts==1);
                s1.setHullApproximation(delta, useLocalUtility, useProjection);
                s1.setGadgetDiscount(discount);
                s1.setEps(eps);
            }
            runner.generate(rootState.getAllPlayers()[LEADER], s1);
            fullTime = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            System.out.println("Gadget LP size = " + s1.getFinalLPSize());
            System.out.println(fullTime);
        }
//
        if (algVersion.equals("F")) {
            int lpSolvingAlg = 1;
            if(args.length > 6) lpSolvingAlg = Integer.parseInt(args[6]);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            algConfig = new StackelbergConfig(rootState);
            expander = new PursuitExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
//            startGeneration = threadBean.getCurrentThreadCpuTime();
//            new GambitEFG().write("flipItConsistency.gbt", rootState, expander);
            SumForbiddingStackelbergLP s2;
//                s2 = new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2 = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
            s2.setLPSolvingMethod(lpSolvingAlg);
            runner.generate(rootState.getAllPlayers()[LEADER], s2);
//                mergeISsOutsideSGStats(issOutside, runner.getIssOutsideSubGame(LEADER));
            fullTime = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            System.out.println("Full LP size = " + s2.getFinalLpSize());
            System.out.println(fullTime);
        }


    }

    public static void runFlipItMLPOneSeed(String[] args) {
        LEADER = 0;
        FlipItGameInfo gameInfo;
        int seed = 10;
        boolean usePass = true;
        boolean isEmpty = true;
        int maxSchemaSize = 3;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
            seed = Integer.parseInt(args[4]);
            usePass = Integer.parseInt(args[5]) > 0;
            isEmpty = Integer.parseInt(args[6]) > 0;
            maxSchemaSize = Integer.parseInt(args[7]);
            String graphFile = isEmpty ? "flipit_empty" : "flipit_simple";
            graphFile+= graphSize + ".txt";
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
            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        System.out.println();
        System.out.println("Running seed: " + (seed));

        rootState = initGame(gameInfo, seed);
        gameInfo.ENABLE_PASS = usePass;


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        runner.COUNT_PRUNED = false;

        algConfig.USE_SCHEMATA = true;
        algConfig.MAX_SCHEMA_SIZE = maxSchemaSize;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.USE_RANDOM = false;
        algConfig.USE_UPPERBOUND_CUT = false;

        StackelbergMultipleLPs mLPs = new StackelbergMultipleLPs(
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0],
                rootState.getAllPlayers()[1], gameInfo, expander);

        runner.generate(rootState.getAllPlayers()[0], mLPs);
    }

    public static void runFlipItRandomOneSeedEmpty(String[] args) {
        LEADER = 0;
        FlipItGameInfo gameInfo;
        int seed = 10;
        int rpCount = 3;
        boolean isLog = false;
        boolean usePass = true;
        boolean isEmpty = true;
        double samplingProb = 0.001;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
                seed = Integer.parseInt(args[4]);
                rpCount = Integer.parseInt(args[5]);
                samplingProb = Double.parseDouble(args[6]);
                isLog = Integer.parseInt(args[7]) > 0;
                usePass = Integer.parseInt(args[8]) > 0;
                isEmpty = Integer.parseInt(args[9]) > 0;
            String graphFile = isEmpty ? "flipit_empty" : "flipit_simple";
            graphFile+= graphSize + ".txt";
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
            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        System.out.println();
        System.out.println("Running seed: " + (seed));
        System.out.println("Max RP count: " + rpCount);
        System.out.println("Sampling prob: " + samplingProb);

        rootState = initGame(gameInfo, seed);
        gameInfo.ENABLE_PASS = usePass;


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        algConfig.USE_SCHEMATA = false;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.USE_RANDOM = true;
        algConfig.MAX_RPS = rpCount;
        algConfig.RPS_PROB = samplingProb;

        RandomlySamplingIterator.USE_LOG = isLog;

        StackelbergSequenceFormMultipleLPs mLPs = new StackelbergSequenceFormMultipleLPs(
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0],
                rootState.getAllPlayers()[1], gameInfo, expander);

        RandomlySamplingIterator.USE_LOG = isLog;

        runner.generate(rootState.getAllPlayers()[0], mLPs);
    }

    public static void runFlipItSchemataOneSeed(String[] args) {
        LEADER = 0;
        FlipItGameInfo gameInfo;
        int seed = 10;
        boolean usePass = true;
        boolean isEmpty = true;
        boolean useMILP = false;
        int maxSchemaSize = 3;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
            seed = Integer.parseInt(args[4]);
            usePass = Integer.parseInt(args[5]) > 0;
            isEmpty = Integer.parseInt(args[6]) > 0;
            maxSchemaSize = Integer.parseInt(args[7]);
            useMILP = Integer.parseInt(args[8]) > 0;
            String graphFile = isEmpty ? "flipit_empty" : "flipit_simple";
            graphFile+= graphSize + ".txt";
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
            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        System.out.println();
        System.out.println("Running seed: " + (seed));

        rootState = initGame(gameInfo, seed);
        gameInfo.ENABLE_PASS = usePass;


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        runner.COUNT_PRUNED = true;

        algConfig.GENERATE_SCHEMATA = false;
        algConfig.USE_SCHEMATA = false;
        algConfig.MAX_SCHEMA_SIZE = maxSchemaSize;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.USE_RANDOM = false;
        algConfig.USE_UPPERBOUND_CUT = false;

        StackelbergSequenceFormLP mLPs = null;

        if(useMILP){
            algConfig.GENERATE_SCHEMATA = true;
            algConfig.USE_SCHEMATA = false;
            algConfig.MAX_SCHEMA_SIZE = maxSchemaSize;
            StackelbergSequenceFormMILP.RESTRICT_TO_SMALL = true;
            mLPs = new StackelbergSequenceFormMILP(
                    new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0],
                    rootState.getAllPlayers()[1], gameInfo, expander);
        }
        else {
            algConfig.GENERATE_SCHEMATA = false;
            algConfig.USE_SCHEMATA = true;
            mLPs = new StackelbergSequenceFormMultipleLPs(
                    new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0],
                    rootState.getAllPlayers()[1], gameInfo, expander);
        }

        runner.generate(rootState.getAllPlayers()[0], mLPs);
    }

    public static void runFlipItRandomOneSeed(String[] args) {
        LEADER = 0;
        FlipItGameInfo gameInfo;
        int seed = 10;
        boolean usePass = true;
        boolean isEmpty = true;
        int randomSeed = 3;
        boolean isLog = true;
        double samplingProb = 0.01;
        int rpCount = 5;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
            seed = Integer.parseInt(args[4]);
            usePass = Integer.parseInt(args[5]) > 0;
            isEmpty = Integer.parseInt(args[6]) > 0;
            randomSeed = Integer.parseInt(args[7]);
            isLog = Integer.parseInt(args[8]) > 0;
            rpCount = Integer.parseInt(args[9]);
            samplingProb = Double.parseDouble(args[10]);
            String graphFile = isEmpty ? "flipit_empty" : "flipit_simple";
            graphFile+= graphSize + ".txt";
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
            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        System.out.println();
        System.out.println("Running seed: " + (seed));

        rootState = initGame(gameInfo, seed);
        gameInfo.ENABLE_PASS = usePass;


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        algConfig.USE_SCHEMATA = false;
        algConfig.USE_FEASIBILITY_CUT = false;
        algConfig.USE_RANDOM = true;
        algConfig.MAX_RPS = rpCount;
        algConfig.RPS_PROB = samplingProb;

        RandomlySamplingIterator.USE_LOG = isLog;
        RandomlySamplingIterator.SEED = randomSeed;

        StackelbergSequenceFormMultipleLPs mLPs = new StackelbergSequenceFormMultipleLPs(
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0],
                rootState.getAllPlayers()[1], gameInfo, expander);

        RandomlySamplingIterator.USE_LOG = isLog;

        runner.generate(rootState.getAllPlayers()[0], mLPs);
    }

    public static void runFlipItFullOneSeed(String[] args) {
        LEADER = 0;
        FlipItGameInfo gameInfo;
        int seed = 10;
        boolean usePass = true;
        boolean isEmpty = true;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
            seed = Integer.parseInt(args[4]);
            usePass = Integer.parseInt(args[5]) > 0;
            isEmpty = Integer.parseInt(args[6]) > 0;
            String graphFile = isEmpty ? "flipit_empty" : "flipit_simple";
            graphFile+= graphSize + ".txt";
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
            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

        System.out.println();
        System.out.println("Running seed: " + (seed));

        rootState = initGame(gameInfo, seed);
        gameInfo.ENABLE_PASS = usePass;


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        SumForbiddingStackelbergLP fullLP = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);

        runner.generate(rootState.getAllPlayers()[0], fullLP);
    }


    public static double runFlipItOneSeed(String[] args) {
        LEADER = 0;
        boolean runComplete = false;
        boolean runOracle = false;
        FlipItGameInfo gameInfo;
        int seed = 10;
        String algversion = "";
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
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
    seed = Integer.parseInt(args[4]);
    algversion = args[5];
                switch (algversion) {
        case "F":
            runComplete = true;
            break;
        case "O":
            runOracle = true;
            break;
    }
}
        }
                gameInfo.ZERO_SUM_APPROX = false;
                GameState rootState;

                ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;

        double expectedGadgetDepth = 0.0;
        double expectedGadgetSize = 0.0;

        double averageRawISSize = 0.0;
        double averageISSizeWithoutLeaves = 0.0;
        double averageISSizeWithSingletonLeaves = 0.0;
        double averageSequencesSize = 0.0;
        double averageGadgetDepth = 0.0;
        double averageLPSize = 0.0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        int startingSeed = 0;
        System.out.println();
        System.out.println("Running seed " + (seed));

        rootState = initGame(gameInfo, seed);
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        double fullGameGV = 0.0;
        double oracleGameGV = 0.0;

        StackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);


        SumForbiddingStackelbergLP fullLP = null;
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        fullLP = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
        long startGeneration = threadBean.getCurrentThreadCpuTime();
        if (runComplete) {
            int lpSolvingAlg = 1;
//            if(args.length > 6) lpSolvingAlg = Integer.parseInt(args[6]);
            double eps = 1e-7;
//            if(args.length > 8) eps = Double.valueOf(args[8]);
            fullLP.setLPSolvingMethod(lpSolvingAlg);
            fullLP.setEps(eps);
            boolean usePass = Integer.parseInt(args[6]) > 0;
            FlipItGameInfo.ENABLE_PASS = usePass;
            runner.generate(rootState.getAllPlayers()[LEADER], fullLP);
        }

        fullGameGV = runner.getGameValue();
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
        double fullISSize = algConfig.getAllInformationSets().size();
        int fullISSizeWithSingletonLeaves = algConfig.getNumberOfISsWithSingletonLeaves();
        int fullISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();
        int fullFinalLPSize = fullLP.getFinalLpSize();
        int fullSequencesSize = algConfig.getAllSequences().size();

        String fullLPName = fullLP.getClass().getSimpleName();
        fullLP = null;
        runner = null;
        System.gc();

        algConfig = new LeaderGenerationConfig(rootState);
        expander = new FlipItExpander<>(algConfig);
        runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        GadgetOracle2pSumForbiddingLP oracle = null;// = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);

//            LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        if (runOracle) {
            int lpSolvingAlg = 1;
            if(args.length > 7) lpSolvingAlg = Integer.parseInt(args[7]);
            int use_discounts = 1;
            if(args.length > 8) use_discounts = Integer.parseInt(args[8]);
            double eps = 1e-7;
            if(args.length > 9) eps = Double.valueOf(args[9]);
            double delta = 0.05;
            boolean useProjection = true;
            boolean useLocalUtility = false;
            if(args.length > 10){
                delta = Double.valueOf(args[10]);
                useProjection = args[11].equals("1");
                useLocalUtility = args[12].equals("1");
            }
            int rounding = 2;
            double discount = Math.round(((int) Math.pow(10, rounding)) * 0.01 * gameInfo.getMaxUtility()) / Math.pow(10, rounding);
            if(args.length > 13){
                discount = Math.round(((int) Math.pow(10, rounding)) * Double.valueOf(args[13]) * gameInfo.getMaxUtility()) / Math.pow(10, rounding);
            }
            String gadgetType = "LP";
            if (args.length > 6) gadgetType = args[6];
            switch (gadgetType) {
                case "LP":
                    oracle = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "BMILP":
                    oracle = new GadgetOracle2pShallowestBrokenCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
                case "AMILP":
                    oracle = new GadgetOracle2pShallowestAllCplexLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    break;
            }
            oracle.setGadgetDiscount(discount);
            oracle.setLPSolvingMethod(lpSolvingAlg);
            oracle.setEpsilonDiscounts(use_discounts == 1);
            oracle.setEps(eps);
            oracle.setHullApproximation(delta, useLocalUtility, useProjection);
            runner.generate(rootState.getAllPlayers()[LEADER], oracle);
//            for (ISKey key : algConfig.getAllInformationSets().keySet()) System.out.println(key);
//            new GambitEFG().write("small_flipit_example.gbt", rootState, expander);
        }
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
//            restrictedGameRatio += (double) oracle.getRestrictedGameSizeWithSingletonLeaves() / fullISSize;//oracle.getRestrictedGameRatio();
        oracleGameGV = runner.getGameValue();
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

        int oracleRawISSize = algConfig.getAllInformationSets().size();
        int oracleISSizeWithoutLeaves = algConfig.getAllInformationSets().size();
        int oracleISSizeWithSingletonLeaves = algConfig.getAllInformationSets().size();
        if (oracle instanceof GadgetSefceLP && runOracle) {
            oracleISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();
            oracleISSizeWithSingletonLeaves = ((GadgetSefceLP) oracle).getRestrictedGameSizeWithSingletonLeaves();
        }
        int oracleFinalLPSize = 0;
        if (oracle instanceof GadgetSefceLP && runOracle) {
            oracleFinalLPSize = ((GadgetSefceLP) oracle).getFinalLPSize();
        }

        averageRawISSize += (double) oracleRawISSize / fullISSize;
        averageISSizeWithoutLeaves += (double) oracleISSizeWithoutLeaves / fullISSizeWithoutLeaves;
        averageISSizeWithSingletonLeaves += (double) oracleISSizeWithSingletonLeaves / fullISSizeWithSingletonLeaves;
        averageSequencesSize += (double) algConfig.getAllSequences().size() / fullSequencesSize;
        averageLPSize += (double) oracleFinalLPSize / fullFinalLPSize;

//        expectedGadgetDepth += oracle.getExpectedGadgetDepth();
//        expectedGadgetSize += oracle.getExpectedGadgetSize();

        System.out.println(oracleGameGV + " / " + fullGameGV);
        if (Double.isNaN(oracleGameGV) || Double.isNaN(fullGameGV) || Math.abs(oracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(seed);

//            System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed - startingSeed + 1) + " ; " + restrictedGameRatioII / (seed - startingSeed + 1));
        System.out.println("Average IS size w/ leaves = " + (averageISSizeWithoutLeaves / (seed - startingSeed + 1)));
        System.out.println("Average IS size w 1-leaves = " + (averageISSizeWithSingletonLeaves / (seed - startingSeed + 1)));
        System.out.println("Average raw IS size = " + (averageRawISSize / (seed - startingSeed + 1)));
        System.out.println("Average # of seqs = " + (averageSequencesSize / (seed - startingSeed + 1)));
        System.out.println("Average gadget depth = " + (expectedGadgetDepth));
        System.out.println("Average gadget size = " + (expectedGadgetSize));
        System.out.println("Average LPSize = " + (averageLPSize / (seed - startingSeed + 1)));
        if (runComplete) {
            System.out.println("Full LP size = " + fullFinalLPSize);
            System.out.println(fullTime);
        }
        if (runOracle) {
            System.out.println("Gadget LP size = " + oracleFinalLPSize);
            System.out.println(oracleTime);
        }
        return fullGameGV;
    }

    public static void runFlipIt(String[] args) {
        LEADER = 0;
        int startingSeed = 17;
        boolean runComplete = true;
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[1]);
            String graphSize = args[2];
            String graphFile = "flipit_simple" + graphSize + ".txt";
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

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;

        double expectedGadgetDepth = 0.0;
        double expectedGadgetSize = 0.0;

        double averageRawISSize = 0.0;
        double averageISSizeWithoutLeaves = 0.0;
        double averageISSizeWithSingletonLeaves = 0.0;
        double averageSequencesSize = 0.0;
        double averageGadgetDepth = 0.0;
        double averageLPSize = 0.0;

        double minRGSize = Double.POSITIVE_INFINITY;
        int minRGSizeSeed = -1;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();


        for (int seed = startingSeed; seed < startingSeed+maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            rootState = initGame(gameInfo, seed);
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

            double fullGameGV = 0.0;
            double oracleGameGV = 0.0;

            StackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            SumForbiddingStackelbergLP fullLP = null;
            if (runComplete) {
                runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
                fullLP = new SumForbiddingStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo);
                fullLP.setLPSolvingMethod(4);
                runner.generate(rootState.getAllPlayers()[LEADER], fullLP);

                fullGameGV = runner.getGameValue();
            }
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
            double fullISSize = algConfig.getAllInformationSets().size();
            int fullISSizeWithSingletonLeaves = algConfig.getNumberOfISsWithSingletonLeaves();
            int fullISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();
            int fullFinalLPSize = fullLP.getFinalLpSize();
            int fullSequencesSize = algConfig.getAllSequences().size();

            String fullLPName = fullLP.getClass().getSimpleName();
            fullLP = null;
            runner = null;
            System.gc();

            algConfig = new LeaderGenerationConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            GadgetOracle2pSumForbiddingLP oracle = new GadgetOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo);
            oracle.setLPSolvingMethod(4);
//            LeaderOracle2pSumForbiddingLP oracle = new LeaderOracle2pSumForbiddingLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
            runner.generate(rootState.getAllPlayers()[LEADER], oracle);
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            restrictedGameRatio += (double) oracle.getRestrictedGameSizeWithSingletonLeaves() / fullISSize;//oracle.getRestrictedGameRatio();
            oracleGameGV = runner.getGameValue();
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

            int oracleRawISSize = algConfig.getAllInformationSets().size();
            int oracleISSizeWithoutLeaves = algConfig.getAllInformationSets().size();
            int oracleISSizeWithSingletonLeaves = algConfig.getAllInformationSets().size();
            if (oracle instanceof GadgetSefceLP) {
                oracleISSizeWithoutLeaves = algConfig.getNumberOfISsWithoutLeaves();
                oracleISSizeWithSingletonLeaves = ((GadgetSefceLP) oracle).getRestrictedGameSizeWithSingletonLeaves();
            }
            int oracleFinalLPSize = 0;
            if (oracle instanceof GadgetSefceLP) {
                oracleFinalLPSize = ((GadgetSefceLP) oracle).getFinalLPSize();
            }

            averageRawISSize += (double) oracleRawISSize / fullISSize;
            averageISSizeWithoutLeaves += (double) oracleISSizeWithoutLeaves / fullISSizeWithoutLeaves;
            averageISSizeWithSingletonLeaves += (double) oracleISSizeWithSingletonLeaves / fullISSizeWithSingletonLeaves;
            averageSequencesSize += (double) algConfig.getAllSequences().size() / fullSequencesSize;
            averageLPSize += (double) oracleFinalLPSize / fullFinalLPSize;

            double lpSizeRatio = (double) oracleFinalLPSize / fullFinalLPSize;
            if (lpSizeRatio < minRGSize){
                minRGSize = lpSizeRatio;
                minRGSizeSeed = seed;
            }

            expectedGadgetDepth += oracle.getExpectedGadgetDepth();
            expectedGadgetSize += oracle.getExpectedGadgetSize();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

//            System.out.println("Average restricted game ratios = " + restrictedGameRatioI / (seed - startingSeed + 1) + " ; " + restrictedGameRatioII / (seed - startingSeed + 1));
            System.out.println("Average IS size w/ leaves = " + (averageISSizeWithoutLeaves / (seed - startingSeed + 1)));
            System.out.println("Average IS size w 1-leaves = " + (averageISSizeWithSingletonLeaves / (seed - startingSeed + 1)));
            System.out.println("Average raw IS size = " + (averageRawISSize / (seed - startingSeed + 1)));
            System.out.println("Average # of seqs = " + (averageSequencesSize / (seed - startingSeed + 1)));
            System.out.println("Average gadget depth = " + (expectedGadgetDepth / (seed - startingSeed + 1)));
            System.out.println("Average gadget size = " + (expectedGadgetSize / (seed - startingSeed + 1)));
            System.out.println("Average LPSize = " + (averageLPSize / (seed - startingSeed + 1)));
            System.out.println("Final number of cons = " + oracle.getClass().getSimpleName() + " : " + oracleFinalLPSize +
                    " ; " + fullLPName + " : " + fullFinalLPSize);
            System.out.println(fullLPName + " time = " + fullTime + " ; " + oracle.getClass().getSimpleName() + " time = " + oracleTime);
            System.out.println("Time ratio = " + ((double) oracleTime / fullTime));
            if (oracle instanceof GadgetSefceLP)
                System.out.println("Gadget making time = " + ((GadgetSefceLP) oracle).getOverallGadgetMakingTime() / 1000000l);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
            System.out.println(minRGSize + " / " + minRGSizeSeed);
            oracle = null;
            runner = null;
            System.gc();
        }
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        ArrayList<Double> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) / (double)oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
        double sum = 0.0; for (double d : times) sum += d;
        System.out.println("Average = " + (sum/times.size()));
    }

    public static GameState initGame(FlipItGameInfo gameInfo, int seed) {
        gameInfo.ZERO_SUM_APPROX = false;

        int rounding = 3;
        final double MAX_COST = 10, MAX_REWARD = 10;//MAX_COST;
        int numberOfNodes = (new Graph(gameInfo.graphFile)).getAllNodes().size();//Integer.parseInt(gameInfo.graphFile.substring(gameInfo.graphFile.length() - 5, gameInfo.graphFile.length() - 4));
//        System.out.println(numberOfNodes);
        HighQualityRandom random = new HighQualityRandom(seed);
        double[] costs = new double[numberOfNodes];
        double[] rewards = new double[numberOfNodes];
        if(FlipItGameInfo.GENERATE_NODES_UNIFORMLY){
            for (int i = 0; i < numberOfNodes; i++) {
                rewards[i] = 1 + random.nextInt(100);
                if(FlipItGameInfo.REWARD_ALWAYS_HIGHER)
                    costs[i] = random.nextInt((int)rewards[i]);
                else
                    costs[i] = 1 + random.nextInt(100);
            }
        }
        else{
            for (int i = 0; i < numberOfNodes; i++) {
                int type = random.nextInt(4);
                switch (type) {
                    // HW [6..10], HC[6..10]
                    case 0:
                        costs[i] = 6 + random.nextInt(5);
                        rewards[i] = 6 + random.nextInt(5);
                        break;
                    // HW[6..10], LC[3..6]
                    case 1:
                        costs[i] = 3 + random.nextInt(4);
                        rewards[i] = 6 + random.nextInt(5);
                        break;
                    // LW[3..6], HC[5..9]
                    case 2:
                        costs[i] = 5 + random.nextInt(5);
                        rewards[i] = 3 + random.nextInt(4);
                        break;
                    // LW[1..4], LC[1..4]
                    case 3:
                        costs[i] = 1 + random.nextInt(4);
                        rewards[i] = 1 + random.nextInt(4);
                        break;
                }
        }

//            costs[i] = Math.round(((int) Math.pow(10, rounding)) * MAX_COST * random.nextDouble()) / Math.pow(10, rounding);
//            rewards[i] = Math.round(((int) Math.pow(10, rounding)) * MAX_REWARD * random.nextDouble()) / Math.pow(10, rounding);
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
