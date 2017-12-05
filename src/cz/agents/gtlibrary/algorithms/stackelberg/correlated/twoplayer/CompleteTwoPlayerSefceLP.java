package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.RecyclingLPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.RecyclingMILPTable;
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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Created by Jakub Cerny on 01/09/2017.
 */
public class CompleteTwoPlayerSefceLP extends StackelbergSequenceFormLP implements Solver{

//    public static boolean USE_BR_CUT = false;

    protected double eps;
    protected LPTable lpTable;
    protected Player leader;
    protected Player follower;
    protected GameInfo info;
    protected ThreadMXBean threadBean;
    protected Pair<Map<Sequence, Double>, Double> dummyResult = new Pair<>(null, Double.NEGATIVE_INFINITY);
    protected StackelbergConfig algConfig;
    protected Expander<SequenceInformationSet> expander;
//    protected FollowerBestResponse followerBestResponse;

    protected double gameValue;
    protected int finalLpSize;

    // NOT BEHAVIORAL STRATEGY: if non-null .. can be used as a threat
    protected final boolean GET_STRATEGY = true;
    protected final boolean EXPORT_LP = false;

    public CompleteTwoPlayerSefceLP(Player leader, GameInfo info) {
        super(new Player[]{info.getAllPlayers()[0], info.getAllPlayers()[1]}, leader, info.getOpponent(leader));
        lpTable = new LPTable(); //RecyclingMILPTable();
        this.leader = leader;
        this.follower = info.getOpponent(leader);
        this.info = info;
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.eps = 1e-8;
        this.finalLpSize = 0;
    }

    public LPTable getLpTable(){
        return  lpTable;
    }

    @Override
    public String getInfo(){
        return "Complete two-player SEFCE LP.";
    }

    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander){
        return calculateLeaderStrategies((AlgorithmConfig) algConfig, expander);
    }

    public int getFinalLpSize(){
        return finalLpSize;
    }

    public double getRestrictedGameRatio(){
        return 1.0;
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfig = (StackelbergConfig) algConfig;
        this.expander = (Expander<SequenceInformationSet>)expander;
//        followerBestResponse = new FollowerBestResponse(this.algConfig.getRootState(), expander, this.algConfig, leader, follower);
        long startTime = threadBean.getCurrentThreadCpuTime();

        addObjective();
        createPContinuationConstraints();
        createSequenceConstraints();
        createISActionConstraints();

        System.out.println("LP build...");
        if (GET_STRATEGY) lpTable.watchAllPrimalVariables();
        overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
        Pair<Map<Sequence, Double>, Double> result = solve();

        resultStrategies.put(leader, result.getLeft());
        resultValues.put(leader, result.getRight());
        return gameValue;
    }

    protected Pair<Map<Sequence, Double>, Double> solve() {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

//            if (GET_STRATEGY) lpTable.watchAllPrimalVariables();
            LPData lpData = lpTable.toCplex();
            finalLpSize = lpData.getSolver().getNrows();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            if (EXPORT_LP) lpData.getSolver().exportModel("Complete2pSEFCE.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            lpData.getSolver().solve();
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            printBinaryVariableValues(lpData);
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                gameValue = lpData.getSolver().getObjValue();

                System.out.println("-----------------------");
                System.out.println("LP reward: " + gameValue );
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
//                Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getBehavioralStrategy(lpData, follower);
////                Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(followerBehavStrat, lpData);
//                Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));


                // compute RPs
                Map<Sequence, Double> leaderRealPlan = null;
                if (GET_STRATEGY) leaderRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, leader));
//                Map<Sequence, Double> followerRealPlan = behavioralToRealizationPlan(getBehavioralStrategy(lpData, follower));

                if (leaderRealPlan != null)
                    return new Pair<Map<Sequence, Double>, Double>(leaderRealPlan, gameValue);
                return new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), gameValue);
            } else {
                System.err.println(lpData.getSolver().getStatus());
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return dummyResult;
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


    protected Map<InformationSet, Map<Sequence, Double>> getBehavioralStrategy(LPData lpData, Player player) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

//                    if (currentValue > eps)
//                        System.out.println(varKey + " : " + currentValue);

//                    eps = -0.1;
                    if (currentValue > eps)
                        if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet()))
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

//                                    System.out.println(playerSequence + " : " + currentValue + " / " + behavioralStrat);
                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

//                                System.out.println(playerSequence + " : " + currentValue + " / " + behavioralStrategy);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, behavioralStrategy);
                                }
                            }
                }
            }
        }

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Pair) {
                Pair varKey = (Pair) entry.getKey();

                if (varKey.getLeft() instanceof Sequence && varKey.getRight() instanceof Sequence) {
                    Sequence playerSequence = player.equals(leader) ? (Sequence) varKey.getLeft() : (Sequence) varKey.getRight();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);
                    if (currentValue > 0){
                    if (isStrategy == null) {
                        isStrategy = new HashMap<>();
                        strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                    }
                    if (!isStrategy.containsKey(playerSequence)) isStrategy.put(playerSequence, currentValue);
                }}}}
        return strategy;
    }


    protected double getBehavioralStrategy(LPData lpData, Pair varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair(sequenceCopy, varKey.getRight()) : new Pair<>(varKey.getLeft(), sequenceCopy));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (playerSequence.getPlayer().equals(leader) ? varKey.getRight() : varKey.getLeft()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair(sequenceCopy, opponentSequence) : new Pair<>(opponentSequence, sequenceCopy));
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
                        if (behavioral.containsKey(prefix.getLastInformationSet()) && behavioral.get(prefix.getLastInformationSet()).containsKey(prefix))
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
            System.out.println(((Pair)entry.getKey()).getLeft());
            e.printStackTrace();
        }
        return currentValue;
    }

    protected Double getValueFromCplex(LPData lpData, Object varKey) {
        Double currentValue = null;

        try {
//            System.out.println(varKey + " #:" + varKey.hashCode());
//            System.out.println(lpData.getWatchedPrimalVariables().containsKey(varKey));
            currentValue = lpData.getSolver().getValue(lpData.getWatchedPrimalVariables().get(varKey));
        } catch (IloException e) {
            System.out.println(varKey);
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


//            System.out.println(eqKey);

            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, varKey, 1);
            lpTable.setConstraintType(eqKey, 2);
            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(sequence)) {
                Double[] seqCombUtilities = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, sequence);

                if (seqCombUtilities != null) {
                    double utility = seqCombUtilities[follower.getId()];


                    if (utility != 0) {
//                        System.out.println(leaderSequence + " : " + followerSequence);
                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, followerSequence), -utility);
                    }
                }
            }
            if (algConfig.getReachableSets(sequence) != null)
                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(sequence)) {
                    if (reachableSet.getOutgoingSequences() != null && !reachableSet.getOutgoingSequences().isEmpty())
                        lpTable.setConstraint(eqKey, new Pair<>(reachableSet, followerSequence), -1);
                }
        }
    }

    protected void addObjective() {
//        for (Map.Entry<GameState, Double[]> entry : algConfig.getActualNonZeroUtilityValuesInLeafsGenSum().entrySet()) {
//            lpTable.setObjective(createSeqPairVarKey(entry.getKey()), entry.getValue()[leader.getId()]);
//        }
        for( Map.Entry<Map<Player, Sequence>, Double[]> entry : algConfig.getUtilityForSequenceCombinationGenSum().entrySet()){
            lpTable.setObjective(createSeqPairVarKey(entry.getKey().get(leader), entry.getKey().get(follower)), entry.getValue()[leader.getId()]);
        }
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
//        if (true) return;
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

//        System.out.println(algConfig.getSequencesFor(leader));

        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)) {
//            System.out.println(leaderSequence + " #: " + leaderSequence.hashCode());
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

//        lpTable.watchPrimalVariable(varKey, varKey);
//        lpTable.setLowerBound(varKey, 0);
//        lpTable.setUpperBound(varKey, 1);
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

    public Double getResultForPlayer(Player player){
        return gameValue;
    }


}

