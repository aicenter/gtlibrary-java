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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StackelbergSequenceFormIterativeLP extends StackelbergSequenceFormLP {

    protected RecyclingLPTable lpTable;
    protected Player leader;
    protected Player follower;
    protected GameInfo info;
    protected ThreadMXBean threadBean;

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
        long startTime = threadBean.getCurrentThreadCpuTime();

        addObjective(algConfig);
        createPContinuationConstraints(algConfig, expander);
        createSequenceConstraints(algConfig, expander);
        createISActionConstraints(algConfig, expander);

        System.out.println("LP build...");
        lpTable.watchAllPrimalVariables();
        try {
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            lpData.getSolver().exportModel("SSEIter.lp");
            System.out.println("Solving...");
            long cplexTime = threadBean.getCurrentThreadCpuTime();
            lpData.getSolver().solve();

            System.out.println("cplex solving time: " + (threadBean.getCurrentThreadCpuTime() - cplexTime) / 1e6);
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
            System.out.println(lpData.getSolver().getStatus());
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

                System.out.println("Value: " + value);
                resultValues.put(leader, value);
                if (isFollowerRPPure(lpData))
                    System.out.println("Follower RP is pure...");
                else
                    System.out.println("Follower RP is not pure...");
                for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
                    double variableValue = lpData.getSolver().getValue(entry.getValue());

//                    if (variableValue > 0)
                    System.out.println(entry.getKey() + ": " + variableValue);
                }
                return value;
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return Double.NaN;
    }

    private boolean isFollowerRPPure(LPData lpData) {
        Map<Sequence, Double> leaderRealizationPlan = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Double oldValue = leaderRealizationPlan.get(varKey.getLeft());
                    Double currentValue = null;

                    try {
                        currentValue = lpData.getSolver().getValue(entry.getValue());
                    } catch (IloException e) {
                        e.printStackTrace();
                    }
                    if (oldValue == null) {
                        if (currentValue > 1e-8)
                            leaderRealizationPlan.put((Sequence) varKey.getLeft(), currentValue);
                    } else {
                        if (currentValue > 1e-8 && Math.abs(oldValue - currentValue) > 1e-8)
                            return false;
                    }
                }
            }
        }
        return true;
    }

    private void createISActionConstraints(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
            Sequence shortenedSequence = new ArrayListSequenceImpl(followerSequence);

            if (!shortenedSequence.isEmpty())
                shortenedSequence.removeLast();
            for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
                if (informationSet.getPlayer().equals(follower)) {
                    if (shortenedSequence.isPrefixOf(informationSet.getPlayersHistory()))
                        createISActionConstraint(algConfig, expander, followerSequence, informationSet);
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

    private void createISActionConstraint(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander, Sequence followerSequence, SequenceInformationSet informationSet) {
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
                        lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSequence), -utility);
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
        for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
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
//                if (!reachableSet.getOutgoingSequences().isEmpty())
//                    for (Action action : expander.getActions(reachableSet)) {
//                        Sequence sequenceCopy = new ArrayListSequenceImpl(followerSequence);
//
//                        sequenceCopy.addLast(action);
//                        lpTable.setConstraint(followerSequence, new Pair<>("v", sequenceCopy), -1);
//                    }
            }
        }

    }


    private void createPContinuationConstraints(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        createInitPConstraint();
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            List<Action> actions = expander.getActions(informationSet);
            Player opponent = info.getOpponent(informationSet.getPlayer());

            for (GameState gameState : informationSet.getAllStates()) {
                if (!gameState.isGameEnd())
                    createPContinuationConstraint(actions, opponent, gameState);
            }
        }
    }

    private void createPContinuationConstraint(List<Action> actions, Player opponent, GameState gameState) {
        Pair<Sequence, Sequence> eqKey = createSeqPairVarKey(gameState);

        lpTable.setConstraint(eqKey, eqKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(gameState.getSequenceForPlayerToMove());

            sequenceCopy.addLast(action);
            Pair<Sequence, Sequence> varKey = createSeqPairVarKey(sequenceCopy, gameState.getSequenceFor(opponent));

            lpTable.setConstraint(eqKey, varKey, 1);
        }
    }

    private Pair<Sequence, Sequence> createSeqPairVarKey(Sequence sequence1, Sequence sequence2) {
        Pair<Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Pair<>(sequence1, sequence2) : new Pair<>(sequence2, sequence1);

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
