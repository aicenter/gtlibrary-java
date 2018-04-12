package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb;

import cz.agents.gtlibrary.algorithms.bestresponse.ImperfectRecallBestResponseImpl;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.ReusingBilinearTable;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.*;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oldimpl.BNBCandidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
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

@Deprecated
public class ReusingBilinearSequenceFormBNB {
    public static double BILINEAR_PRECISION = 0.0001;
    public static double EPS = 0.000001;

    private final Player player;
    private final Player opponent;


    private ReusingBilinearTable table;
    private Expander<SequenceFormIRInformationSet> expander;
    private GameInfo gameInfo;
    private Double finalValue = null;
    private int maxRefinements = 6;

    private Candidate currentBest;

    private boolean DEBUG = false;
    public static boolean SAVE_LPS = false;

    private double globalLB;
    private Action mostBrokenAction;
    private double mostBrokenActionValue;

    private int nodeCount = 0;
    private StrategyLP strategyLP;

    public static void main(String[] args) {
        runRandomGame();
//        runBRTest();
    }

    private static void runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new RandomGameInfo());
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);

        ReusingBilinearSequenceFormBNB solver = new ReusingBilinearSequenceFormBNB(BRTestGameInfo.FIRST_PLAYER, expander, new RandomGameInfo());

        GambitEFG exporter = new GambitEFG();
        exporter.write("RG.gbt", root, expander);

        solver.setExpander(expander);
        System.out.println("Information sets: " + config.getCountIS(0));
        System.out.println("Sequences P1: " + config.getSequencesFor(solver.player).size());
        ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
        long start = mxBean.getCurrentThreadCpuTime();

        solver.solve(config);
        System.out.println((mxBean.getCurrentThreadCpuTime() - start) / 1e6);
        System.out.println("GAME ID " + RandomGameInfo.seed + " = " + solver.finalValue);
    }

    private static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig(new BRTestGameInfo());
        Expander<SequenceFormIRInformationSet> expander = new BRTestExpander<>(config);

        builder.build(new BRTestGameState(), config, expander);
        ReusingBilinearSequenceFormBNB solver = new ReusingBilinearSequenceFormBNB(BRTestGameInfo.FIRST_PLAYER, expander, new BRTestGameInfo());

        solver.solve(config);
    }

    public ReusingBilinearSequenceFormBNB(Player player, Expander<SequenceFormIRInformationSet> expander, GameInfo info) {
        this.table = new ReusingBilinearTable();
        this.player = player;
        this.opponent = info.getOpponent(player);
        this.gameInfo = info;
        this.expander = expander;
    }

    public void solve(SequenceFormIRConfig config) {
        strategyLP = new StrategyLP(config);
        buildBaseLP(config);
        try {
            LPData lpData = table.toCplex();

            if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
            lpData.getSolver().solve();
            System.out.println(lpData.getSolver().getStatus());
            System.out.println(lpData.getSolver().getObjValue());
            Queue<Candidate> fringe = new PriorityQueue<>();

            currentBest = createCandidate(lpData, config);
            if (DEBUG) System.out.println("most violated action: " + currentBest.getAction());
            if (DEBUG) System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getLb());

            if (isConverged(currentBest))
                return;
            fringe.add(currentBest);

            while (!fringe.isEmpty()) {
                Candidate current = pollCandidateWithUBHigherThanBestLB(fringe);

//                if (currentBest.getLb() < current.getLb()) {
//                    currentBest = current;
//                    if(DEBUG) System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getLb());
//                }
                if (isConverged(current)) {
                    currentBest = current;
                    System.out.println(current);
                    return;
                }
                current.getChanges().updateTable(table);
                int precision = table.getPrecisionFor(current.getAction());
//                int fixedDigits = current.getFixedDigitsForCurrentAction() + 1;
//
//                if (precision <= fixedDigits + 1 && precision < 7)
//                    table.refinePrecisionOfRelevantBilinearVars(current.getAction());
                if (precision < 7)
                    addMiddleChildOf(current, fringe, config);
                addLeftChildOf(current, fringe, config);
                addRightChildOf(current, fringe, config);
                current.getChanges().removeChanges(table);
                table.resetVariableBounds();
            }

            LPData checkData = table.toCplex();

            checkData.getSolver().exportModel("modelAfterAlg.lp");
            System.out.println(currentBest);
            currentBest.getChanges().updateTable(table);
            lpData = table.toCplex();

            lpData.getSolver().solve();
            Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);
//            assert definedEverywhere(p1Strategy, config);
//            assert equalsInPRInformationSets(p1Strategy, config, lpData);
//            assert isConvexCombination(p1Strategy, lpData, config);
            double lowerBound = getLowerBound(p1Strategy);
            double upperBound = getUpperBound(lpData);

            System.out.println("UB: " + upperBound + " LB: " + lowerBound);
            p1Strategy.entrySet().stream().forEach(System.out::println);
            finalValue = lowerBound;
            checkCurrentBestOnCleanLP(config);
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    private void checkCurrentBestOnCleanLP(SequenceFormIRConfig config) throws IloException {
        System.out.println("Check!!!!!!!!!!!!!!");
        table = new ReusingBilinearTable();
        buildBaseLP(config);
        LPData checkData = table.toCplex();

        checkData.getSolver().exportModel("cleanModel.lp");

        currentBest.getChanges().updateTable(table);
        LPData lpData = table.toCplex();

        lpData.getSolver().solve();
        Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);

        assert definedEverywhere(p1Strategy, config);
        assert equalsInPRInformationSets(p1Strategy, config, lpData);
        assert isConvexCombination(p1Strategy, lpData, config);
        double lowerBound = getLowerBound(p1Strategy);
        double upperBound = getUpperBound(lpData);
        System.out.println("UB: " + upperBound + " LB: " + lowerBound);
        p1Strategy.entrySet().stream().forEach(System.out::println);
    }

//    public int[] getNonDeltaValue(Object object, LPData data) {
//        double reward = 0;
//        IloNumVar[][] actionWValues = table.getWVariablesFor(object);
//        int[] exactValue = new int[actionWValues[0].length];
//
//        try {
//            for (int k = 0; k < 2; k++) {
//                reward += k*data.getSolver().getValue(actionWValues[k][0]);
//            }
//            for (int k = 1; k < 10; k++) {
//                for (int i = 1; i < actionWValues[k].length; i++) {
//                    reward += k * data.getSolver().getValue(actionWValues[k][i]) * Math.pow(10, -i);
//                }
//            }
//            int intValue = (int)Math.round(reward*Math.pow(10, exactValue.length - 1));
//
//            for (int i = 0; i < exactValue.length; i++) {
//                exactValue[i] = (int) (intValue/Math.pow(10, exactValue.length - 1 - i));
//            }
//            return exactValue;
//        } catch (IloException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    //  this is not solved yet, rework to change providers or export the duplicity
    private void addMiddleChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) {
        Changes newChanges = new Changes(current.getChanges());
        int[] probability = getMiddleExactProbability(current);
        Change change = new MiddleChange(current.getAction(), probability);

        newChanges.add(change);
        try {
            if (change.updateW(table)) {
                LPData lpData = table.toCplex();

                if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
                lpData.getSolver().solve();
                if (DEBUG) {
                    System.out.println(lpData.getSolver().getStatus());
                    System.out.println(lpData.getSolver().getObjValue());
                    System.out.println(newChanges);
                }
                if (lpData.getSolver().getStatus().equals(IloCplex.Status.Optimal)) {
                    Candidate candidate = createCandidate(newChanges, lpData, config);

                    if (DEBUG) System.out.println("Candidate: " + candidate + " vs " + currentBest);
                    if (isConverged(candidate)) {
                        if (candidate.getLb() > currentBest.getLb()) {
                            currentBest = candidate;
                            System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getUb());
                        }
                    } else if (candidate.getUb() > currentBest.getLb()) {
                        if (DEBUG) System.out.println("most violated action: " + candidate.getAction());
                        fringe.add(candidate);
                    }
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        } finally {
            change.removeWUpdate(table);
        }
    }

    private int[] getMiddleExactProbability(Candidate current) {
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

    private void addRightChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) { // tady teď v remove nemažu všechnoa si ne? nebo jo když se to volá v toCplex
        if (current.getActionProbability()[0] == 1)
            return;
        Changes newChanges = new Changes(current.getChanges());
        int[] probability = getRightExactProbability(current);
        Change change = new RightChange(current.getAction(), probability);

        newChanges.add(change);
        try {
            if (change.updateW(table)) {
                LPData lpData = table.toCplex();

                if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
                lpData.getSolver().solve();
                if (DEBUG) {
                    System.out.println(lpData.getSolver().getStatus());
                    System.out.println(lpData.getSolver().getObjValue());
                    System.out.println(newChanges);
                }
                if (lpData.getSolver().getStatus().equals(IloCplex.Status.Optimal)) {
                    Candidate candidate = createCandidate(newChanges, lpData, config);

                    if (DEBUG) System.out.println("Candidate: " + candidate + " vs " + currentBest);
                    if (isConverged(candidate)) {
                        if (candidate.getLb() > currentBest.getLb()) {
                            currentBest = candidate;
                            System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getUb());
                        }
                    } else if (candidate.getUb() > currentBest.getLb()) {
                        if (DEBUG) System.out.println("most violated action: " + candidate.getAction());

                        fringe.add(candidate);
                    }
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        } finally {
            change.removeWUpdate(table);
        }
    }

    private int[] getRightExactProbability(Candidate current) {
        int[] probability = new int[current.getActionProbability().length];

        System.arraycopy(current.getActionProbability(), 0, probability, 0, probability.length);
        probability[probability.length - 1]++;
        return probability;
    }

    private void addLeftChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) {
        Changes newChanges = new Changes(current.getChanges());
        int[] probability = getLeftExactProbability(current);

        if (isZero(probability))
            return;
        if (newChanges.stream().anyMatch(change -> (change instanceof LeftChange && probability.equals(change.getFixedDigitArrayValue()))))
            probability[probability.length - 1]--;
        Change change = new LeftChange(current.getAction(), probability);

        newChanges.add(change);
        try {
            if (change.updateW(table)) {
                LPData lpData = table.toCplex();

                if (SAVE_LPS) lpData.getSolver().exportModel("bilinSQFnew.lp");
                lpData.getSolver().solve();
                if (DEBUG) {
                    System.out.println(lpData.getSolver().getStatus());
                    System.out.println(lpData.getSolver().getObjValue());
                    System.out.println(newChanges);
                }
                if (lpData.getSolver().getStatus().equals(IloCplex.Status.Optimal)) {
                    Candidate candidate = createCandidate(newChanges, lpData, config);

                    if (DEBUG) System.out.println("Candidate: " + candidate + " vs " + currentBest);
                    if (isConverged(candidate)) {
                        if (DEBUG) System.out.println("converged");
                        if (candidate.getLb() > currentBest.getLb()) {
                            currentBest = candidate;
                            System.out.println("LB: " + currentBest.getLb() + " UB: " + currentBest.getUb());
                        }
                    } else if (candidate.getUb() > currentBest.getLb()) {
                        if (DEBUG) System.out.println("most violated action: " + candidate.getAction());
                        fringe.add(candidate);
                    }
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        } finally {
            change.removeWUpdate(table);
        }
    }

    private boolean isZero(int[] probability) {
        if(Arrays.stream(probability).anyMatch(prob -> prob > 0))
            return false;
        return true;
    }

    private int[] getLeftExactProbability(Candidate current) {
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

//    private double updateProbabilityForLeft(Candidate current, int[] fixedDigitArray) {
//        double probability = Math.floor(Math.pow(10, fixedDigitArray.length - 1) * current.getActionProbability()) /
//                Math.pow(10, fixedDigitArray.length - 1);
//
//        if (probability == current.getActionProbability())
//            probability -= Math.pow(10, -fixedDigitArray.length + 1);
//        return probability;
//    }

    private int getDigit(double value, int digit) {
        int firstDigit = (int) Math.floor(value);

        if (digit == 0)
            return firstDigit;
        double tempValue = value - firstDigit;

        tempValue = Math.floor(tempValue * Math.pow(10, digit));
        return (int) (tempValue - 10 * (long) (tempValue / 10));
    }

    private boolean isConverged(Candidate currentBest) {
        return isConverged(currentBest.getLb(), currentBest.getUb());
    }

    private Candidate pollCandidateWithUBHigherThanBestLB(Queue<Candidate> fringe) {
        Candidate current = null;

        while (current == null || (current.getUb() < currentBest.getLb() && !fringe.isEmpty()))
            current = fringe.poll();
        return current;
    }

    private boolean isConverged(double globalLB, double globalUB) {
        return Math.abs(globalUB - globalLB) < 1e-4;
    }

    private void buildBaseLP(SequenceFormIRConfig config) {
        addObjective();
        addRPConstraints(config);
        addBehaviorStrategyConstraints(config);
        markAllBilinearVariables(config);
        addValueConstraints(config);
    }

    private Candidate createCandidate(Changes changes, LPData lpData, SequenceFormIRConfig config) throws IloException {
        Map<Action, Double> p1Strategy = extractBehavioralStrategyLP(config, lpData);

        assert definedEverywhere(p1Strategy, config);
        assert equalsInPRInformationSets(p1Strategy, config, lpData);
        assert isConvexCombination(p1Strategy, lpData, config);
        double lowerBound = getLowerBound(p1Strategy);
        double upperBound = getUpperBound(lpData);
        Action action = findMostViolatedBilinearConstraints(config, lpData);
        int[] exactProbability = getExactProbability(p1Strategy.get(action), table.getPrecisionFor(action));

        return new Candidate(lowerBound, upperBound, changes, action, exactProbability);
    }

    private int[] getExactProbability(Double value, int precision) {
        int[] exactValue = new int[precision];
        int intValue = (int) Math.floor(value * Math.pow(10, precision));

        for (int i = 0; i < exactValue.length; i++) {
            exactValue[i] = (int) (intValue / Math.pow(10, exactValue.length - i));
            intValue -= exactValue[i] * Math.pow(10, exactValue.length - i);
        }
        return exactValue;
    }

    private Candidate createCandidate(LPData lpData, SequenceFormIRConfig config) throws IloException {
        return createCandidate(new Changes(), lpData, config);
    }

    private double getUpperBound(LPData lpData) throws IloException {
        return lpData.getSolver().getObjValue();
    }

    private double getLowerBound(Map<Action, Double> p1Strategy) {
        ImperfectRecallBestResponseImpl br = new ImperfectRecallBestResponseImpl(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);

        br.getBestResponse(p1Strategy);
        return -br.getValue();
    }

    private boolean isConsistent(Action action, double strategy, BNBCandidate candidate) {
        for (Pair<BNBCandidate.ChangeType, Triplet<Integer, Action, Double>> change : candidate.getChanges()) {
            Action changeAction = change.getRight().getSecond();

            if (changeAction.equals(action)) {
                if (change.getLeft().equals(BNBCandidate.ChangeType.LEFT)) {
//                    double truncValue = ((int) (change.getRight().getThird() * (10 ^ (change.getRight().getFirst())))) / ((double)((10 ^ change.getRight().getFirst())));

                    if (strategy - 1e-5 >= change.getRight().getThird())
                        return false;
                } else if (change.getLeft().equals(BNBCandidate.ChangeType.RIGHT)) {
//                    double truncValue = ((int) (change.getRight().getThird() * (10 ^ (change.getRight().getFirst())))) / ((double)(10 ^ change.getRight().getFirst()));

                    if (strategy + 1e-5 < change.getRight().getThird())
                        return false;
                } else {
                    assert change.getRight().getFirst() > 1;
                    for (int i = 2; i <= change.getRight().getFirst(); i++) {
                        int stratDigit = getDigit(strategy, i - 1);
                        int correctDigit = getDigit(change.getRight().getThird(), i - 1);

                        if (stratDigit != correctDigit) {
                            if (Math.abs(change.getRight().getThird() - strategy) > Math.pow(10, -(change.getRight().getFirst() - 1)) + Math.pow(10, -(change.getRight().getFirst())))
                                return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isConsistent(Map<Action, Double> strategy, BNBCandidate candidate) {
        for (Map.Entry<Action, Double> entry : strategy.entrySet()) {
            if (!isConsistent(entry.getKey(), entry.getValue(), candidate))
                return false;
        }
        return true;
    }

    private boolean equalsInPRInformationSets(Map<Action, Double> p1Strategy, SequenceFormIRConfig config, LPData lpData) throws IloException {
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

    private boolean definedEverywhere(Map<Action, Double> p1Strategy, SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player) && !informationSet.getActions().isEmpty()) {
                double sum = 0;

                for (Action action : informationSet.getActions()) {
                    Double value = p1Strategy.get(action);

                    if (value != null)
                        sum += value;
                }
                if (Math.abs(1 - sum) > 1e-7)
                    return false;
            }
        }
        return true;
    }

    private boolean isConvexCombination(Map<Action, Double> p1Strategy, LPData lpdata, SequenceFormIRConfig config) throws IloException {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (!informationSet.getPlayer().equals(player))
                continue;
            Map<Action, Double> ubs = new HashMap<>();
            Map<Action, Double> lbs = new HashMap<>();

            for (Map.Entry<Sequence, Set<Sequence>> entry : informationSet.getOutgoingSequences().entrySet()) {
                double incomingSeqProb = lpdata.getSolver().getValue(lpdata.getVariables()[table.getVariableIndex(entry.getKey())]);

                if (incomingSeqProb > 1e-8)
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

    private boolean isOut(Map<Action, Double> lbs, Map<Action, Double> ubs, Map<Action, Double> p1Strategy) {
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

    private void updateUbs(Map<Action, Double> ubs, Action action, double behavStrat) {
        Double ub = ubs.get(action);

        if (ub == null || behavStrat > ub)
            ubs.put(action, behavStrat);
    }

    private void updateLbs(Map<Action, Double> lbs, Action action, double behavStrat) {
        Double lb = lbs.get(action);

        if (lb == null || behavStrat < lb)
            lbs.put(action, behavStrat);
    }

    private void markAllBilinearVariables(SequenceFormIRConfig config) {

        config.getSequencesFor(player)
                .stream()
                .filter(s -> !s.isEmpty())
                .filter(s -> ((SequenceFormIRInformationSet) s.getLastInformationSet()).hasIR())
                .forEach(s -> markBilinearFor(s));
//        for (Sequence sequence : config.getSequencesFor(player)) {
//            if (sequence.isEmpty())
//                continue;
//            if (!((SequenceFormIRInformationSet) sequence.getLastInformationSet()).hasIR())
//                continue;
//            markBilinearFor(sequence);
//        }
    }

    private void markBilinearFor(Sequence sequence) {
        table.markAsBilinear(sequence, sequence.getSubSequence(sequence.size() - 1), sequence.getLast());
    }

    private void addValueConstraints(SequenceFormIRConfig config) {
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
                    table.setConstraint(eqKey, compatibleSequence, -utility);
            }
        }
    }

    private void addBehaviorStrategyConstraints(SequenceFormIRConfig config) {
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

    private void addRPConstraints(SequenceFormIRConfig config) {
        table.setConstraint("rpInit", new ArrayListSequenceImpl(player), 1);
        table.setConstant("rpInit", 1);
        table.setConstraintType("rpInit", 1);
        config.getAllInformationSets().values().stream().filter(i -> i.getPlayer().equals(player)).forEach(i -> addRPConstraint(i));
        addRPVarBounds(config);
    }

    private void addRPVarBounds(SequenceFormIRConfig config) {
        config.getSequencesFor(player).stream().forEach(s -> setZeroOneBounds(s));
    }

    private void setZeroOneBounds(Object object) {
        table.setLowerBound(object, 0);
        table.setUpperBound(object, 1);
    }

    private void addRPConstraint(SequenceFormIRInformationSet informationSet) {
        for (Map.Entry<Sequence, Set<Sequence>> outgoingEntry : informationSet.getOutgoingSequences().entrySet()) {
            Object eqKey = new Pair<>(informationSet, outgoingEntry.getKey());

            table.setConstraint(eqKey, outgoingEntry.getKey(), 1);
            table.setConstraintType(eqKey, 1);
            for (Sequence sequence : outgoingEntry.getValue()) {
                table.setConstraint(eqKey, sequence, -1);
            }
        }
    }

    private void addObjective() {
        table.setObjective(new Pair<>("root", new ArrayListSequenceImpl(opponent)), 1);
    }

    //version with shallowest IS
    private Action findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
        Set<Action> actions = config.getSequencesFor(player).stream()
                .filter(s -> !s.isEmpty())
                .filter(s -> ((SequenceFormIRInformationSet) s.getLastInformationSet()).hasIR())
                .map(Sequence::getLast).collect(Collectors.toSet());
        double currentShallowestDepth = Double.POSITIVE_INFINITY;
        double currentShallowestError = Double.NEGATIVE_INFINITY;
        Action currentBest = null;

        for (Action a : actions) {
            SequenceFormIRInformationSet is = (SequenceFormIRInformationSet) a.getInformationSet();
            double average = 0;
            ArrayList<Double> specValues = new ArrayList<>();

            for (Map.Entry<Sequence, Set<Sequence>> entry : is.getOutgoingSequences().entrySet()) {
                if (data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]) > 0) {
                    Sequence productSequence = new ArrayListSequenceImpl(entry.getKey());
                    productSequence.addLast(a);
                    double sV = data.getSolver().getValue(data.getVariables()[table.getVariableIndex(productSequence)]);
                    sV = sV / data.getSolver().getValue(data.getVariables()[table.getVariableIndex(entry.getKey())]);
                    average += sV;
                    specValues.add(sV);
                }
            }
            if (specValues.size() == 0)
                average = 0;
            else
                average = average / specValues.size();

            double error = getError(average, specValues);

            if (error > 1e-4) {  //nesmis se zaseknout na malé chybě a furt jí vracet zkontorlovat když dávám more shallow, že ta chyba je alespoň něco
                double avgDepth = getAverageDepth(is);

                if (avgDepth < currentShallowestDepth) {
                    currentShallowestDepth = avgDepth;
                    currentShallowestError = error;
                    currentBest = a;
                } else if (avgDepth == currentShallowestDepth) {
                    if (currentShallowestError <= error) {
                        currentShallowestError = error;
                        currentBest = a;
                    }
                }

            }
        }
        if (currentBest == null)
            currentBest = actions.iterator().next();
        return currentBest;
    }

//    private Action findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
//        return mostBrokenAction;
//    }

    private double getError(double average, ArrayList<Double> specValues) {
        return specValues.stream().mapToDouble(d -> Math.abs(average - d)).sum();
    }

//    private Set<Action> findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
//        Set<Action> set = new HashSet<>(1);
//
//        set.add(mostBrokenAction);
//        return set;
//    }

    private double getAverageDepth(SequenceFormIRInformationSet informationSet) {
        return informationSet.getOutgoingSequences().keySet().stream().mapToInt(s -> s.size()).average().getAsDouble();
    }


    public Expander getExpander() {
        return expander;
    }

    public void setExpander(Expander expander) {
        this.expander = expander;
    }

    public Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
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

    public Map<Action, Double> extractBehavioralStrategyLP(SequenceFormIRConfig config, LPData lpData) throws IloException {
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> P1Strategy = new HashMap<>();

        mostBrokenAction = null;
        mostBrokenActionValue = Double.NEGATIVE_INFINITY;
        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
            if (!i.getPlayer().equals(player)) continue;
            boolean allZero = true;
            if (i.hasIR()) {
                strategyLP.clear();
                for (Map.Entry<Sequence, Set<Sequence>> entry : i.getOutgoingSequences().entrySet()) {
                    for (Sequence outgoingSequence : entry.getValue()) {
                        double outgiongSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(outgoingSequence)]);
                        double incomingSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(entry.getKey())]);

                        if (incomingSeqProb > 0) {
                            allZero = false;
                            strategyLP.add(entry.getKey(), outgoingSequence, incomingSeqProb, outgiongSeqProb);
                        }
                    }
                }
                if (!allZero) {
                    Map<Action, Double> strategy = strategyLP.getStartegy();

                    i.getActions().stream()
                            .filter(action -> !strategy.containsKey(action))
                            .forEach(action -> strategy.put(action, 0d));
                    P1Strategy.putAll(strategy);
                    Pair<Action, Double> actionCostPair = strategyLP.getMostExpensiveActionCostPair();

                    if (mostBrokenActionValue < actionCostPair.getRight()) {
                        mostBrokenActionValue = actionCostPair.getRight();
                        mostBrokenAction = actionCostPair.getLeft();
                    }
                }
            } else if (!i.getOutgoingSequences().isEmpty()) {
                assert i.getOutgoingSequences().size() == 1;
                double incomingSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(i.getOutgoingSequences().keySet().iterator().next())]);

                if (incomingSeqProb > 0) {
                    allZero = false;
                    for (Sequence outgoingSequence : i.getOutgoingSequences().entrySet().iterator().next().getValue()) {
                        double outgoingSeqProb = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(outgoingSequence)]);

                        P1Strategy.put(outgoingSequence.getLast(), outgoingSeqProb / incomingSeqProb);
                    }
                    i.getActions().stream()
                            .filter(action -> !P1Strategy.containsKey(action))
                            .forEach(action -> P1Strategy.put(action, 0d));
                }
            }
            if (allZero && i.getActions().size() > 0) {
                P1Strategy.put(i.getActions().iterator().next(), 1d);

                i.getActions().stream()
                        .filter(action -> !P1Strategy.containsKey(action))
                        .forEach(action -> P1Strategy.put(action, 0d));
            }
        }
        if (mostBrokenAction == null)
            mostBrokenAction = addFirstAvailable(config);
        return P1Strategy;
    }

    private Action addFirstAvailable(SequenceFormIRConfig config) {
        for (SequenceFormIRInformationSet informationSet : config.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(player) && informationSet.hasIR() && !informationSet.getActions().isEmpty())
                return informationSet.getActions().iterator().next();
        }
        config.getAllInformationSets().values().stream()
                .filter(informationSet -> informationSet.hasIR())
                .forEach(informationSet -> {
                    assert false;
                });
        return null;
    }

    public Map<Action, Double> extractBehavioralStrategyBestFirst(SequenceFormIRConfig config, LPData lpData) throws IloException {
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> P1Strategy = new HashMap<>();
        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
            if (!i.getPlayer().equals(player)) continue;
            boolean allZero = true;
            for (Action a : i.getActions()) {
                double average = 0;
                int count = 0;
                Sequence bestSubS = null;
                double maxBestSubS = Double.NEGATIVE_INFINITY;
                for (Sequence subS : i.getOutgoingSequences().keySet()) {
                    if (lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]) > maxBestSubS) {
                        maxBestSubS = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(subS)]);
                        bestSubS = subS;
                    }
                }
                Sequence s = new ArrayListSequenceImpl(bestSubS);
                s.addLast(a);

                if (lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(bestSubS)]) > 0) {
                    double sV = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
                    sV = sV / lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(bestSubS)]);
                    average += sV;
                    count++;
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

    public Map<Sequence, Double> extractRPStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
        Map<Sequence, Double> P1StrategySeq = new HashMap<>();
        for (Sequence s : config.getSequencesFor(player)) {
            if (s.isEmpty()) continue;
            Action a = s.getLast();
            double seqValue = lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]);
            if (DEBUG) System.out.println(s + " = " + seqValue);

            P1StrategySeq.put(s, lpData.getSolver().getValue(lpData.getVariables()[table.getVariableIndex(s)]));
        }
        return P1StrategySeq;
    }

}
