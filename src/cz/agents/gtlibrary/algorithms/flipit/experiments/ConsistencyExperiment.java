package cz.agents.gtlibrary.algorithms.flipit.experiments;

import cz.agents.gtlibrary.algorithms.flipit.bayesian.BayesianStackelbergRunner;
import cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.SumForbiddingBayesianStackelbergLP;
import cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.gadget.SFGadgetBayesianStackelbergLP;
import cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.gadget.ShallowestBrokenCplexGadgetBayesianStackelbergLP;
import cz.agents.gtlibrary.algorithms.flipit.bayesian.milp.BayesianStackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergRunner;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.LeaderGenerationConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.tables.GadgetLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Triplet;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jakub Cerny on 20/09/2017.
 */
public class ConsistencyExperiment {

    final static int LEADER = 0;
    final static int depth = 3;
    final static boolean USE_MILP = false;
//    static boolean pureStrategies = false;

    public static void main(String[] args) {
        String[] spec = new String[]{"2", "5", "2", "F", "2", "F"};
        if (args.length == 0) {
//        runFlipIt(args);
            runFlipIt(spec);
//            runFlipItOneSeed(spec);
//            runFlipItwithMilp(spec);
//            runOneSeed(spec, 9);
        } else {
            runFlipItOneSeed(args);
        }
    }

    public static void runFlipItOneSeed(String[] args) {
        boolean runComplete = true;
        FlipItGameInfo gameInfo;
        int seed = 10;
        String algversion = "";
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int numTypes = 2;
            int depth = Integer.parseInt(args[1]);
            int graphSize = Integer.parseInt(args[2]);
            String graphFile = "flipit_empty" + graphSize + ".txt";
            seed = Integer.parseInt(args[4]);
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, numTypes, graphFile, seed);
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
                algversion = args[5];

            }
        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState;

//        System.out.println("Numtypes: " + FlipItGameInfo.numTypes);

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;
        long rgGenerationTime = 0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        StackelbergConfig algConfig = null;// = new StackelbergConfig(rootState);
        System.out.println();
        System.out.println("Running seed " + (seed));

        rootState = initGame(gameInfo, seed);
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        double fullGameGV = 0.0;
        double oracleGameGV = 0.0;
        long startGeneration;
        BayesianStackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        if (algversion.equals("O")) {

            algConfig = new LeaderGenerationConfig(rootState);//StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
//            SFGadgetBayesianStackelbergLP oracle = new SFGadgetBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo);
            SFGadgetBayesianStackelbergLP oracle = new ShallowestBrokenCplexGadgetBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo);
//            LeaderGenerationBayesianStackelbergLP oracle = new LeaderGenerationBayesianStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
            runner.generate(rootState.getAllPlayers()[LEADER], oracle);
//            restrictedGameRatio += oracle.getRestrictedGameRatio();
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();
            lpGenerationTime += oracle.getOverallConstraintGenerationTime();
            lpSolvingTime += oracle.getOverallConstraintLPSolvingTime();
            System.out.println("Bnb count = " + oracle.getBnbBranchingCount());
            System.out.println("Sefce generation count = " + oracle.getSefceIterations());
            System.out.println("Expanding within bnb count = " + oracle.getExpandingAfterBnB());
            System.out.println("Sefce generation time = " + oracle.getSolvingForSefceTime()/ 1000000l);
            System.out.println("Broken strategy identification time = " + oracle.getBrokenStrategyIdentificationTime()/ 1000000l);
            System.out.println(runner.getGameValue());
            System.out.println(oracleTime);
        }
//            brokenStrategyFindingTime += oracle.getBrokenStrategyIdentificationTime();
//            deviationsFindingTime += oracle.getDeviationIdentificationTime();
//            rgGenerationTime += oracle.getRestrictedGameGenerationTime();

        if (algversion.equals("F")) {
            algConfig = new StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            SumForbiddingBayesianStackelbergLP full = new SumForbiddingBayesianStackelbergLP(gameInfo, expander);
            runner.generate(rootState.getAllPlayers()[LEADER], full);
//                runner.generate(rootState.getAllPlayers()[LEADER], new BayesianStackelbergSequenceFormMILP(rootState.getAllPlayers(), FlipItGameInfo.DEFENDER, FlipItGameInfo.ATTACKER, gameInfo, expander));

//                full.getLpTable().compareExistingConstraints(oTable);
//                oTable.compareExistingConstraints(full.getLpTable());
//                oracleTable.compareExistingConstraints(full.getLpTable());
            fullGameGV = runner.getGameValue();
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            System.out.println(full.getLPInvocationCount());
            System.out.println(runner.getGameValue());
            System.out.println(fullTime);
        }
    }

    public static void runFlipIt(String[] args) {
        boolean runComplete = true;
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int numTypes = Integer.parseInt(args[0]);
            int depth = Integer.parseInt(args[1]);
            int graphSize = Integer.parseInt(args[2]);
            String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, numTypes, graphFile, 10);
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

//        System.out.println("Numtypes: " + FlipItGameInfo.numTypes);

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;
        long rgGenerationTime = 0;

        int vanillaBnBIterations = 0;
        int gadgetBnBIterations = 0;
        int gadgetSefceIterations = 0;
        int gadgetSefceAfterBnBIterations = 0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        StackelbergConfig algConfig = null;// = new StackelbergConfig(rootState);

        int startseed = 26;
        maxseed += startseed;

        for (int seed = startseed; seed < maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            gameInfo.setFollowerTypes(seed);
            rootState = initGame(gameInfo, seed);
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


            algConfig = new StackelbergConfig(rootState);
            FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

            double fullGameGV = 0.0;
            double oracleGameGV = 0.0;

            BayesianStackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();

            algConfig = new LeaderGenerationConfig(rootState);//StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            SFGadgetBayesianStackelbergLP oracle;
            if (!USE_MILP)
                oracle = new SFGadgetBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo);
            else
                oracle = new ShallowestBrokenCplexGadgetBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo);

//            LeaderGenerationBayesianStackelbergLP oracle = new LeaderGenerationBayesianStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
            runner.generate(rootState.getAllPlayers()[LEADER], oracle);
            restrictedGameRatio += oracle.getRestrictedGameRatio();
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();
            lpGenerationTime += oracle.getOverallConstraintGenerationTime();
            lpSolvingTime += oracle.getOverallConstraintLPSolvingTime();
            brokenStrategyFindingTime += oracle.getBrokenStrategyIdentificationTime();
            rgGenerationTime += oracle.getSolvingForSefceTime();
            gadgetBnBIterations += oracle.getBnbBranchingCount();
            gadgetSefceAfterBnBIterations += oracle.getExpandingAfterBnB();
            gadgetSefceIterations += oracle.getSefceIterations();
//            brokenStrategyFindingTime += oracle.getBrokenStrategyIdentificationTime();
//            deviationsFindingTime += oracle.getDeviationIdentificationTime();
//            rgGenerationTime += oracle.getRestrictedGameGenerationTime();

//            algConfig = new StackelbergConfig(rootState);
//            expander = new FlipItExpander<>(algConfig);
//            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
//            SumForbiddingBayesianStackelbergLP full = new SumForbiddingBayesianStackelbergLP(gameInfo, expander);
//            runner.generate(rootState.getAllPlayers()[LEADER], full);
//            oracleGameGV = runner.getGameValue();

            if (runComplete) {
                startGeneration = threadBean.getCurrentThreadCpuTime();
                algConfig = new StackelbergConfig(rootState);
                expander = new FlipItExpander<>(algConfig);
                runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
                SumForbiddingBayesianStackelbergLP full = new SumForbiddingBayesianStackelbergLP(gameInfo, expander);
                runner.generate(rootState.getAllPlayers()[LEADER], full);
//                runner.generate(rootState.getAllPlayers()[LEADER], new BayesianStackelbergSequenceFormMILP(rootState.getAllPlayers(), FlipItGameInfo.DEFENDER, FlipItGameInfo.ATTACKER, gameInfo, expander));

//                full.getLpTable().compareExistingConstraints(oTable);
//                oTable.compareExistingConstraints(full.getLpTable());
//                oracleTable.compareExistingConstraints(full.getLpTable());
                fullGameGV = runner.getGameValue();
                vanillaBnBIterations += full.getLPInvocationCount();
            }
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            System.out.println("Average restricted game ratio = " + restrictedGameRatio / (seed - startseed + 1));
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
        System.out.println("Full game time = " + fullTime + "; oracle time = " + oracleTime);
        System.out.println("Average oracle time = " + oracleTime / (double) (maxseed - startseed));
        System.out.println("Expected bnb iterations = " + vanillaBnBIterations / (double) (maxseed - startseed) + " / " + gadgetBnBIterations / (double) (maxseed - startseed));
        System.out.println("Expected sefce iterations = " + gadgetSefceIterations / (double) (maxseed - startseed));
        System.out.println("Expected expanding in bnb count = " + gadgetSefceAfterBnBIterations / (double) (maxseed - startseed));
        System.out.println("Cplex building time (BnB / SO / SMO): " + lpGenerationTime / 1000000l / (double) (maxseed - startseed));
        System.out.println("Cplex solving time (BnB / SO / SMO): " + lpSolvingTime / 1000000l / (double) (maxseed - startseed));
//        System.out.println("Branching count (SO / SMO): " + oracle.getBnbBranchingCount() + " / " + matrixOracle.getBnbBranchingCount());
//        System.out.println("RG resizing count (SO / SMO): " + oracle.getGenerationCount() + " / " + matrixOracle.getGenerationCount());
//        System.out.println("RG size: " + restrictedGameRatio / (double) maxseed);
        System.out.println("RG generation time (SO / SMO): " + rgGenerationTime / 1000000l / (double) (maxseed - startseed));
        System.out.println("Bnb time (SO / SMO): " + (oracleTime / (double) (maxseed - startseed) - rgGenerationTime / 1000000l / (double) (maxseed - startseed)));
//        System.out.println("Deviation identification time (SO / SMO): " + deviationsFindingTime / 1000000l / (double) maxseed);
        System.out.println("Broken strategy identification time (SO / SMO): " + brokenStrategyFindingTime / 1000000l / (double) (maxseed - startseed));
        System.out.println("Number of ISs: " + algConfig.getAllInformationSets().size());
    }


    public static void runFlipItWithTableComparing(String[] args) {
        boolean runComplete = true;
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int numTypes = Integer.parseInt(args[0]);
            int depth = Integer.parseInt(args[1]);
            int graphSize = Integer.parseInt(args[2]);
            String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, numTypes, graphFile, 10);
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

//        System.out.println("Numtypes: " + FlipItGameInfo.numTypes);

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        double restrictedGameRatio = 0.0;

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        long fullTime = 0;
        long oracleTime = 0;

        long lpGenerationTime = 0;
        long lpSolvingTime = 0;
        long brokenStrategyFindingTime = 0;
        long deviationsFindingTime = 0;
        long rgGenerationTime = 0;


        ArrayList<Long> fullTimes = new ArrayList<>();
        ArrayList<Long> oracleTimes = new ArrayList<>();

        StackelbergConfig algConfig = null;// = new StackelbergConfig(rootState);

        int startseed = 26;
        maxseed += startseed;

        for (int seed = startseed; seed < maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            rootState = initGame(gameInfo, seed);
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


            algConfig = new StackelbergConfig(rootState);
            FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

            double fullGameGV = 0.0;
            double oracleGameGV = 0.0;

            BayesianStackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();

            algConfig = new LeaderGenerationConfig(rootState);//StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            SFGadgetBayesianStackelbergLP oracle = new SFGadgetBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo);
//            LeaderGenerationBayesianStackelbergLP oracle = new LeaderGenerationBayesianStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
            runner.generate(rootState.getAllPlayers()[LEADER], oracle);
            restrictedGameRatio += oracle.getRestrictedGameRatio();
            oracleGameGV = runner.getGameValue();
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();
            lpGenerationTime += oracle.getOverallConstraintGenerationTime();
            lpSolvingTime += oracle.getOverallConstraintLPSolvingTime();
//            brokenStrategyFindingTime += oracle.getBrokenStrategyIdentificationTime();
//            deviationsFindingTime += oracle.getDeviationIdentificationTime();
//            rgGenerationTime += oracle.getRestrictedGameGenerationTime();
            LPTable oracleTable = oracle.getLpTable();

            algConfig = new LeaderGenerationConfig(rootState);//StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            SFGadgetBayesianStackelbergLP oracle2 = new SFGadgetBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo);
//            LeaderGenerationBayesianStackelbergLP oracle = new LeaderGenerationBayesianStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
            runner.generate(rootState.getAllPlayers()[LEADER], oracle2);
            LPTable oTable = oracle2.getLpTable();

            if (runComplete) {
                algConfig = new StackelbergConfig(rootState);
                expander = new FlipItExpander<>(algConfig);
                runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
                SumForbiddingBayesianStackelbergLP full = new SumForbiddingBayesianStackelbergLP(gameInfo, expander, oracleTable);
                runner.generate(rootState.getAllPlayers()[LEADER], full);
//                runner.generate(rootState.getAllPlayers()[LEADER], new BayesianStackelbergSequenceFormMILP(rootState.getAllPlayers(), FlipItGameInfo.DEFENDER, FlipItGameInfo.ATTACKER, gameInfo, expander));

//                full.getLpTable().compareExistingConstraints(oTable);
//                oTable.compareExistingConstraints(full.getLpTable());
//                oracleTable.compareExistingConstraints(full.getLpTable());
                fullGameGV = runner.getGameValue();

                algConfig = new LeaderGenerationConfig(rootState);//StackelbergConfig(rootState);
                expander = new FlipItExpander<>(algConfig);
                runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
                startGeneration = threadBean.getCurrentThreadCpuTime();
                Map<Object, Integer> primalWatch = new HashMap<>();
                int i = 0;
                for (Object o : oTable.getPrimalWatch().keySet()) {
                    i++;
                    if (!oTable.getPrimalWatch().containsKey(o)) {
                        System.out.println("c");
                        System.exit(0);
                    }
                    if (o instanceof Triplet && ((Triplet) o).getFirst() instanceof Sequence) {
//                        System.out.println(oracleTable.getPrimalWatch().size() + " / " + oTable.getPrimalWatch().size());
                        for (Object v : oracleTable.getPrimalWatch().keySet())
                            if (o.equals(v)) {
//                                System.out.printf(i + " ");
//                                System.out.println(o + " / " + o.hashCode());
//                                System.out.println(v + " / " + v.hashCode());
//                                System.out.println("///////");
                                if (!o.toString().equals(v.toString()) || o.hashCode() != v.hashCode()) {
                                    System.out.println("ch");
                                    System.exit(0);
                                }
                                Sequence first = new ArrayListSequenceImpl((Sequence) ((Triplet) o).getFirst());
                                Sequence second = null;
                                second = (Sequence) ((Triplet) v).getSecond();
                                Triplet<Sequence, Sequence, FollowerType> key = new Triplet<>(first, second, ((FollowerType) ((Triplet) o).getThird()));
//                                primalWatch.put(key, oTable.getPrimalWatch().get(o).intValue());
                                if (i == 81 || i == 9) {
                                    Sequence secondO = (Sequence) ((Triplet) o).getSecond();
                                    for (Action a : secondO) {
                                        FlipItAction fa = (FlipItAction) a;
                                        System.out.println(fa.hashCode() + " " + fa.getController() + " " + fa.getControlNode() + " " + fa.isNoop() + " " + fa.getInformationSet().hashCode() + " " + System.identityHashCode(fa));
                                    }
                                    for (Action a : second) {
                                        FlipItAction fa = (FlipItAction) a;
                                        System.out.println(fa.hashCode() + " " + fa.getController() + " " + fa.getControlNode() + " " + fa.isNoop() + " " + fa.getInformationSet().hashCode() + " " + System.identityHashCode(fa));
                                    }
//                                    System.exit(0);
                                }
//                                {
//                                    if (i==81 || i == 9){
//                                        System.out.println(o);
////                                        System.out.println((Sequence) ((Triplet) v).getSecond());
//                                        second = (Sequence) ((Triplet) v).getSecond();
//                                        Triplet<Sequence, Sequence, FollowerType> key = new Triplet<>(first, second, ((FollowerType) ((Triplet) o).getThird()));
//                                        primalWatch.put(key, oTable.getPrimalWatch().get(o).intValue());
//                                    }
//                                    else second = (Sequence) ((Triplet) o).getSecond();
////                                    else second = new ArrayListSequenceImpl((Sequence) ((Triplet) v).getSecond());
//                                }
//                                if(second.toString().equals("Attacker: [(N0:10916370)]")) System.out.println(i);
                            }
                    }
                }
//                System.out.println(primalWatch.size());
//                System.exit(0);
                SFGadgetBayesianStackelbergLP oracle3 = new SFGadgetBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo, (GadgetLPTable) oTable, primalWatch);
//            LeaderGenerationBayesianStackelbergLP oracle = new LeaderGenerationBayesianStackelbergLP(rootState.getAllPlayers()[LEADER], gameInfo, false, true);
                runner.generate(rootState.getAllPlayers()[LEADER], oracle3);
//                oracleGameGV = runner.getGameValue();

            }
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

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
        System.out.println("Full game time = " + fullTime + "; oracle time = " + oracleTime);
        System.out.println("Cplex building time (BnB / SO / SMO): " + lpGenerationTime / 1000000l / (double) maxseed);
        System.out.println("Cplex solving time (BnB / SO / SMO): " + lpSolvingTime / 1000000l / (double) maxseed);
//        System.out.println("Branching count (SO / SMO): " + oracle.getBnbBranchingCount() + " / " + matrixOracle.getBnbBranchingCount());
//        System.out.println("RG resizing count (SO / SMO): " + oracle.getGenerationCount() + " / " + matrixOracle.getGenerationCount());
        System.out.println("RG size: " + restrictedGameRatio / (double) maxseed);
        System.out.println("RG generation time (SO / SMO): " + rgGenerationTime / 1000000l / (double) maxseed);
        System.out.println("Deviation identification time (SO / SMO): " + deviationsFindingTime / 1000000l / (double) maxseed);
        System.out.println("Broken strategy identification time (SO / SMO): " + brokenStrategyFindingTime / 1000000l / (double) maxseed);
        System.out.println("Number of ISs: " + algConfig.getAllInformationSets().size());
    }

    public static GameState initGame(FlipItGameInfo gameInfo, int seed) {
        gameInfo.ZERO_SUM_APPROX = false;

//        System.out.println("Numtypes: " + FlipItGameInfo.numTypes);

        int rounding = 1;
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

//        System.out.println("Numtypes: " + FlipItGameInfo.numTypes);

        return rootState;
    }

    public static void runFlipItwithMilp(String[] args) {
        boolean runComplete = true;
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int numTypes = Integer.parseInt(args[0]);
            int depth = Integer.parseInt(args[1]);
            int graphSize = Integer.parseInt(args[2]);
            String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, numTypes, graphFile, 10);
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

//        System.out.println("Numtypes: " + FlipItGameInfo.numTypes);

        ArrayList<Integer> notConvergedSeeds = new ArrayList<>();
        ArrayList<Integer> unboundedSeeds = new ArrayList<>();
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

        int milpdiff = 0;
        int bnbdiff = 0;

        for (int seed = 0; seed < maxseed; seed++) {
            System.out.println();
            System.out.println("Running seed " + (seed) + " of " + (maxseed - 1));

            rootState = initGame(gameInfo, seed);
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

            double fullGameGV = 0.0;
            double oracleGameGV = 0.0;

            BayesianStackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

            long startGeneration = threadBean.getCurrentThreadCpuTime();
            if (runComplete) {
                runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
                runner.generate(rootState.getAllPlayers()[LEADER], new SumForbiddingBayesianStackelbergLP(gameInfo, expander));

                fullGameGV = runner.getGameValue();
            }
            fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

            algConfig = new StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            StackelbergRunner srunner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            StackelbergSequenceFormMILP smilp = new StackelbergSequenceFormMILP(gameInfo.getAllPlayers(), FlipItGameInfo.DEFENDER, FlipItGameInfo.ATTACKER, gameInfo, expander);
            srunner.generate(rootState.getAllPlayers()[LEADER], smilp);
            double sval = srunner.getGameValue();

            algConfig = new StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);
            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
            startGeneration = threadBean.getCurrentThreadCpuTime();
            BayesianStackelbergSequenceFormMILP milp = new BayesianStackelbergSequenceFormMILP(gameInfo.getAllPlayers(), FlipItGameInfo.DEFENDER, FlipItGameInfo.ATTACKER, gameInfo, expander);
            runner.generate(rootState.getAllPlayers()[LEADER], milp);
//            restrictedGameRatio += oracle.getRestrictedGameRatio();
            oracleGameGV = runner.getGameValue();
            if (oracleGameGV == Double.NEGATIVE_INFINITY) unboundedSeeds.add(seed);
            oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
            oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

            if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
                notConvergedSeeds.add(seed);

            if (Math.abs(sval - fullGameGV) > 0.001)
                bnbdiff++;

            if (Math.abs(oracleGameGV - sval) > 0.001)
                milpdiff++;


            System.out.println("Average restricted game ratio = " + restrictedGameRatio / (seed + 1));
            System.out.println("BnB game time = " + fullTime + "; milp time = " + oracleTime);
            System.out.println("Number of not converged = " + notConvergedSeeds.size());
        }
        System.out.println("Not converged seeds = " + notConvergedSeeds.toString());
        System.out.println("Unbounded seeds = " + unboundedSeeds.toString());
        System.out.println("Milp diff count= " + milpdiff);
        System.out.println("Bnb diff count= " + bnbdiff);
        ArrayList<Long> times = new ArrayList<>();
        for (int i = 0; i < fullTimes.size(); i++)
            times.add(fullTimes.get(i) - oracleTimes.get(i));
        System.out.println("Min = " + Collections.min(times));
        System.out.println("Max = " + Collections.max(times));
        Collections.sort(times);
        System.out.println("Median = " + times.get(Math.round(times.size() / 2)));
    }

    public static void runOneSeed(String[] args, int seed) {
        boolean runComplete = true;
        boolean outputStrategy = true;
        FlipItGameInfo gameInfo;
        int maxseed = 10;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int numTypes = Integer.parseInt(args[0]);
            int depth = Integer.parseInt(args[1]);
            int graphSize = Integer.parseInt(args[2]);
            String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, numTypes, graphFile, 10);
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

//        System.out.println("Numtypes: " + FlipItGameInfo.numTypes);

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

//        for (int seed = 0; seed < maxseed; seed++) {
        System.out.println();
        System.out.println("Running seed " + (seed));

        rootState = initGame(gameInfo, seed);
//            gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo(depth, bf, seed);
//            rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();


        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);

        double fullGameGV = 0.0;
        double oracleGameGV = 0.0;

        BayesianStackelbergRunner runner;// = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

        long startGeneration = threadBean.getCurrentThreadCpuTime();
        if (runComplete) {
            runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
            SumForbiddingBayesianStackelbergLP bnb = new SumForbiddingBayesianStackelbergLP(gameInfo, expander);
            bnb.setOUTPUT_STRATEGY(outputStrategy);
            runner.generate(rootState.getAllPlayers()[LEADER], bnb);

            fullGameGV = runner.getGameValue();
        }
        fullTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        fullTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);

        algConfig = new StackelbergConfig(rootState);
        expander = new FlipItExpander<>(algConfig);
        runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
        startGeneration = threadBean.getCurrentThreadCpuTime();
        BayesianStackelbergSequenceFormMILP milp = new BayesianStackelbergSequenceFormMILP(gameInfo.getAllPlayers(), FlipItGameInfo.DEFENDER, FlipItGameInfo.ATTACKER, gameInfo, expander);
        milp.setOUTPUT_STRATEGY(outputStrategy);
        runner.generate(rootState.getAllPlayers()[LEADER], milp);
//            restrictedGameRatio += oracle.getRestrictedGameRatio();
        oracleGameGV = runner.getGameValue();
        oracleTime += (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;
        oracleTimes.add((threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l);
//            restrictedGameRatio += runner.getRestrictedGameRatio();

        if (Math.abs(oracleGameGV - fullGameGV) > 0.001)
            notConvergedSeeds.add(seed);

        System.out.println("Average restricted game ratio = " + restrictedGameRatio / (seed + 1));
        System.out.println("BnB game time = " + fullTime + "; milp time = " + oracleTime);
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


}
