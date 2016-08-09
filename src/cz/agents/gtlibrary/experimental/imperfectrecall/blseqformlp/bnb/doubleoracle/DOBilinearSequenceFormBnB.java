package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponse;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.BilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.BilinearSequenceFormBnB;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.*;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.expandconditions.DummyExpandCondition;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.expandconditions.ExpandCondition;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.gameexpander.GameExpander;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.gameexpander.GameExpanderImpl;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.utils.StrategyLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

public class DOBilinearSequenceFormBnB extends BilinearSequenceFormBnB{
    public static boolean DEBUG = false;
    public static boolean SAVE_LPS = false;
    public static double EPS = 1e-3;

    protected ImperfectRecallBestResponse br;
    protected ExpandCondition expandCondition = new DummyExpandCondition();
    protected GameExpander gameExpander;

    public static void main(String[] args) {
//        new Scanner(System.in).next();
//        runRandomGame();
        runBRTest();
    }

    public static double runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);
        DOBilinearSequenceFormBnB solver = new DOBilinearSequenceFormBnB(RandomGameInfo.FIRST_PLAYER, root, expander, new RandomGameInfo());

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
        return solver.finalValue;
    }

    protected static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);
        GameState root = new BRTestGameState();

        builder.build(root, config, expander);
        DOBilinearSequenceFormBnB solver = new DOBilinearSequenceFormBnB(BRTestGameInfo.FIRST_PLAYER, root, expander, new BRTestGameInfo());

        solver.solve(new SequenceFormIRConfig());
    }

    public DOBilinearSequenceFormBnB(Player player, GameState root, Expander<SequenceFormIRInformationSet> fullGameExpander, GameInfo info) {
        super(player, fullGameExpander, info);
        br = new DOImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, fullGameExpander, gameInfo);
        gameExpander = new GameExpanderImpl(player, root, fullGameExpander, info);
    }

    public void solve(SequenceFormIRConfig restrictedGameConfig) {
        strategyLP = new StrategyLP(restrictedGameConfig);
        long buildingTimeStart = mxBean.getCurrentThreadCpuTime();

        if(restrictedGameConfig.getAllInformationSets().isEmpty())
            initRestrictedGame(restrictedGameConfig);
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
                DOCandidate current = (DOCandidate) pollCandidateWithUBHigherThanBestLB(fringe);

//                System.out.println(current + " vs " + currentBest);
//                System.out.println(current.getChanges());
                if (isConverged(current)) {
                    currentBest = current;
                    System.out.println(current);
                    return;
                }
                if(expandCondition.validForExpansion(restrictedGameConfig, current))
                    gameExpander.expand(restrictedGameConfig, current.getMinPlayerBestResponse());
                addMiddleChildOf(current, fringe, restrictedGameConfig);
                addLeftChildOf(current, fringe, restrictedGameConfig);
                addRightChildOf(current, fringe, restrictedGameConfig);
            }
            finalValue = currentBest.getLb();
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



    protected boolean isZero(int[] probability) {
        return !Arrays.stream(probability).anyMatch(prob -> prob > 0);
    }

    protected int[] getLeftExactProbability(Candidate current) {
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


//    protected int getDigit(double value, int digit) {
//        int firstDigit = (int) Math.floor(value);
//
//        if (digit == 0)
//            return firstDigit;
//        double tempValue = value - firstDigit;
//
//        tempValue = Math.floor(tempValue * Math.pow(10, digit));
//        return (int) (tempValue - 10 * (long) (tempValue / 10));
//    }

    protected boolean isConverged(Candidate currentBest) {
        return isConverged(currentBest.getLb(), currentBest.getUb());
    }

    protected DOCandidate createCandidate(Changes changes, LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);

        assert definedEverywhere(p1Strategy, config);
        assert equalsInPRInformationSets(p1Strategy, config, lpData);
        assert isConvexCombination(p1Strategy, lpData, config);
        Pair<Double, Map<Action, Double>> lowerBoundAndBR = getLowerBoundAndBR(p1Strategy);
        double upperBound = getUpperBound(lpData);
        Action action = findMostViolatedBilinearConstraints(config, lpData);
        int[] exactProbability = getExactProbability(p1Strategy.get(action), table.getPrecisionFor(action));

        assert lowerBoundAndBR.getLeft() <= upperBound + 1e-6;
        return new DOCandidate(lowerBoundAndBR.getLeft(), upperBound, changes, action, exactProbability, lowerBoundAndBR.getRight());
    }


    protected double getUpperBound(LPData lpData) throws IloException {
        return lpData.getSolver().getObjValue();
    }

    protected Pair<Double, Map<Action, Double>> getLowerBoundAndBR(Map<Action, Double> p1Strategy) {
        long start = mxBean.getCurrentThreadCpuTime();

        Map<Action, Double> bestResponse = br.getBestResponse(p1Strategy);
        BRTime += (mxBean.getCurrentThreadCpuTime() - start) / 1e6;
        return new Pair<>(-br.getValue(), bestResponse);
    }
}
