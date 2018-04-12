package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.br.FollowerBestResponse;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import javax.sound.midi.MidiDevice;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class SumForbiddingStackelbergLP extends StackelbergSequenceFormLP {

    public static boolean USE_BR_CUT = false;

    protected double eps;
    protected RecyclingMILPTable lpTable;
    protected Player leader;
    protected Player follower;
    protected GameInfo info;
    protected ThreadMXBean threadBean;
    protected Pair<Map<Sequence, Double>, Double> dummyResult = new Pair<>(null, Double.NEGATIVE_INFINITY);
    protected StackelbergConfig algConfig;
    protected Expander<SequenceInformationSet> expander;
    protected FollowerBestResponse followerBestResponse;

    protected int lpInvocationCount;
    protected boolean solved = false;

    public SumForbiddingStackelbergLP(Player leader, GameInfo info) {
        super(new Player[]{info.getAllPlayers()[0], info.getAllPlayers()[1]}, leader, info.getOpponent(leader));
        lpTable = new RecyclingMILPTable();
        this.leader = leader;
        this.follower = info.getOpponent(leader);
        this.info = info;
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.lpInvocationCount = 0;
        this.eps = 1e-8;
    }

    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        this.algConfig = algConfig;
        this.expander = expander;
        followerBestResponse = new FollowerBestResponse(algConfig.getRootState(), expander, algConfig, leader, follower);
        long startTime = threadBean.getCurrentThreadCpuTime();

        addObjective();
//        addObjectiveViaConstraint(algConfig);
        createPContinuationConstraints();
        createSequenceConstraints();
        createISActionConstraints();

        System.out.println("LP build...");
        lpTable.watchAllPrimalVariables();
        overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
        Pair<Map<Sequence, Double>, Double> result = solve(-info.getMaxUtility(), info.getMaxUtility());

        resultStrategies.put(leader, result.getLeft());
        resultValues.put(leader, result.getRight());
        return result.getRight();
    }

    protected Pair<Map<Sequence, Double>, Double> solve(double lowerBound, double upperBound) {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

//            updateLowerBound(lowerBound);
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            lpData.getSolver().exportModel("SSEIter.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            lpData.getSolver().solve();
            lpInvocationCount++;
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            printBinaryVariableValues(lpData);
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

                System.out.println("-----------------------");
                System.out.println("LP reward: " + value + " lower bound: " + lowerBound);
//                System.out.println("n it: " + lpData.getSolver().getNiterations());
//                System.out.println("n nodes: " + lpData.getSolver().getNnodes());
//                for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//                    if (entry.getKey() instanceof Pair && ((Pair) entry.getKey()).getLeft() instanceof Sequence && ((Pair) entry.getKey()).getRight() instanceof Sequence) {
//                        double variableValue = lpData.getSolver().getValue(entry.getValue());
//
//                        if (variableValue != 0)
//                            System.out.println(entry.getKey() + ": " + variableValue);
//                    }
//                }
                Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getBehavioralStrategy(lpData, follower);
                Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(followerBehavStrat, lpData);
                Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));

//                GenSumUtilityCalculator calculator = new GenSumUtilityCalculator(algConfig.getRootState(), expander);
//
//                System.out.println(Arrays.toString(calculator.computeUtility(getP1Strategy(leaderRealPlan, followerRealPlan), getP2Strategy(leaderRealPlan, followerRealPlan))));
//                System.out.println("follower behav. strat.");
//                for (Map.Entry<InformationSet, Map<Sequence, Double>> entry : followerBehavStrat.entrySet()) {
//                    System.out.println(entry);
//                }
                if (brokenStrategyCauses == null) {
//                    System.out.println("Found solution candidate with reward: " + reward);
//                    Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));
//                    Map<Sequence, Double> followerRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, follower));
////
//                    System.out.println("Leader real. plan:");
//                    for (Map.Entry<Sequence, Double> entry : leaderRealPlan.entrySet()) {
//                        System.out.println(entry);
//                    }
//                    System.out.println("Follower real. plan:");
//                    for (Map.Entry<Sequence, Double> entry : followerRealPlan.entrySet()) {
//                        System.out.println(entry);
//                    }
//                    return new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), value);
                    return new Pair<Map<Sequence, Double>, Double>(leaderRealPlan, value);
                } else {
                    if (USE_BR_CUT) {
                        Pair<Map<Sequence, Double>, Double> result = followerBestResponse.computeBestResponseTo(leaderRealPlan);

                        System.out.println("BR: " + result.getRight());
                        if (lowerBound < result.getRight()) {
                            System.out.println("lower bound increased from " + lowerBound + " to " + result.getRight());
                            lowerBound = result.getRight();
                        }

                        if (Math.abs(lowerBound - value) < eps) {
                            System.out.println("solution found BR");
                            return new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), value);
                        }
                    }
                    if (value <= lowerBound + eps) {
                        System.out.println("***********lower bound " + lowerBound + " not exceeded, cutting***********");
                        return dummyResult;
                    }
                    return handleBrokenStrategyCause(lowerBound, upperBound, lpData, value, brokenStrategyCauses);
                }
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return dummyResult;
    }

    private Strategy getP1Strategy(Map<Sequence, Double> leaderRealPlan, Map<Sequence, Double> followerRealPlan) {
        return new NoMissingSeqStrategy(leader.getId() == 0 ? leaderRealPlan : followerRealPlan);
    }

    private Strategy getP2Strategy(Map<Sequence, Double> leaderRealPlan, Map<Sequence, Double> followerRealPlan) {
        return new NoMissingSeqStrategy(leader.getId() == 1 ? leaderRealPlan : followerRealPlan);
    }

    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
        Map<Sequence, Double> shallowestBrokenStrategyCause = null;

        for (Map.Entry<InformationSet, Map<Sequence, Double>> isStrategy : strategy.entrySet()) {
            if (isStrategy.getValue().size() > 1) {
                if (shallowestBrokenStrategyCause == null) {
                    shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                } else {
                    Sequence candidate = isStrategy.getValue().keySet().iterator().next();
                    Sequence bestSoFar = shallowestBrokenStrategyCause.keySet().iterator().next();

                    if (candidate.size() < bestSoFar.size()) {
                        shallowestBrokenStrategyCause = new HashMap<>(isStrategy.getValue());
                    }
                }
            }
        }
        if (shallowestBrokenStrategyCause == null)
            return null;
        return sort(shallowestBrokenStrategyCause, shallowestBrokenStrategyCause.keySet());
    }

    protected Iterable<Sequence> sort(final Map<Sequence, Double> shallowestBrokenStrategyCause, final Collection<Sequence> allSeq) {
        List<Sequence> list = new ArrayList<>(allSeq);

        Collections.sort(list, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                return Double.compare(shallowestBrokenStrategyCause.get(o1), shallowestBrokenStrategyCause.get(o2));
            }
        });
        return list;
    }

    protected void printBinaryVariableValues(LPData data) {
        for (Map.Entry<Object, IloNumVar> entry : data.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair key = (Pair) entry.getKey();

                if (key.getLeft().equals("binary"))
                    try {
                        System.out.println(entry.getKey() + ": " + data.getSolver().getValue(entry.getValue()));
                    } catch (IloException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
        Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;

        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);

            if (result.getRight() > currentBest.getRight()) {
                currentBest = result;
                if (currentBest.getRight() >= value - eps) {
                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent reward " + value + "----------------");
                    return currentBest;
                }
            }
            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
        }
        return currentBest;
    }

    protected void updateLowerBound(double lowerBound) {
        lpTable.setConstant("obj_const", lowerBound);
    }

    protected double getLowerBound(double lowerBound, Pair<Map<Sequence, Double>, Double> currentBest) {
        return Math.max(lowerBound, currentBest.getRight());
    }


    protected void restrictFollowerPlay(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " fixed to zero");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);

                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraintType(eqKey, 1);
                    }
                }
            }
        }
    }

    protected void removeRestriction(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " released");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr", p);

                        lpTable.removeFromConstraint(eqKey, p);
                        lpTable.removeConstant(eqKey);
                    }
                }
            }
        }
    }

    //    protected Map<InformationSet, Map<Sequence, Double>> getBehavioralStrategy(LPData lpData, Player player) {
//        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();
//
//        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//            if (entry.getKey() instanceof Pair) {
//                Pair varKey = (Pair) entry.getKey();
//
//                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
//                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
//                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
//                    Double currentValue = getValueFromCplex(lpData, entry);
//
//                    if (currentValue > eps)
//                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
//                            if (isStrategy == null) {
//                                if (currentValue > eps) {
//                                    isStrategy = new HashMap<>();
//                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                    isStrategy.put(playerSequence, behavioralStrat);
//                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
//                                }
//                            } else {
//                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);
//
//                                if (behavioralStrategy > eps) {
//                                    isStrategy.put(playerSequence, behavioralStrategy);
//                                }
//                            }
//                }
//            }
//        }
//        return strategy;
//    }

    protected Map<InformationSet, Map<Sequence, Double>> getSequenceEvaluation(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                Double oldValue = isStrategy.get(playerSequence);
                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, oldValue == null ? behavioralStrategy : behavioralStrategy + oldValue);
                                }
                            }
                }
            }
        }
        return strategy;
    }

    protected Map<InformationSet, Map<Sequence, Double>> getBehavioralStrategy(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, behavioralStrategy);
                                }
                            }
                }
            }
        }
        return strategy;
    }

    protected Map<InformationSet, Map<Sequence, Double>> getLeaderBehavioralStrategy(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();
//        Map<Pair<Sequence, Sequence>, Double> normalProcessedPairs = new HashMap<>();
//        Map<Pair<Sequence, Sequence>, Double> additionalProcessedPairs = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isRelevantTo(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet())) {
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, behavioralStrategy);
                                }
                            }
//                            if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet(), followerRealPlan)) {
//                                try {
//                                    normalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
//                                } catch (IloException e) {
//                                    e.printStackTrace();
//                                }
//                            } else {
//                                try {
//                                    additionalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
//                                } catch (IloException e) {
//                                    e.printStackTrace();
//                                }
//                            }
                        }
                }
            }
        }
        return strategy;
    }


    protected double getBehavioralStrategy(LPData lpData, Pair varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, varKey.getRight()) : new Pair<>(varKey.getLeft(), sequenceCopy));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (playerSequence.getPlayer().equals(leader) ? varKey.getRight() : varKey.getLeft()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, opponentSequence) : new Pair<>(opponentSequence, sequenceCopy));
                }
            }
            behavioralStrat /= previousValue;
        }
        return behavioralStrat;
    }

    protected boolean isSequenceFrom(Sequence sequence, InformationSet informationSet) {
        if (informationSet == null)
            return sequence.isEmpty();
        assert !sequence.getPlayer().equals(informationSet.getPlayer());
        for (GameState gameState : informationSet.getAllStates()) {
            if (gameState.getSequenceFor(sequence.getPlayer()).equals(sequence))
                return true;
        }
        return false;
    }

    protected boolean isRelevantTo(Sequence sequence, InformationSet informationSet) {
        if (informationSet == null)
            return sequence.isEmpty();
        assert !sequence.getPlayer().equals(informationSet.getPlayer());
        for (GameState gameState : informationSet.getAllStates()) {
            if (gameState.getSequenceFor(sequence.getPlayer()).equals(sequence))
                return true;
        }
        for (GameState gameState : informationSet.getAllStates()) {
            if (sequence.isPrefixOf(gameState.getSequenceFor(sequence.getPlayer())))
                return true;
        }
        return false;
    }

    protected Map<Sequence, Double> behavioralToRealizationPlan(Map<InformationSet, Map<Sequence, Double>> behavioral) {
        Map<Sequence, Double> realizationPlan = new HashMap<>();

        for (Map<Sequence, Double> map : behavioral.values()) {
            for (Map.Entry<Sequence, Double> entry : map.entrySet()) {
                if (entry.getKey().isEmpty()) {
                    assert entry.getValue() == 1;
                    realizationPlan.put(entry.getKey(), entry.getValue());
                } else {
                    double probability = 1;

                    for (Sequence prefix : entry.getKey().getAllPrefixes()) {
                        probability *= behavioral.get(prefix.getLastInformationSet()).get(prefix);
                    }
                    realizationPlan.put(entry.getKey(), probability);
                }
            }
        }
        return realizationPlan;
    }

    private double getSum(Map<Sequence, Double> isStrategy) {
        double sum = 0;

        for (Double value : isStrategy.values()) {
            sum += value;
        }
        return sum;
    }

    protected Double getValueFromCplex(LPData lpData, Map.Entry<Object, IloNumVar> entry) {
        Double currentValue = null;

        try {
            currentValue = lpData.getSolver().getValue(entry.getValue());
        } catch (IloException e) {
            e.printStackTrace();
        }
        return currentValue;
    }

    protected Double getValueFromCplex(LPData lpData, Object varKey) {
        Double currentValue = null;

        try {
            currentValue = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
        } catch (IloException e) {
            e.printStackTrace();
        }
        return currentValue;
    }

    protected void createISActionConstraints() {
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(follower)) {
                if (!informationSet.getOutgoingSequences().isEmpty()) {
                    Sequence outgoingSequence = informationSet.getOutgoingSequences().iterator().next();

                    for (Action action : outgoingSequence) {
                        for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            createISActionConstraint(algConfig, relevantSequence, informationSet);
                        }
                    }
                    createISActionConstraint(algConfig, new ArrayListSequenceImpl(follower), informationSet);
                }
            }
        }
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(follower)) {
                for (Sequence sequence : informationSet.getOutgoingSequences()) {
                    Object eqKey = new Triplet<>(informationSet, sequence, "eq");
                    Object varKey = new Pair<>(informationSet, sequence);
                    Object contVarKey = new Pair<>("v", sequence);

                    lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                    lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                    lpTable.setConstraint(eqKey, varKey, 1);
                    lpTable.setConstraint(eqKey, contVarKey, -1);
                    lpTable.setConstraintType(eqKey, 1);
                }
            }
        }
    }

    protected void createISActionConstraint(StackelbergConfig algConfig, Sequence followerSequence, SequenceInformationSet informationSet) {
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            Object eqKey = new Triplet<>(informationSet, sequence, followerSequence);
            Object varKey = new Pair<>(informationSet, followerSequence);

            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, varKey, 1);
            lpTable.setConstraintType(eqKey, 2);
            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(sequence)) {
                Double[] seqCombUtilities = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, sequence);

                if (seqCombUtilities != null) {
                    double utility = seqCombUtilities[follower.getId()];

                    if (utility != 0)
                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, followerSequence), -utility);
                }
            }
            if (algConfig.getReachableSets(sequence) != null)
                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(sequence)) {
                    if (reachableSet.getOutgoingSequences() != null && !reachableSet.getOutgoingSequences().isEmpty())
                        lpTable.setConstraint(eqKey, new Pair<>(reachableSet, followerSequence), -1);
                }
        }
    }

    protected void addObjectiveViaConstraint(StackelbergConfig algConfig) {
        lpTable.setConstraint("obj", "obj", 1);
        lpTable.setLowerBound("obj", Double.NEGATIVE_INFINITY);
//        lpTable.setConstraintType("obj", 1);
        for (GameState leaf : algConfig.getAllLeafs()) {
            lpTable.setConstraint("obj", createSeqPairVarKey(leaf), -leaf.getUtilities()[leader.getId()] * leaf.getNatureProbability());
        }

        lpTable.setObjective("obj", 1);

        lpTable.setConstraint("obj_const", "obj", 1);
        lpTable.setConstraintType("obj_const", 2);
        lpTable.setConstant("obj_const", -info.getMaxUtility());
    }

    protected void addObjective() {
        for (Map.Entry<GameState, Double[]> entry : algConfig.getActualNonZeroUtilityValuesInLeafsGenSum().entrySet()) {
            lpTable.setObjective(createSeqPairVarKey(entry.getKey()), entry.getValue()[leader.getId()]);
        }
//        for (GameState leaf : algConfig.getAllLeafs()) {
//            lpTable.setObjective(createSeqPairVarKey(leaf), leaf.getUtilities()[leader.getId()] * leaf.getNatureProbability());
//        }
    }

    protected void createSequenceConstraints() {
        createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower));
        for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
            createSequenceConstraint(algConfig, followerSequence);
        }
    }

    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence) {
        Object varKey = new Pair<>("v", followerSequence);

        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(followerSequence, 1);
        lpTable.setConstraint(followerSequence, varKey, 1);
        for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
            Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);

            if (seqCombValue != null) {
                double followerValue = seqCombValue[follower.getId()];

                if (followerValue != 0)
                    lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);
            }
        }
        for (SequenceInformationSet reachableSet : algConfig.getReachableSets(followerSequence)) {
            for (Sequence sequence : reachableSet.getOutgoingSequences()) {
                Object contVarKey = new Pair<>("v", sequence);

                lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(followerSequence, contVarKey, -1);
            }
        }
    }

    protected void createPContinuationConstraints() {
        createInitPConstraint();
        Set<Object> blackList = new HashSet<>();
        Set<Pair<Sequence, Sequence>> pStops = new HashSet<>();

        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            List<Action> actions = expander.getActions(informationSet);
            Player opponent = info.getOpponent(informationSet.getPlayer());

            for (GameState gameState : informationSet.getAllStates()) {
                if (!gameState.isGameEnd())
                    createPContinuationConstraint(actions, opponent, gameState, blackList, pStops);
            }
        }

        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)) {
            for (Sequence compatibleFollowerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)) {
                for (Action action : compatibleFollowerSequence) {
                    Sequence actionHistory = ((PerfectRecallInformationSet)action.getInformationSet()).getPlayersHistory();
                    Object eqKeyFollower = new Triplet<>(leaderSequence, actionHistory, action.getInformationSet());

                    if (!blackList.contains(eqKeyFollower)) {
                        blackList.add(eqKeyFollower);
                        Pair<Sequence, Sequence> varKey = createSeqPairVarKey(leaderSequence, actionHistory);

                        lpTable.setConstraintType(eqKeyFollower, 1);
                        lpTable.setConstraint(eqKeyFollower, varKey, -1);
                        for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, followerSequence), 1);
                        }
                    }

                    ListIterator<Action> leaderSeqIterator = leaderSequence.listIterator(leaderSequence.size());
                    Action leaderAction;

                    while (leaderSeqIterator.hasPrevious()) {
                        leaderAction = leaderSeqIterator.previous();
                        Sequence leaderHistory = ((PerfectRecallInformationSet)leaderAction.getInformationSet()).getPlayersHistory();
                        Object eqKeyLeader = new Triplet<>(leaderHistory, actionHistory, leaderAction);

                        if (!blackList.contains(eqKeyLeader)) {
                            blackList.add(eqKeyLeader);
                            Pair<Sequence, Sequence> varKey = createSeqPairVarKey(leaderHistory, actionHistory);

                            lpTable.setConstraintType(eqKeyLeader, 1);
                            lpTable.setConstraint(eqKeyLeader, varKey, -1);
                            for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                                lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderContinuation, actionHistory), 1);
                            }
                        }

                        for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            Object eqKeyLeaderCont = new Triplet<>(leaderHistory, followerSequence, leaderAction.getInformationSet());

                            if (!blackList.contains(eqKeyLeaderCont)) {
                                blackList.add(eqKeyLeaderCont);
                                Pair<Sequence, Sequence> varKeyCont = createSeqPairVarKey(leaderHistory, followerSequence);

                                lpTable.setConstraintType(eqKeyLeaderCont, 1);
                                lpTable.setConstraint(eqKeyLeaderCont, varKeyCont, -1);
                                for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                                    lpTable.setConstraint(eqKeyLeaderCont, createSeqPairVarKey(leaderContinuation, followerSequence), 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    protected void createPContinuationConstraint(List<Action> actions, Player opponent, GameState gameState, Set<Object> blackList, Set<Pair<Sequence, Sequence>> pStops) {
        Triplet<Sequence, Sequence, InformationSet> eqKey = new Triplet<Sequence, Sequence, InformationSet>(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), algConfig.getInformationSetFor(gameState));

        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);
        Pair<Sequence, Sequence> varKey = createSeqPairVarKey(gameState);

        pStops.add(varKey);
        lpTable.setConstraint(eqKey, varKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(gameState.getSequenceForPlayerToMove());

            sequenceCopy.addLast(action);
            Pair<Sequence, Sequence> contVarKey = createSeqPairVarKey(sequenceCopy, gameState.getSequenceFor(opponent));

            pStops.add(contVarKey);
            lpTable.setConstraint(eqKey, contVarKey, 1);
        }
    }

    protected Pair<Sequence, Sequence> createSeqPairVarKey(Sequence sequence1, Sequence sequence2) {
        Pair<Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Pair<>(sequence1, sequence2) : new Pair<>(sequence2, sequence1);

        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Pair<Sequence, Sequence> createSeqPairVarKeyCheckExistence(Sequence sequence1, Sequence sequence2) {
        Pair<Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Pair<>(sequence1, sequence2) : new Pair<>(sequence2, sequence1);

        assert lpTable.exists(varKey);
        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Pair<Sequence, Sequence> createSeqPairVarKey(GameState gameState) {
        return createSeqPairVarKey(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower));
    }

    protected void createInitPConstraint() {
        lpTable.setConstraint("initP", createSeqPairVarKey(new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower)), 1);
        lpTable.setConstant("initP", 1);
        lpTable.setConstraintType("initP", 1);
    }

    public int getLPInvocationCount() {
        return lpInvocationCount;
    }
}
