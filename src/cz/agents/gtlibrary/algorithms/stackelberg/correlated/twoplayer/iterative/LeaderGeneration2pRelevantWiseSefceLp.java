package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.ColumnGenerationLPTable;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 01/09/2017.
 */
public class LeaderGeneration2pRelevantWiseSefceLp extends CompleteTwoPlayerSefceLP {

    //    protected HashMap<Sequence,HashSet<Sequence>> relevantForFollower;
    protected HashMap<SequenceInformationSet,HashSet<Sequence>> relevantForLeaderInP;
    protected HashMap<SequenceInformationSet,HashSet<Sequence>> relevantForFollowerInP;
    protected HashMap<Sequence,HashSet<Sequence>> relevantForLeaderInDeviation;
    protected HashSet<Sequence> leaderRG;
    protected HashSet<Sequence> followerRG;

    protected final boolean DEBUG = false;
    protected final boolean DEBUG_COSTS = false;
    protected final boolean USE_ORIG_PCONT = false;

    protected final boolean DO = false;
    protected boolean GREEDY = false; // First / All
    protected boolean MAX = false; // Max / All

    protected final int seed = 0;
    protected final boolean HEURISTIC = false;

    protected final boolean CONVERT_TO_CANONIC = false;
    protected final boolean CHECK_EXISTENCE = false;
    protected final boolean USE_GENERATING_TABLE = false;
    protected boolean ADD_ALL_PREFIXES = false;
    protected final boolean EXPORT_LP = false;


    protected int initialSize;

    protected HashMap<Integer, HashMap<Object, Double>> dualsHistory;

    public LeaderGeneration2pRelevantWiseSefceLp(Player leader, GameInfo info) {
        super(leader, info);
        if (USE_GENERATING_TABLE) lpTable = new ColumnGenerationLPTable();
//        relevantForFollower = new HashMap<>();
        relevantForLeaderInP = new HashMap<>();
        relevantForFollowerInP =  new HashMap<>();
        relevantForLeaderInDeviation = new HashMap<>();
        leaderRG = new HashSet<>();
        followerRG = new HashSet<>();
    }

    public LeaderGeneration2pRelevantWiseSefceLp(Player leader, GameInfo info, boolean greedy, boolean max) {
        super(leader, info);
//        lpTable = new ConstraintGeneratingLPTable();
//        relevantForFollower = new HashMap<>();
        relevantForLeaderInP = new HashMap<>();
        relevantForLeaderInDeviation = new HashMap<>();
        leaderRG = new HashSet<>();
        followerRG = new HashSet<>();
        this.GREEDY = greedy;
        this.MAX = max;
    }

    public double getRestrictedGameRatio(){
//        System.out.println("Best: " + ((double)initialSize) / algConfig.getSequencesFor(leader).size());
//        System.out.println("Follower: " + ((double)followerRG.size()) / algConfig.getSequencesFor(follower).size());
//        return ((double)leaderRG.size()) / algConfig.getSequencesFor(leader).size();
        return (((double)leaderRG.size() + followerRG.size())/((StackelbergConfig) algConfig).getAllSequences().size());
    }

//    public double getBestRestrictedGameRatio(){
//        return ((double)leaderRG.size()) / algConfig.getSequencesFor(leader).size();
//    }

    protected void findInitialRGs(){
        leaderRG.add(new ArrayListSequenceImpl(leader));
//        leaderRG.addAll(algConfig.getSequencesFor(leader));
        followerRG.add(new ArrayListSequenceImpl(follower));
//        followerRG.addAll(algConfig.getSequencesFor(follower));

        if (DEBUG){
            leaderRG.addAll(algConfig.getSequencesFor(leader));
            followerRG.addAll(algConfig.getSequencesFor(follower));
            return;
        }

        ArrayList<GameState> stack = new ArrayList<>();

        stack.add(algConfig.getRootState().copy());

        while (stack.size() > 0) {
            GameState currentState = stack.remove(stack.size()-1);//removeFirst();

            if (currentState.isGameEnd()) {
//                pool.push(currentState);
                continue;
            }
            {
                if (currentState.getPlayerToMove().equals(leader)) {
//                        Action defaultAction = expander.getActions(currentState).get(0);
                    Sequence seq = algConfig.getInformationSetFor(currentState).getOutgoingSequences().iterator().next();
                    for (Sequence lseq : algConfig.getInformationSetFor(currentState).getOutgoingSequences()) {
                        if (leaderRG.contains(lseq)) {
                            seq = lseq;
                            break;
                        }
                    }
                        leaderRG.add(seq);
                        GameState poolState = currentState.performAction(seq.getLast());
                        stack.add(poolState);
//                        stack.add(currentState.performAction(defaultAction));

                } else {
//                    Sequence seq = algConfig.getInformationSetFor(currentState).getOutgoingSequences().iterator().next();
                    for (Sequence fseq : algConfig.getInformationSetFor(currentState).getOutgoingSequences()) {
                        GameState poolState = currentState.performAction(fseq.getLast());
                        stack.add(poolState);
                    }
                }
            }
//            pool.push(currentState);
        }

        stack = new ArrayList<>();

        stack.add(algConfig.getRootState().copy());

        while (stack.size() > 0) {
            GameState currentState = stack.remove(stack.size()-1);//removeFirst();

            if (currentState.isGameEnd()) {
//                pool.push(currentState);
                continue;
            }
            {
                if (currentState.getPlayerToMove().equals(leader)) {
//                        Action defaultAction = expander.getActions(currentState).get(0);
//                    Sequence seq = algConfig.getInformationSetFor(currentState).getOutgoingSequences().iterator().next();
                    for (Sequence lseq : algConfig.getInformationSetFor(currentState).getOutgoingSequences()) {
                        GameState poolState = currentState.performAction(lseq.getLast());
                        stack.add(poolState);
                    }
//                        stack.add(currentState.performAction(defaultAction));

                } else {
                    Sequence seq = algConfig.getInformationSetFor(currentState).getOutgoingSequences().iterator().next();
                    for (Sequence fseq : algConfig.getInformationSetFor(currentState).getOutgoingSequences()) {
                        if (leaderRG.contains(fseq)) {
                            seq = fseq;
                            break;
                        }
                    }
                    followerRG.add(seq);
                    GameState poolState = currentState.performAction(seq.getLast());
                    stack.add(poolState);
                }
            }
//            pool.push(currentState);
        }
    }

    protected void setNewConstraint(Object eqKey, Object varKey, double value){
        if (lpTable instanceof ColumnGenerationLPTable){
            ((ColumnGenerationLPTable)lpTable).setNewConstraint(eqKey, varKey, value);
        }
        else{
            lpTable.setConstraint(eqKey, varKey, value);
        }
    }

    protected void setNewObjective( Object varKey, double value){
        if (lpTable instanceof ColumnGenerationLPTable){
            ((ColumnGenerationLPTable)lpTable).setNewObjective(varKey, value);
        }
        else{
            lpTable.setObjective(varKey, value);
        }
    }

    protected void addLeaderSequencesToLP(HashSet<Sequence> leaderSequences, HashSet<Sequence> followerSequences){
        int numberOfNonExistant = 0;
        System.out.println("# of cons - before adding seqs: "  + lpTable.columnCount());
        boolean originalPContinuation = DEBUG && USE_ORIG_PCONT;
        for (Sequence leaderSequence : leaderSequences){
//            if (!leaderRG.containsKey(leaderSequence)) leaderRG.put(leaderSequence, new HashSet<>());
//            leaderRG.get(leaderSequence);
            // update objective, 6, 7 pouze pro sekvence mirici do listu
            for (Sequence followerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)){
//                leaderRG.get(leaderSequence).add(followerSequence);

                Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
                if (seqCombValue != null) {
                    double followerValue = seqCombValue[follower.getId()];
                    double leaderValue = seqCombValue[leader.getId()];

                    // objective
                    if (leaderValue != 0)
                        if (leaderSequences.contains(leaderSequence) || followerSequences.contains(followerSequence))
                            setNewObjective(createSeqPairVarKey(leaderSequence, followerSequence), leaderValue);

                    if (followerValue != 0) {
                        // 6:
                        if (leaderSequences.contains(leaderSequence) || followerSequences.contains(followerSequence))
                            setNewConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);

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
                                    Object eqKey = new Triplet<>(followerSequence.getLastInformationSet(), followerSequence, stateFollowerSeq);
                                    if (CHECK_EXISTENCE && !lpTable.existsEqKey(eqKey)){ numberOfNonExistant ++; continue;}
                                    Object varKey = createSeqPairVarKey(leaderSequence, stateFollowerSeq);
                                    if (leaderSequences.contains(leaderSequence) || followerSequences.contains(stateFollowerSeq))
                                        setNewConstraint(eqKey, varKey, -followerValue);
                                }
                            }
                        }
                    }
                }
            }

            if (!originalPContinuation){

                // 4 -> druha cast
                if (!leaderSequence.isEmpty()) {
                    Sequence leaderSubSeq = leaderSequence.getSubSequence(0, leaderSequence.size() - 1);
                    if (relevantForLeaderInP.containsKey(leaderSequence.getLastInformationSet()))//leaderSubSeq))
                        for (Sequence followerSequence : relevantForLeaderInP.get(leaderSequence.getLastInformationSet())){//leaderSubSeq)) {
                            Object eqKey = new Pair<SequenceInformationSet, Sequence>((SequenceInformationSet) leaderSequence.getLastInformationSet(), followerSequence);
                            if ((leaderSequences.contains(leaderSequence) || followerSequences.contains(followerSequence))){
                                setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSequence), -1.0);
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
                    if (relevantForLeaderInP.containsKey(set)){//set.getPlayersHistory())) {
                        for (Sequence followerSequence : relevantForLeaderInP.get(set)){//set.getPlayersHistory())) {
//                            System.out.println(leaderRG.size() + " / " + followerRG.size());
//                            if (followerSequences == null) System.out.println("0");
//                            if (leaderSequences == null) System.out.println("1");
//                            if (set.getPlayersHistory() == null) System.out.println("2");
//                            if (followerSequence == null) System.out.println("3");
                            if (leaderSequences.contains(set.getPlayersHistory()) || followerSequences.contains(followerSequence)) {
                                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, followerSequence);
                                setNewConstraint(eqKey, createSeqPairVarKey(set.getPlayersHistory(), followerSequence), 1.0);
                                lpTable.setConstant(eqKey, 0.0);
                                lpTable.setConstraintType(eqKey, 1);
                            }
                        }
                    }
                }
                // 5
                if (set.getPlayer().equals(follower) && !set.getOutgoingSequences().isEmpty()) {
                    if(relevantForFollowerInP.containsKey(set)){
                        for (Sequence leaderSequence : relevantForFollowerInP.get(set)){
//                            if (!leaderSequences.containsKey(leaderSequence)) continue;
//                    for (Sequence leaderSequence : leaderSequences) {
//                        if (relevantForFollowerInP.containsKey(leaderSequence) && relevantForFollowerInP.get(leaderSequence).contains(set.getPlayersHistory())) {
                            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
                            if (leaderSequences.contains(leaderSequence) || followerSequences.contains(set.getPlayersHistory()))
                                setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
                            for (Sequence outgoing : set.getOutgoingSequences())
                                if (leaderSequences.contains(leaderSequence) || followerSequences.contains(outgoing))
                                    setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoing), -1.0);
                            lpTable.setConstant(eqKey, 0.0);
                            lpTable.setConstraintType(eqKey, 1);
                        }
                    }
                }
            }
        }
        System.out.println("# of cons - after adding seqs: "  + lpTable.columnCount());
        System.out.println("# of non-existent: " + numberOfNonExistant);
//        leaderRG.putAll(leaderSequences);
    }

    protected void generateFollowerConstraints(){
        // generate relevant parts of cons 3,6,7,8

        // 3 :
        lpTable.setConstraint("initP", createSeqPairVarKey(new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower)), 1);
        lpTable.setConstant("initP", 1);
        lpTable.setConstraintType("initP", 1);

        // 6 :
        createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower));
        for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
            createSequenceConstraint(algConfig, followerSequence);
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
                            createISActionConstraint(algConfig, relevantSequence, informationSet);
                        }
                    }
                    createISActionConstraint(algConfig, new ArrayListSequenceImpl(follower), informationSet);
                }
            }
        }

        // 8 :
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(follower)) {
                for (Sequence sequence : informationSet.getOutgoingSequences()) {
                    Object eqKey = new Triplet<>(informationSet, sequence, "eq");
                    Object varKey = new Pair<>(informationSet, sequence);
                    Object contVarKey = new Pair<>("v", sequence);

                    if(CONVERT_TO_CANONIC){
                        Object vKey = new Pair<>("w", varKey);
                        Object contVKey = new Pair<>("w", sequence);
                        lpTable.setConstraint(eqKey, vKey, -1);
                        lpTable.setConstraint(eqKey, contVKey, 1);
                    }
                    else {
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

    @Override
    protected void createISActionConstraint(StackelbergConfig algConfig, Sequence followerSequence, SequenceInformationSet informationSet) {
        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            Object eqKey = new Triplet<>(informationSet, sequence, followerSequence);
            Object varKey = new Pair<>(informationSet, followerSequence);
            Object vKey;


//            System.out.println(eqKey);

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
                        varKey = new Pair<>(reachableSet, followerSequence);
                        lpTable.setConstraint(eqKey, varKey, -1);
                        if(CONVERT_TO_CANONIC){
                            vKey = new Pair<>("w", varKey);
                            lpTable.setConstraint(eqKey, vKey, 1);
                        }
                    }
                }
        }
    }

    @Override
    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence) {
        Object varKey = new Pair<>("v", followerSequence);

//        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(followerSequence, 1);
        if (CONVERT_TO_CANONIC){
            Object vKey = new Pair<>("w", followerSequence);
            lpTable.setConstraint(followerSequence, vKey, -1);
        }
        else{
            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        }
        lpTable.setConstraint(followerSequence, varKey, 1);
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
                Object contVarKey = new Pair<>("v", sequence);
                if (CONVERT_TO_CANONIC){
                    Object contVKey = new Pair<>("w", sequence);
                    lpTable.setConstraint(followerSequence, contVKey, 1);
                }
                else {
                    lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                }
                lpTable.setConstraint(followerSequence, contVarKey, -1);
            }
        }
    }




    protected Pair<HashSet<Sequence>, HashSet<Sequence>> findLeaderDeviation(LPData lpData){
//        HashSet<Object> constraints = newlpData.getWatchedDualVariables().keySet();
//        HashMap<Sequence, HashSet<Sequence>> deviations = new HashMap<>();
        HashSet<Sequence> newLeaderSeqs = new HashSet();
        HashSet<Sequence> newFollowerSeqs = new HashSet();
        HashMap<Object, Double> duals = new HashMap<>();
        Sequence minSequence = null;
        double minCost = Double.POSITIVE_INFINITY;
        double maxCost = Double.NEGATIVE_INFINITY;

        // NFl, NFf, Eu, dev, Eq, init
        Double[] maxDuals = new Double[6];
        Double[] minDuals = new Double[6];
        Arrays.fill(maxDuals, Double.NEGATIVE_INFINITY);
        Arrays.fill(minDuals, Double.POSITIVE_INFINITY);
        try {
            if (DEBUG_COSTS) {
                System.out.println();
                System.out.println("Duals: ");
            }
            for (Object con : lpData.getWatchedDualVariables().keySet()) {
                duals.put(con, lpData.getSolver().getDual(lpData.getWatchedDualVariables().get(con)));
                if (DEBUG_COSTS)// && Math.abs(duals.get(con)) > 0.001)
                    System.out.println(con + " : " + duals.get(con));
                if (DEBUG_COSTS){
                    int type = -1;
                    if (con instanceof Pair){
                        Pair<SequenceInformationSet, Sequence> p = (Pair<SequenceInformationSet, Sequence>)con;
                        if (p.getLeft().getPlayer().equals(leader)) type = 0;
                        else
                            type = 1;
                    }
                    if (con instanceof Sequence) type = 2;
                    if (con instanceof Triplet){
                        Triplet t = (Triplet)con;
                        if (t.getThird() instanceof String) type = 4;
                        else type = 3;
                    }
                    if (con instanceof String) type = 5;
                    if (duals.get(con) > maxDuals[type]) maxDuals[type] = duals.get(con);
                    if (duals.get(con) < minDuals[type]) minDuals[type] = duals.get(con);
                }
            }
            if (DEBUG_COSTS) {
                System.out.println("Max duals= " + Arrays.toString(maxDuals).replaceAll(",",",\t\t"));
                System.out.println("Min duals= " + Arrays.toString(minDuals).replaceAll(",",",\t\t"));
//                System.exit(0);
                System.out.println();
            }
        }
        catch (Exception e){e.printStackTrace();}
        HashMap<Sequence, HashMap<Sequence, ArrayList<Pair<Object,Double>>>> rcs = new HashMap<>();
        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)){
//            if(leaderSequence.isEmpty()) continue;
//            if(true){
            if(true){
//                System.out.println(leaderSequence);
                HashMap<Sequence, Double> costs = new HashMap<>();
                if (DEBUG_COSTS) rcs.put(leaderSequence, new HashMap<>());
                for (Sequence followerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)){
//                    if (DEBUG_COSTS) System.out.println("Against seq: " + followerSequence);
//                    if (lpTable.exists(createSeqPairVarKey(leaderSequence, followerSequence)) || lpData.getWatchedPrimalVariables().containsKey(createSeqPairVarKey(leaderSequence, followerSequence)))
//                        System.out.println("ERROR!");
//                    double reducedCost = 0.0;
                    Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
                    if (seqCombValue != null) {
                        double followerValue = seqCombValue[follower.getId()];
                        double leaderValue = seqCombValue[leader.getId()];

                        if (!leaderRG.contains(leaderSequence) || !followerRG.contains(followerSequence)) {
                            // objective
                            if (leaderValue != 0) {
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - leaderValue);
                                if (DEBUG_COSTS) {
                                    if (!rcs.get(leaderSequence).containsKey(followerSequence))
                                        rcs.get(leaderSequence).put(followerSequence, new ArrayList<>());
                                    rcs.get(leaderSequence).get(followerSequence).add(new Pair<>("crit", -leaderValue));
//                                System.out.println("Against: " + followerSequence + ", crit: -lutil");
                                }
                            }

                            if (followerValue != 0) {
                                // 6:
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - followerValue * duals.get(followerSequence));
                                if (DEBUG_COSTS) {
                                    if (!rcs.get(leaderSequence).containsKey(followerSequence))
                                        rcs.get(leaderSequence).put(followerSequence, new ArrayList<>());
                                    rcs.get(leaderSequence).get(followerSequence).add(new Pair<>(followerSequence, -followerValue));
//                                System.out.println("Against: " + followerSequence + ", dual: " + followerSequence + ", -futil");
                                }
//                            reducedCost += -followerValue * duals.get(followerSequence);
//                            lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);
                            }
                        }

                        if (followerValue != 0) {

                            // 7:
                            if (followerSequence.getLastInformationSet() == null) continue;
                            for (GameState state : followerSequence.getLastInformationSet().getAllStates()){
                                Sequence stateLeaderSeq = state.getSequenceFor(leader);
//                            System.out.println(stateLeaderSeq);
                                if (relevantForLeaderInDeviation.containsKey(stateLeaderSeq)) {
                                    for (Sequence stateFollowerSeq : relevantForLeaderInDeviation.get(stateLeaderSeq)) {
                                        if (stateFollowerSeq.size() > 0 && followerSequence.size() > 0 && !((SequenceInformationSet)stateFollowerSeq.getLastInformationSet()).getPlayersHistory().isPrefixOf(followerSequence.getSubSequence(followerSequence.size()-1)))
                                            continue;
                                        Object eqKey = new Triplet<>(followerSequence.getLastInformationSet(), followerSequence, stateFollowerSeq);
                                        if(CHECK_EXISTENCE && !lpTable.existsEqKey(eqKey)) continue;
                                        if (!leaderRG.contains(leaderSequence) || !followerRG.contains(stateFollowerSeq)) {
                                            if (!costs.containsKey(stateFollowerSeq))
                                                costs.put(stateFollowerSeq, 0.0);
                                            costs.put(stateFollowerSeq, costs.get(stateFollowerSeq) - followerValue * duals.get(eqKey));
                                        }
                                        if (DEBUG_COSTS) {
                                            if (!rcs.get(leaderSequence).containsKey(stateFollowerSeq)) rcs.get(leaderSequence).put(stateFollowerSeq, new ArrayList<>());
                                            rcs.get(leaderSequence).get(stateFollowerSeq).add(new Pair<>(eqKey, -followerValue));
//                                            System.out.println("Against: " + stateFollowerSeq + ", dual: " + eqKey + ", -futil");
                                        }
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
                            Object eqKey = new Pair<SequenceInformationSet, Sequence>((SequenceInformationSet) leaderSequence.getLastInformationSet(), followerSequence);
//                            lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSequence), -1.0);
//                            lpTable.setConstant(eqKey, 0.0);
//                            lpTable.setConstraintType(eqKey, 1);
                            if (DEBUG_COSTS) {
                                if (!rcs.get(leaderSequence).containsKey(followerSequence)) rcs.get(leaderSequence).put(followerSequence, new ArrayList<>());
                                rcs.get(leaderSequence).get(followerSequence).add(new Pair<>(eqKey, -1.0));
//                                    System.out.println("Against: " + followerSequence + ", dual: " + eqKey + ", -1");
                            }
                            if (!duals.containsKey(eqKey)) continue;
//                            System.out.println("eqkey found");
                            if (!leaderRG.contains(leaderSequence) || !followerRG.contains(followerSequence)) {
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - 1 * duals.get(eqKey));
                            }

                        }
                }

                if (!ADD_ALL_PREFIXES) {
                    for (SequenceInformationSet set : algConfig.getReachableSets(leaderSequence))
                        if (relevantForLeaderInP.containsKey(set)) {//set.getPlayersHistory())) {
                            for (Sequence followerSequence : relevantForLeaderInP.get(set)) {//set.getPlayersHistory())) {
                                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, followerSequence);
                                if (!duals.containsKey(eqKey)) continue;
//                                if(!leaderRG.contains(leaderSequence)) System.exit(0);
                                if (!leaderRG.contains(leaderSequence) || !followerRG.contains(followerSequence)) {
                                    if (!costs.containsKey(followerSequence))
                                        costs.put(followerSequence, 0.0);
                                    costs.put(followerSequence, costs.get(followerSequence) + 1 * duals.get(eqKey));
                                }
                            }
                        }
                }


                //In use only for tests on existing variables
                for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
                    // 4 -> prvni cast
//                    if (set.getPlayer().equals(leader) && !set.getOutgoingSequences().isEmpty() && leaderSequence.equals(set.getPlayersHistory())) {
//                        if (relevantForLeaderInP.containsKey(set)){//set.getPlayersHistory())) {
//                            for (Sequence followerSequence : relevantForLeaderInP.get(set)){//set.getPlayersHistory())) {
//                                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, followerSequence);
//                                if (!duals.containsKey(eqKey)) continue;
//                                if(!leaderRG.contains(leaderSequence)) System.exit(0);
//                                if (!costs.containsKey(followerSequence))
//                                    costs.put(followerSequence, 0.0);
//                                costs.put(followerSequence, costs.get(followerSequence) + 1 * duals.get(eqKey));
////                                lpTable.setConstraint(eqKey, createSeqPairVarKey(set.getPlayersHistory(), followerSequence), 1.0);
////                                lpTable.setConstant(eqKey, 0.0);
////                                lpTable.setConstraintType(eqKey, 1);
//                            }
//                        }
//                    }
                    // 5
                    if (set.getPlayer().equals(follower) && !set.getOutgoingSequences().isEmpty()) {
                        if(relevantForFollowerInP.containsKey(set)){
                            if (relevantForFollowerInP.get(set).contains(leaderSequence)){
//                                if (!leaderSequences.contains(leaderSequence)) continue;
//                    for (Sequence leaderSequence : leaderSequences) {
//                        if (relevantForFollowerInP.containsKey(leaderSequence) && relevantForFollowerInP.get(leaderSequence).contains(set.getPlayersHistory())) {
                                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
                                if (!duals.containsKey(eqKey)) continue;
//                                if(!leaderRG.contains(leaderSequence)) System.exit(0);
                                if (!leaderRG.contains(leaderSequence) || !followerRG.contains(set.getPlayersHistory())) {
                                    if (!costs.containsKey(set.getPlayersHistory()))
                                        costs.put(set.getPlayersHistory(), 0.0);
                                    costs.put(set.getPlayersHistory(), costs.get(set.getPlayersHistory()) + 1 * duals.get(eqKey));
                                }
//                                lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
                                for (Sequence outgoing : set.getOutgoingSequences()) {
//                                    lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoing), -1.0);
                                    if (!leaderRG.contains(leaderSequence) || !followerRG.contains(outgoing)) {
                                        if (!costs.containsKey(outgoing))
                                            costs.put(outgoing, 0.0);
                                        costs.put(outgoing, costs.get(outgoing) - 1 * duals.get(eqKey));
                                    }
                                }
//                                lpTable.setConstant(eqKey, 0.0);
//                                lpTable.setConstraintType(eqKey, 1);
                            }
                        }
                    }
                }


//                }

//                for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
//                    // 4 -> prvni cast
//                    if (set.getPlayer().equals(leader) && !set.getOutgoingSequences().isEmpty() && leaderSequence.equals(set.getPlayersHistory())) {
//                        if (relevantForLeaderInP.containsKey(set.getPlayersHistory())) {
//                            for (Sequence followerSequence : relevantForLeaderInP.get(set.getPlayersHistory())) {
//                                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, followerSequence);
//                                if (!duals.containsKey(eqKey)) continue;
//                                System.out.println("eqkey found");
////                                lpTable.setConstraint(eqKey, createSeqPairVarKey(set.getPlayersHistory(), followerSequence), 1.0);
////                                lpTable.setConstant(eqKey, 0.0);
////                                lpTable.setConstraintType(eqKey, 1);
//                                if (!costs.containsKey(followerSequence))
//                                    costs.put(followerSequence, 0.0);
//                                costs.put(followerSequence, costs.get(followerSequence) + 1 * duals.get(eqKey));
//                            }
//                        }
//                    }
//                    // 5
//                    if (set.getPlayer().equals(follower) && !set.getOutgoingSequences().isEmpty()) {
////                        for (Sequence leaderSequence : leaderSequences) {
//                            if (relevantForLeaderInP.containsKey(leaderSequence) && relevantForLeaderInP.get(leaderSequence).contains(set.getPlayersHistory())) {
//                                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
//                                if (!duals.containsKey(eqKey)) continue;
////                                lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
//                                if (!costs.containsKey(set.getPlayersHistory()))
//                                    costs.put(set.getPlayersHistory(), 0.0);
//                                costs.put(set.getPlayersHistory(), costs.get(set.getPlayersHistory()) + 1 * duals.get(eqKey));
//                                for (Sequence outgoing : set.getOutgoingSequences()){
//                                    if (!costs.containsKey(outgoing))
//                                        costs.put(outgoing, 0.0);
//                                    costs.put(outgoing, costs.get(outgoing) - 1 * duals.get(eqKey));
//                                }
////                                    lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoing), -1.0);
////                                lpTable.setConstant(eqKey, 0.0);
////                                lpTable.setConstraintType(eqKey, 1);
//                            }
////                        }
//                    }
//                }
                if (costs.isEmpty()) continue;
                for (Sequence fseq : costs.keySet())
                    if (costs.get(fseq) < -eps) {
                        leaderRG.add(leaderSequence);
                        newLeaderSeqs.add(leaderSequence);

                        for (Sequence seq : leaderSequence.getAllPrefixes()){
                            if (!leaderRG.contains(seq)){
                                leaderRG.add(seq);
                                newLeaderSeqs.add(seq);
                            }
                        }
//                        leaderRG.addAll(leaderSequence.getAllPrefixes());
//                        newLeaderSeqs.addAll(leaderSequence.getAllPrefixes());
//                        if (!deviations.containsKey(leaderSequence))
//                            deviations.put(leaderSequence, new HashSet<>());
                        {
                            if (ADD_ALL_PREFIXES) {
                                for (Sequence seq : fseq.getAllPrefixes())
                                    if (!followerRG.contains(seq)){
                                        followerRG.add(seq);
                                        newFollowerSeqs.add(seq);
                                    }
//                                followerRG.addAll(fseq.getAllPrefixes());
//                                newFollowerSeqs.addAll(fseq.getAllPrefixes());
                            } else {
                                followerRG.add(fseq);
                                newFollowerSeqs.add(fseq);
                            }
                        }
                    }
//                double min = Collections.min(costs.values());
//                double max = Collections.max(costs.values());
//                if (DEBUG_COSTS && (Math.abs(min) > 0 || Math.abs(max) > 0)) {
//                    System.out.println();
//                    System.out.println("Leader sequence: " + leaderSequence);
//                    System.out.println("Corresponding follower sequences and RCs: ");
//                    for (Sequence seq : costs.keySet()) {
//                        System.out.println(seq);
//                        for (Pair p : rcs.get(leaderSequence).get(seq))
//                            System.out.println("\t"+p);
//                        if (Math.abs(costs.get(seq)) > 0) System.out.println(seq + " : " + costs.get(seq));
//                    }
//                }
//                if (max > maxCost) maxCost = max;
////                if ( max >  0.0 * -eps) {
//                if ( min <   0.0 * -eps) { // !algConfig.getCompatibleSequencesFor(leaderSequence).isEmpty() &&
//                    if (!MAX) {
//                        if (ADD_ALL_PREFIXES) {
//                            for (Sequence prefix : leaderSequence.getAllPrefixes())
//                                if (!leaderRG.contains(prefix)) deviations.add(prefix);
//                        }
//                        else{
//                            deviations.add(leaderSequence);
//                        }
//                    }
//                    else{
//                        if (min < minCost){
//                            minCost = min;
//                            minSequence = leaderSequence;
//                        }
//                    }
//                    if (GREEDY) {
//                        System.out.println("Deviation: "+ min );
//                        break;
//                    }
//                }

            }
        }

//        if (MAX && minSequence != null) {
//            if (leaderRG.contains(minSequence)) System.out.println("ERROR: " + minSequence);
//            System.out.println("Minimum deviation: " + maxCost);
//            System.out.println("Maximum deviation: " + minCost);
//            System.out.println("Deviation sequence: " + minSequence + "; #: " + minSequence.getLastInformationSet().hashCode());
//            deviations.add(minSequence);
//            if (ADD_ALL_PREFIXES) {
//                for (Sequence prefix : minSequence.getAllPrefixes())
//                    if (!leaderRG.contains(prefix)) deviations.add(prefix);
//            }
//        }

//        if (DEBUG_COSTS) System.out.println("Deviations: " + deviations.toString());
//        System.out.println(deviations);
        return  new Pair<HashSet<Sequence>, HashSet<Sequence>>(newLeaderSeqs, newFollowerSeqs);
//        return new HashSet<>();
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfig = (StackelbergConfig) algConfig;
        this.expander = (Expander<SequenceInformationSet>)expander;
//        followerBestResponse = new FollowerBestResponse(this.algConfig.getRootState(), expander, this.algConfig, leader, follower);
        long startTime = threadBean.getCurrentThreadCpuTime();

        // construct initial LP
        generateFollowerConstraints();

        // find relevant sequences
        generateRelevantSequences();

//        System.out.println("////////////////////////////////////");
//        System.out.println("          RELEVANT SEQUENCES");
//        System.out.println("////////////////////////////////////");
//        for (Sequence seq : relevantForLeaderInP.keySet()){
//            System.out.println(seq);
//            for (Sequence s : relevantForLeaderInP.get(seq))
//                System.out.println("    " + s);
//        }
//        System.out.println("////////////////////////////////////");


        Pair<HashSet<Sequence>, HashSet<Sequence>> newSequences;
        findInitialRGs();
        addLeaderSequencesToLP(leaderRG, followerRG);

//        System.exit(0);

//        ((ConstraintGeneratingLPTable)lpTable).watchAllDualVariables();

        boolean updated = true;
        LPData lpData = null;
//        double value = 0.0;
        int iteration = 0;
        while(updated){
            // solve LP
            try {
                iteration++;
                System.out.println("-----------------------");
                System.out.println("Iteration "+iteration);
//                System.out.println("RG: " + leaderRG.toString());
                System.out.printf("Watching...");
                lpTable.watchAllDualVariables();
                System.out.println("done.");
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Generating cplex...");
                lpData = lpTable.toCplex();
                System.out.println("done.");
                overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
                if (EXPORT_LP) lpData.getSolver().exportModel("Iter2pSEFCE_" +iteration + ".lp");
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
            System.out.printf("Finding deviations...");
            newSequences = findLeaderDeviation(lpData);
            System.out.println("done.");
            // update LP
            if (newSequences.getLeft().isEmpty() && newSequences.getRight().isEmpty())
                updated = false;
            else {
                System.out.printf("Adding new sequences...");
                addLeaderSequencesToLP(newSequences.getLeft(), newSequences.getRight());
                System.out.println("done.");
            }
            System.out.println("RG size: " +(leaderRG.size() + followerRG.size())+ "/"+((StackelbergConfig) algConfig).getAllSequences().size());
        }

        finalLpSize = lpData.getSolver().getNrows();


//        addObjective();
//        createSequenceConstraints();
//        createISActionConstraints();

//        System.exit(0);

//        System.out.println("LP build...");
//        lpTable.watchAllPrimalVariables();
        overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//        Pair<Map<Sequence, Double>, Double> result = solve();

//        resultStrategies.put(leader, result.getLeft());
//        resultValues.put(leader, result.getRight());
//        return result.getRight();
        return gameValue;
    }

    @Override
    public String getInfo() {
        return "Iterative two-player SEFCE LP.";
    }

    protected void generateRelevantSequences() {
        if (DEBUG && USE_ORIG_PCONT) return;
        createInitPConstraint();
        Set<Object> blackList = new HashSet<>();
        Set<Pair<Sequence, Sequence>> pStops = new HashSet<>();

        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            List<Action> actions = expander.getActions(informationSet);
            Player opponent = info.getOpponent(informationSet.getPlayer());

            for (GameState gameState : informationSet.getAllStates()) {
                if (!gameState.isGameEnd())
                    generateRelevantSequence(actions, opponent, gameState, blackList, pStops);
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

//                        lpTable.setConstraintType(eqKeyFollower, 1);
//                        lpTable.setConstraint(eqKeyFollower, varKey, -1);
                        // Leader fixed, follower continuation
//                        if (!relevantForFollowerInP.containsKey(varKey.getLeft())) relevantForFollowerInP.put(varKey.getLeft(), new HashSet<>());
//                        relevantForFollowerInP.get(varKey.getLeft()).add(varKey.getRight());
                        if (!relevantForFollowerInP.containsKey((SequenceInformationSet) action.getInformationSet())) relevantForFollowerInP.put((SequenceInformationSet) action.getInformationSet(), new HashSet<>());
                        relevantForFollowerInP.get((SequenceInformationSet) action.getInformationSet()).add(varKey.getLeft());

//                        for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
//                            lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, followerSequence), 1);
//                        }
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

//                            lpTable.setConstraintType(eqKeyLeader, 1);
//                            lpTable.setConstraint(eqKeyLeader, varKey, -1);
                            // Follower fixed, leader continuation
//                            if (!relevantForLeaderInP.containsKey(varKey.getLeft())) relevantForLeaderInP.put(varKey.getLeft(), new HashSet<>());
//                            relevantForLeaderInP.get(varKey.getLeft()).add(varKey.getRight());
                            if (!relevantForLeaderInP.containsKey((SequenceInformationSet) leaderAction.getInformationSet())) relevantForLeaderInP.put((SequenceInformationSet) leaderAction.getInformationSet(), new HashSet<>());
                            relevantForLeaderInP.get((SequenceInformationSet) leaderAction.getInformationSet()).add(varKey.getRight());


//                            for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
//                                lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderContinuation, actionHistory), 1);
//                            }
                        }

                        for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            Object eqKeyLeaderCont = new Triplet<>(leaderHistory, followerSequence, leaderAction.getInformationSet());

                            if (!blackList.contains(eqKeyLeaderCont)) {
                                blackList.add(eqKeyLeaderCont);
                                Pair<Sequence, Sequence> varKeyCont = createSeqPairVarKey(leaderHistory, followerSequence);

//                                lpTable.setConstraintType(eqKeyLeaderCont, 1);
//                                lpTable.setConstraint(eqKeyLeaderCont, varKeyCont, -1);
                                // Follower fixed, leader continuation
//                                if (!relevantForLeaderInP.containsKey(varKeyCont.getLeft())) relevantForLeaderInP.put(varKeyCont.getLeft(), new HashSet<>());
//                                relevantForLeaderInP.get(varKeyCont.getLeft()).add(varKeyCont.getRight());
                                if (!relevantForLeaderInP.containsKey((SequenceInformationSet) leaderAction.getInformationSet())) relevantForLeaderInP.put((SequenceInformationSet) leaderAction.getInformationSet(), new HashSet<>());
                                relevantForLeaderInP.get((SequenceInformationSet) leaderAction.getInformationSet()).add(varKeyCont.getRight());

//                                for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
//                                    lpTable.setConstraint(eqKeyLeaderCont, createSeqPairVarKey(leaderContinuation, followerSequence), 1);
//                                }
                            }
                        }
                    }
                }
            }
        }
//        for (Sequence followerSequence : relevantForFollower.keySet())
//            for (Sequence leaderSequence : relevantForFollower.get(followerSequence)){
//                if (!relevantForLeader.containsKey(leaderSequence)) relevantForLeader.put(leaderSequence, new HashSet<>());
//                relevantForLeader.get(leaderSequence).add(followerSequence);
//            }
//        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)) {
//            if (!relevantForLeaderInP.containsKey(leaderSequence)) relevantForLeaderInP.put(leaderSequence, new HashSet<>());
//            relevantForLeaderInP.get(leaderSequence).add(new ArrayListSequenceImpl(follower));
//        }
    }

    protected void generateRelevantSequence(List<Action> actions, Player opponent, GameState gameState, Set<Object> blackList, Set<Pair<Sequence, Sequence>> pStops) {
        Triplet<Sequence, Sequence, InformationSet> eqKey = new Triplet<Sequence, Sequence, InformationSet>(gameState.getSequenceFor(leader), gameState.getSequenceFor(follower), algConfig.getInformationSetFor(gameState));

        if (blackList.contains(eqKey))
            return;
        blackList.add(eqKey);
        Pair<Sequence, Sequence> varKey = createSeqPairVarKey(gameState);

//        pStops.add(varKey);

//        System.out.println("tu");

//        if(gameState.getPlayerToMove().equals(leader)) {
//            if (!relevantForLeaderInP.containsKey(varKey.getLeft()))
//                relevantForLeaderInP.put(varKey.getLeft(), new HashSet<>());
//            relevantForLeaderInP.get(varKey.getLeft()).add(varKey.getRight());
//        }
//        else{
//            if (!relevantForFollowerInP.containsKey(varKey.getLeft()))
//                relevantForFollowerInP.put(varKey.getLeft(), new HashSet<>());
//            relevantForFollowerInP.get(varKey.getLeft()).add(varKey.getRight());
//        }

        if(gameState.getPlayerToMove().equals(leader)) {
            if (!relevantForLeaderInP.containsKey(algConfig.getInformationSetFor(gameState)))
                relevantForLeaderInP.put(algConfig.getInformationSetFor(gameState), new HashSet<>());
            relevantForLeaderInP.get(algConfig.getInformationSetFor(gameState)).add(varKey.getRight());
        }
        else{
            if (!relevantForFollowerInP.containsKey(algConfig.getInformationSetFor(gameState)))
                relevantForFollowerInP.put(algConfig.getInformationSetFor(gameState), new HashSet<>());
            relevantForFollowerInP.get(algConfig.getInformationSetFor(gameState)).add(varKey.getLeft());
        }

//        lpTable.setConstraint(eqKey, varKey, -1);
//        lpTable.setConstraintType(eqKey, 1);
//        for (Action action : actions) {
//            Sequence sequenceCopy = new ArrayListSequenceImpl(gameState.getSequenceForPlayerToMove());
//
//            sequenceCopy.addLast(action);
//            Pair<Sequence, Sequence> contVarKey = createSeqPairVarKey(sequenceCopy, gameState.getSequenceFor(opponent));
//
//            pStops.add(contVarKey);
//            lpTable.setConstraint(eqKey, contVarKey, 1);
//        }
    }

}
