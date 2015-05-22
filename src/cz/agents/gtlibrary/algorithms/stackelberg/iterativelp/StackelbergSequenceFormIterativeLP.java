package cz.agents.gtlibrary.algorithms.stackelberg.iterativelp;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class StackelbergSequenceFormIterativeLP extends StackelbergSequenceFormLP {

    protected RecyclingLPTable lpTable;
    protected Player leader;
    protected Player follower;
    protected GameInfo info;
    protected ThreadMXBean threadBean;
    protected Pair<Map<Sequence, Double>, Double> dummyResult = new Pair<>(null, Double.NEGATIVE_INFINITY);

    public StackelbergSequenceFormIterativeLP(Player leader, GameInfo info) {
        super(info.getAllPlayers(), leader, info.getOpponent(leader));
        lpTable = new RecyclingLPTable();
        this.leader = leader;
        this.follower = info.getOpponent(leader);
        this.info = info;
        this.threadBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        addObjective(algConfig);
        createPContinuationConstraints(algConfig, expander);
        createSequenceConstraints(algConfig, expander);
        createISActionConstraints(algConfig);

        System.out.println("LP build...");
        lpTable.watchAllPrimalVariables();
        Pair<Map<Sequence, Double>, Double> result = solve(-info.getMaxUtility(), info.getMaxUtility());

        resultStrategies.put(leader, result.getLeft());
        resultValues.put(leader, result.getRight());
        return result.getRight();
    }

    private Pair<Map<Sequence, Double>, Double> solve(double lowerBound, double upperBound) {
        //TODO: use bounds
        try {
            LPData lpData = lpTable.toCplex();
            lpData.getSolver().exportModel("SSEIter.lp");
            lpData.getSolver().solve();
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

                System.out.println("LP value: " + value);

                for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
                    double variableValue = lpData.getSolver().getValue(entry.getValue());

                    System.out.println(entry.getKey() + ": " + variableValue);
                }
                System.out.println("-----------------------");
                Set<Sequence> brokenStrategyCauses = getShallowestBrokenStrategyCauses(lpData);

                if (brokenStrategyCauses == null) {
                    System.out.println("Found solution candidate with value: " + value);
                    System.out.println("Leader real. plan:");
                    for (Map.Entry<Sequence, Double> entry : behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader)).entrySet()) {
                        System.out.println(entry);
                    }
                    System.out.println("Follower real. plan:");
                    for (Map.Entry<Sequence, Double> entry : behavioralToRealizationPlan(getBehavioralStrategy(lpData, follower)).entrySet()) {
                        System.out.println(entry);
                    }
                    return new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), value);
                } else {
                    Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;

                    for (Sequence brokenStrategyCause : brokenStrategyCauses) {
                        restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
                        Pair<Map<Sequence, Double>, Double> result = solve(lowerBound, upperBound);

                        if (result.getRight() > currentBest.getRight())
                            currentBest = result;
                        removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
                    }
                    return currentBest;
                }
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return dummyResult;
    }


    private void restrictFollowerPlay(Sequence brokenStrategyCause, Set<Sequence> brokenStrategyCauses, LPData lpData) {
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

    private void removeRestriction(Sequence brokenStrategyCause, Set<Sequence> brokenStrategyCauses, LPData lpData) {
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

    private Set<Sequence> getShallowestBrokenStrategyCauses(LPData lpData) {
        return findShallowestBrokenStrategyCause(getBehavioralStrategy(lpData, follower));
    }

    private Map<InformationSet, Map<Sequence, Double>> getBehavioralStrategy(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
                        if (isStrategy == null) {
                            if (currentValue > 1e-8) {
                                isStrategy = new HashMap<>();
                                double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                isStrategy.put(playerSequence, behavioralStrat);
                                strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                            }
                        } else {
                            Double oldValue = isStrategy.get(playerSequence);
                            double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                            if (behavioralStrategy > 1e-8) {
                                assert oldValue == null || Math.abs(oldValue - behavioralStrategy) < 1e-8;
                                isStrategy.put(playerSequence, behavioralStrategy);
                            }
                        }
                }
            }
        }
        return strategy;
    }

    private double getBehavioralStrategy(LPData lpData, Pair varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            behavioralStrat /= getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, varKey.getRight()) : new Pair<>(varKey.getLeft(), sequenceCopy));
        }
        return behavioralStrat;
    }

    private boolean isSequenceFrom(Sequence sequence, InformationSet informationSet) {
        if (informationSet == null)
            return sequence.isEmpty();
        assert !sequence.getPlayer().equals(informationSet.getPlayer());
        for (GameState gameState : informationSet.getAllStates()) {
            if (gameState.getSequenceFor(sequence.getPlayer()).equals(sequence))
                return true;
        }
        return false;
    }

    private Map<Sequence, Double> behavioralToRealizationPlan(Map<InformationSet, Map<Sequence, Double>> behavioral) {
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

    private Set<Sequence> findShallowestBrokenStrategyCause(Map<InformationSet, Map<Sequence, Double>> strategy) {
        Set<Sequence> shallowestBrokenStrategyCause = null;

        for (Map<Sequence, Double> isStrategy : strategy.values()) {
            if (isStrategy.size() > 1) {
                if (shallowestBrokenStrategyCause == null) {
                    shallowestBrokenStrategyCause = isStrategy.keySet();
                } else {
                    Sequence candidate = isStrategy.keySet().iterator().next();
                    Sequence bestSoFar = shallowestBrokenStrategyCause.iterator().next();

                    if (candidate.size() < bestSoFar.size())
                        shallowestBrokenStrategyCause = new HashSet<>(isStrategy.keySet());
                    else if (candidate.size() == bestSoFar.size())
                        shallowestBrokenStrategyCause.addAll(isStrategy.keySet());
                }
            }
        }
        return shallowestBrokenStrategyCause;
    }

    private Double getValueFromCplex(LPData lpData, Map.Entry<Object, IloNumVar> entry) {
        Double currentValue = null;

        try {
            currentValue = lpData.getSolver().getValue(entry.getValue());
        } catch (IloException e) {
            e.printStackTrace();
        }
        return currentValue;
    }

    private Double getValueFromCplex(LPData lpData, Object varKey) {
        Double currentValue = null;

        try {
            currentValue = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
        } catch (IloException e) {
            e.printStackTrace();
        }
        return currentValue;
    }

    private void createISActionConstraints(StackelbergConfig algConfig) {
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

    private void createISActionConstraint(StackelbergConfig algConfig, Sequence followerSequence, SequenceInformationSet informationSet) {
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            Object eqKey = new Triplet<>(informationSet, sequence, followerSequence);
            Object varKey = new Pair<>(informationSet, followerSequence);

            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, varKey, 1);
            lpTable.setConstraintType(eqKey, 2);
            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(informationSet.getPlayersHistory())) {
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

    private void addObjective(StackelbergConfig algConfig) {
        for (GameState leaf : algConfig.getAllLeafs()) {
            lpTable.setObjective(createSeqPairVarKey(leaf), leaf.getUtilities()[leader.getId()] * leaf.getNatureProbability());
        }
    }


    private void createSequenceConstraints(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower));
        for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
            createSequenceConstraint(algConfig, followerSequence);
        }
    }

    private void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence) {
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


    private void createPContinuationConstraints(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        createInitPConstraint();
        Set<Object> blackList = new HashSet<>();

        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            List<Action> actions = expander.getActions(informationSet);
            Player opponent = info.getOpponent(informationSet.getPlayer());

            for (GameState gameState : informationSet.getAllStates()) {
                if (!gameState.isGameEnd())
                    createPContinuationConstraint(actions, opponent, gameState, blackList);
            }
        }

        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)) {
            for (Sequence compatibleFollowerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)) {
                for (Action action : compatibleFollowerSequence) {
                    Object eqKeyFollower = new Triplet<>(leaderSequence, action.getInformationSet().getPlayersHistory(), follower);

                    if (!blackList.contains(eqKeyFollower)) {
                        blackList.add(eqKeyFollower);
                        lpTable.setConstraintType(eqKeyFollower, 1);
                        lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, action.getInformationSet().getPlayersHistory()), -1);
                        for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, followerSequence), 1);
                        }
                    }

                    for (Action leaderAction : leaderSequence) {
                        Object eqKeyLeader = new Triplet<>(leaderAction.getInformationSet().getPlayersHistory(), action.getInformationSet().getPlayersHistory(), leader);

                        if (!blackList.contains(eqKeyLeader)) {
                            blackList.add(eqKeyLeader);
                            lpTable.setConstraintType(eqKeyLeader, 1);
                            lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderAction.getInformationSet().getPlayersHistory(), action.getInformationSet().getPlayersHistory()), -1);
                            for (Sequence leaderContinutation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                                lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderContinutation, action.getInformationSet().getPlayersHistory()), 1);
                            }
                        }
                    }
                }
            }
        }
    }


    private void createPContinuationConstraint(List<Action> actions, Player opponent, GameState gameState, Set<Object> blackList) {
        Triplet<Sequence, Sequence, Player> eqKey = new Triplet<>(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), gameState.getPlayerToMove());

        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);
        Object varKey = createSeqPairVarKey(gameState);

        lpTable.setConstraint(eqKey, varKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(gameState.getSequenceForPlayerToMove());

            sequenceCopy.addLast(action);
            Pair<Sequence, Sequence> contVarKey = createSeqPairVarKey(sequenceCopy, gameState.getSequenceFor(opponent));

            lpTable.setConstraint(eqKey, contVarKey, 1);
        }
    }

    private Pair<Sequence, Sequence> createSeqPairVarKey(Sequence sequence1, Sequence sequence2) {
        Pair<Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Pair<>(sequence1, sequence2) : new Pair<>(sequence2, sequence1);

        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    private Pair<Sequence, Sequence> createSeqPairVarKeyCheckExistence(Sequence sequence1, Sequence sequence2) {
        Pair<Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Pair<>(sequence1, sequence2) : new Pair<>(sequence2, sequence1);

        assert lpTable.exists(varKey);
        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    private Pair<Sequence, Sequence> createSeqPairVarKey(GameState gameState) {
        return createSeqPairVarKey(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower));
    }

    private void createInitPConstraint() {
        lpTable.setConstraint("initP", createSeqPairVarKey(new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower)), 1);
        lpTable.setConstant("initP", 1);
        lpTable.setConstraintType("initP", 1);
    }
}
