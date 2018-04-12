package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponseImpl;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.imperfectrecall.IRBPGGameState;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.ir.IROshiZumoGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.ir.IRGenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.ir.IRKuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomabstraction.P1RandomAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionExpander;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr.CPRRGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.*;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.DoubleOracleIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.DoubleOracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.utils.StrategyLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

public class BilinearSequenceFormBnB {
    public static boolean DEBUG = false;
    public static boolean SAVE_LPS = false;
    public static boolean USE_INVALID_CUTS = true;
    public static boolean USE_DUPLICITY_CUTS = true;
    public static boolean USE_BINARY_HALVING = true;
    public static double EPS = 1e-3;

    protected final Player player;
    protected final Player opponent;

    protected ImperfectRecallBestResponseImpl br;
    protected BilinearTable table;
    protected Expander<SequenceFormIRInformationSet> expander;
    protected GameInfo gameInfo;
    protected Double finalValue = null;
    protected int maxRefinements = 7;

    protected Candidate currentBest;

    protected Action mostBrokenAction;
    protected double mostBrokenActionValue;
    protected StrategyLP strategyLP;

    protected Map<Changes, Double> ubs;

    protected ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
    protected long CPLEXTime = 0;
    protected long strategyLPTime = 0;
    protected long CPLEXInvocationCount = 0;
    protected long BRTime = 0;
    protected long lpBuildingTime = 0;
    protected int cuts;
    protected int invalidCuts = 0;

    public static void main(String[] args) {
//        new Scanner(System.in).next();
//        runRandomGame();
//        runCPRRAbstractedRandomGame();
//        runAbstractedRandomGame();
//        runKuhnPoker();
//        runGenericPoker();
        runBPG();
//        runBRTest();
//        runOZ();
    }

    public static double runCPRRAbstractedRandomGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> wrappedConfig = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(wrappedConfig);
        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), wrappedConfig);
        efg.generateCompleteGame();

        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo()));
        GameState root = new P1RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, config);
        CPRRExpander<SequenceFormIRInformationSet> cprrExpander = new CPRRExpander<>(expander);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, cprrExpander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder.build(new CPRRGameState(root), cprrExpander.getAlgorithmConfig(), cprrExpander);
        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", new CPRRGameState(root), cprrExpander);

        solver.setExpander(expander);
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("P1 sequence count: " + config.getSequencesFor(RandomGameInfo.FIRST_PLAYER).size());
        System.out.println("P2 sequence count: " + config.getSequencesFor(RandomGameInfo.SECOND_PLAYER).size());
        System.out.println("Information set count: " + config.getAllInformationSets().size());
        return solver.finalValue;
    }

    public static double runAbstractedRandomGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> wrappedConfig = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(wrappedConfig);
        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), wrappedConfig);
        efg.generateCompleteGame();

        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo()));
        GameState root = new P1RandomAbstractionGameStateFactory().createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, config);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, expander, new RandomAbstractionGameInfo(new RandomGameInfo()));

        cz.agents.gtlibrary.domain.randomgameimproved.io.BasicGameBuilder.build(root, expander.getAlgorithmConfig(), expander);
        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("P1 sequence count: " + config.getSequencesFor(RandomGameInfo.FIRST_PLAYER).size());
        System.out.println("P2 sequence count: " + config.getSequencesFor(RandomGameInfo.SECOND_PLAYER).size());
        System.out.println("Information set count: " + config.getAllInformationSets().size());
        return solver.finalValue;
    }

    public static double runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new RandomGameInfo());
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, expander, new RandomGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());

        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        return solver.finalValue;
    }

    public static double runBPG() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BPGGameInfo());
        GameState root = new IRBPGGameState();
        Expander<SequenceFormIRInformationSet> expander = new BPGExpander<>(config);

        builder.build(root, config, expander);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(BPGGameInfo.DEFENDER, expander, new BPGGameInfo());

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        System.out.println("Sequences P2: " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());

        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        return solver.finalValue;
    }

    public static double runOZ() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new OZGameInfo());
        GameState root = new IROshiZumoGameState();
        Expander<SequenceFormIRInformationSet> expander = new OshiZumoExpander<>(config);

        builder.build(root, config, expander);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(OZGameInfo.FIRST_PLAYER, expander, new OZGameInfo());

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        System.out.println("Sequences P2: " + config.getSequencesFor(OZGameInfo.SECOND_PLAYER).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());

        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(OZGameInfo.FIRST_PLAYER).size() + ", " + config.getSequencesFor(OZGameInfo.SECOND_PLAYER).size());
        return solver.finalValue;
    }




    public static double runKuhnPoker() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BPGGameInfo());
        GameState root = new IRKuhnPokerGameState();
        Expander<SequenceFormIRInformationSet> expander = new KuhnPokerExpander<>(config);

        builder.build(root, config, expander);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(KPGameInfo.FIRST_PLAYER, expander, new KPGameInfo());

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());

        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        return solver.finalValue;
    }

    public static double runGenericPoker() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new GPGameInfo());
        GameState root = new IRGenericPokerGameState();
        Expander<SequenceFormIRInformationSet> expander = new GenericPokerExpander<>(config);

        builder.build(root, config, expander);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(GPGameInfo.FIRST_PLAYER, expander, new GPGameInfo());

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());

        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        return solver.finalValue;
    }


    protected static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);

        builder.build(new BRTestGameState(), config, expander);
        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        solver.solve(config);
    }

    public BilinearSequenceFormBnB(Player player, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.table = new BilinearTable();
        this.player = player;
        this.opponent = info.getOpponent(player);
        this.gameInfo = info;
        this.expander = expander;
        br = new ImperfectRecallBestResponseImpl(opponent, expander, gameInfo);
        ubs = new HashMap<>();
    }

    public void solve(SequenceFormIRConfig config) {
        strategyLP = new StrategyLP(config);
        long buildingTimeStart = mxBean.getCurrentThreadCpuTime();

        buildBaseLP(table, config);
        lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
        try {
            LPData lpData = table.toCplex();

            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
            long start = mxBean.getCurrentThreadCpuTime();

            lpData.getSolver().solve();
            CPLEXInvocationCount++;
            CPLEXTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());
            Queue<Candidate> fringe = new PriorityQueue<>();

            currentBest = createCandidate(lpData, config);
            if (DEBUG) System.out.println("most violated action: " + currentBest.getAction());
            if (DEBUG) System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getLb());

            if (isConverged(currentBest)) {
                finalValue = currentBest.getLb();
                return;
            }
            fringe.add(currentBest);
            int it = 0;

            while (!fringe.isEmpty()) {
                it++;
                Candidate current = pollCandidateWithUBHigherThanBestLB(fringe);

                System.out.println(current + " vs " + currentBest);
//                System.out.println(current.getChanges());
                if (isConverged(current)) {
                    currentBest = current;
                    System.out.println(current);
                    break;
                }
                if (Math.abs(currentBest.getLb() - current.getUb()) < 1e-4 * gameInfo.getMaxUtility()) {
                    System.out.println(current);
                    break;
                }

                addMiddleChildOf(current, fringe, config);
                addLeftChildOf(current, fringe, config);
                addRightChildOf(current, fringe, config);
            }
            finalValue = currentBest.getLb();
            Map<Sequence, Double> rp = ((OracleCandidate) currentBest).getMaxPlayerRealPlan();

            System.out.println("Support: " + rp.values().stream().filter(v -> v > 1e-8).count());
//            table.clearTable();
//            buildBaseLP(config);
//            LPData checkData = table.toCplex();
//
//            checkData.getSolver().exportModel("modelAfterAlg.lp");
//            System.out.println(currentBest);
//            currentBest.getChanges().updateTable(table);
//            lpData = table.toCplex();
//
//            lpData.getSolver().solve();
//            Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);
//            assert definedEverywhere(p1Strategy, config);
//            assert equalsInPRInformationSets(p1Strategy, config, lpData);
//            assert isConvexCombination(p1Strategy, lpData, config);
//            double lowerBound = getLowerBound(p1Strategy);
//            double upperBound = getUpperBound(lpData);
//
//            System.out.println("UB: " + upperBound + " LB: " + lowerBound);
//            p1Strategy.entrySet().stream().forEach(System.out::println);
//            finalValue = lowerBound;
//            checkCurrentBestOnCleanLP(config);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    public long getCPLEXTime() {
        return CPLEXTime;
    }

    public long getStrategyLPTime() {
        return strategyLPTime;
    }

    public long getCPLEXInvocationCount() {
        return CPLEXInvocationCount;
    }

    public long getBRTime() {
        return BRTime;
    }

    public long getLpBuildingTime() {
        return lpBuildingTime;
    }

    protected double checkOnCleanLP(SequenceFormIRConfig config, Candidate candidate) throws IloException {
//        System.out.println("Check!!!!!!!!!!!!!!");
        BilinearTable table = new BilinearTable();
        buildBaseLP(table, config);
        LPData checkData = table.toCplex();

        checkData.getSolver().exportModel("cleanModel.lp");

        candidate.getChanges().updateTable(table);
        LPData lpData = table.toCplex();

        lpData.getSolver().solve();

//        Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);

//        assert definedEverywhere(p1Strategy, config);
//        assert equalsInPRInformationSets(p1Strategy, config, lpData);
//        assert isConvexCombination(p1Strategy, lpData, config);
//        double lowerBound = getLowerBound(p1Strategy);
        double upperBound = getUpperBound(lpData);
//        System.out.println("UB: " + upperBound + " LB: " + lowerBound);
//        p1Strategy.entrySet().forEach(System.out::println);
        return upperBound;
    }

    protected int[] getMiddleExactProbability(Candidate current) {
        int[] probability;

        if (current.getActionProbability()[0] == 1) {
            probability = new int[current.getActionProbability().length];
            System.arraycopy(current.getActionProbability(), 0, probability, 0, probability.length);
            probability[0] = 0;
            for (int i = 1; i < probability.length; i++) {
                probability[i] = 9;
            }
        } else {
            probability = current.getActionProbability();
        }
        return probability;
    }

    protected void addMiddleChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) {
        Changes newChanges = new Changes(current.getChanges());
        int[] probability = getMiddleExactProbability(current);
        Change change = new MiddleChange(current.getAction(), probability);
        long buildingTimeStart = mxBean.getCurrentThreadCpuTime();

        table.clearTable();
        if (!BilinearTable.DELETE_PRECISION_CONSTRAINTS_ONLY)
            buildBaseLP(table, config);
        current.getChanges().updateTable(table);
        lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
        int precision = table.getPrecisionFor(current.getAction());

        if (precision >= maxRefinements)
            return;
        updateChangesForMiddle(newChanges, change);
        if (USE_INVALID_CUTS && !newChanges.isValid()) {
            invalidCuts++;
            return;
        }
        if (USE_DUPLICITY_CUTS && ubs.containsKey(newChanges)) {
            cuts++;
            return;
        }
        assert !current.getChanges().contains(change);
        ubs.put(newChanges, 0d);
        applyNewChangeAndSolve(fringe, config, newChanges, change, current instanceof DoubleOracleCandidate ? (DoubleOracleCandidate) current : null);
    }

    protected void addRightChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) {
        table.clearTable();

        Changes newChanges = new Changes(current.getChanges());
        DigitArray probability = getRightExactProbability(current);
        if (probability.getArray()[0] == 1)
            return;
        Change change = new RightChange(current.getAction(), probability);
        long buildingTimeStart = mxBean.getCurrentThreadCpuTime();

        if (!BilinearTable.DELETE_PRECISION_CONSTRAINTS_ONLY)
            buildBaseLP(table, config);
        current.getChanges().updateTable(table);
        lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
        updateChangesForRight(newChanges, change);
        if (USE_INVALID_CUTS && !newChanges.isValid()) {
            invalidCuts++;
            return;
        }
        if (USE_DUPLICITY_CUTS && ubs.containsKey(newChanges)) {
            cuts++;
            return;
        }
        assert !current.getChanges().contains(change);
        ubs.put(newChanges, 0d);
        applyNewChangeAndSolve(fringe, config, newChanges, change, current instanceof DoubleOracleCandidate ? (DoubleOracleCandidate) current : null);
    }

    private void updateChangesForLeft(Changes newChanges, Change change) {
        newChanges.add(change);
//        Map<Action, DigitArray> lbs = new HashMap<>();
//        Map<Action, DigitArray> ubs = new HashMap<>();
//
//        for (Change oldChange : newChanges) {
//            updateBounds(lbs, ubs, oldChange);
//        }
//        Set<Action> actions = ((SequenceFormIRInformationSet) change.getAction().getInformationSet()).getActions();
//        DigitArray ubSum = getUBSum(actions, change.getAction(), ubs);
//        DigitArray needed = DigitArray.ONE.subtract(change.getFixedDigitArrayValue());
//
//        for (Action action : actions) {
//            if (action.equals(change.getAction()))
//                continue;
//            DigitArray currentUBSum = ubSum.subtract(ubs.getOrDefault(action, DigitArray.ONE));
//            DigitArray currentLB = needed.subtract(currentUBSum);
//
//            moveToProbabilityInterval(currentLB);
//            assert positive(currentLB);
//            if (currentLB.isGreaterThan(lbs.getOrDefault(action, DigitArray.ZERO)))
//                for (int i = 2; i <= currentLB.size(); i++) {
//                    newChanges.add(new RightChange(action, currentLB.getReducedPrecisionDigitArray(i)));
//                }
//        }
    }

    private boolean positive(DigitArray currentLB) {
        return currentLB.stream().allMatch(i -> i >= 0);
    }

    private void updateBounds(Map<Action, DigitArray> lbs, Map<Action, DigitArray> ubs, Change oldChange) {
        if (oldChange instanceof LeftChange) {
            DigitArray oldUB = ubs.get(oldChange.getAction());

            if (oldUB == null || oldUB.isGreaterThan(oldChange.getFixedDigitArrayValue()))
                ubs.put(oldChange.getAction(), oldChange.getFixedDigitArrayValue());
        } else if (oldChange instanceof RightChange) {
            DigitArray oldLB = lbs.get(oldChange.getAction());

            if (oldLB == null || oldChange.getFixedDigitArrayValue().isGreaterThan(oldLB))
                lbs.put(oldChange.getAction(), oldChange.getFixedDigitArrayValue());
        } else {
            DigitArray newUB = new DigitArray(oldChange.getFixedDigitArrayValue());
            DigitArray oldUB = ubs.get(oldChange.getAction());
            DigitArray oldLB = lbs.get(oldChange.getAction());

            for (int i = newUB.size() - 1; i >= 0; i--) {
                if (newUB.get(i) == 9) {
                    newUB.set(i, 0);
                } else {
                    newUB.set(i, newUB.get(i) + 1);
                    break;
                }
            }
            if (oldUB == null || oldUB.isGreaterThan(newUB))
                ubs.put(oldChange.getAction(), new DigitArray(newUB));
            if (oldLB == null || oldChange.getFixedDigitArrayValue().isGreaterThan(oldLB))
                lbs.put(oldChange.getAction(), oldChange.getFixedDigitArrayValue());
        }
    }

    private DigitArray getUBSum(Set<Action> actions, Action toIgnore, Map<Action, DigitArray> ubs) {
        DigitArray sum = DigitArray.ZERO;

        for (Action action : actions) {
            if (action.equals(toIgnore))
                continue;
            sum = sum.add(ubs.getOrDefault(action, DigitArray.ONE));
        }
        return sum;
    }


    private void updateChangesForRight(Changes newChanges, Change change) {
        newChanges.add(change);
//        Map<Action, DigitArray> lbs = new HashMap<>();
//        Map<Action, DigitArray> ubs = new HashMap<>();
//
//        for (Change oldChange : newChanges) {
//            updateBounds(lbs, ubs, oldChange);
//        }
//        Set<Action> actions = ((SequenceFormIRInformationSet) change.getAction().getInformationSet()).getActions();
//        DigitArray lbSum = getLBSum(actions, change.getAction(), lbs);
//        DigitArray limit = DigitArray.ONE.subtract(change.getFixedDigitArrayValue());
//
//        for (Action action : actions) {
//            if (action.equals(change.getAction()))
//                continue;
//            DigitArray currentLBSum = lbSum.subtract(lbs.getOrDefault(action, DigitArray.ZERO));
//            DigitArray currentUB = limit.subtract(currentLBSum);
//
//            moveToProbabilityInterval(currentUB);
//            assert positive(currentUB);
//            if (ubs.getOrDefault(action, DigitArray.ONE).isGreaterThan(currentUB))
//                for (int i = 2; i <= currentUB.size(); i++) {
//                    newChanges.add(new LeftChange(action, currentUB.getReducedPrecisionDigitArray(i)));
//                }
//        }
    }

    private void moveToProbabilityInterval(DigitArray digitArray) {
        if (!digitArray.isGreaterThan(DigitArray.ZERO)) {
            for (int i = 0; i < digitArray.size(); i++) {
                digitArray.set(i, 0);
            }
        } else if (digitArray.isGreaterThan(DigitArray.ONE)) {
            digitArray.set(0, 1);
            for (int i = 1; i < digitArray.size(); i++) {
                digitArray.set(i, 0);
            }
        }
        assert positive(digitArray);
    }

    private DigitArray getLBSum(Set<Action> actions, Action toIgnore, Map<Action, DigitArray> lbs) {
        DigitArray sum = DigitArray.ZERO;

        for (Action action : actions) {
            if (action.equals(toIgnore))
                continue;
            sum = sum.add(lbs.getOrDefault(action, DigitArray.ZERO));
        }
        return sum;
    }

    private void updateChangesForMiddle(Changes newChanges, Change change) {
        newChanges.add(change);
//        Map<Action, DigitArray> lbs = new HashMap<>();
//        Map<Action, DigitArray> ubs = new HashMap<>();
//
//        for (Change oldChange : newChanges) {
//            updateBounds(lbs, ubs, oldChange);
//        }
//        Set<Action> actions = ((SequenceFormIRInformationSet) change.getAction().getInformationSet()).getActions();
//        DigitArray lbSum = getLBSum(actions, change.getAction(), lbs);
//        DigitArray limit = DigitArray.ONE.subtract(change.getFixedDigitArrayValue());
//
//        for (Action action : actions) {
//            if (action.equals(change.getAction()))
//                continue;
//            DigitArray currentLBSum = lbSum.subtract(lbs.getOrDefault(action, DigitArray.ZERO));
//            DigitArray currentUB = limit.subtract(currentLBSum);
//
//            moveToProbabilityInterval(currentUB);
//            assert positive(currentUB);
//            if (ubs.getOrDefault(action, DigitArray.ONE).isGreaterThan(currentUB))
//                for (int i = 2; i <= currentUB.size(); i++) {
//                    newChanges.add(new LeftChange(action, currentUB.getReducedPrecisionDigitArray(i)));
//                }
//        }
//        DigitArray ubSum = getUBSum(actions, change.getAction(), ubs);
//        DigitArray needed = decrementLSD(DigitArray.ONE.subtract(change.getFixedDigitArrayValue()));
//
//        for (Action action : actions) {
//            if (action.equals(change.getAction()))
//                continue;
//            DigitArray currentUBSum = ubSum.subtract(ubs.getOrDefault(action, DigitArray.ONE));
//            DigitArray currentLB = needed.subtract(currentUBSum);
//
//            moveToProbabilityInterval(currentLB);
//            assert positive(currentLB);
//            if (currentLB.isGreaterThan(lbs.getOrDefault(action, DigitArray.ZERO)))
//                for (int i = 2; i <= currentLB.size(); i++) {
//                    newChanges.add(new RightChange(action, currentLB.getReducedPrecisionDigitArray(i)));
//                }
//        }
    }

    protected void applyNewChangeAndSolve(Queue<Candidate> fringe, SequenceFormIRConfig config, Changes newChanges, Change change, DoubleOracleCandidate oldCandidate) {
        try {
            long buildingTimeStart = mxBean.getCurrentThreadCpuTime();
            boolean updated = change.updateW(table);

            lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
            if (updated) {
                LPData lpData = table.toCplex();
//                System.out.println("solved");

                if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
                long start = mxBean.getCurrentThreadCpuTime();

                lpData.getSolver().solve();
//                System.out.println(CPLEXInvocationCount++);
                CPLEXTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
                if (DEBUG) {
                    System.out.println(lpData.getSolver().getStatus());
                    System.out.println(lpData.getSolver().getObjValue());
                    System.out.println(newChanges);
                }
                if (lpData.getSolver().getStatus().equals(IloCplex.Status.Optimal)) {
                    Candidate candidate = createCandidate(newChanges, lpData, config);

//                    assert Math.abs(candidate.getUb() - checkOnCleanLP(config, candidate)) < 1e-4;
                    if (DEBUG) System.out.println("Candidate: " + candidate + " vs " + currentBest);
                    if (candidate.getLb() > currentBest.getLb()) {
                        currentBest = candidate;
                        System.out.println("current best: " + currentBest);
                    }
                    if (isConverged(candidate)) {
                        if (candidate.getLb() > currentBest.getLb()) {
                            currentBest = candidate;
                            System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getUb());
                        }
                    } else if (candidate.getUb() > currentBest.getLb() + EPS) {
                        if (DEBUG) System.out.println("most violated action: " + candidate.getAction());

                        fringe.add(candidate);
                    }
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    protected Map<Object, Double> getSlackValues(LPData lpData) throws IloException {
        Map<Object, Double> slackVariables = new HashMap<>();

        for (Map.Entry<Object, IloRange> entry : lpData.getWatchedDualVariables().entrySet()) {
            slackVariables.put(entry.getKey(), lpData.getSolver().getDual(entry.getValue()));
        }
        return slackVariables;
    }


    protected DigitArray getRightExactProbability(Candidate current) {
        if (USE_BINARY_HALVING) {
            return DigitArray.getAverage(current.getChanges().getLbFor(current.getAction()),
                    current.getChanges().getUbFor(current.getAction()), table.getPrecisionFor(current.getAction()));
        }
        int[] probability = new int[current.getActionProbability().length];

        System.arraycopy(current.getActionProbability(), 0, probability, 0, probability.length);
        for (int i = probability.length - 1; i >= 0; i--) {
            if (probability[i] == 9) {
                probability[i] = 0;
            } else {
                probability[i]++;
                break;
            }
        }
        return new DigitArray(probability, true);
    }

    protected void addLeftChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) {
        table.clearTable();
        Changes newChanges = new Changes(current.getChanges());
        DigitArray probDigit = getLeftExactProbability(current);

        if (alreadyPresentLeft(newChanges, probDigit))
            probDigit = probDigit.decrementLSD();
        if (DigitArray.ZERO.isGreaterThan(probDigit) || probDigit.equals(DigitArray.ZERO))
            return;
        Change change = new LeftChange(current.getAction(), probDigit);
        long buildingTimeStart = mxBean.getCurrentThreadCpuTime();

        if (!BilinearTable.DELETE_PRECISION_CONSTRAINTS_ONLY)
            buildBaseLP(table, config);
        current.getChanges().updateTable(table);
        lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
        updateChangesForLeft(newChanges, change);
        if (USE_INVALID_CUTS && !newChanges.isValid()) {
            invalidCuts++;
            return;
        }
        if (USE_DUPLICITY_CUTS && ubs.containsKey(newChanges)) {
            cuts++;
            return;
        }
        assert !current.getChanges().contains(change);
        ubs.put(newChanges, 0d);
        applyNewChangeAndSolve(fringe, config, newChanges, change, current instanceof DoubleOracleCandidate ? (DoubleOracleCandidate) current : null);
    }

    private boolean alreadyPresentLeft(Changes newChanges, DigitArray probDigit) {
        return newChanges.stream().anyMatch(change -> (change instanceof LeftChange && probDigit.equals(change.getFixedDigitArrayValue())));
    }

    protected boolean isZero(int[] probability) {
        return !Arrays.stream(probability).anyMatch(prob -> prob > 0);
    }

    protected DigitArray getLeftExactProbability(Candidate current) {
        if (USE_BINARY_HALVING) {
            return DigitArray.getAverage(current.getChanges().getLbFor(current.getAction()),
                    current.getChanges().getUbFor(current.getAction()), table.getPrecisionFor(current.getAction()));
        }
        int[] probability;

        if (current.getActionProbability()[0] == 1) {
            probability = new int[current.getActionProbability().length];
            System.arraycopy(current.getActionProbability(), 0, probability, 0, probability.length);
            probability[0] = 0;
            for (int i = 1; i < probability.length; i++) {
                probability[i] = 9;
            }
        } else {
            probability = current.getActionProbability();
        }
        return new DigitArray(probability, true);
    }


//    protected int getDigit(double reward, int digit) {
//        int firstDigit = (int) Math.floor(reward);
//
//        if (digit == 0)
//            return firstDigit;
//        double tempValue = reward - firstDigit;
//
//        tempValue = Math.floor(tempValue * Math.pow(10, digit));
//        return (int) (tempValue - 10 * (long) (tempValue / 10));
//    }

    protected boolean isConverged(Candidate currentBest) {
        return isConverged(currentBest.getLb(), currentBest.getUb());
    }

    protected Candidate pollCandidateWithUBHigherThanBestLB(Queue<Candidate> fringe) {
        Candidate current = null;

        while (current == null || (current.getUb() < currentBest.getLb() && !fringe.isEmpty()))
            current = fringe.poll();
        return current;
    }

    protected boolean isConverged(double globalLB, double globalUB) {
        return Math.abs(globalUB - globalLB) < 1e-4 * gameInfo.getMaxUtility() || globalLB > globalLB;
    }

    protected void buildBaseLP(BilinearTable table, SequenceFormIRConfig config) {
        addObjective(table);
        addRPConstraints(table, config);
        addBehaviorStrategyConstraints(table, config);
        markAllBilinearVariables(table, config);
        addValueConstraints(table, config);
    }

    protected Candidate createCandidate(Changes changes, LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);

        assert definedEverywhere(p1Strategy, config);
        assert equalsInPRInformationSets(p1Strategy, config, lpData);
        assert isConvexCombination(p1Strategy, lpData, config);
        double lowerBound = getLowerBound(p1Strategy);
        double upperBound = getUpperBound(lpData);
        Action action = findMostViolatedBilinearConstraints(config, lpData);
        int[] exactProbability = getExactProbability(p1Strategy.get(action), table.getPrecisionFor(action), action, changes);

//        assert lowerBound <= upperBound + 1e-6;
        return new OracleCandidate(lowerBound, upperBound, changes, action, exactProbability, 0, extractRPStrategy(config, lpData), null, null, 0);
    }

    protected int[] getExactProbability(Double value, int precision, Action action, Changes changes) {
        int[] exactValue = new int[precision];
        int intValue = (int) Math.floor(value * Math.pow(10, precision));

        for (int i = 0; i < exactValue.length; i++) {
            exactValue[i] = (int) (intValue / Math.pow(10, exactValue.length - i));
            intValue -= exactValue[i] * Math.pow(10, exactValue.length - i);
        }
        DigitArray currentProbability = new DigitArray(exactValue, true);

        if (intValue > 4)
            currentProbability = currentProbability.incrementLSD();
        DigitArray ub = changes.getUbFor(action);
        DigitArray lb = changes.getLbFor(action);

        if (!ub.equals(DigitArray.ONE) && currentProbability.equals(ub))
            currentProbability = currentProbability.decrementLSD();
        if (lb.isGreaterThan(currentProbability))
            currentProbability = currentProbability.incrementLSD();
//        assert (currentProbability.isGreaterThan(lb) || currentProbability.size() > lb.size()) && (ub.isGreaterThan(currentProbability) || ub.equals(DigitArray.ONE));
//        if(!lb.equals(DigitArray.ZERO) && currentProbability.equals(lb))
//            currentProbability = incrementLSD(currentProbability);
        return currentProbability.getArray();
    }


    protected Candidate createCandidate(LPData lpData, SequenceFormIRConfig config) throws IloException {
        return createCandidate(new Changes(), lpData, config);
    }

    protected double getUpperBound(LPData lpData) throws IloException {
        return lpData.getSolver().getObjValue();
    }

    protected double getLowerBound(Map<Action, Double> p1Strategy) {
        long start = mxBean.getCurrentThreadCpuTime();

        br.getBestResponse(p1Strategy);
        BRTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
        return -br.getValue();
    }

    protected boolean equalsInPRInformationSets(Map<Action, Double> p1Strategy, SequenceFormIRConfig config, LPData lpData) throws IloException {
        Map<Action, Double> altStrategy = extractBehavioralStrategy(config, lpData);

        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player) && !informationSet.hasIR()) {
                for (Action action : informationSet.getActions()) {
                    if (Math.abs(p1Strategy.get(action) - altStrategy.get(action)) > 1e-8)
                        return false;
                }
            }
        }
        return true;
    }

    protected boolean definedEverywhere(Map<Action, Double> p1Strategy, SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player) && !informationSet.getActions().isEmpty()) {
                double sum = 0;

                for (Action action : informationSet.getActions()) {
                    Double value = p1Strategy.get(action);

                    if (value != null)
                        sum += value;
                }
                if (Math.abs(1 - sum) > 1e-4)
                    return false;
            }
        }
        return true;
    }

    protected boolean isConvexCombination(Map<Action, Double> p1Strategy, LPData lpdata, SequenceFormIRConfig config) throws IloException {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (!informationSet.getPlayer().equals(player))
                continue;
            Map<Action, Double> ubs = new HashMap<>();
            Map<Action, Double> lbs = new HashMap<>();

            for (Map.Entry<Sequence, Set<Sequence>> entry : informationSet.getOutgoingSequences().entrySet()) {
                double incomingSeqProb = lpdata.getSolver().getValue(lpdata.getVariables()[table.getVariableIndex(entry.getKey())]);

                if (incomingSeqProb > 1e-4)
                    for (Sequence outgoingSequence : entry.getValue()) {
                        double outgoingSeqProb = lpdata.getSolver().getValue(lpdata.getVariables()[table.getVariableIndex(outgoingSequence)]);
                        double behavStrat = outgoingSeqProb / incomingSeqProb;

                        updateUbs(ubs, outgoingSequence.getLast(), behavStrat);
                        updateLbs(lbs, outgoingSequence.getLast(), behavStrat);
                    }

            }
            if (isOut(lbs, ubs, p1Strategy))
                return false;
        }
        return true;
    }

    protected boolean isOut(Map<Action, Double> lbs, Map<Action, Double> ubs, Map<Action, Double> p1Strategy) {
        assert ubs.size() == lbs.size();
        for (Action action : lbs.keySet()) {
            double strategy = p1Strategy.get(action);
            double lb = lbs.get(action);
            double ub = ubs.get(action);

            if (strategy + 1e-6 < lb || strategy - 1e-6 > ub)
                return true;
        }
        return false;
    }

    protected void updateUbs(Map<Action, Double> ubs, Action action, double behavStrat) {
        Double ub = ubs.get(action);

        if (ub == null || behavStrat > ub)
            ubs.put(action, behavStrat);
    }

    protected void updateLbs(Map<Action, Double> lbs, Action action, double behavStrat) {
        Double lb = lbs.get(action);

        if (lb == null || behavStrat < lb)
            lbs.put(action, behavStrat);
    }

    protected void markAllBilinearVariables(BilinearTable table, SequenceFormIRConfig config) {
        config.getSequencesFor(player)
                .stream()
                .filter(s -> !s.isEmpty())
                .filter(s -> ((SequenceFormIRInformationSet) s.getLastInformationSet()).hasIR())
                .forEach(s -> markBilinearFor(table, s));
    }

    protected void markBilinearFor(BilinearTable table, Sequence sequence) {
        table.markAsBilinear(sequence, sequence.getSubSequence(sequence.size() - 1), sequence.getLast());
    }

    protected void addValueConstraints(BilinearTable table, SequenceFormIRConfig config) {
        for (Sequence sequence : config.getSequencesFor(opponent)) {
            Object eqKey;
            Object informationSet;
            Sequence subsequence;

            if (sequence.isEmpty()) {
                eqKey = "v_init";
                informationSet = "root";
                subsequence = new ArrayListSequenceImpl(opponent);
            } else {
                subsequence = sequence.getSubSequence(sequence.size() - 1);
                informationSet = sequence.getLastInformationSet();
                eqKey = new Triplet<>(informationSet, subsequence, sequence.getLast());
            }
            table.watchDualVariable(eqKey, eqKey);
            Object varKey = new Pair<>(informationSet, subsequence);

            table.setConstraint(eqKey, varKey, 1);
            table.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            config.getReachableSets(sequence)
                    .stream()
                    .filter(reachableSet -> !reachableSet.getActions().isEmpty())
                    .filter(reachableSet -> reachableSet.getOutgoingSequences().get(sequence) != null)
                    .filter(reachableSet -> !reachableSet.getOutgoingSequences().get(sequence).isEmpty())
                    .filter(reachableSet -> reachableSet.getPlayer().equals(opponent))
                    .forEach(reachableSet -> table.setConstraint(eqKey, new Pair<>(reachableSet, sequence), -1));
            for (Sequence compatibleSequence : config.getCompatibleSequencesFor(sequence)) {
                Double utility = config.getUtilityFor(sequence, compatibleSequence);

                if (utility != null)
                    table.setConstraint(eqKey, compatibleSequence, (player.getId() == 0 ? -1 : 1) * utility);
            }
        }
    }

    protected void addBehaviorStrategyConstraints(BilinearTable table, SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (!informationSet.hasIR())
                continue;
            if (informationSet.getPlayer().equals(player)) {
                for (Action action : informationSet.getActions()) {
                    table.setConstraint(informationSet, action, 1);
                    table.setLowerBound(action, 0);
                    table.setUpperBound(action, 1);
                }
                table.setConstant(informationSet, 1);
                table.setConstraintType(informationSet, 1);
            }
        }
    }

    protected void addRPConstraints(BilinearTable table, SequenceFormIRConfig config) {
        table.setConstraint("rpInit", new ArrayListSequenceImpl(player), 1);
        table.setConstant("rpInit", 1);
        table.setConstraintType("rpInit", 1);
        config.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(player)).forEach(i -> addRPConstraint(table, i));
        addRPVarBounds(table, config);
    }

    protected void addRPVarBounds(BilinearTable table, SequenceFormIRConfig config) {
        config.getSequencesFor(player).forEach(s -> setZeroOneBounds(table, s));
    }

    protected void setZeroOneBounds(BilinearTable table, Object object) {
        table.setLowerBound(object, 0);
        table.setUpperBound(object, 1);
    }

    protected void addRPConstraint(BilinearTable table, SequenceFormIRInformationSet informationSet) {
        for (Map.Entry<Sequence, Set<Sequence>> outgoingEntry : informationSet.getOutgoingSequences().entrySet()) {
            Object eqKey = new Pair<>(informationSet, outgoingEntry.getKey());

            table.setConstraint(eqKey, outgoingEntry.getKey(), 1);
            table.setConstraintType(eqKey, 1);
            for (Sequence sequence : outgoingEntry.getValue()) {
                table.setConstraint(eqKey, sequence, -1);
            }
        }
    }

    protected void addObjective(BilinearTable table) {
        table.setObjective(new Pair<>("root", new ArrayListSequenceImpl(opponent)), 1);
    }

//    /**
//     * Chooses the action a causing the highest error * 10^(MAX_DEPTH - average depth of the IS where a is played)
//     *
//     * @param config
//     * @param data
//     * @return
//     * @throws IloException
//     */
//    protected Action findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
//        Set<Action> actions = config.getSequencesFor(player).stream()
//                .filter(s -> !s.isEmpty())
//                .filter(s -> ((SequenceFormIRInformationSet) s.getLastInformationSet()).hasIR())
//                .map(Sequence::getLast).collect(Collectors.toSet());
//        double currentError = Double.NEGATIVE_INFINITY;
//        Action currentBest = null;
//
//        for (Action action : actions) {
//            SequenceFormIRInformationSet is = (SequenceFormIRInformationSet) action.getInformationSet();
//            ArrayList<Double> specValues = new ArrayList<>();
//
//            for (Map.Entry<Sequence, Set<Sequence>> entry : is.getOutgoingSequences().entrySet()) {
//                if (data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]) > 0) {
//                    Sequence productSequence = new ArrayListSequenceImpl(entry.getKey());
//
//                    productSequence.addLast(action);
//                    specValues.add(data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]) /
//                            data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]));
//                }
//            }
//            OptionalDouble average = specValues.stream().mapToDouble(Double::doubleValue).average();
//
//            if (!average.isPresent())
//                continue;
//            double exponent = gameInfo.getMaxDepth() + 1 - getAverageDepth(is);
//
//            if(exponent < 1)
//                System.err.println("exponent malformed");
//            double error = getError(average.getAsDouble(), specValues);
//            if(error > 1e-4) {
//                error *= Math.pow(10, 5*exponent);
//                if (error > currentError) {
//                    currentError = error;
//                    currentBest = action;
//                }
//            }
//        }
//        if (currentBest == null)
//            currentBest = addFirstAvailable(config);
//        return currentBest;
//    }

    /**
     * Chooses the action a causing the highest error in the shallowest information set (according to average length of sequence leading to this IS)
     *
     * @param config
     * @param data
     * @return
     * @throws IloException
     */
    protected Action findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
        Set<Action> actions = config.getSequencesFor(player).stream()
                .filter(s -> !s.isEmpty())
                .filter(s -> ((SequenceFormIRInformationSet) s.getLastInformationSet()).hasIR())
                .map(Sequence::getLast).collect(Collectors.toSet());
        double currentError = Double.NEGATIVE_INFINITY;
        double currentDepth = Double.POSITIVE_INFINITY;
        Action currentBest = null;

        for (Action action : actions) {
            SequenceFormIRInformationSet is = (SequenceFormIRInformationSet) action.getInformationSet();
            ArrayList<Double> specValues = new ArrayList<>();

            for (Map.Entry<Sequence, Set<Sequence>> entry : is.getOutgoingSequences().entrySet()) {
                if (data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]) > 0) {
                    Sequence productSequence = new ArrayListSequenceImpl(entry.getKey());

                    productSequence.addLast(action);
                    specValues.add(data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]) /
                            data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]));
                }
            }
            OptionalDouble average = specValues.stream().mapToDouble(Double::doubleValue).average();

            if (!average.isPresent())
                continue;
            double error = getError(average.getAsDouble(), specValues);

            if (error > 1e-4) {
                double averageDepth = getAverageDepth(is);

                if (averageDepth < currentDepth) {
                    currentDepth = averageDepth;
                    currentError = error;
                    currentBest = action;
                } else if (Math.abs(averageDepth - currentDepth) <= 0) {
                    if (error > currentError) {
                        currentError = error;
                        currentBest = action;
                    }
                }
            }
        }
        if (currentBest == null)
            currentBest = addFirstAvailable(config);
        return currentBest;
    }


//    /**
//     * Returns the action causing the highest error according to the StrategyLP
//     * @param config
//     * @param data
//     * @return
//     * @throws IloException
//     */
//    protected Action findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
//        return mostBrokenAction;
//    }

    protected double getError(double average, ArrayList<Double> specValues) {
        return specValues.stream().mapToDouble(d -> Math.abs(average - d)).sum();
    }


    protected double getAverageDepth(SequenceFormIRInformationSet informationSet) {
        return informationSet.getOutgoingSequences().keySet().stream().mapToInt(Sequence::size).average().getAsDouble();
    }


    public Expander getExpander() {
        return expander;
    }

    public void setExpander(Expander expander) {
        this.expander = expander;
    }

    protected Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> P1Strategy = new HashMap<>();
        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
            if (!i.getPlayer().equals(player)) continue;
            boolean allZero = true;
            for (Action a : i.getActions()) {
                double average = 0;
                int count = 0;
                for (Sequence subS : i.getOutgoingSequences().keySet()) {
                    Sequence s = new ArrayListSequenceImpl(subS);
                    s.addLast(a);

                    if (lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]) > 0) {
                        double sV = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
                        sV = sV / lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
                        average += sV;
                        count++;
                    }
                }
                if (count == 0) average = 0;
                else average = average / count;

                if (DEBUG) System.out.println(a + " = " + average);
                P1Strategy.put(a, average);

                if (average > 0) allZero = false;
            }
            if (allZero && i.getActions().size() > 0) {
                P1Strategy.put(i.getActions().iterator().next(), 1d);
            }
        }
        return P1Strategy;
    }

    protected Map<Action, Double> extractBehavioralStrategyLP(SequenceFormIRConfig config, LPData lpData) throws IloException {
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> p1Strategy = new HashMap<>();

//        mostBrokenAction = null;
        mostBrokenActionValue = 0;
        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
            if (!i.getPlayer().equals(player))
                continue;
            boolean allZero = true;

            if (i.hasIR()) {
                strategyLP.clear();
                for (Map.Entry<Sequence, Set<Sequence>> entry : i.getOutgoingSequences().entrySet()) {
                    for (Sequence outgoingSequence : entry.getValue()) {
                        double outgoingSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(outgoingSequence)]);
                        double incomingSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(entry.getKey())]);

                        if (incomingSeqProb > 1e-4) {
                            allZero = false;
                            strategyLP.add(entry.getKey(), outgoingSequence, incomingSeqProb, Math.max(outgoingSeqProb, 0));
                        }
                    }
                }
                if (!allZero) {
                    long start = mxBean.getCurrentThreadCpuTime();

                    Map<Action, Double> strategy = strategyLP.getStartegy();
                    strategyLPTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
                    i.getActions().forEach(action -> strategy.putIfAbsent(action, 0d));
                    p1Strategy.putAll(strategy);
                    Pair<Action, Double> actionCostPair = strategyLP.getMostExpensiveActionCostPair();
//
                    if (mostBrokenActionValue < Math.abs(actionCostPair.getRight())) {
                        mostBrokenActionValue = Math.abs(actionCostPair.getRight());
//                        mostBrokenAction = actionCostPair.getLeft();
                    }
                }
            } else if (!i.getOutgoingSequences().isEmpty()) {
                assert i.getOutgoingSequences().size() == 1;
                double incomingSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(i.getOutgoingSequences().keySet().iterator().next())]);

                if (incomingSeqProb > 0) {
                    allZero = false;
                    for (Sequence outgoingSequence : i.getOutgoingSequences().entrySet().iterator().next().getValue()) {
                        double outgoingSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(outgoingSequence)]);

                        p1Strategy.put(outgoingSequence.getLast(), outgoingSeqProb / incomingSeqProb);
                    }
                }
            }
            if (allZero && i.getActions().size() > 0)
                p1Strategy.put(i.getActions().iterator().next(), 1d);
            i.getActions().forEach(action -> p1Strategy.putIfAbsent(action, 0d));
        }
//        if (mostBrokenAction == null)
//            mostBrokenAction = addFirstAvailable(config);
        return p1Strategy;
    }

    protected Action addFirstAvailable(SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player) && informationSet.hasIR() && !informationSet.getActions().isEmpty())
                return informationSet.getActions().iterator().next();
        }
//        assert !config.getAllInformationSets().values().stream().anyMatch(SequenceFormIRInformationSet::hasIR);
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player) && !informationSet.getActions().isEmpty())
                return informationSet.getActions().iterator().next();
        }
        return null;
    }

    public Map<Sequence, Double> extractRPStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
        Map<Sequence, Double> P1StrategySeq = new HashMap<>();
        for (Sequence s : config.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            double seqValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
            if (DEBUG) System.out.println(s + " = " + seqValue);

            P1StrategySeq.put(s, lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]));
        }
        return P1StrategySeq;
    }
}
