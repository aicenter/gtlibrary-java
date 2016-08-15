package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponse;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.Changes;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.expandconditions.ExpandCondition;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.expandconditions.ExpandConditionImpl;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander.GameExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander.ReducedSingleOracleGameExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander.SingleOracleGameExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.utils.StrategyLP;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class OracleBilinearSequenceFormBnB extends BilinearSequenceFormBnB {
    public static boolean DEBUG = false;
    public static boolean SAVE_LPS = false;
    public static double EPS = 1e-3;

    protected ImperfectRecallBestResponse br;
    protected ExpandCondition expandCondition = new ExpandConditionImpl();
    protected GameExpander gameExpander;

    public static void main(String[] args) {
//        new Scanner(System.in).next();
        runRandomGame();
//        runBRTest();
    }

    public static double runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);
        OracleBilinearSequenceFormBnB solver = new OracleBilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, root, expander, new RandomGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(new SequenceFormIRConfig());
        System.out.println("CPLEX time: " + solver.getCPLEXTime());
        System.out.println("StrategyLP time: " + solver.getStrategyLPTime());
        System.out.println("Overall time: " + (mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("CPLEX invocation count: " + solver.getCPLEXInvocationCount());
        System.out.println("BR time: " + solver.getBRTime());
        System.out.println("LP building time: " + solver.getLpBuildingTime());

        System.out.println("Memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
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

    public OracleBilinearSequenceFormBnB(Player player, GameState root, Expander<SequenceFormIRInformationSet> fullGameExpander, GameInfo info) {
        super(player, fullGameExpander, info);
        br = new OracleImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, fullGameExpander, gameInfo);
        gameExpander = new SingleOracleGameExpander(player, root, fullGameExpander, info);
    }

    public void solve(SequenceFormIRConfig restrictedGameConfig) {
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
            CPLEXInvocationCount++;
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

            while (!fringe.isEmpty()) {
                OracleCandidate current = (OracleCandidate) pollCandidateWithUBHigherThanBestLB(fringe);

//                System.out.println(current + " vs " + currentBest);
//                System.out.println(current.getChanges());
                if (isConverged(current)) {
                    currentBest = current;
                    System.out.println(current);
                    return;
                }
                if (expandCondition.validForExpansion(restrictedGameConfig, current)) {
                    gameExpander.expand(restrictedGameConfig, current);
                    table.clearTable();
                    buildingTimeStart = mxBean.getCurrentThreadCpuTime();
                    buildBaseLP(table, restrictedGameConfig);
                    lpBuildingTime += (mxBean.getCurrentThreadCpuTime() - buildingTimeStart) / 1e6;
                }
                addMiddleChildOf(current, fringe, restrictedGameConfig);
                addLeftChildOf(current, fringe, restrictedGameConfig);
                addRightChildOf(current, fringe, restrictedGameConfig);
            }
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
    }

    protected void initRestrictedGame(SequenceFormIRConfig restrictedGameConfig) {
        gameExpander.expand(restrictedGameConfig, br.getBestResponse(new HashMap<>()));
    }

    protected OracleCandidate createCandidate(Changes changes, LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);

        assert definedEverywhere(p1Strategy, config);
        assert equalsInPRInformationSets(p1Strategy, config, lpData);
        assert isConvexCombination(p1Strategy, lpData, config);
        Pair<Double, Map<Action, Double>> lowerBoundAndBR = getLowerBoundAndBR(p1Strategy);
        double upperBound = getUpperBound(lpData);
        Action action = findMostViolatedBilinearConstraints(config, lpData);
        int[] exactProbability = getExactProbability(p1Strategy.get(action), table.getPrecisionFor(action));

        assert lowerBoundAndBR.getLeft() <= upperBound + 1e-6;
        return new OracleCandidate(lowerBoundAndBR.getLeft(), upperBound, changes, action, exactProbability, mostBrokenActionValue, extractRPStrategy(config, lpData), lowerBoundAndBR.getRight());
    }

    protected Pair<Double, Map<Action, Double>> getLowerBoundAndBR(Map<Action, Double> p1Strategy) {
        long start = mxBean.getCurrentThreadCpuTime();
        Map<Action, Double> bestResponse = br.getBestResponse(p1Strategy);

        BRTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
        return new Pair<>(-br.getValue(), bestResponse);
    }
}
