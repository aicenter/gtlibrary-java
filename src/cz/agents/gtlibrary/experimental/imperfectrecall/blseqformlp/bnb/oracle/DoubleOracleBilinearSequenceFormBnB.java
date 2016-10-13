package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle;

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
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.imperfectrecall.IRTTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.ir.IRGenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.ir.IRKuhnPokerGameState;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionExpander;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionGameInfo;
import cz.agents.gtlibrary.domain.randomabstraction.RandomAbstractionGameStateFactory;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Change;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.DoubleOracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander.TempLeafDoubleOracleGameExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.utils.StrategyLP;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class DoubleOracleBilinearSequenceFormBnB extends OracleBilinearSequenceFormBnB {
    public static boolean DEBUG = false;
    public static boolean EXPORT_GBT = false;
    public static boolean SAVE_LPS = false;
    public static boolean RESOLVE_CURRENT_BEST = false;
    public static boolean STATE_CACHE_USE = true;
    public static double EPS = 1e-3;

    protected GameState root;
    private long testTime = 0;

    public static void main(String[] args) {
        new Scanner(System.in).next();
//        runRandomGame();
//        runAbstractedRandomGame();
        runTTT();
//        runBPG();
//        runBRTest();
//        runKuhnPoker();
//        runGenericPoker();
    }

    public static double runAbstractedRandomGame() {
        GameState wrappedRoot = new RandomGameState();
        SequenceFormConfig<SequenceInformationSet> wrappedConfig = new SequenceFormConfig<>();
        Expander<SequenceInformationSet> wrappedExpander = new RandomGameExpander<>(wrappedConfig);
        FullSequenceEFG efg = new FullSequenceEFG(wrappedRoot, wrappedExpander, new RandomGameInfo(), wrappedConfig);
        efg.generateCompleteGame();

        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new RandomAbstractionGameInfo(new RandomGameInfo()));
        GameState root = RandomAbstractionGameStateFactory.createRoot(wrappedRoot, wrappedExpander.getAlgorithmConfig());
        Expander<SequenceFormIRInformationSet> expander = new RandomAbstractionExpander<>(wrappedExpander, config);
        DoubleOracleBilinearSequenceFormBnB solver = new DoubleOracleBilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, root, expander, new RandomAbstractionGameInfo(new RandomGameInfo()));

//        GambitEFG exporter = new GambitEFG();
//        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Expander time: " + solver.getExpanderTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Oracle self time: " + solver.getSelfTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("P1 sequence count: " + config.getSequencesFor(RandomGameInfo.FIRST_PLAYER).size());
        System.out.println("P2 sequence count: " + config.getSequencesFor(RandomGameInfo.SECOND_PLAYER).size());
        System.out.println("Information set count: " + config.getAllInformationSets().size());
        return solver.finalValue;
    }

    public static double runKuhnPoker() {
        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new BPGGameInfo());
        GameState root = new IRKuhnPokerGameState();
        Expander<SequenceFormIRInformationSet> expander = new KuhnPokerExpander<>(config);

//        builder.build(root, config, expander);
//        System.out.println("game build");
        DoubleOracleBilinearSequenceFormBnB solver = new DoubleOracleBilinearSequenceFormBnB(KPGameInfo.FIRST_PLAYER, root, expander, new KPGameInfo());

//        GambitEFG exporter = new GambitEFG();
//        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
//        System.out.println("Information sets: " + config.getCountIS(0));
//        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Expander time: " + solver.getExpanderTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Oracle self time: " + solver.getSelfTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        return solver.finalValue;
    }

    public static double runGenericPoker() {
        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new BPGGameInfo());
        GameState root = new IRGenericPokerGameState();
        Expander<SequenceFormIRInformationSet> expander = new GenericPokerExpander<>(config);

//        builder.build(root, config, expander);
//        System.out.println("game build");
        DoubleOracleBilinearSequenceFormBnB solver = new DoubleOracleBilinearSequenceFormBnB(GPGameInfo.FIRST_PLAYER, root, expander, new GPGameInfo());

//        GambitEFG exporter = new GambitEFG();
//        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
//        System.out.println("Information sets: " + config.getCountIS(0));
//        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Expander time: " + solver.getExpanderTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Oracle self time: " + solver.getSelfTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(BPGGameInfo.DEFENDER).size() + ", " + config.getSequencesFor(BPGGameInfo.ATTACKER).size());
        return solver.finalValue;
    }

    public static double runTTT() {
//        BasicGameBuilder builder = new BasicGameBuilder();
        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new TTTInfo());
        GameState root = new IRTTTState();
        Expander<SequenceFormIRInformationSet> expander = new TTTExpander<>(config);

//        builder.build(root, config, expander);
//        System.out.println("game build");
        DoubleOracleBilinearSequenceFormBnB solver = new DoubleOracleBilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, root, expander, new TTTInfo());

//        GambitEFG exporter = new GambitEFG();
//        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
//        System.out.println("Information sets: " + config.getCountIS(0));
//        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Expander time: " + solver.getExpanderTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Oracle self time: " + solver.getSelfTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("IS count: " + config.getAllInformationSets().size());
        System.out.println("Sequence count: " + config.getSequencesFor(TTTInfo.XPlayer) + ", " + config.getSequencesFor(TTTInfo.OPlayer));
        return solver.finalValue;
    }

    public static double runBPG() {
        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new BPGGameInfo());
        GameState root = new IRBPGGameState();
        Expander<SequenceFormIRInformationSet> expander = new BPGExpander<>(config);

//        builder.build(root, config, expander);
//        System.out.println("game build");
        DoubleOracleBilinearSequenceFormBnB solver = new DoubleOracleBilinearSequenceFormBnB(BPGGameInfo.DEFENDER, root, expander, new BPGGameInfo());

//        GambitEFG exporter = new GambitEFG();
//        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
//        System.out.println("Information sets: " + config.getCountIS(0));
//        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Expander time: " + solver.getExpanderTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Oracle self time: " + solver.getSelfTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
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
        GameState root = new BRTestGameState();

        builder.build(root, config, expander);
        OracleBilinearSequenceFormBnB solver = new OracleBilinearSequenceFormBnB(BRTestGameInfo.FIRST_PLAYER, root, expander, new BRTestGameInfo());

        solver.solve(new SequenceFormIRConfig(new BRTestGameInfo()));
    }

    public static double runRandomGame() {
//        BasicGameBuilder builder = new BasicGameBuilder();
        DoubleOracleIRConfig config = new DoubleOracleIRConfig(new RandomGameInfo());
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

//        builder.build(root, config, expander);
        DoubleOracleBilinearSequenceFormBnB solver = new DoubleOracleBilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, root, expander, new RandomGameInfo());

//        GambitEFG exporter = new GambitEFG();
//        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
//        System.out.println("Information sets: " + config.getCountIS(0));
//        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());
        System.out.println("Expander time: " + solver.getExpanderTime());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
        System.out.println("Oracle self time: " + solver.getSelfTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("cuts: " + solver.cuts);
        System.out.println("invalid cuts: " + solver.invalidCuts);
        System.out.println("P1 sequence count: " + config.getSequencesFor(RandomGameInfo.FIRST_PLAYER).size());
        System.out.println("P2 sequence count: " + config.getSequencesFor(RandomGameInfo.SECOND_PLAYER).size());
        System.out.println("Information set count: " + config.getAllInformationSets().size());
        return solver.finalValue;
    }

    public DoubleOracleBilinearSequenceFormBnB(Player player, GameState root, Expander<SequenceFormIRInformationSet> fullGameExpander, GameInfo info) {
        super(player, root, fullGameExpander, info);
//        br = new LinearOracleImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, root, fullGameExpander, gameInfo);
//        br = new OracleImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, fullGameExpander, gameInfo);
//        gameExpander = new ReducedSingleOracleGameExpander(player, root, fullGameExpander, info);
        this.root = root;
        gameExpander = new TempLeafDoubleOracleGameExpander(player, root, fullGameExpander, info);
    }

    public void solve(DoubleOracleIRConfig restrictedGameConfig) {
        long selfStart = mxBean.getCurrentThreadCpuTime();
        expansionCount = 0;

        strategyLP = new StrategyLP(restrictedGameConfig);
        if (restrictedGameConfig.getAllInformationSets().isEmpty())
            initRestrictedGame(restrictedGameConfig);
        long buildingTimeStart = mxBean.getCurrentThreadCpuTime();

        buildBaseLP(table, restrictedGameConfig);
        lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
        try {
            LPData lpData = table.toCplex();

            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
            long start = mxBean.getCurrentThreadCpuTime();

            lpData.getSolver().solve();
            System.out.println(CPLEXInvocationCount++);
            CPLEXTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());
            Queue<Candidate> fringe = new PriorityQueue<>();

            currentBest = createCandidate(lpData, restrictedGameConfig);
            if (DEBUG) System.out.println("most violated action: " + currentBest.getAction());
            if (DEBUG) System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getLb());

            fringe.add(currentBest);
            int it = 1;

            while (!fringe.isEmpty()) {
                OracleCandidate current = (OracleCandidate) pollCandidateWithUBHigherThanBestLB(fringe);

                it++;
//                System.out.println(current.getPrecisionError());
//                System.out.println(current + " vs " + currentBest);
                if (isConverged(current) && it > 2) {
                    currentBest = current;
                    System.out.println(current);
                    break;
                }
                if (Math.abs(currentBest.getLb() - current.getUb()) < 1e-4 * gameInfo.getMaxUtility() && it > 2) {
                    System.out.println(currentBest);
                    break;
                }
                if (expansionCount > current.getExpansionCount()) {
                    table.clearTable();
                    current.getChanges().updateTable(table);
                    applyNewChangeAndSolve(fringe, restrictedGameConfig, current.getChanges(), Change.EMPTY);
                    if (RESOLVE_CURRENT_BEST)
                        updateCurrentBest(restrictedGameConfig);
                } else {
//                if (expandCondition.validForExpansion(restrictedGameConfig, current)) {
                    boolean expanded = gameExpander.expand(restrictedGameConfig, current);

                    if (expanded) {
                        expansionCount++;
                    }
                    expanderTime += gameExpander.getSelfTime();
                    BRTime += gameExpander.getBRTime();
                    table.clearTable();
                    buildingTimeStart = mxBean.getCurrentThreadCpuTime();
                    buildBaseLP(table, restrictedGameConfig);
                    lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
//                    OracleCandidate samePrecisionCandidate = createCandidate(current.getChanges(), table.toCplex(), restrictedGameConfig);
//
//                    fringe.add(samePrecisionCandidate);
                    if (expansionCount > current.getExpansionCount()) {
//                        System.out.println("expand");
                        current.getChanges().updateTable(table);
                        applyNewChangeAndSolve(fringe, restrictedGameConfig, current.getChanges(), Change.EMPTY);
                        if (RESOLVE_CURRENT_BEST)
                            updateCurrentBest(restrictedGameConfig);
                    } else {
//                        System.out.println("prec");
                        assert current.getPrecisionError() > 1e-8;
                        addMiddleChildOf(current, fringe, restrictedGameConfig);
                        addLeftChildOf(current, fringe, restrictedGameConfig);
                        addRightChildOf(current, fringe, restrictedGameConfig);
                    }
                }
//                } else {
//                    addMiddleChildOf(current, fringe, restrictedGameConfig);
//                    addLeftChildOf(current, fringe, restrictedGameConfig);
//                    addRightChildOf(current, fringe, restrictedGameConfig);
//                }
            }
            System.out.println("Nodes expanded by BR: " + gameExpander.getBRExpandedNodes());
            finalValue = currentBest.getLb();
            System.out.println("final value: " + finalValue);
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
        System.out.println("TEST TIME: " + ((TempLeafDoubleOracleGameExpander) gameExpander).getTestTime());
        System.out.println("TEST TIME1: " + testTime);
        selfTime = (long) ((mxBean.getCurrentThreadCpuTime() - selfStart) / 1e6 - getLpBuildingTime() - getBRTime() - getExpanderTime() - getCPLEXTime() - getStrategyLPTime());
    }

    protected void updateCurrentBest(SequenceFormIRConfig restrictedGameConfig) {
        table.clearTable();
        long buildingTimeStart = mxBean.getCurrentThreadCpuTime();

        buildBaseLP(table, restrictedGameConfig);
        lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
        currentBest.getChanges().updateTable(table);
        try {
            buildingTimeStart = mxBean.getCurrentThreadCpuTime();

            lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
            LPData lpData = table.toCplex();
//                System.out.println("solved");

            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
            long start = mxBean.getCurrentThreadCpuTime();

            lpData.getSolver().solve();
//                System.out.println(CPLEXInvocationCount++);
            CPLEXTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
            if (lpData.getSolver().getStatus().equals(IloCplex.Status.Optimal)) {
                Candidate candidate = createLightWeightCandidate(currentBest.getChanges(), lpData, restrictedGameConfig);

//                assert Math.abs(candidate.getUb() - checkOnCleanLP(restrictedGameConfig, candidate)) < 1e-4;
                if (DEBUG) System.out.println("Candidate: " + candidate + " vs " + currentBest);
                if (candidate.getLb() > currentBest.getLb()) {
                    currentBest = candidate;
                    System.out.println("current best: " + currentBest);
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    protected OracleCandidate createCandidate(Changes changes, LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Action, Double> maxPlayerStrategy = extractBehavioralStrategyLP(config, lpData);

//        assert definedEverywhere(maxPlayerStrategy, config);
        assert equalsInPRInformationSets(maxPlayerStrategy, config, lpData);
//        assert isConvexCombination(maxPlayerStrategy, lpData, config);
        Pair<Double, Map<Action, Double>> lowerBoundAndBR = getLowerBoundAndBR(maxPlayerStrategy);
        double lpUB = getUpperBound(lpData);
        List<Map<Action, Double>> possibleBestResponses = getPossibleBestResponseActions(lpData, config);

        possibleBestResponses.add(lowerBoundAndBR.getRight());

//        Pair<GameState, Double> bestPending = ((DoubleOracleIRConfig) config).getBestPending(possibleBestResponses, opponent);
        double upperBound = /*bestPending == null ? */lpUB /*: Math.max(lpUB, bestPending.getRight())*/;

        assert upperBound > lowerBoundAndBR.getLeft() - 1e-3;
        Action action = findMostViolatedBilinearConstraints(config, lpData);
        int[] exactProbability = getExactProbability(maxPlayerStrategy.get(action), table.getPrecisionFor(action), action, changes);

        assert lowerBoundAndBR.getLeft() <= upperBound + 1e-6;
        return new DoubleOracleCandidate(lowerBoundAndBR.getLeft(), upperBound, changes, action, exactProbability,
                mostBrokenActionValue, extractRPStrategy(config, lpData), maxPlayerStrategy, lowerBoundAndBR.getRight(), expansionCount, possibleBestResponses, lpUB);
    }

    protected OracleCandidate createLightWeightCandidate(Changes changes, LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Action, Double> maxPlayerStrategy = extractBehavioralStrategyLP(config, lpData);

//        assert definedEverywhere(maxPlayerStrategy, config);
        assert equalsInPRInformationSets(maxPlayerStrategy, config, lpData);
//        assert isConvexCombination(maxPlayerStrategy, lpData, config);
        Pair<Double, Map<Action, Double>> lowerBoundAndBR = getLowerBoundAndBR(maxPlayerStrategy);
        double lpUB = getUpperBound(lpData);
//        List<Map<Action, Double>> possibleBestResponses = getPossibleBestResponseActions(lpData, config);

//        possibleBestResponses.add(lowerBoundAndBR.getRight());

//        Pair<GameState, Double> bestPending = ((DoubleOracleIRConfig) config).getBestPending(possibleBestResponses, opponent);
        double upperBound = /*bestPending == null ? */lpUB /*: Math.max(lpUB, bestPending.getRight())*/;

        assert upperBound > lowerBoundAndBR.getLeft() - 1e-3;
//        Action action = findMostViolatedBilinearConstraints(config, lpData);
//        int[] exactProbability = getExactProbability(maxPlayerStrategy.get(action), table.getPrecisionFor(action), action, changes);

        assert lowerBoundAndBR.getLeft() <= upperBound + 1e-6;
        return new DoubleOracleCandidate(lowerBoundAndBR.getLeft(), upperBound, changes, null, null,
                mostBrokenActionValue, extractRPStrategy(config, lpData), maxPlayerStrategy, lowerBoundAndBR.getRight(), expansionCount, null, lpUB);
    }

    protected List<Map<Action, Double>> getPossibleBestResponseActions(LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Sequence, Set<Action>> possibleBestResponseActions = new HashMap<>();

        for (Map.Entry<Object, IloRange> entry : lpData.getWatchedDualVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet)
                if (lpData.getSolver().getSlack(entry.getValue()) < 1e-8) {
                    Set<Action> currentActions = possibleBestResponseActions.getOrDefault(((Triplet<InformationSet, Sequence, Action>) entry.getKey()).getSecond(), new HashSet<>());

                    currentActions.add(((Triplet<InformationSet, Sequence, Action>) entry.getKey()).getThird());
                    possibleBestResponseActions.put(((Triplet<InformationSet, Sequence, Action>) entry.getKey()).getSecond(), currentActions);
                }
        }
        return splitToSeparateBRs(possibleBestResponseActions, config);
    }

    protected void applyNewChangeAndSolve(Queue<Candidate> fringe, SequenceFormIRConfig config, Changes newChanges, Change change) {
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
                    DoubleOracleCandidate candidate = (DoubleOracleCandidate) createCandidate(newChanges, lpData, config);

//                    assert Math.abs(candidate.getUb() - checkOnCleanLP(config, candidate)) < 1e-4;
                    if (DEBUG) System.out.println("Candidate: " + candidate + " vs " + currentBest);

                    if (candidate.getLb() > currentBest.getLb()) {
                        currentBest = candidate;
                        System.out.println("current best: " + currentBest);
                    }
//                    long testStart = mxBean.getCurrentThreadCpuTime();
//                    ((TempLeafDoubleOracleGameExpander) gameExpander).updatePendingAndTempLeafsForced(root, (DoubleOracleIRConfig) config, ((DoubleOracleCandidate)candidate).getPossibleBestResponses());
//                   assert ((DoubleOracleIRConfig) config).pendingAvailable(expander, candidate.getMaxPlayerStrategy(), candidate.getPossibleBestResponses(), gameInfo.getOpponent(player)) == ((TempLeafDoubleOracleGameExpander) gameExpander).pendingAvailable(root, ((DoubleOracleIRConfig) config), candidate.getMaxPlayerStrategy(), candidate.getPossibleBestResponses());
//                    testTime += (mxBean.getCurrentThreadCpuTime() - testStart) / 1e6;
                    if (((TempLeafDoubleOracleGameExpander) gameExpander).pendingAvailable(root, ((DoubleOracleIRConfig) config), candidate.getMaxPlayerStrategy(), candidate.getPossibleBestResponses())) {
                        candidate.setUb(Double.POSITIVE_INFINITY);
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

//    private List<Map<Action, Double>> splitToSeparateBRs(Map<Sequence, Set<Action>> possibleBestResponseActions, SequenceFormIRConfig config) {
//        return splitToSeparateBRs(root, possibleBestResponseActions, new ArrayList<>(), new HashMap<>(), true, config);
//    }
//
//    private List<Map<Action, Double>> splitToSeparateBRs(GameState state, Map<Sequence, Set<Action>> possibleBestResponseActions, List<Map<Action, Double>> brSplit, Map<Action, Double> currentBR, boolean last, SequenceFormIRConfig config) {
//        if (state.isGameEnd() || config.getTerminalStates().contains(state)) {
//            if (last)
//                brSplit.add(currentBR);
//            return brSplit;
//        }
//        if (state.isPlayerToMoveNature() || state.getPlayerToMove().equals(player)) {
//            List<Action> actions = expander.getActions(state);
//            int counter = 0;
//            List<GameState> possibleSuccessors = new ArrayList<>();
//
//            for (Action action : actions) {
//                GameState nextState = state.performAction(action);
//
//                if (config.getSequencesFor(player).contains(nextState.getSequenceFor(player)))
//                    possibleSuccessors.add(state.performAction(action)) ;
//            }
//            for (GameState successor : possibleSuccessors) {
//                splitToSeparateBRs(successor, possibleBestResponseActions, brSplit, currentBR, last && (++counter) == possibleSuccessors.size(), config);
//            }
//            return brSplit;
//        }
//        boolean first = true;
//        Map<Action, Double> currentBRCopy = new HashMap<>(currentBR);
//
//        for (Action action : expander.getActions(state)) {
//            if (!possibleBestResponseActions.get(state.getSequenceForPlayerToMove()).contains(action))
//                continue;
//            Map<Action, Double> currentBRActionCopy;
//
//            if (first)
//                currentBRActionCopy = currentBR;
//            else
//                currentBRActionCopy = new HashMap<>(currentBRCopy);
//            currentBRActionCopy.put(action, 1d);
//            splitToSeparateBRs(state.performAction(action), possibleBestResponseActions, brSplit, currentBRActionCopy, last, config);
//            first = false;
//        }
//        return brSplit;
//    }


    private List<Map<Action, Double>> splitToSeparateBRs(Map<Sequence, Set<Action>> possibleBestResponseActions, SequenceFormIRConfig config) {
        Deque<GameState> queue = new ArrayDeque<>();

        queue.add(root);
        return splitToSeparateBRs(queue, possibleBestResponseActions, new ArrayList<>(), new HashMap<>(), new HashMap<>(), config);
    }

    private List<Map<Action, Double>> splitToSeparateBRs(Deque<GameState> queue, Map<Sequence, Set<Action>> possibleBestResponseActions, List<Map<Action, Double>> brSplit, Map<Action, Double> currentBR, Map<ISKey, Action> fixedInIS, SequenceFormIRConfig config) {
        while (!queue.isEmpty()) {
            GameState state = queue.removeFirst();

            if (state.isGameEnd() || config.getTerminalStates().contains(state))
                continue;
            if (state.isPlayerToMoveNature() || state.getPlayerToMove().equals(player)) {
                expander.getActions(state).stream()
                        .map(a -> state.performAction(a))
                        .filter(s -> config.getSequencesFor(player).contains(s.getSequenceFor(player)))
                        .forEach(s -> queue.addLast(s));
                continue;
            }
            Action fixedActionInIS = fixedInIS.get(state.getISKeyForPlayerToMove());
            if (fixedActionInIS != null) {
                queue.addLast(state.performAction(fixedActionInIS));
                continue;
            }
            Action firstAction = null;

            if (!possibleBestResponseActions.containsKey(state.getSequenceForPlayerToMove()))
                return brSplit;
            for (Action action : expander.getActions(state)) {
                if (!possibleBestResponseActions.get(state.getSequenceForPlayerToMove()).contains(action))
                    continue;
                if (firstAction == null) {
                    queue.addLast(state.performAction(action));
                    currentBR.put(action, 1d);
                    firstAction = action;
                    fixedInIS.put(state.getISKeyForPlayerToMove(), action);
                } else {
                    Deque<GameState> queueCopy = new ArrayDeque<>(queue);

                    queueCopy.removeLast();
                    Map<Action, Double> currentBRCopy = new HashMap<>(currentBR);

                    currentBRCopy.remove(firstAction);
                    queueCopy.addLast(state.performAction(action));
                    currentBRCopy.put(action, 1d);
                    Map<ISKey, Action> fixedInISCopy = new HashMap<>(fixedInIS);

                    fixedInISCopy.put(state.getISKeyForPlayerToMove(), action);
                    splitToSeparateBRs(queueCopy, possibleBestResponseActions, brSplit, currentBRCopy, fixedInISCopy, config);
                }
            }
        }
        brSplit.add(currentBR);
        return brSplit;
    }
}