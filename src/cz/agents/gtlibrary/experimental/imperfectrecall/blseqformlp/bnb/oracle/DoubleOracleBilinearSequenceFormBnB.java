package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle;

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
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class DoubleOracleBilinearSequenceFormBnB extends OracleBilinearSequenceFormBnB {
    public static boolean DEBUG = false;
    public static boolean EXPORT_GBT = true;
    public static boolean SAVE_LPS = false;
    public static boolean RESOLVE_CURRENT_BEST = true;
    public static double EPS = 1e-3;

    protected GameState root;

    public static void main(String[] args) {
//        new Scanner(System.in).next();
        runRandomGame();
//        runTTT();
//        runBPG();
//        runBRTest();
    }

    public static double runTTT() {
//        BasicGameBuilder builder = new BasicGameBuilder();
        DoubleOracleIRConfig config = new DoubleOracleIRConfig();
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
        DoubleOracleIRConfig config = new DoubleOracleIRConfig();
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

    public static double runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        DoubleOracleIRConfig config = new DoubleOracleIRConfig();
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);
        DoubleOracleBilinearSequenceFormBnB solver = new DoubleOracleBilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, root, expander, new RandomGameInfo());

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
        return solver.finalValue;
    }

    protected static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        GameState root = new BRTestGameState();

        builder.build(root, config, expander);
        OracleBilinearSequenceFormBnB solver = new OracleBilinearSequenceFormBnB(BRTestGameInfo.FIRST_PLAYER, root, expander, new BRTestGameInfo());

        solver.solve(new SequenceFormIRConfig());
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

            if (isConverged(currentBest)) {
                finalValue = currentBest.getLb();
                return;
            }
            fringe.add(currentBest);
            int it = 1;

            while (!fringe.isEmpty()) {
                OracleCandidate current = (OracleCandidate) pollCandidateWithUBHigherThanBestLB(fringe);

//                System.out.println(current + " vs " + currentBest);
                if (isConverged(current)) {
                    currentBest = current;
                    System.out.println(current);
                    break;
                }
                if (Math.abs(currentBest.getLb() - current.getUb()) < 1e-4 * gameInfo.getMaxUtility()) {
                    System.out.println(current);
                    break;
                }
                if (expansionCount > current.getExpansionCount()) {
                    current.getChanges().updateTable(table);
                    applyNewChangeAndSolve(fringe, restrictedGameConfig, current.getChanges(), Change.EMPTY);
                    if (RESOLVE_CURRENT_BEST && !current.equals(currentBest))
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
                        current.getChanges().updateTable(table);
                        applyNewChangeAndSolve(fringe, restrictedGameConfig, current.getChanges(), Change.EMPTY);
                        if (RESOLVE_CURRENT_BEST && !current.equals(currentBest))
                            updateCurrentBest(restrictedGameConfig);
                    } else {
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
        selfTime = (long) ((mxBean.getCurrentThreadCpuTime() - selfStart) / 1e6 - getLpBuildingTime() - getBRTime() - getExpanderTime() - getCPLEXTime() - getStrategyLPTime());
    }

    protected OracleCandidate createCandidate(Changes changes, LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Action, Double> maxPlayerStrategy = extractBehavioralStrategyLP(config, lpData);

//        assert definedEverywhere(maxPlayerStrategy, config);
        assert equalsInPRInformationSets(maxPlayerStrategy, config, lpData);
        assert isConvexCombination(maxPlayerStrategy, lpData, config);
        Pair<Double, Map<Action, Double>> lowerBoundAndBR = getLowerBoundAndBR(maxPlayerStrategy);
        double lpUB = getUpperBound(lpData);
        Set<Action> possibleBestResponses = getPossibleBestResponseActions(lpData);
        Map.Entry<GameState, Double> bestPending = ((DoubleOracleIRConfig) config).getBestPending(possibleBestResponses, opponent);
        double upperBound = bestPending == null ? lpUB : Math.max(lpUB, bestPending.getValue());

        assert upperBound > lowerBoundAndBR.getLeft() - 1e-3;
        Action action = findMostViolatedBilinearConstraints(config, lpData);
        int[] exactProbability = getExactProbability(maxPlayerStrategy.get(action), table.getPrecisionFor(action), action, changes);

        assert lowerBoundAndBR.getLeft() <= upperBound + 1e-6;
        return new DoubleOracleCandidate(lowerBoundAndBR.getLeft(), upperBound, changes, action, exactProbability,
                mostBrokenActionValue, extractRPStrategy(config, lpData), maxPlayerStrategy, lowerBoundAndBR.getRight(), expansionCount, possibleBestResponses);
    }

}
