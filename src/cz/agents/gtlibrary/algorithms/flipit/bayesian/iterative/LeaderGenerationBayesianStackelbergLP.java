package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.ColumnGenerationLPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.LeaderGenerationTwoPlayerSefceLP;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Quadruple;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 06/10/2017.
 */
public class LeaderGenerationBayesianStackelbergLP extends LeaderGenerationTwoPlayerSefceLP{
    protected int bnbBranchingCount = 0;
    protected int generationCount = 0;
    protected HashMap<Object,Integer> restrictions;

    protected boolean MEASURE_TIME = true;
    protected boolean GENERATION_BEFORE_FIXING = true;

    protected long deviationIdentificationTime = 0;
    protected long brokenStrategyIdentificationTime = 0;
    protected long restrictedGameGenerationTime = 0;

    protected double eps_;
//    private long

    public LeaderGenerationBayesianStackelbergLP(Player leader, GameInfo info) {
        super(leader, info);
        restrictions =  new HashMap<>();
//        lpTable = new RecyclingLPTable();
//        System.out.println(lpTable.getClass().getCanonicalName());
//        System.exit(0);
    }

    public LeaderGenerationBayesianStackelbergLP(Player leader, GameInfo info, boolean useColumnGenTable) {
        super(leader, info);
        restrictions =  new HashMap<>();
        if (useColumnGenTable) lpTable = new ColumnGenerationLPTable();
        else
            lpTable = new LPTable();
//        lpTable = new RecyclingLPTable();
//        System.out.println(lpTable.getClass().getCanonicalName());
//        System.exit(0);
    }

    public LeaderGenerationBayesianStackelbergLP(Player leader, GameInfo info, boolean useColumnGenTable, boolean sequentialGeneration) {
        super(leader, info);
        restrictions =  new HashMap<>();
        if (useColumnGenTable) lpTable = new ColumnGenerationLPTable();
        else
            lpTable = new LPTable();
        GENERATION_BEFORE_FIXING = sequentialGeneration;
//        eps = 1e-8;
//        lpTable = new RecyclingLPTable();
//        System.out.println(lpTable.getClass().getCanonicalName());
//        System.exit(0);
    }

    @Override
    protected void generateFollowerConstraints(){
        // generate relevant parts of cons 3,6,7,8

        // 3 :
        for(FollowerType type : FlipItGameInfo.types) {
            lpTable.setConstraint("initP" + type, createSeqTripletVarKey(type, new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower)), 1);
            lpTable.setConstant("initP" + type, 1);
            lpTable.setConstraintType("initP" + type, 1);

            // 6 :
            createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower), type);
            for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
                createSequenceConstraint(algConfig, followerSequence, type);
            }
        }

            // 7 :
            for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
                if (informationSet.getPlayer().equals(follower)) {
                    if (!informationSet.getOutgoingSequences().isEmpty()) {
                        Sequence outgoingSequence = informationSet.getOutgoingSequences().iterator().next();

                        for (Action action : outgoingSequence) {
                            for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                                for (Sequence sequence : informationSet.getOutgoingSequences()) {
                                    for (Sequence leaderSeq : algConfig.getCompatibleSequencesFor(sequence)) {
                                        if (!relevantForLeaderInDeviation.containsKey(leaderSeq))
                                            relevantForLeaderInDeviation.put(leaderSeq, new HashSet<>());
//                                    Double[] seqCombUtilities = algConfig.getGenSumSequenceCombinationUtility(leaderSeq, sequence);
//                                    if (seqCombUtilities!= null && seqCombUtilities[follower.getId()] != 0)
                                        relevantForLeaderInDeviation.get(leaderSeq).add(relevantSequence);
                                        relevantForLeaderInDeviation.get(leaderSeq).add(new ArrayListSequenceImpl(follower));
                                    }
                                }
                                for (FollowerType type : FlipItGameInfo.types) {
                                    createISActionConstraint(algConfig, relevantSequence, informationSet, type);
                                }
                            }
                        }
                        for (FollowerType type : FlipItGameInfo.types) {
                            createISActionConstraint(algConfig, new ArrayListSequenceImpl(follower), informationSet, type);
                        }
                    }
                }
            }

            // 8 :
            for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
                if (informationSet.getPlayer().equals(follower)) {
                    for (Sequence sequence : informationSet.getOutgoingSequences()) {
                        for (FollowerType type : FlipItGameInfo.types) {
                            Object eqKey = new Quadruple<>(type, informationSet, sequence, "eq");
                            Object varKey = new Triplet<>(type, informationSet, sequence);
                            Object contVarKey = new Triplet<>(type, "v", sequence);

                            if (CONVERT_TO_CANONIC) {
                                Object vKey = new Pair<>("w", varKey);
                                Object contVKey = new Pair<>("w", sequence);
                                lpTable.setConstraint(eqKey, vKey, -1);
                                lpTable.setConstraint(eqKey, contVKey, 1);
                            } else {
                                lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                                lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                            }
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
            Object eqKey = new Quadruple<>(type, informationSet, sequence, followerSequence);
            Object varKey = new Triplet<>(type, informationSet, followerSequence);
            Object vKey;

            lpTable.setConstraint(eqKey, varKey, 1);
            if (CONVERT_TO_CANONIC) {
                Pair<String, Triplet> epsilon = new Pair("v", eqKey);
                lpTable.setConstraint(eqKey, epsilon, -1);
//            lpTable.setLowerBound(epsilon, Double.NEGATIVE_INFINITY);
//            lpTable.setUpperBound(epsilon, 0.0);
//            lpTable.setConstraintType(eqKey, 2);
                lpTable.setConstraintType(eqKey, 1);
                vKey = new Pair<>("w", varKey);
                lpTable.setConstraint(eqKey, vKey, -1);
            }
            else{
                lpTable.setConstraintType(eqKey, 2);
                lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            }
//            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(sequence)) {
//                Double[] seqCombUtilities = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, sequence);
//
//                if (seqCombUtilities != null) {
//                    double utility = seqCombUtilities[follower.getId()];
//
//                    if (utility != 0)
//                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, followerSequence), -utility);
//                }
//            }
            if (algConfig.getReachableSets(sequence) != null)
                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(sequence)) {
                    if (reachableSet.getOutgoingSequences() != null && !reachableSet.getOutgoingSequences().isEmpty()) {
                        varKey = new Triplet<>(type, reachableSet, followerSequence);
                        lpTable.setConstraint(eqKey, varKey, -1);
                        if(CONVERT_TO_CANONIC){
                            vKey = new Pair<>("w", varKey);
                            lpTable.setConstraint(eqKey, vKey, 1);
                        }
                    }
                }
        }
    }

    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence, FollowerType type) {
        Object varKey = new Triplet<>(type, "v", followerSequence);
        Object eqKey = new Pair<>(type, followerSequence);

//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(eqKey, 1);
        if (CONVERT_TO_CANONIC){
            Object vKey = new Triplet<>(type, "w", followerSequence);
            lpTable.setConstraint(eqKey, vKey, -1);
        }
        else{
            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        }
        lpTable.setConstraint(eqKey, varKey, 1);
//        for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor(followerSequence)) {
//            Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
//
//            if (seqCombValue != null) {
//                double followerValue = seqCombValue[follower.getId()];
//
//                if (followerValue != 0)
//                    lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);
//            }
//        }
        for (SequenceInformationSet reachableSet : algConfig.getReachableSets(followerSequence)) {
            for (Sequence sequence : reachableSet.getOutgoingSequences()) {
                Object contVarKey = new Triplet<>(type, "v", sequence);
                if (CONVERT_TO_CANONIC){
                    Object contVKey = new Triplet<>(type, "w", sequence);
                    lpTable.setConstraint(eqKey, contVKey, 1);
                }
                else {
                    lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                }
                lpTable.setConstraint(eqKey, contVarKey, -1);
            }
        }
    }


    @Override
    public double calculateLeaderStrategies(StackelbergConfig algConfig, Expander<SequenceInformationSet> expander) {
        this.algConfig = algConfig;
        this.expander = expander;
//        followerBestResponse = new FollowerBestResponse(algConfig.getRootState(), expander, algConfig, leader, follower);
        long startTime = threadBean.getCurrentThreadCpuTime();

        generateRelevantSequences();

        // construct initial LP
        generateFollowerConstraints();

//        System.out.println("////////////////////////////////////");
//        System.out.println("          RELEVANT SEQUENCES");
//        System.out.println("////////////////////////////////////");
//        for (Sequence seq : relevantForLeaderInP.keySet()){
//            System.out.println(seq);
//            for (Sequence s : relevantForLeaderInP.get(seq))
//                System.out.println("    " + s);
//        }
//        System.out.println("////////////////////////////////////");


        HashSet<Sequence> leaderSequences = findLeaderInitialRG();
        addLeaderSequencesToLP(leaderSequences);

//        System.out.println("LP build...");
//        lpTable.watchAllPrimalVariables();
        overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
        Pair<Map<Sequence, Double>, Double> result = solve(-info.getMaxUtility(), info.getMaxUtility());

        resultStrategies.put(leader, result.getLeft());
        resultValues.put(leader, result.getRight());
        System.out.println("RG size: " +leaderRG.size()+ "/"+((StackelbergConfig) algConfig).getSequencesFor(leader).size());
        gameValue = result.getRight();
        return result.getRight();
    }

    protected LPData solveForSefce(HashSet<Sequence> leaderSequences){
        addLeaderSequencesToLP(leaderSequences);

        boolean updated = true;
        long startTime;
        LPData lpData = null;
//        double value = 0.0;
//        int iteration = 0;
        while(updated){
            // solve LP
            try {
                generationCount++;
                System.out.println("-----------------------");
                System.out.println("Iteration "+generationCount);
                System.out.printf("Watching...");
                lpTable.watchAllDualVariables();
                lpTable.watchAllPrimalVariables();
                System.out.println("done.");
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Generating cplex...");
                lpData = lpTable.toCplex();
                System.out.println("done.");
                overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//                lpData.getSolver().exportModel("Iter2pSEFCE_" +iteration + ".lp");
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Solving...");
                lpData.getSolver().solve();
                System.out.println("done.");
                overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
                if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                    gameValue = lpData.getSolver().getObjValue();
                    System.out.println("LP reward: " + gameValue );
                } else {
//                    lpData.getSolver().getConflict();
                    System.err.println(lpData.getSolver().getStatus());
                }
            } catch (IloException e) {
                e.printStackTrace();
            }

            // calculate reduced costs -> find deviation
            startTime = threadBean.getCurrentThreadCpuTime();
            System.out.printf("Finding deviations...");
            leaderSequences = findLeaderDeviation(lpData);
            System.out.println("done.");
            deviationIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            // update LP
            if (leaderSequences.isEmpty())
                updated = false;
            else {
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Adding new sequences...");
                addLeaderSequencesToLP(leaderSequences);
                System.out.println("done.");
                restrictedGameGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
            }
            System.out.println("RG size: " +leaderRG.size()+ "/"+((StackelbergConfig) algConfig).getSequencesFor(leader).size());
        }
        return  lpData;
    }

    protected void addLeaderSequencesToLP(HashSet<Sequence> leaderSequences){
        int numberOfNonExistant = 0;
        System.out.println("# of cons - before adding seqs: "  + lpTable.columnCount());
        boolean originalPContinuation = DEBUG && USE_ORIG_PCONT;
        for (Sequence leaderSequence : leaderSequences){
            leaderRG.add(leaderSequence);
            // update objective, 6, 7 pouze pro sekvence mirici do listu
            for (Sequence followerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)){


                Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
                if (seqCombValue != null) {
//                    double followerValue = seqCombValue[follower.getId()];
                    double leaderValue = seqCombValue[leader.getId()];

                    // objective
                    if (leaderValue != 0) {
                        for (FollowerType type : FlipItGameInfo.types)
                            setNewObjective(createSeqTripletVarKey(type, leaderSequence, followerSequence), leaderValue * type.getPrior());
                    }

//                    if (followerValue != 0) {
                        // 6:
                    for (FollowerType type : FlipItGameInfo.types)
                        setNewConstraint(new Pair(type, followerSequence), createSeqTripletVarKey(type, leaderSequence, followerSequence), -seqCombValue[type.getID()+1]);

                        // 7:
                        if(followerSequence.getLastInformationSet() == null) continue;
                        for (GameState state : followerSequence.getLastInformationSet().getAllStates()){
                            Sequence stateLeaderSeq = state.getSequenceFor(leader);
//                            System.out.println(stateLeaderSeq);
                            if (relevantForLeaderInDeviation.containsKey(stateLeaderSeq)) {
                                for (Sequence stateFollowerSeq : relevantForLeaderInDeviation.get(stateLeaderSeq)) {
                                    if (stateFollowerSeq.size() > 0 && followerSequence.size() > 0 && !((SequenceInformationSet)stateFollowerSeq.getLastInformationSet()).getPlayersHistory().isPrefixOf(followerSequence.getSubSequence(followerSequence.size()-1)))
                                        continue;
//                                    lpTable.getEquationIndex()
//                                    if (stateFollowerSeq.size() == 1) continue;
                                    for (FollowerType type : FlipItGameInfo.types) {
                                        Object eqKey = new Quadruple<>(type, followerSequence.getLastInformationSet(), followerSequence, stateFollowerSeq);
                                        if (CHECK_EXISTENCE && !lpTable.existsEqKey(eqKey)) {
                                            numberOfNonExistant++;
                                            continue;
                                        }
                                        Object varKey = createSeqTripletVarKey(type, leaderSequence, stateFollowerSeq);
                                        setNewConstraint(eqKey, varKey, -seqCombValue[type.getID()+1]);
                                    }
                                }
                            }
                        }
//                    }
                }
            }

            if (!originalPContinuation){

                // 4 -> druha cast
                if (!leaderSequence.isEmpty()) {
                    Sequence leaderSubSeq = leaderSequence.getSubSequence(0, leaderSequence.size() - 1);
                    if (relevantForLeaderInP.containsKey(leaderSequence.getLastInformationSet()))//leaderSubSeq))
                        for (Sequence followerSequence : relevantForLeaderInP.get(leaderSequence.getLastInformationSet())){//leaderSubSeq)) {
                            for (FollowerType type : FlipItGameInfo.types) {
                                Object eqKey = new Triplet<>(type, (SequenceInformationSet) leaderSequence.getLastInformationSet(), followerSequence);
                                setNewConstraint(eqKey, createSeqTripletVarKey(type, leaderSequence, followerSequence), -1.0);
                                lpTable.setConstant(eqKey, 0.0);
                                lpTable.setConstraintType(eqKey, 1);
                            }
                        }
                }
            }
        }
        if (originalPContinuation)
            createPContinuationConstraints();
        else {
            for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
                // 4 -> prvni cast
                if (set.getPlayer().equals(leader) && !set.getOutgoingSequences().isEmpty()) {
                    if (leaderSequences.contains(set.getPlayersHistory()) && relevantForLeaderInP.containsKey(set)){//set.getPlayersHistory())) {
                        for (Sequence followerSequence : relevantForLeaderInP.get(set)){//set.getPlayersHistory())) {
                            for (FollowerType type : FlipItGameInfo.types) {
                                Object eqKey = new Triplet<>(type, set, followerSequence);
                                setNewConstraint(eqKey, createSeqTripletVarKey(type, set.getPlayersHistory(), followerSequence), 1.0);
                                lpTable.setConstant(eqKey, 0.0);
                                lpTable.setConstraintType(eqKey, 1);
                            }
                        }
                    }
                    // leader indifference in types
                    for (Sequence leaderSequence : set.getOutgoingSequences()) {
                        if (!leaderSequences.contains(leaderSequence)) continue;
                        Triplet setActionVarKey = new Triplet("sIA", set, leaderSequence);
                        for (FollowerType type : FlipItGameInfo.types){
                            Pair eqKey = new Pair(type, setActionVarKey);
                            HashMap<Sequence,Collection<Triplet>> ps = new HashMap<>();
                            boolean nonNullRelevant = false;

                            for (GameState gameState : set.getAllStates()){
                                Sequence natureSequence = gameState.getSequenceFor(FlipItGameInfo.NATURE);
                                if (!ps.containsKey(natureSequence))
                                    ps.put(natureSequence, new ArrayList<>());
                                for (Sequence followerSequence : getRelevantSequencesFor(gameState)){
                                    nonNullRelevant = true;
                                    if (!isPrefixOfRelevant(ps.get(natureSequence), followerSequence)) {
                                        ps.get(natureSequence).add(new Triplet<FollowerType,  Sequence, Sequence>(type, leaderSequence, followerSequence));
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
                // 5
                if (set.getPlayer().equals(follower) && !set.getOutgoingSequences().isEmpty()) {
                    if(relevantForFollowerInP.containsKey(set)){
                        for (Sequence leaderSequence : relevantForFollowerInP.get(set)){
                            if (!leaderSequences.contains(leaderSequence)) continue;
//                    for (Sequence leaderSequence : leaderSequences) {
//                        if (relevantForFollowerInP.containsKey(leaderSequence) && relevantForFollowerInP.get(leaderSequence).contains(set.getPlayersHistory())) {
                            for (FollowerType type : FlipItGameInfo.types) {
                                Object eqKey = new Triplet<>(type, set, leaderSequence);
                                setNewConstraint(eqKey, createSeqTripletVarKey(type, leaderSequence, set.getPlayersHistory()), 1.0);
                                for (Sequence outgoing : set.getOutgoingSequences())
                                    setNewConstraint(eqKey, createSeqTripletVarKey(type, leaderSequence, outgoing), -1.0);
                                lpTable.setConstant(eqKey, 0.0);
                                lpTable.setConstraintType(eqKey, 1);
                            }
                        }
                    }
                }
            }
        }
        System.out.println("# of cons - after adding seqs: "  + lpTable.columnCount());
        System.out.println("# of non-existent: " + numberOfNonExistant);
    }

    protected boolean isPrefixOfRelevant(Collection<Triplet> ps, Sequence followerSequence){
        for (Triplet key : ps)
            if (followerSequence.isPrefixOf((Sequence)key.getThird()))
                return true;
        return false;
    }

    protected HashSet<Sequence> getRelevantSequencesFor(GameState gameState) {
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
        return relevantSequences;
    }

    protected Triplet<FollowerType, Sequence, Sequence> createSeqTripletVarKey(FollowerType type, Sequence sequence1, Sequence sequence2) {
        Triplet<FollowerType, Sequence, Sequence> varKey = sequence1.getPlayer().equals(leader) ? new Triplet<>(type, sequence1, sequence2) : new Triplet<>(type, sequence2, sequence1);

//        lpTable.watchPrimalVariable(varKey, varKey);
//        lpTable.setLowerBound(varKey, 0);
//        lpTable.setUpperBound(varKey, 1);
        return varKey;
    }


    //    @Override
    protected Pair<Map<Sequence, Double>, Double> solve(double lowerBound, double upperBound) {
        try {
            long startTime = threadBean.getCurrentThreadCpuTime();
            System.out.println("-----------------------");
            System.out.println("Invocation: "+ bnbBranchingCount);

//            updateLowerBound(lowerBound);
            System.out.printf("Watching...");
            lpTable.watchAllDualVariables();
            lpTable.watchAllPrimalVariables();
            System.out.println("done.");
            LPData lpData = lpTable.toCplex();

            overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            lpData.getSolver().exportModel("SSEIter.lp");
            startTime = threadBean.getCurrentThreadCpuTime();
            System.out.printf("Solving...");
            lpData.getSolver().solve();
            System.out.println("done.");
            bnbBranchingCount++;
            overallConstraintLPSolvingTime += threadBean.getCurrentThreadCpuTime() - startTime;
//            printBinaryVariableValues(lpData);
            if (lpData.getSolver().getStatus() == IloCplex.Status.Optimal) {
                double value = lpData.getSolver().getObjValue();

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
//                System.out.println("RG:");
//                for (Sequence seq : leaderRG) System.out.println(seq);
//                System.out.println("///////////////");
//                System.out.println("Primal vars:");
//                for (Object var : lpData.getWatchedPrimalVariables().keySet())
//                    if (var instanceof Pair) System.out.println(var);

                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Finding deviations....");
                HashSet<Sequence> leaderSequences = findLeaderDeviation(lpData);
                System.out.println("done.");
                deviationIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;

                if (GENERATION_BEFORE_FIXING && !leaderSequences.isEmpty()) {
                    lpData = solveForSefce(leaderSequences);
                    value = lpData.getSolver().getObjValue();
                    leaderSequences.clear();
                }

//                startTime = threadBean.getCurrentThreadCpuTime();
//                System.out.printf("Finding broken strategy...");
//                Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getBehavioralStrategy(lpData, follower);
//                Iterable<Sequence> brokenStrategyCauses = getBrokenStrategyCauses(followerBehavStrat, lpData);
//                System.out.println("done.");
//                System.out.println("BSC: " + brokenStrategyCauses);
//                brokenStrategyIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//                Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));

                startTime = threadBean.getCurrentThreadCpuTime();
                Map<Pair<FollowerType,Sequence>,Double> causes = new HashMap<>();
//                Map<FollowerType,Map<InformationSet, Map<Sequence, Double>>> leaderStrategies = new HashMap<>();
                Map<FollowerType,Map<InformationSet, Map<Sequence, Double>>> followersStrategies = new HashMap<>();
//                int causeDepth = Integer.MAX_VALUE;
                for (FollowerType type : FlipItGameInfo.types) {
                    Map<InformationSet, Map<Sequence, Double>> followerBehavStrat = getFollowerBehavioralStrategy(lpData, type);
                    followersStrategies.put(type,followerBehavStrat);
                    Map<Sequence, Double> cause = getBrokenStrategyCauses(followerBehavStrat, lpData);


                    if (cause != null && !causes.isEmpty() && cause.keySet().iterator().next().size() < causes.keySet().iterator().next().getRight().size())
                        causes.clear();
                    if (cause != null && causes.isEmpty()){
                        for (Sequence sequence : cause.keySet()) {
                            causes.put(new Pair(type, sequence), cause.get(sequence));
                        }
                    }
//                    if (cause != null){
//                        for (Sequence sequence : cause.keySet()) {
//                            causes.put(new Pair(type, sequence), cause.get(sequence));
//                        }
//                    }


                }
//                checkLeaderStrategiesConsistent(leaderStrategies);
                Iterable<Pair<FollowerType,Sequence>> brokenStrategyCauses = sortWithTypes(causes,causes.keySet());
                brokenStrategyIdentificationTime += threadBean.getCurrentThreadCpuTime() - startTime;



                if (causes.isEmpty()) {
                    lpData = null;
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
                    if (leaderSequences.isEmpty()) {
//                        Map<Sequence, Double> leaderRealPlan = behavioralToRealizationPlan(getLeaderBehavioralStrategy(lpData, leader));
                        return new Pair<Map<Sequence, Double>, Double>(new HashMap<>(), value);
                    }
                    else{
                        startTime = threadBean.getCurrentThreadCpuTime();
                        addLeaderSequencesToLP(leaderSequences);
                        restrictedGameGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
                        leaderSequences = null;
                        return solve(lowerBound, upperBound);
                    }
                } else {
                    if (value <= lowerBound + eps && leaderSequences.isEmpty()) {
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

    protected Map<InformationSet, Map<Sequence, Double>> getFollowerBehavioralStrategy(LPData lpData, FollowerType type) {
        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();

        for (Map.Entry<Object, IloNumVar> entry : lpData.getWatchedPrimalVariables().entrySet()) {
            if (entry.getKey() instanceof Triplet) {
                Triplet varKey = (Triplet) entry.getKey();

                if (varKey.getThird() instanceof Sequence && varKey.getSecond() instanceof Sequence && varKey.getFirst() instanceof FollowerType && varKey.getFirst().equals(type)) {
//                    System.out.println("Extracting strategy");
                    Sequence playerSequence = (Sequence) varKey.getThird();
                    Map<Sequence, Double> isStrategy = strategy.get(playerSequence.getLastInformationSet());
                    Double currentValue = getValueFromCplex(lpData, entry);

                    if (currentValue > eps)
                        if (isSequenceFrom((Sequence) varKey.getSecond(), playerSequence.getLastInformationSet()))
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

    protected double getFollowerBehavioralStrategy(LPData lpData, Triplet varKey, Sequence playerSequence, Double currentValue) {
        double behavioralStrat = currentValue;

        if (!playerSequence.isEmpty()) {
            Sequence sequenceCopy = new ArrayListSequenceImpl(playerSequence);

            sequenceCopy.removeLast();
            double previousValue = getValueFromCplex(lpData, new Triplet<>(varKey.getFirst(), varKey.getSecond(), sequenceCopy));

            if (previousValue == 0) {
                Sequence opponentSequence = new ArrayListSequenceImpl((Sequence) (varKey.getSecond()));

                if (!opponentSequence.isEmpty()) {
                    opponentSequence.removeLast();
                    previousValue = getValueFromCplex(lpData, new Triplet<>(varKey.getFirst(), opponentSequence, sequenceCopy));
                }
            }
            behavioralStrat /= previousValue;
        }
//        System.out.println(playerSequence.toString() + "type = " + ((FollowerType)varKey.getThird()).getID() +  " : " + behavioralStrat);
        return behavioralStrat;
    }

    protected HashSet<Sequence> findLeaderDeviation(LPData lpData){
//        HashSet<Object> constraints = newlpData.getWatchedDualVariables().keySet();
        HashSet<Sequence> deviations = new HashSet<>();
        HashMap<Object, Double> duals = new HashMap<>();
        Sequence minSequence = null;
        double minCost = Double.POSITIVE_INFINITY;
        double maxCost = Double.NEGATIVE_INFINITY;
        boolean breakSequenceIteration = false;
        boolean breakTypeIteration = false;
        try {
            for (Object con : lpData.getWatchedDualVariables().keySet())
                duals.put(con, lpData.getSolver().getDual(lpData.getWatchedDualVariables().get(con)));
        }
        catch (Exception e){e.printStackTrace();}
        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)){
//            if(leaderSequence.isEmpty()) continue;
//            if(true){
            if(!leaderRG.contains(leaderSequence)){
                if (breakSequenceIteration) break;
                breakTypeIteration = false;
//                System.out.println(leaderSequence);
                for (FollowerType type : FlipItGameInfo.types) {
                    if (breakTypeIteration) break;
                    HashMap<Sequence, Double> costs = new HashMap<>();
                    for (Sequence followerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)) {
//                    if (lpTable.exists(createSeqPairVarKey(leaderSequence, followerSequence)) || lpData.getWatchedPrimalVariables().containsKey(createSeqPairVarKey(leaderSequence, followerSequence)))
//                        System.out.println("ERROR!");
//                    double reducedCost = 0.0;
                        Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
                        if (seqCombValue != null) {
                            double followerValue = seqCombValue[type.getID() + 1];
                            double leaderValue = seqCombValue[leader.getId()];

                            // objective
                            if (leaderValue != 0) {
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - leaderValue);
                            }

                            if (followerValue != 0) {
                                // 6:
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - followerValue * duals.get(new Pair(type, followerSequence)));
//                            reducedCost += -followerValue * duals.get(followerSequence);
//                            lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);

                                // 7:
                                if (followerSequence.getLastInformationSet() == null) continue;
                                for (GameState state : followerSequence.getLastInformationSet().getAllStates()) {
                                    Sequence stateLeaderSeq = state.getSequenceFor(leader);
//                            System.out.println(stateLeaderSeq);
                                    if (relevantForLeaderInDeviation.containsKey(stateLeaderSeq)) {
                                        for (Sequence stateFollowerSeq : relevantForLeaderInDeviation.get(stateLeaderSeq)) {
                                            if (stateFollowerSeq.size() > 0 && followerSequence.size() > 0 && !((SequenceInformationSet) stateFollowerSeq.getLastInformationSet()).getPlayersHistory().isPrefixOf(followerSequence.getSubSequence(followerSequence.size() - 1)))
                                                continue;
                                            Object eqKey = new Quadruple<>(type, followerSequence.getLastInformationSet(), followerSequence, stateFollowerSeq);
                                            if (CHECK_EXISTENCE && !lpTable.existsEqKey(eqKey)) continue;
                                            if (!costs.containsKey(stateFollowerSeq))
                                                costs.put(stateFollowerSeq, 0.0);
                                            costs.put(stateFollowerSeq, costs.get(stateFollowerSeq) - followerValue * duals.get(eqKey));
//                                        Object varKey = createSeqPairVarKey(leaderSequence, stateFollowerSeq);
//                                        lpTable.setConstraint(eqKey, varKey, -followerValue);
                                        }
                                    }
                                }
                            }
                        }
                    }
//                boolean TEST = true;
//                if(!TEST) {
                    if (!leaderSequence.isEmpty()) {
//                        Sequence leaderSubSeq = leaderSequence.getSubSequence(0, leaderSequence.size() - 1);
                        if (relevantForLeaderInP.containsKey(leaderSequence.getLastInformationSet()))//leaderSubSeq))
                            for (Sequence followerSequence : relevantForLeaderInP.get(leaderSequence.getLastInformationSet())) {//leaderSubSeq)) {
                                Object eqKey = new Triplet<>(type, (SequenceInformationSet) leaderSequence.getLastInformationSet(), followerSequence);
//                            lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSequence), -1.0);
//                            lpTable.setConstant(eqKey, 0.0);
//                            lpTable.setConstraintType(eqKey, 1);
                                if (!duals.containsKey(eqKey)) continue;
//                            System.out.println("eqkey found");
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - 1 * duals.get(eqKey));

                            }
                    }
                    if (costs.isEmpty()) continue;
                    double min = Collections.min(costs.values());
                    double max = Collections.max(costs.values());
                    if (max > maxCost) maxCost = max;
//                if ( max >  0.0 * -eps) {
                    if (min < 0.0 * eps) { // !algConfig.getCompatibleSequencesFor(leaderSequence).isEmpty() &&
                        if (!MAX) {
                            breakTypeIteration = true;
                            for (Sequence prefix : leaderSequence.getAllPrefixes())
                                if (!leaderRG.contains(prefix)) deviations.add(prefix);
                        } else {
                            if (min < minCost) {
                                minCost = min;
                                minSequence = leaderSequence;
                            }
                        }
                        if (GREEDY) {
                            System.out.println("Deviation: " + min);
                            breakSequenceIteration = true;
                        }
                    }
                }

            }
        }

        if (MAX && minSequence != null) {
            if (leaderRG.contains(minSequence)) System.out.println("ERROR: " + minSequence);
            System.out.println("Minimum deviation: " + maxCost);
            System.out.println("Maximum deviation: " + minCost);
            System.out.println("Deviation sequence: " + minSequence + "; #: " + minSequence.getLastInformationSet().hashCode());
            deviations.add(minSequence);
            for (Sequence prefix : minSequence.getAllPrefixes())
                if (!leaderRG.contains(prefix)) deviations.add(prefix);
        }

        return  deviations;

//        return new HashSet<>();
    }

    protected Map<Sequence, Double> getBrokenStrategyCauses(Map<InformationSet, Map<Sequence, Double>> strategy, LPData lpData) {
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

    protected double getLowerBound(double lowerBound, Pair<Map<Sequence, Double>, Double> currentBest) {
        return Math.max(lowerBound, currentBest.getRight());
    }

    protected void restrictFollowerPlay(Sequence brokenStrategyCause, LPData lpData, FollowerType type) {
        System.out.println(brokenStrategyCause + " of type " + type.getID() +  " fixed to zero");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Triplet) {
                if (((Triplet) varKey).getThird() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getFirst() instanceof FollowerType && ((Triplet) varKey).getFirst().equals(type)) {
                    Triplet<FollowerType, Sequence, Sequence> p = (Triplet<FollowerType, Sequence, Sequence>) varKey;

                    if (p.getThird().equals(brokenStrategyCause)) {
                        Pair<String, Triplet<FollowerType, Sequence, Sequence>> eqKey = new Pair<>("restr", p);

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
                if (((Triplet) varKey).getThird() instanceof Sequence && ((Triplet) varKey).getSecond() instanceof Sequence && ((Triplet) varKey).getFirst() instanceof FollowerType && ((Triplet) varKey).getFirst().equals(type)) {
                    Triplet<FollowerType, Sequence, Sequence> p = (Triplet<FollowerType, Sequence, Sequence>) varKey;

                    if (p.getThird().equals(brokenStrategyCause)) {
                        Pair<String, Triplet<FollowerType, Sequence, Sequence>> eqKey = new Pair<>("restr", p);

                        lpTable.removeFromConstraint(eqKey, p);
                        lpTable.setConstant(eqKey, 0.0);
                    }
                }
            }
        }
    }


//    protected Pair<Map<Sequence, Double>, Double> handleBrokenStrategyCause(double lowerBound, double upperBound, LPData lpData, double value, Iterable<Sequence> brokenStrategyCauses) {
//        Pair<Map<Sequence, Double>, Double> currentBest = dummyResult;
//
//        for (Sequence brokenStrategyCause : brokenStrategyCauses) {
//            restrictFollowerPlay(brokenStrategyCause, brokenStrategyCauses, lpData);
//            Pair<Map<Sequence, Double>, Double> result = solve(getLowerBound(lowerBound, currentBest), upperBound);
//
//            if (result.getRight() > currentBest.getRight()) {
//                currentBest = result;
//                if (currentBest.getRight() >= value - eps) {
//                    System.out.println("----------------currentBest " + currentBest.getRight() + " reached parent reward " + value + "----------------");
//                    return currentBest;
//                }
//            }
//            removeRestriction(brokenStrategyCause, brokenStrategyCauses, lpData);
//        }
//        return currentBest;
//    }
//
//    protected double getLowerBound(double lowerBound, Pair<Map<Sequence, Double>, Double> currentBest) {
//        return Math.max(lowerBound, currentBest.getRight());
//    }


    protected void restrictFollowerPlay(Sequence brokenStrategyCause, Iterable<Sequence> brokenStrategyCauses, LPData lpData) {
        System.out.println(brokenStrategyCause + " fixed to zero");
        for (Object varKey : lpData.getWatchedPrimalVariables().keySet()) {
            if (varKey instanceof Pair) {
                if (((Pair) varKey).getLeft() instanceof Sequence && ((Pair) varKey).getRight() instanceof Sequence) {
                    Pair<Sequence, Sequence> p = (Pair<Sequence, Sequence>) varKey;

                    if (p.getRight().equals(brokenStrategyCause)) {
                        if (!restrictions.containsKey(varKey)) restrictions.put(varKey, 0);
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr_"+restrictions.get(varKey), p);

//                        lpTable.setConstraint(eqKey, p, 1);
                        setNewConstraint(eqKey, p, 1);
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
                        int key = restrictions.get(varKey);
                        Pair<String, Pair<Sequence, Sequence>> eqKey = new Pair<>("restr_"+key, p);
                        restrictions.replace(varKey, key+1);

//                        lpTable.removeFromConstraint(eqKey, p);
                        removeFromConstraint(eqKey, p);
//                        lpTable.removeConstant(eqKey);
                        lpTable.setConstant(eqKey, 0.0);
                    }
                }
            }
        }
    }

    protected void removeFromConstraint(Object eqKey, Object varKey){

        if (lpTable instanceof ColumnGenerationLPTable)
            setNewConstraint(eqKey, new Pair("neg", varKey), -1);
        else{
            lpTable.removeFromConstraint(eqKey, varKey);
        }
    }

//    protected Map<InformationSet, Map<Sequence, Double>> getLeaderBehavioralStrategy(LPData lpData, Player player) {
//        Map<InformationSet, Map<Sequence, Double>> strategy = new HashMap<>();
////        Map<Pair<Sequence, Sequence>, Double> normalProcessedPairs = new HashMap<>();
////        Map<Pair<Sequence, Sequence>, Double> additionalProcessedPairs = new HashMap<>();
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
//                        if (isRelevantTo(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet())) {
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
////                            if (isSequenceFrom(player.equals(leader) ? (Sequence) varKey.getRight() : (Sequence) varKey.getLeft(), playerSequence.getLastInformationSet(), followerRealPlan)) {
////                                try {
////                                    normalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
////                                } catch (IloException e) {
////                                    e.printStackTrace();
////                                }
////                            } else {
////                                try {
////                                    additionalProcessedPairs.put(varKey, lpData.getSolver().getValue(entry.getValue()));
////                                } catch (IloException e) {
////                                    e.printStackTrace();
////                                }
////                            }
//                        }
//                }
//            }
//        }
//        return strategy;
//    }

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

    public int getBnbBranchingCount() {
        return bnbBranchingCount;
    }

    public int getGenerationCount() {
        return generationCount;
    }

    public long getDeviationIdentificationTime() {
        return deviationIdentificationTime;
    }

    public long getBrokenStrategyIdentificationTime() {
        return brokenStrategyIdentificationTime;
    }

    public long getRestrictedGameGenerationTime() {
        return restrictedGameGenerationTime;
    }

    @Override
    public Double getResultForPlayer(Player p) {
        return gameValue;
    }
}
