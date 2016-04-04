package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb;

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
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.*;
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

public class BilinearSequenceFormBnB {
    public static boolean DEBUG = false;
    public static boolean SAVE_LPS = false;
    public static double EPS = 1e-3;

    private final Player player;
    private final Player opponent;


    private BilinearTable table;
    private Expander<SequenceFormIRInformationSet> expander;
    private GameInfo gameInfo;
    private Double finalValue = null;
    private int maxRefinements = 7;

    private Candidate currentBest;

    private Action mostBrokenAction;
    private double mostBrokenActionValue;
    private StrategyLP strategyLP;

    public static void main(String[] args) {
        runRandomGame();
//        runBRTest();
    }

    public static double runRandomGame() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
        GameState root = new RandomGameState();
        Expander<SequenceFormIRInformationSet> expander = new RandomGameExpander<>(config);

        builder.build(root, config, expander);

        BilinearSequenceFormBnB solver = new BilinearSequenceFormBnB(BRTestGameInfo.FIRST_PLAYER, expander, new RandomGameInfo());

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
        return solver.finalValue;
    }

    private static void runBRTest() {
        BasicGameBuilder builder = new BasicGameBuilder();
        SequenceFormIRConfig config = new SequenceFormIRConfig();
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

            if (isConverged(currentBest)) {
                finalValue = currentBest.getLb();
                return;
            }
            fringe.add(currentBest);

            while (!fringe.isEmpty()) {
                Candidate current = pollCandidateWithUBHigherThanBestLB(fringe);

                if (isConverged(current)) {
                    currentBest = current;
                    System.out.println(current);
                    return;
                }
                addMiddleChildOf(current, fringe, config);
                addLeftChildOf(current, fringe, config);
                addRightChildOf(current, fringe, config);
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

    private void checkCurrentBestOnCleanLP(SequenceFormIRConfig config) throws IloException {
        System.out.println("Check!!!!!!!!!!!!!!");
        table = new BilinearTable();
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

    private void addMiddleChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) {
        Changes newChanges = new Changes(current.getChanges());
        int[] probability = getMiddleExactProbability(current);
        Change change = new MiddleChange(current.getAction(), probability);

        table.clearTable();
        buildBaseLP(config);
        current.getChanges().updateTable(table);
        int precision = table.getPrecisionFor(current.getAction());

        if (precision >= maxRefinements)
            return;
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

    private void addRightChildOf(Candidate current, Queue<Candidate> fringe, SequenceFormIRConfig config) {
        if (current.getActionProbability()[0] == 1)
            return;
        Changes newChanges = new Changes(current.getChanges());
        int[] probability = getRightExactProbability(current);
        Change change = new RightChange(current.getAction(), probability);

        table.clearTable();
        buildBaseLP(config);
        current.getChanges().updateTable(table);
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
        if (newChanges.stream().anyMatch(change -> (change instanceof LeftChange && Arrays.equals(probability, change.getFixedDigitArrayValue()))))
            probability[probability.length - 1]--;
        Change change = new LeftChange(current.getAction(), probability);

        table.clearTable();
        buildBaseLP(config);
        current.getChanges().updateTable(table);
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

    private boolean isZero(int[] probability) {
        if (Arrays.stream(probability).anyMatch(prob -> prob > 0))
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
        ImperfectRecallBestResponse br = new ImperfectRecallBestResponse(RandomGameInfo.SECOND_PLAYER, expander, gameInfo);

        br.getBestResponse(p1Strategy);
        return -br.getValue();
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

    /**
     * Chooses the action a causing the highest error * 10^(MAX_DEPTH - average depth of the IS where a is played)
     *
     * @param config
     * @param data
     * @return
     * @throws IloException
     */
    private Action findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
        Set<Action> actions = config.getSequencesFor(player).stream()
                .filter(s -> !s.isEmpty())
                .filter(s -> ((SequenceFormIRInformationSet) s.getLastInformationSet()).hasIR())
                .map(Sequence::getLast).collect(Collectors.toSet());
        double currentError = Double.NEGATIVE_INFINITY;
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
            double error = getError(average.getAsDouble(), specValues) * Math.pow(10, gameInfo.getMaxDepth() - getAverageDepth(is));

            if (error > currentError) {
                currentError = error;
                currentBest = action;
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
//    private Action findMostViolatedBilinearConstraints(SequenceFormIRConfig config, LPData data) throws IloException {
//        return mostBrokenAction;
//    }

    private double getError(double average, ArrayList<Double> specValues) {
        return specValues.stream().mapToDouble(d -> Math.abs(average - d)).sum();
    }


    private double getAverageDepth(SequenceFormIRInformationSet informationSet) {
        return informationSet.getOutgoingSequences().keySet().stream().mapToInt(Sequence::size).average().getAsDouble();
    }


    public Expander getExpander() {
        return expander;
    }

    public void setExpander(Expander expander) {
        this.expander = expander;
    }

    private Map<Action, Double> extractBehavioralStrategy(SequenceFormIRConfig config, LPData lpData) throws IloException {
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

    private Map<Action, Double> extractBehavioralStrategyLP(SequenceFormIRConfig config, LPData lpData) throws IloException {
        if (DEBUG) System.out.println("----- P1 Actions -----");
        Map<Action, Double> P1Strategy = new HashMap<>();

        mostBrokenAction = null;
        mostBrokenActionValue = Double.NEGATIVE_INFINITY;
        for (SequenceFormIRInformationSet i : config.getAllInformationSets().values()) {
            if (!i.getPlayer().equals(player))
                continue;
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
        assert !config.getAllInformationSets().values().stream().anyMatch(SequenceFormIRInformationSet::hasIR);
        return config.getAllInformationSets().values().iterator().next().getActions().iterator().next();
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
