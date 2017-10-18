package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.RecyclingMILPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.br.FollowerBestResponse;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.NodePointsFlipItGameState;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.NoMissingSeqStrategy;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Quadruple;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class SumForbiddingBayesianStackelbergLP extends StackelbergSequenceFormLP {

    public static boolean USE_BR_CUT = false;
    private static final boolean CHECK_HASH_COLLISIONS = false;

    protected double eps;
    protected LPTable lpTable;
    protected Player leader;
    protected Player follower;
    protected FlipItGameInfo info;
    protected ThreadMXBean threadBean;
    protected Pair<Map<Sequence, Double>, Double> dummyResult = new Pair<>(null, Double.NEGATIVE_INFINITY);
    protected StackelbergConfig algConfig;
    protected Expander<SequenceInformationSet> expander;
    protected FollowerBestResponse followerBestResponse;

    protected boolean OUTPUT_STRATEGY = false;


    protected int lpInvocationCount;
    protected boolean solved = false;

    protected int restrictingTypeIndex = 0;

    public SumForbiddingBayesianStackelbergLP(FlipItGameInfo info, Expander expander) {
        super(new Player[]{info.getAllPlayers()[0], info.getAllPlayers()[1]}, FlipItGameInfo.DEFENDER, FlipItGameInfo.ATTACKER);
        lpTable = new LPTable();//RecyclingMILPTable();
        this.leader = FlipItGameInfo.DEFENDER;
        this.follower = FlipItGameInfo.ATTACKER;
        this.info = info;
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.lpInvocationCount = 0;
        this.eps = 1e-7;
        this.restrictingTypeIndex = 0;

    }

    public void setOUTPUT_STRATEGY(boolean output_strategy){
        this.OUTPUT_STRATEGY = output_strategy;
    }

    private boolean checkHashCollisions(Collection<?> objects){
        HashMap<Integer, Object> hashes = new HashMap<>();
        boolean hashColision = false;
        for (Object sequence : objects) {
            if (hashes.keySet().contains(sequence.hashCode())) {
                System.out.println("HASH COLLISION : " + sequence + " ; " + hashes.get(sequence.hashCode()) + " : " + sequence.hashCode());
                if (sequence instanceof NodePointsFlipItGameState){
                    System.out.println(((NodePointsFlipItGameState)sequence).getISKeyForPlayerToMove());
                    System.out.println(((NodePointsFlipItGameState)hashes.get(sequence.hashCode())).getISKeyForPlayerToMove());
                    System.out.println(hashes.get(sequence.hashCode()).hashCode());
                    System.out.println(sequence.hashCode());
                }
                hashColision = true;
            }
//            for (Object object : hashes.values())
//                if (object.equals(sequence))
//                    System.out.println("EQUALS collision");
            hashes.put(sequence.hashCode(),sequence);
        }
        if (!hashColision) System.out.println("NO HASH COLLISION : " + objects.iterator().next().getClass().getSimpleName());
        return hashColision;
    }

    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        this.algConfig = algConfig;
        this.expander = expander;
//        followerBestResponse = new FollowerBestResponse(algConfig.getRootState(), expander, algConfig, leader, follower);
        long startTime = threadBean.getCurrentThreadCpuTime();

        if (CHECK_HASH_COLLISIONS) {
            checkHashCollisions(algConfig.getAllSequences());
            checkHashCollisions(algConfig.getAllInformationSets().values());
            checkHashCollisions(Arrays.asList(FlipItGameInfo.types));
            ArrayList<GameState> gameStates = new ArrayList<>();
            for (InformationSet set : algConfig.getAllInformationSets().values()) {
                gameStates.addAll(set.getAllStates());
//            boolean hashColision = checkHashCollisions(set.getAllStates());
//            if (hashColision) {
//                System.out.println("----");
//                System.out.println(set.getPlayer());
//                for (GameState state : set.getAllStates()) {
////                    System.out.println(state);
//                }
//            }
            }
            checkHashCollisions(gameStates);
        }

        addObjective();                     // READY
//        addObjectiveViaConstraint(algConfig);
        createPContinuationConstraints();   // READY
        createSequenceConstraints();        // READY
        createISActionConstraints();        // READY : v >= ... AND v(...) = v(...)
//        addIndifferentLeaderRestrictionsInRoot();
//        addIndifferentLeaderRestrictions();

        addIndifferentLeaderRestrictionsDueToChance();
//        addIndifferentLeaderRestrictionsThroughRelevant();

//        forceOptimumFollowerStrategy();

        System.out.println("LP build...");
        lpTable.watchAllPrimalVariables();
        overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
        Pair<Map<Sequence, Double>, Double> result = solve(-info.getMaxUtility(), info.getMaxUtility());

        resultStrategies.put(leader, result.getLeft());
        resultValues.put(leader, result.getRight());
        return result.getRight();
    }

        protected void addIndifferentLeaderRestrictionsInRoot(){
            for(Sequence sequence : algConfig.getAllInformationSets().get(algConfig.getRootState().getISKeyForPlayerToMove()).getOutgoingSequences()){
                for (int i = 1 ; i < FlipItGameInfo.numTypes; i++){
                    Triplet eqKey = new Triplet("leaderRpRestr",sequence,FlipItGameInfo.types[i]);
                    lpTable.setConstraintType(eqKey, 1);
                    lpTable.setConstraint(eqKey, new Triplet<>(sequence,new ArrayListSequenceImpl(FlipItGameInfo.ATTACKER),FlipItGameInfo.types[0]), 1);
                    lpTable.setConstraint(eqKey, new Triplet<>(sequence,new ArrayListSequenceImpl(FlipItGameInfo.ATTACKER),FlipItGameInfo.types[i]), -1);
                    lpTable.setConstant(eqKey, 0.0);
                }
            }
        }

    protected void forbidNonOptimumFollowerStrategy(){
        try {
            LPData lpData = lpTable.toCplex();
            for (Sequence followerSequence : algConfig.getAllSequences()) {
                if (followerSequence.getPlayer().equals(FlipItGameInfo.ATTACKER)) {
//                    System.out.println(followerSequence.toString());
                    if (!FlipItGameInfo.type1optimumSet.contains(followerSequence.toString())) {
//                        System.out.println("Sequence for restricting found: " + followerSequence);
                        restrictFollowerPlay(followerSequence, lpData, FlipItGameInfo.types[0]);
                    }
                    if (!FlipItGameInfo.type2optimumSet.contains(followerSequence.toString())) {
//                        System.out.println("Sequence for restricting found: " + followerSequence);
                        restrictFollowerPlay(followerSequence, lpData, FlipItGameInfo.types[1]);
                    }
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    protected void forceOptimumFollowerStrategy(){
        try {
            LPData lpData = lpTable.toCplex();
            for (Sequence followerSequence : algConfig.getAllSequences()) {
                if (followerSequence.getPlayer().equals(FlipItGameInfo.ATTACKER)) {
//                    System.out.println(followerSequence.toString());
                    if (FlipItGameInfo.type1optimumSet.contains(followerSequence.toString())) {
//                        System.out.println("Sequence for restricting found: " + followerSequence);
                        enforceFollowerPlay(followerSequence, lpData, FlipItGameInfo.types[0]);
                    }
                    if (FlipItGameInfo.type2optimumSet.contains(followerSequence.toString())) {
//                        System.out.println("Sequence for restricting found: " + followerSequence);
                        enforceFollowerPlay(followerSequence, lpData, FlipItGameInfo.types[1]);
                    }
                }
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
    }

    protected void enforceFollowerPlay(Sequence brokenStrategyCause, LPData lpData, FollowerType type) {
        System.out.println(brokenStrategyCause +" of type " + type.getID() + " enforced");
        if (brokenStrategyCause.isEmpty()) return;
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Triplet) {
                if (((Triplet) varKey).getFirst() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getThird() instanceof FollowerType && ((Triplet) varKey).getThird().equals(type)) {
                    Triplet<Sequence, Sequence,FollowerType> p = (Triplet<Sequence, Sequence, FollowerType>) varKey;

                    if (p.getSecond().equals(brokenStrategyCause)) {
                        Pair<String, Triplet<Sequence, Sequence,FollowerType>> eqKey = new Pair<>("BR restr", p);
                        Triplet<Sequence, Sequence, FollowerType> prefixKey = new Triplet<>(p.getFirst(), brokenStrategyCause.getSubSequence(brokenStrategyCause.size() - 1), type);

                        assert lpData.getWatchedPrimalVariables().containsKey(prefixKey);
                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraint(eqKey, prefixKey, -1);
                        lpTable.setConstraintType(eqKey, 1);
                    }
                }
            }
        }
    }

    protected boolean isPrefixOfRelevant(Collection<Triplet> ps, Sequence followerSequence){
        for (Triplet key : ps)
            if (followerSequence.isPrefixOf((Sequence)key.getSecond()))
                return true;
        return false;
    }

    protected void addIndifferentLeaderRestrictionsThroughRelevant(){
        for (Sequence seq : algConfig.getSequencesFor(leader)){
            if (seq.isEmpty()) continue;
            Object seqVarKey = seq;
            lpTable.setLowerBound(seqVarKey, 0.0);
            lpTable.setUpperBound(seqVarKey, 1.0);
            for (FollowerType type : FlipItGameInfo.types){
                Object eqKey = new Pair(type, seq);
                lpTable.setConstraint(eqKey, seqVarKey, 1.0);
                lpTable.setConstraintType(eqKey, 1);
            }
            for (SequenceInformationSet set : algConfig.getAllInformationSets().values())
                if (seq.getPlayer().equals(leader) && set.getPlayersHistory().equals(seq)) {
                    for (GameState gameState : set.getAllStates()) {
                        for (Sequence followerSequence : getAllRelevantSequencesFor(gameState)) {
                            for (FollowerType type : FlipItGameInfo.types) {
                                Object eqKey = new Pair(type, seq);
                                lpTable.setConstraint(eqKey, new Triplet<>(seq, followerSequence, type), -1.0);
                            }
                        }
                    }
                }
        }
    }

    protected void addIndifferentLeaderRestrictionsDueToChance(){
        for (SequenceInformationSet leaderSet : algConfig.getAllInformationSets().values()) {
            if (leaderSet.getPlayer().equals(FlipItGameInfo.DEFENDER)) {
                for (Sequence leaderSequence : leaderSet.getOutgoingSequences()) {
                    Triplet setActionVarKey = new Triplet("sIA", leaderSet, leaderSequence);
                    for (FollowerType type : FlipItGameInfo.types){
                        Pair eqKey = new Pair(setActionVarKey, type);
                        HashMap<Sequence,Collection<Triplet>> ps = new HashMap<>();
                        boolean nonNullRelevant = false;

                        for (GameState gameState : leaderSet.getAllStates()){
                            Sequence natureSequence = gameState.getSequenceFor(FlipItGameInfo.NATURE);
                            if (!ps.containsKey(natureSequence))
                                ps.put(natureSequence, new ArrayList<>());
                            for (Sequence followerSequence : getRelevantSequencesFor(gameState)){
                                nonNullRelevant = true;
                                if (!isPrefixOfRelevant(ps.get(natureSequence), followerSequence)) {
                                    ps.get(natureSequence).add(new Triplet<Sequence, Sequence, FollowerType>(leaderSequence, followerSequence, type));
                                }
                            }
                        }


                        if (nonNullRelevant){
                            for (Collection<Triplet> relevantPs : ps.values()){
                                for (Triplet key : relevantPs) {
                                    lpTable.setConstraint(eqKey, key, -1.0);
                                }
                                lpTable.setConstraint(eqKey, setActionVarKey, 1.0);
                                lpTable.setLowerBound(setActionVarKey, 0.0);
                                lpTable.setUpperBound(setActionVarKey, 1.0);
                                lpTable.setConstraintType(eqKey, 1);
                            }
                        }
                    }
                }
            }
        }
    }

    private HashSet<Sequence> getRelevantSequencesFor(GameState gameState) {
        HashSet<Sequence> relevantSequences = new HashSet<Sequence>();
//        relevantSequences.add(new ArrayListSequenceImpl(FlipItGameInfo.ATTACKER));
        Sequence followerSequence = new ArrayListSequenceImpl(gameState.getSequenceFor(FlipItGameInfo.ATTACKER));
        boolean isLastIS = true;
        while(followerSequence.size() > 0){
            SequenceInformationSet lastIS = (SequenceInformationSet) followerSequence.getLastInformationSet();
            for (Sequence outgoing : lastIS.getOutgoingSequences()){
                if (isLastIS || !outgoing.isPrefixOf(gameState.getSequenceFor(FlipItGameInfo.ATTACKER))) {
                    relevantSequences.add(outgoing);
                }
            }
            isLastIS = false;
            followerSequence.removeLast();
        }
//        relevantSequences.add(followerSequence);
        return relevantSequences;
    }

    private HashSet<Sequence> getAllRelevantSequencesFor(GameState gameState) {
        HashSet<Sequence> relevantSequences = new HashSet<Sequence>();
//        relevantSequences.add(new ArrayListSequenceImpl(FlipItGameInfo.ATTACKER));
        Sequence followerSequence = new ArrayListSequenceImpl(gameState.getSequenceFor(FlipItGameInfo.ATTACKER));
        boolean isLastIS = true;
        while(followerSequence.size() > 0){
            SequenceInformationSet lastIS = (SequenceInformationSet) followerSequence.getLastInformationSet();
            for (Sequence outgoing : lastIS.getOutgoingSequences()){
//                if (isLastIS || !outgoing.isPrefixOf(gameState.getSequenceFor(FlipItGameInfo.ATTACKER))) {
                    relevantSequences.add(outgoing);
//                }
            }
            isLastIS = false;
            followerSequence.removeLast();
        }
        relevantSequences.add(followerSequence);
        return relevantSequences;
    }

    private HashSet<Sequence> getRelevantSequencesWithoutChanceFor(GameState gameState) {
        HashSet<Sequence> relevantSequences = new HashSet<Sequence>();
        Sequence followerSequence = new ArrayListSequenceImpl(gameState.getSequenceFor(FlipItGameInfo.ATTACKER));
        while(followerSequence.size() > 0){
            SequenceInformationSet lastIS = (SequenceInformationSet) followerSequence.getLastInformationSet();
            for (Sequence outgoing : lastIS.getOutgoingSequences()){
                    relevantSequences.add(outgoing);
            }
            followerSequence.removeLast();
        }
        return relevantSequences;
    }

    protected void addIndifferentLeaderRestrictions(){
        boolean setForRelevant;
        for (SequenceInformationSet leaderSet : algConfig.getAllInformationSets().values())
            if (leaderSet.getPlayer().equals(FlipItGameInfo.DEFENDER)) {
                setForRelevant = false;
                for (Sequence followerSequence : getRelevantSequencesFor(leaderSet)){{{
                            for (Sequence leaderSequence : leaderSet.getOutgoingSequences()) {
//                                System.out.println("setting restriction for : "+leaderSequence + " " + followerSequence);
                                Pair varKey = new Pair("pp", leaderSequence);
//                            lpTable.setLowerBound(varKey,Double.NEGATIVE_INFINITY);
                                lpTable.setUpperBound(varKey, 1.0);
//                            lpTable.setUpperBound(varKey,Double.MAX_VALUE);
                                for (FollowerType type : FlipItGameInfo.types) {
                                    Quadruple binaryVarKey = new Quadruple("binary", leaderSequence, followerSequence, type);
                                    Triplet<Sequence, Sequence, FollowerType> relevantSequencesVarKey = new Triplet(leaderSequence, followerSequence, type);

                                    if (!setForRelevant) {
                                        // 4 inequalities
                                        // 1 : p - b - \lambda(l,s,f) \leq 0
                                        Quadruple eqKey1 = new Quadruple("#1", leaderSequence, followerSequence, type);
                                        lpTable.setConstraint(eqKey1, varKey, 1.0);
                                        lpTable.setConstraint(eqKey1, binaryVarKey, -1.0);
                                        lpTable.setConstraint(eqKey1, relevantSequencesVarKey, -1.0);
                                        lpTable.setConstraintType(eqKey1, 0);

                                        Quadruple eqKey2 = new Quadruple("#2", leaderSequence, followerSequence, type);
                                        lpTable.setConstraint(eqKey2, varKey, -1.0);
                                        lpTable.setConstraint(eqKey2, binaryVarKey, -1.0);
                                        lpTable.setConstraint(eqKey2, relevantSequencesVarKey, 1.0);
                                        lpTable.setConstraintType(eqKey2, 0);

                                        Quadruple eqKey3 = new Quadruple("#3", leaderSequence, followerSequence, type);
                                        lpTable.setConstraint(eqKey3, binaryVarKey, 1.0);
                                        lpTable.setConstraint(eqKey3, relevantSequencesVarKey, -1.0);
                                        lpTable.setConstant(eqKey3, 1.0);
                                        lpTable.setConstraintType(eqKey3, 0);

                                        Quadruple eqKey4 = new Quadruple("#4", leaderSequence, followerSequence, type);
                                        lpTable.setConstraint(eqKey4, binaryVarKey, 1.0);
                                        lpTable.setConstraint(eqKey4, relevantSequencesVarKey, 1.0);
                                        lpTable.setConstant(eqKey4, 1.0);
                                        lpTable.setConstraintType(eqKey4, 0);
                                    }

                                    Quadruple eqKey5 = new Quadruple("#5", leaderSequence, followerSequence, type);
                                    lpTable.setConstraint(eqKey5, binaryVarKey, 1.0);
                                    lpTable.setConstant(eqKey5, 1.0);
                                    lpTable.setConstraintType(eqKey5, 0);
                                    for (Sequence otherLeaderSequence : leaderSet.getOutgoingSequences()) {
                                        lpTable.setConstraint(eqKey5, new Triplet<>(otherLeaderSequence, followerSequence, type), 1.0);
//                                        lpTable.setConstraint(eqKey5, new Pair<>("pp",otherLeaderSequence), 1.0);
                                    }
//                                    lpTable.markAsBinary(binaryVarKey);


                                }

                            }
//                            setForRelevant = true;
                        }
                    }
                }
            }
    }

    private HashSet<Sequence> getRelevantSequencesFor(SequenceInformationSet leaderSet) {
        HashSet<Sequence> relevantSequences = new HashSet<>();
        relevantSequences.add(new ArrayListSequenceImpl(follower));
        for (GameState state  : leaderSet.getAllStates())
            relevantSequences.addAll(getRelevantSequencesWithoutChanceFor(state));
        return relevantSequences;
    }

    protected Pair<Map<Sequence, Double>, Double> solve(double lowerBound, double upperBound) {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();

//            updateLowerBound(lowerBound);
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            lpData.getSolver().exportModel("BSSE_LP.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            IloCplex cplex = lpData.getSolver();
            cplex.solve();
            lpInvocationCount++;
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            printBinaryVariableValues(lpData);
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
//                boolean noBrokenStrategies = true;
//                Map<FollowerType,Map<InformationSet, Map<Sequence, Double>>> followerBehavStrats = new HashMap<FollowerType,Map<InformationSet, Map<Sequence, Double>>>();
//                Map<FollowerType,Iterable<Sequence>> followerBrokenStrategyCauses = new HashMap<FollowerType,Iterable<Sequence>>();
//                for (FollowerType type : FlipItGameInfo.types) {
//                    Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getFollowerBehavioralStrategy(lpData, type);
//                    Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(followerBehavStrat, lpData, type);
////                    followerBehavStrats.put(type,followerBehavStrat);
//                    followerBrokenStrategyCauses.put(type,brokenStrategyCauses);
//                    if (brokenStrategyCauses != null) noBrokenStrategies = false;
//                }


//                Map<FollowerType,Iterable<Sequence>> followerBrokenStrategyCauses = new HashMap<FollowerType,Iterable<Sequence>>();
                Map<Pair<FollowerType,Sequence>,Double> causes = new HashMap<>();
                Map<FollowerType,Map<InformationSet, Map<Sequence, Double>>> leaderStrategies = new HashMap<>();
                Map<FollowerType,Map<InformationSet, Map<Sequence, Double>>> followersStrategies = new HashMap<>();
//                int causeDepth = Integer.MAX_VALUE;
                for (FollowerType type : FlipItGameInfo.types) {
                    Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getFollowerBehavioralStrategy(lpData, type);

                    if (OUTPUT_STRATEGY) {
                        System.out.println(type);
                        for (InformationSet set : followerBehavStrat.keySet())
                            for (Sequence seq : followerBehavStrat.get(set).keySet())
                                if (followerBehavStrat.get(set).get(seq) > eps)
                                    System.out.println(seq + " : " + followerBehavStrat.get(set).get(seq));
                    }



                    followersStrategies.put(type,followerBehavStrat);
                    Map<Sequence, Double> cause = getBrokenStrategyCausesWithTypes(followerBehavStrat, lpData, type);


//                    if (cause != null && !causes.isEmpty() && cause.keySet().iterator().next().size() < causes.keySet().iterator().next().getRight().size())
//                        causes.clear();
//                    if (cause != null && causes.isEmpty()){
//                        for (Sequence sequence : cause.keySet()) {
//                            causes.put(new Pair(type, sequence), cause.get(sequence));
//                        }
//                    }

                    if (cause != null){
                        for (Sequence sequence : cause.keySet()) {
                            causes.put(new Pair(type, sequence), cause.get(sequence));
                        }
                    }



                    if (OUTPUT_STRATEGY) {
                        leaderStrategies.put(type, getLeaderBehavioralStrategy(lpData, type));
//                    System.out.println("/////// START ////////");
                        for (InformationSet set : leaderStrategies.get(type).keySet())
                            for (Sequence sequence : leaderStrategies.get(type).get(set).keySet())
                                System.out.println(sequence + " : " + leaderStrategies.get(type).get(set).get(sequence));
//                    System.out.println("//////// END ///////");
                    }

//                    followerBrokenStrategyCauses.put(type,brokenStrategyCauses);
//                    if (brokenStrategyCauses != null) noBrokenStrategies = false;
                }
//                checkLeaderStrategiesConsistent(leaderStrategies);
                Iterable<Pair<FollowerType,Sequence>> brokenStrategyCauses = sortWithTypes(causes,causes.keySet());


//                System.out.println("/////// START ////////");
//                for (FollowerType type : FlipItGameInfo.types) {
//                    System.out.println("Type = " + type.getID());
//                    for (InformationSet set : leaderStrategies.get(type).keySet())
//                        for (Sequence sequence : leaderStrategies.get(type).get(set).keySet())
//                            System.out.println(sequence + " : " + leaderStrategies.get(type).get(set).get(sequence));
//                }
//                for (FollowerType type : FlipItGameInfo.types) {
//                    System.out.println("Type = " + type.getID());
//                    for (InformationSet set : followersStrategies.get(type).keySet())
//                        for (Sequence sequence : followersStrategies.get(type).get(set).keySet())
//                            System.out.println(sequence + " : " + followersStrategies.get(type).get(set).get(sequence));
//                }
//                System.out.println("//////// END ///////");





//                Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));

//                GenSumUtilityCalculator calculator = new GenSumUtilityCalculator(algConfig.getRootState(), expander);
//
//                System.out.println(Arrays.toString(calculator.computeUtility(getP1Strategy(leaderRealPlan, followerRealPlan), getP2Strategy(leaderRealPlan, followerRealPlan))));
//                System.out.println("follower behav. strat.");
//                for (Map.Entry<InformationSet, Map<Sequence, Double>> entry : followerBehavStrat.entrySet()) {
//                    System.out.println(entry);
//                }
                if (causes.isEmpty()) {

                    System.out.println("Found solution candidate with reward: " + value);

//                    System.out.println("/////// START ////////");
//                    for (FollowerType type : FlipItGameInfo.types) {
//                        System.out.println("Type = " + type.getID());
//                        for (InformationSet set : leaderStrategies.get(type).keySet())
//                            for (Sequence sequence : leaderStrategies.get(type).get(set).keySet())
//                                System.out.println(sequence + " : " + leaderStrategies.get(type).get(set).get(sequence));
//                    }
//                    for (FollowerType type : FlipItGameInfo.types) {
//                        System.out.println("Type = " + type.getID());
//                        for (InformationSet set : followersStrategies.get(type).keySet())
//                            for (Sequence sequence : followersStrategies.get(type).get(set).keySet())
//                                System.out.println(sequence + " : " + followersStrategies.get(type).get(set).get(sequence));
//                    }
//                    System.out.println("//////// END ///////");
//
//                    for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
//                        if (entry.getKey() instanceof Triplet) {
//                            Triplet varKey = (Triplet) entry.getKey();
//
//                            if (varKey.getFirst() instanceof Sequence && varKey.getSecond() instanceof Sequence && varKey.getThird() instanceof FollowerType && varKey.getThird().equals(FlipItGameInfo.types[0])) {
////                    System.out.println("Extracting strategy");
//                                Double currentValue = getValueFromCplex(lpData, entry);
//                                if (currentValue > eps  && problematicConstr.contains(varKey)) System.out.println(varKey.getFirst() + ", " + varKey.getSecond() + " : " + currentValue);
//                            }
//                        }
//                    }


//                    printCorrelationPlan(lpData);


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
                    return new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), value);
                } else {
//                    if (USE_BR_CUT) {
//                        Pair<Map<Sequence, Double>, Double> result = followerBestResponse.computeBestResponseTo(leaderRealPlan);
//
//                        System.out.println("BR: " + result.getRight());
//                        if (lowerBound < result.getRight()) {
//                            System.out.println("lower bound increased from " + lowerBound + " to " + result.getRight());
//                            lowerBound = result.getRight();
//                        }
//
//                        if (Math.abs(lowerBound - reward) < eps) {
//                            System.out.println("solution found BR");
//                            return new Pair<Map<Sequence, Double>, Double>(new HashMap<Sequence, Double>(), reward);
//                        }
//                    }
                    if (value <= lowerBound + eps) {
                        System.out.println("***********lower bound " + lowerBound + " not exceeded, cutting***********");
                        return dummyResult;
                    }
                    return handleBrokenStrategyCause(lowerBound, upperBound, lpData, value, brokenStrategyCauses);
                }
            } else {
                System.err.println(lpData.getSolver().getStatus());
                return dummyResult;
            }
        } catch (IloException e) {
            e.printStackTrace();
        }
        return dummyResult;
    }

    private void printCorrelationPlan(LPData lpData) {
        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet) {
                Triplet varKey = (Triplet) entry.getKey();

                if (varKey.getFirst() instanceof Sequence && varKey.getSecond() instanceof Sequence && varKey.getThird() instanceof FollowerType && getValueFromCplex(lpData,entry) > 0.0) {
                System.out.println(varKey.getFirst().toString() + "; " + varKey.getSecond().toString() + "; " + ((FollowerType) varKey.getThird()).getID() + " : " + getValueFromCplex(lpData,entry));

                }
            }
        }
    }

//    private void checkLeaderStrategiesConsistent(Map<FollowerType, Map<InformationSet, Map<Sequence, Double>>> leaderStrategies){
//        for (InformationSet set : algConfig.getAllInformationSets().values()){
//            if (set.getPlayer().equals(FlipItGameInfo.DEFENDER)) continue;
//        }
//    }

    private Strategy getP1Strategy(Map<Sequence, Double> leaderRealPlan, Map<Sequence, Double> followerRealPlan) {
        return new NoMissingSeqStrategy(leader.getId() == 0 ? leaderRealPlan : followerRealPlan);
    }

    private Strategy getP2Strategy(Map<Sequence, Double> leaderRealPlan, Map<Sequence, Double> followerRealPlan) {
        return new NoMissingSeqStrategy(leader.getId() == 1 ? leaderRealPlan : followerRealPlan);
    }

    protected Iterable<Sequence> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData, FollowerType type) {
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
        if (shallowestBrokenStrategyCause == null) {
            return null;
        }
        System.out.println("sorting broken strategy");
        return sort(shallowestBrokenStrategyCause, shallowestBrokenStrategyCause.keySet());
    }

    protected Map<Sequence, Double> getBrokenStrategyCausesWithTypes(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData, FollowerType type) {
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
        if (shallowestBrokenStrategyCause == null) {
//            System.out.println("no broken strategy");
            return null;
        }
//        System.out.println("sorting broken strategy");
        return shallowestBrokenStrategyCause;//sort(shallowestBrokenStrategyCause, shallowestBrokenStrategyCause.keySet());
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

    protected Iterable<Pair<FollowerType, Sequence>> sortWithTypes(final Map<Pair<FollowerType,Sequence>, Double> shallowestBrokenStrategyCause, final Collection<Pair<FollowerType,Sequence>> allSeq) {
        List<Pair<FollowerType,Sequence>> list = new ArrayList<>(allSeq);

        Collections.sort(list, new Comparator<Pair<FollowerType, Sequence>>() {
            @Override
            public int compare(Pair<FollowerType, Sequence> o1, Pair<FollowerType, Sequence> o2) {
                return Double.compare(shallowestBrokenStrategyCause.get(o1), shallowestBrokenStrategyCause.get(o2));
            }
        });
        return list;
    }

    protected void printBinaryVariableValues(LPData data) {
        for (Map.Entry<Object, IloNumVar> entry : data.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet) {
                Triplet key = (Triplet) entry.getKey();

                if (key.getFirst().equals("binary"))
                    try {
                        System.out.println(entry.getKey() + ": " + data.getSolver().getValue(entry.getValue()));
                    } catch (IloException e) {
                        e.printStackTrace();
                    }
            }
        }
    }

    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Pair<FollowerType,Sequence>> followerBrokenStrategyCauses) {
        Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;

        for (Pair<FollowerType,Sequence> brokenStrategyCause : followerBrokenStrategyCauses) {
            restrictFollowerPlay(brokenStrategyCause.getRight(), lpData, brokenStrategyCause.getLeft());
            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);

            if (result.getRight() > currentBest.getRight()) {
                currentBest = result;
                if (currentBest.getRight()  >= value - eps) {
                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent reward " + value + "----------------");
                    removeRestriction(brokenStrategyCause.getRight(), lpData, brokenStrategyCause.getLeft());
                    return currentBest;
                }
            }
            removeRestriction(brokenStrategyCause.getRight(), lpData, brokenStrategyCause.getLeft());
        }
        return currentBest;
    }

    protected void updateLowerBound(double lowerBound) {
        lpTable.setConstant("obj_const", lowerBound);
    }

    protected double getLowerBound(double lowerBound, Pair<Map<Sequence, Double>, Double> currentBest) {
        return Math.max(lowerBound, currentBest.getRight());
    }


    protected void restrictFollowerPlay(Sequence brokenStrategyCause, LPData lpData, FollowerType type) {
        System.out.println(brokenStrategyCause + " of type " + type.getID() +  " fixed to zero");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Triplet) {
                if (((Triplet) varKey).getFirst() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getThird() instanceof FollowerType && ((Triplet) varKey).getThird().equals(type)) {
                    Triplet<Sequence, Sequence,FollowerType> p = (Triplet<Sequence, Sequence, FollowerType>) varKey;

                    if (p.getSecond().equals(brokenStrategyCause)) {
                        Pair<String, Triplet<Sequence, Sequence, FollowerType>> eqKey = new Pair<>("restr", p);

                        lpTable.setConstraint(eqKey, p, 1);
                        lpTable.setConstraintType(eqKey, 1);
                    }
                }
            }
        }
    }

    protected void removeRestriction(Sequence brokenStrategyCause, LPData lpData, FollowerType type) {
        System.out.println(brokenStrategyCause + " of type " + type.getID() +   " released");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Triplet) {
                if (((Triplet) varKey).getFirst() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getThird() instanceof FollowerType && ((Triplet) varKey).getThird().equals(type)) {
                    Triplet<Sequence, Sequence,FollowerType> p = (Triplet<Sequence, Sequence, FollowerType>) varKey;

                    if (p.getSecond().equals(brokenStrategyCause)) {
                        Pair<String, Triplet<Sequence, Sequence, FollowerType>> eqKey = new Pair<>("restr", p);

                        lpTable.removeFromConstraint(eqKey, p);
                        lpTable.setConstant(eqKey, 0.0);
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

    protected Map<InformationSet, Map<Sequence, Double>> getFollowerBehavioralStrategy(LPData lpData, FollowerType type) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet) {
                Triplet varKey = (Triplet) entry.getKey();

                if (varKey.getFirst() instanceof Sequence && varKey.getSecond() instanceof Sequence && varKey.getThird() instanceof FollowerType && varKey.getThird().equals(type)) {
//                    System.out.println("Extracting strategy");
                    Sequence playerSequence = (Sequence) varKey.getSecond();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isSequenceFrom((Sequence) varKey.getFirst(), playerSequence.getLastInformationSet()))
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getFollowerBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getFollowerBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                if (behavioralStrategy > eps) {
                                    isStrategy.put(playerSequence, behavioralStrategy);
                                }
                            }
                }
            }
        }
        return strategy;
    }

    protected HashSet<InformationSet> getInconsistentInformationSets(LPData lpData){
//        boolean isConsistent = true;
        HashSet<InformationSet> inconsistent = new HashSet<>();
        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet) {
                Triplet varKey = (Triplet) entry.getKey();

                if (varKey.getFirst() instanceof Sequence && varKey.getSecond() instanceof Sequence && varKey.getThird() instanceof FollowerType && varKey.getThird().equals(FlipItGameInfo.types[0])) {
//                    System.out.println("Extracting strategy");
                    Sequence playerSequence = (Sequence) varKey.getFirst();
                    if (inconsistent.contains(playerSequence.getLastInformationSet())) continue;
//                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    double[] typesValues = new double[FlipItGameInfo.numTypes];
                    double nonZero = 0.0;
                    for (FollowerType type : FlipItGameInfo.types) {
                        typesValues[type.getID()] = getValueFromCplex(lpData, new Triplet<>(varKey.getFirst(), varKey.getSecond(), type));
                        if (typesValues[type.getID()] != 0.0) nonZero = typesValues[type.getID()];
                    }
//                    Double type0Value = getValueFromCplex(lpData, entry);

                        if (isRelevantTo((Sequence) varKey.getSecond(), playerSequence.getLastInformationSet())) {
                            for (FollowerType type : FlipItGameInfo.types) {
                                if (typesValues[type.getID()] != 0.0 && typesValues[type.getID()] != nonZero) {
                                    inconsistent.add(playerSequence.getLastInformationSet());
                                    break;
                                }
                            }
                        }
                }
            }
        }
        return inconsistent;
    }

    protected Map<InformationSet, Map<Sequence, Double>> getLeaderBehavioralStrategy(LPData lpData, FollowerType type) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet) {
                Triplet varKey = (Triplet) entry.getKey();

                if (varKey.getFirst() instanceof Sequence && varKey.getSecond() instanceof Sequence && varKey.getThird() instanceof FollowerType && varKey.getThird().equals(type)) {
//                    System.out.println("Extracting strategy");
                    Sequence playerSequence = (Sequence) varKey.getFirst();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isRelevantTo((Sequence) varKey.getSecond(), playerSequence.getLastInformationSet())) {
                            if (isStrategy == null) {
                                if (currentValue > eps) {
                                    isStrategy = new HashMap<>();
                                    double behavioralStrat = getLeaderBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

                                    isStrategy.put(playerSequence, behavioralStrat);
                                    strategy.put(playerSequence.getLastInformationSet(), isStrategy);
                                }
                            } else {
                                double behavioralStrategy = getLeaderBehavioralStrategy(lpData, varKey, playerSequence, currentValue);

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

    protected double getLeaderBehavioralStrategy(LPData lpData, Triplet varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, new Triplet<>(sequenceCopy, varKey.getSecond(), varKey.getThird()));

            if (previousValue == 0) {
                System.out.println("ZERO PREVIOUS VALUE");
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) varKey.getSecond());

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, new Triplet<>(sequenceCopy, opponentSequence, varKey.getThird()));
                }
            }
            behavioralStrat /= previousValue;
        }
        return behavioralStrat;
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
                    previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, opponentSequence) : new Pair<Sequence,Sequence>(opponentSequence, sequenceCopy));
                }
            }
            behavioralStrat /= previousValue;
        }
        return behavioralStrat;
    }

    protected double getBehavioralSequence(LPData lpData, Pair varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, varKey.getRight()) : new Pair<>(varKey.getLeft(), sequenceCopy));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (playerSequence.getPlayer().equals(leader) ? varKey.getRight() : varKey.getLeft()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, (playerSequence.getPlayer().equals(leader) ? new Pair<>(sequenceCopy, opponentSequence) : new Pair<>(opponentSequence, sequenceCopy)));
                }
            }
            behavioralStrat /= previousValue;
        }
        return behavioralStrat;
    }

    protected double getFollowerBehavioralStrategy(LPData lpData, Triplet varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, new Triplet<>(varKey.getFirst(), sequenceCopy, varKey.getThird()));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (varKey.getFirst()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, new Triplet<>(opponentSequence, sequenceCopy, varKey.getThird()));
                }
            }
            behavioralStrat /= previousValue;
        }
//        System.out.println(playerSequence.toString() + "type = " + ((FollowerType)varKey.getThird()).getID() +  " : " + behavioralStrat);
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

    protected boolean isRelevantSequenceTo(Sequence sequence, InformationSet informationSet) {
        if (informationSet == null)
            return sequence.isEmpty();
        if ( sequence.isEmpty() ) return true;
        assert !sequence.getPlayer().equals(informationSet.getPlayer());
        for (GameState gameState : informationSet.getAllStates()) {
            if (gameState.getSequenceFor(sequence.getPlayer()).equals(sequence))
                return true;
        }
        Sequence prefix = new ArrayListSequenceImpl(sequence);
        prefix.removeLast();
        for (GameState gameState : informationSet.getAllStates()) {
            if (prefix.isPrefixOf(gameState.getSequenceFor(sequence.getPlayer())))
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

                    for (FollowerType type : FlipItGameInfo.types) {
                        for (Action action : outgoingSequence) {
                            for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                                createISActionConstraint(algConfig, relevantSequence, informationSet, type);
                            }
                        }
                        createISActionConstraint(algConfig, new ArrayListSequenceImpl(follower), informationSet, type);
                    }
                }
            }
        }
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(follower)) {
                for (Sequence sequence : informationSet.getOutgoingSequences()) {
                    for (FollowerType type : FlipItGameInfo.types) {
                        Object eqKey = new Quadruple<>(informationSet, sequence, type, "eq");
                        Object varKey = new Triplet<>(informationSet, sequence, type);
                        Object contVarKey = new Triplet<>("v", sequence, type);

                        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                        lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                        lpTable.setConstraint(eqKey, varKey, 1);
                        lpTable.setConstraint(eqKey, contVarKey, -1);
                        lpTable.setConstraintType(eqKey, 1);
                    }
                }
            }
        }
    }

    protected void createISActionConstraint(StackelbergConfig algConfig, Sequence followerSequence, SequenceInformationSet informationSet, FollowerType type) {
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            Object eqKey = new Quadruple<>(informationSet, sequence, followerSequence, type);
            Object varKey = new Triplet<>(informationSet, followerSequence, type);

            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, varKey, 1);
            lpTable.setConstraintType(eqKey, 2);
            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(sequence)) {
                Double[] seqCombUtilities = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, sequence);

                if (seqCombUtilities != null) {
                    double utility = seqCombUtilities[1+type.getID()] * algConfig.getNatureProbabilityFor(leaderSequence, sequence);

                    if (utility != 0) {
//                        System.out.println("uti: " +  utility);
                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, followerSequence, type), -utility);
                    }
                }
            }
            if (algConfig.getReachableSets(sequence) != null)
                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(sequence)) {
                    if (reachableSet.getOutgoingSequences() != null && !reachableSet.getOutgoingSequences().isEmpty())
                        lpTable.setConstraint(eqKey, new Triplet<>(reachableSet, followerSequence, type), -1);
                }
        }
    }

//    protected void addObjectiveViaConstraint(StackelbergConfig algConfig) {
//        lpTable.setConstraint("obj", "obj", 1);
//        lpTable.setLowerBound("obj", Double.NEGATIVE_INFINITY);
////        lpTable.setConstraintType("obj", 1);
//        for (GameState leaf : algConfig.getAllLeafs()) {
//            lpTable.setConstraint("obj", createSeqPairVarKey(leaf), -leaf.getUtilities()[leader.getId()] * leaf.getNatureProbability());
//        }
//
//        lpTable.setObjective("obj", 1);
//
//        lpTable.setConstraint("obj_const", "obj", 1);
//        lpTable.setConstraintType("obj_const", 2);
//        lpTable.setConstant("obj_const", -info.getMaxUtility());
//    }

    protected void addObjective() {
        for (FollowerType type : FlipItGameInfo.types) {
            for (Map.Entry<GameState, Double[]> entry : algConfig.getActualNonZeroUtilityValuesInLeafsGenSum().entrySet()) {
//                if(entry.getKey().getNatureProbability() < 1.0) System.out.println(" NON-1 NP");;
                lpTable.setObjective(createSeqPairVarKey(entry.getKey(),type), entry.getKey().getNatureProbability()*type.getPrior()*entry.getValue()[leader.getId()]);
            }
        }
//        for (GameState leaf : algConfig.getAllLeafs()) {
//            lpTable.setObjective(createSeqPairVarKey(leaf), leaf.getUtilities()[leader.getId()] * leaf.getNatureProbability());
//        }
    }

    protected void createSequenceConstraints() {
        for (FollowerType type : FlipItGameInfo.types) {
            createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower),type);
            for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
                createSequenceConstraint(algConfig, followerSequence,type);
            }
        }
    }

    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence, FollowerType type) {
        Object varKey = new Triplet<>("v", followerSequence,type);
        Object eqKey = new Pair<>(followerSequence,type);


        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(eqKey, 1);
        lpTable.setConstraint(eqKey, varKey, 1);
        for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
            Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);

            if (seqCombValue != null) {
                double natureProb = (algConfig.getNatureProbabilityFor(leaderSequence,followerSequence) != null) ?
                    algConfig.getNatureProbabilityFor(leaderSequence,followerSequence) : 1.0;
//                System.out.println(FlipItGameInfo.numTypes + " / " + type.getID() + " : " + seqCombValue[type.getID() + 1] + " " + Arrays.toString(seqCombValue));
                double followerValue = seqCombValue[type.getID() + 1] * natureProb; // + leader

                if (followerValue != 0)
                    lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSequence,type), -followerValue);
            }
        }
        for (SequenceInformationSet reachableSet : algConfig.getReachableSets(followerSequence)) {
            for (Sequence sequence : reachableSet.getOutgoingSequences()) {
                Object contVarKey = new Triplet<>("v", sequence, type);

                lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(eqKey, contVarKey, -1);
            }
        }
    }

    protected void createPContinuationConstraints() {
        createInitPConstraint();
        Set<Object> blackList = new HashSet<>();
        Set<Triplet<Sequence, Sequence, FollowerType>> pStops = new HashSet<>();

        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            List<Action> actions = expander.getActions(informationSet);
            Player opponent = info.getOpponent(informationSet.getPlayer());

            for (GameState gameState : informationSet.getAllStates()) {
                if (!gameState.isGameEnd()) {
                    for (FollowerType type : FlipItGameInfo.types) {
                        createPContinuationConstraint(actions, opponent, gameState, blackList, pStops, type);
                    }
                }
            }
        }

        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)) {
            for (Sequence compatibleFollowerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)) {
                for (Action action : compatibleFollowerSequence) {
                    Sequence actionHistory = ((PerfectRecallInformationSet)action.getInformationSet()).getPlayersHistory();

                    for (FollowerType type : FlipItGameInfo.types) {
                        Object eqKeyFollower = new Quadruple<>(leaderSequence, actionHistory, action.getInformationSet(), type);

                        if (!blackList.contains(eqKeyFollower)) {
                            blackList.add(eqKeyFollower);
                            Triplet<Sequence, Sequence, FollowerType> varKey = createSeqPairVarKey(leaderSequence, actionHistory, type);

                            lpTable.setConstraintType(eqKeyFollower, 1);
                            lpTable.setConstraint(eqKeyFollower, varKey, -1);
                            for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                                lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, followerSequence, type), 1);

//                                if (type.getID() == 0){
//                                    Triplet<Sequence, Sequence, FollowerType> typeVarKey = createSeqPairVarKey(leaderSequence, followerSequence, type);
//                                    for (int i = 1 ; i < FlipItGameInfo.numTypes; i++){
//                                        Triplet<FollowerType, FollowerType, Triplet> eqKey = new Triplet<>(type, FlipItGameInfo.types[i], typeVarKey);
//                                        Triplet<Sequence, Sequence, FollowerType> otherTypeVarKey = createSeqPairVarKey(leaderSequence, followerSequence, FlipItGameInfo.types[i]);
//                                        lpTable.setConstraintType(eqKey, 1);
//                                        lpTable.setConstraint(eqKey, typeVarKey, 1);
//                                        lpTable.setConstraint(eqKey, otherTypeVarKey, -1);
//                                        lpTable.setConstant(eqKey, 0.0);
//                                    }
//                                }
                            }
                        }
                    }

                    ListIterator<Action> leaderSeqIterator = leaderSequence.listIterator(leaderSequence.size());
                    Action leaderAction;

                    while (leaderSeqIterator.hasPrevious()) {
                        leaderAction = leaderSeqIterator.previous();
                        Sequence leaderHistory = ((PerfectRecallInformationSet)leaderAction.getInformationSet()).getPlayersHistory();

                        for (FollowerType type : FlipItGameInfo.types) {
                            Object eqKeyLeader = new Quadruple<>(leaderHistory, actionHistory, leaderAction, type);

                            if (!blackList.contains(eqKeyLeader)) {
                                blackList.add(eqKeyLeader);
                                Triplet<Sequence, Sequence, FollowerType> varKey = createSeqPairVarKey(leaderHistory, actionHistory, type);

                                lpTable.setConstraintType(eqKeyLeader, 1);
                                lpTable.setConstraint(eqKeyLeader, varKey, -1);
                                for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                                    lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderContinuation, actionHistory, type), 1);

                                }
                            }

                            for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                                Object eqKeyLeaderCont = new Quadruple<>(leaderHistory, followerSequence, leaderAction.getInformationSet(), type);

                                if (!blackList.contains(eqKeyLeaderCont)) {
                                    blackList.add(eqKeyLeaderCont);
                                    Triplet<Sequence, Sequence, FollowerType> varKeyCont = createSeqPairVarKey(leaderHistory, followerSequence, type);

                                    lpTable.setConstraintType(eqKeyLeaderCont, 1);
                                    lpTable.setConstraint(eqKeyLeaderCont, varKeyCont, -1);
                                    for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
                                        lpTable.setConstraint(eqKeyLeaderCont, createSeqPairVarKey(leaderContinuation, followerSequence, type), 1);

//                                        if (type.getID() == 0){
//                                            Triplet<Sequence, Sequence, FollowerType> typeVarKey = createSeqPairVarKey(leaderContinuation, followerSequence, type);
//                                            for (int i = 1 ; i < FlipItGameInfo.numTypes; i++){
//                                                Triplet<FollowerType, FollowerType, Quadruple> eqKey = new Triplet(type, FlipItGameInfo.types[i], eqKeyLeaderCont);
//                                                Triplet<Sequence, Sequence, FollowerType> otherTypeVarKey = createSeqPairVarKey(leaderContinuation, followerSequence, FlipItGameInfo.types[i]);
//                                                lpTable.setConstraintType(eqKey, 1);
//                                                lpTable.setConstraint(eqKey, typeVarKey, 1);
//                                                lpTable.setConstraint(eqKey, otherTypeVarKey, -1);
//                                                lpTable.setConstant(eqKey, 0.0);
//                                            }
//                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    protected void createPContinuationConstraint(List<Action> actions, Player opponent, GameState gameState, Set<Object> blackList, Set<Triplet<Sequence, Sequence, FollowerType>> pStops, FollowerType type) {
        Quadruple<Sequence, Sequence, InformationSet, FollowerType> eqKey = new Quadruple<Sequence, Sequence, InformationSet, FollowerType>(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), algConfig.getInformationSetFor(gameState), type);

        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);
        Triplet<Sequence, Sequence, FollowerType> varKey = createSeqPairVarKey(gameState, type);

        pStops.add(varKey);
        lpTable.setConstraint(eqKey, varKey, -1);
        lpTable.setConstraintType(eqKey, 1);
        for (Action action : actions) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(gameState.getSequenceForPlayerToMove());

            sequenceCopy.addLast(action);
            Triplet<Sequence, Sequence, FollowerType> contVarKey = createSeqPairVarKey(sequenceCopy, gameState.getSequenceFor(opponent), type);

            pStops.add(contVarKey);
            lpTable.setConstraint(eqKey, contVarKey, 1);
        }
    }

    protected Triplet<Sequence, Sequence,FollowerType> createSeqPairVarKey(Sequence sequence1, Sequence sequence2, FollowerType type) {
        Triplet<Sequence, Sequence, FollowerType> varKey = sequence1.getPlayer().equals(leader) ? new Triplet<Sequence, Sequence, FollowerType>(sequence1, sequence2, type) : new Triplet<Sequence,Sequence,FollowerType>(sequence2, sequence1,type);

        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Triplet<Sequence, Sequence,FollowerType> createSeqPairVarKeyCheckExistence(Sequence sequence1, Sequence sequence2, FollowerType type) {
        Triplet<Sequence, Sequence, FollowerType> varKey = sequence1.getPlayer().equals(leader) ? new Triplet<Sequence, Sequence, FollowerType>(sequence1, sequence2, type) : new Triplet<Sequence,Sequence,FollowerType>(sequence2, sequence1,type);

        assert lpTable.exists(varKey);
        lpTable.watchPrimalVariable(varKey, varKey);
        lpTable.setLowerBound(varKey, 0);
        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }

    protected Triplet<Sequence, Sequence, FollowerType> createSeqPairVarKey(GameState gameState, FollowerType type) {
        return createSeqPairVarKey(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower),type);
    }

    protected void createInitPConstraint() {
        for (FollowerType type : FlipItGameInfo.types) {
            Pair eqKey =  new Pair("initP",type);
            lpTable.setConstraint(eqKey, createSeqPairVarKey(new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower),type), 1);
            lpTable.setConstant(eqKey, 1);
            lpTable.setConstraintType(eqKey, 1);
        }
    }

    public int getLPInvocationCount() {
        return lpInvocationCount;
    }
}
