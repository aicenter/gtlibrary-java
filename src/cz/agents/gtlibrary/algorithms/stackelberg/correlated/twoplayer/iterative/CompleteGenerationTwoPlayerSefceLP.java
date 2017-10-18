package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.domain.flipit.FlipItExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
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
public class CompleteGenerationTwoPlayerSefceLP extends CompleteTwoPlayerSefceLP {

    //    protected HashMap<Sequence,HashSet<Sequence>> relevantForFollower;
    protected HashMap<SequenceInformationSet,HashSet<Sequence>> relevantForLeaderInP;
    protected HashMap<SequenceInformationSet,HashSet<Sequence>> relevantForFollowerInP;
    protected HashMap<Sequence,HashSet<Sequence>> relevantForLeaderInDeviation;
    protected HashSet<Sequence> leaderRG;
    protected HashSet<Sequence> followerRG;

    protected final boolean DEBUG = false;
    protected final boolean USE_ORIG_PCONT = false;

    protected final boolean DO = false;
    protected boolean GREEDY = false; // First / All
    protected boolean MAX = false; // Max / All

    protected int initialSize;

    public CompleteGenerationTwoPlayerSefceLP(Player leader, GameInfo info) {
        super(leader, info);
//        lpTable = new ConstraintGeneratingLPTable();
//        relevantForFollower = new HashMap<>();
        relevantForLeaderInP = new HashMap<>();
        relevantForFollowerInP =  new HashMap<>();
        relevantForLeaderInDeviation = new HashMap<>();
        leaderRG = new HashSet<>();
        followerRG = new HashSet<>();
    }

    public CompleteGenerationTwoPlayerSefceLP(Player leader, GameInfo info, boolean greedy, boolean max) {
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
        System.out.println("Best: " + ((double)initialSize) / algConfig.getSequencesFor(leader).size());
        return ((double)leaderRG.size()*followerRG.size()) / algConfig.getAllSequences().size();
    }

//    public double getBestRestrictedGameRatio(){
//        return ((double)leaderRG.size()) / algConfig.getSequencesFor(leader).size();
//    }

    protected Pair<HashSet<Sequence>, HashSet<Sequence>> findInitialRG(){

        if (DEBUG) return new Pair<>(new HashSet(algConfig.getSequencesFor(leader)), new HashSet(algConfig.getSequencesFor(follower)));

        HashSet<Sequence> initialLeaderSequences = new HashSet<>();
        HashSet<Sequence> initialFollowerSequences = new HashSet<>();

        if (DO){
//            Constructor constructor = null;
//            try {
//                constructor = expander.getClass().getConstructor(DoubleOracleConfig.class, AlgorithmConfig.class);
//            } catch (NoSuchMethodException e) {
//                e.printStackTrace();
//            }
            DoubleOracleConfig<DoubleOracleInformationSet> config = new DoubleOracleConfig<DoubleOracleInformationSet>(algConfig.getRootState(), info);
            Expander<DoubleOracleInformationSet> exp = null;
            if (expander instanceof RandomGameExpander) exp = new RandomGameExpander<DoubleOracleInformationSet>(config);
            if (expander instanceof cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander) exp = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(config);
            if (expander instanceof FlipItExpander) exp = new FlipItExpander<DoubleOracleInformationSet>(config);
//            try {
//                exp = constructor.newInstance(config);
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
            GeneralDoubleOracle doefg = new GeneralDoubleOracle(algConfig.getRootState(), exp, info, config);
            Map<Player, Map<Sequence, Double>> rps = doefg.generate(null);
            for (Sequence seq : rps.get(leader).keySet())
                initialLeaderSequences.add(seq);
        }
        else {
            double maxUtility = Double.NEGATIVE_INFINITY;
            GameState maxState = null;
            for (GameState leaf : algConfig.getAllLeafs())
                if (leaf.getUtilities()[leader.getId()] > maxUtility) {
                    maxUtility = leaf.getUtilities()[leader.getId()];
                    maxState = leaf;
                }
//        initialSequences.add(maxState.getSequenceFor(leader));
            initialLeaderSequences.addAll(maxState.getSequenceFor(leader).getAllPrefixes());
            initialFollowerSequences.addAll(maxState.getSequenceFor(follower).getAllPrefixes());
        }
//        System.out.println("Best sequence = "+maxState.getSequenceFor(leader));
//        for (Sequence seq : initialSequences)
//            System.out.println(seq);
        HashMap<SequenceInformationSet, Action> leaderStrategy = new HashMap<>();
        for (Sequence seq : initialLeaderSequences)
            for (Action a : seq)
                leaderStrategy.put((SequenceInformationSet) a.getInformationSet(), a);
        HashMap<SequenceInformationSet, Action> followerStrategy = new HashMap<>();
        for (Sequence seq : initialFollowerSequences)
            for (Action a : seq)
                followerStrategy.put((SequenceInformationSet) a.getInformationSet(), a);
        for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
            if (set.getPlayer().equals(leader)) {
                if (leaderStrategy.containsKey(set) || set.getOutgoingSequences().isEmpty()) continue;
                Sequence leaderSeq = set.getPlayersHistory();
                boolean reachable = true;
                for (Action a : leaderSeq) {
                    if (!leaderStrategy.containsKey((SequenceInformationSet) a.getInformationSet()))
                        break;
                    if (!leaderStrategy.get((SequenceInformationSet) a.getInformationSet()).equals(a)) {
                        reachable = false;
                        break;
                    }
                }
                if (reachable) {
                    leaderSeq = set.getOutgoingSequences().iterator().next();
                    initialLeaderSequences.addAll(leaderSeq.getAllPrefixes());
                    for (Action a : leaderSeq)
                        leaderStrategy.put((SequenceInformationSet) a.getInformationSet(), a);
                }
            }
            if (set.getPlayer().equals(follower)) {
                if (followerStrategy.containsKey(set) || set.getOutgoingSequences().isEmpty()) continue;
                Sequence leaderSeq = set.getPlayersHistory();
                boolean reachable = true;
                for (Action a : leaderSeq) {
                    if (!followerStrategy.containsKey((SequenceInformationSet) a.getInformationSet()))
                        break;
                    if (!followerStrategy.get((SequenceInformationSet) a.getInformationSet()).equals(a)) {
                        reachable = false;
                        break;
                    }
                }
                if (reachable) {
                    leaderSeq = set.getOutgoingSequences().iterator().next();
                    initialFollowerSequences.addAll(leaderSeq.getAllPrefixes());
                    for (Action a : leaderSeq)
                        followerStrategy.put((SequenceInformationSet) a.getInformationSet(), a);
                }
            }
        }
        initialSize = initialLeaderSequences.size();
        System.out.println("Initial RG size = " + initialLeaderSequences.size() + "/" + algConfig.getSequencesFor(leader).size() +
                "; " + initialFollowerSequences.size() + "/" + algConfig.getSequencesFor(follower));
        return new Pair<>(initialLeaderSequences, initialFollowerSequences);
    }

    protected void addLeaderSequencesToLP(HashSet<Sequence> leaderSequences){
        boolean originalPContinuation = DEBUG && USE_ORIG_PCONT;
        for (Sequence leaderSequence : leaderSequences){
            leaderRG.add(leaderSequence);
            // update objective, 6, 7 pouze pro sekvence mirici do listu
            for (Sequence followerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)){
                if (!followerRG.contains(followerSequence)) continue;

                Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
                if (seqCombValue != null) {
                    double followerValue = seqCombValue[follower.getId()];
                    double leaderValue = seqCombValue[leader.getId()];

                    // objective
                    if (leaderValue != 0)
                        lpTable.setObjective(createSeqPairVarKey(leaderSequence, followerSequence), leaderValue);

                    if (followerValue != 0) {
                        // 6:
                        lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);

                        // 7:
                        if(followerSequence.getLastInformationSet() == null) continue;
                        for (GameState state : followerSequence.getLastInformationSet().getAllStates()){
                            Sequence stateLeaderSeq = state.getSequenceFor(leader);
//                            System.out.println(stateLeaderSeq);
                            if (relevantForLeaderInDeviation.containsKey(stateLeaderSeq)) {
                                for (Sequence stateFollowerSeq : relevantForLeaderInDeviation.get(stateLeaderSeq)) {
                                    if (!followerRG.contains(stateFollowerSeq)) continue;
                                    if (stateFollowerSeq.size() > 0 && followerSequence.size() > 0 && !((SequenceInformationSet)stateFollowerSeq.getLastInformationSet()).getPlayersHistory().isPrefixOf(followerSequence.getSubSequence(followerSequence.size()-1)))
                                        continue;
                                    Object eqKey = new Triplet<>(followerSequence.getLastInformationSet(), followerSequence, stateFollowerSeq);
                                    Object varKey = createSeqPairVarKey(leaderSequence, stateFollowerSeq);
                                    lpTable.setConstraint(eqKey, varKey, -followerValue);
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
                            lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSequence), -1.0);
                            lpTable.setConstant(eqKey, 0.0);
                            lpTable.setConstraintType(eqKey, 1);
                        }
                }
            }
        }
        if (originalPContinuation)
            createPContinuationConstraints();
        else {
            for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
                // 4 -> prvni cast
                if (set.getPlayer().equals(leader) && !set.getOutgoingSequences().isEmpty() && leaderSequences.contains(set.getPlayersHistory())) {
                    if (relevantForLeaderInP.containsKey(set)){//set.getPlayersHistory())) {
                        for (Sequence followerSequence : relevantForLeaderInP.get(set)){//set.getPlayersHistory())) {
                            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, followerSequence);
                            lpTable.setConstraint(eqKey, createSeqPairVarKey(set.getPlayersHistory(), followerSequence), 1.0);
                            lpTable.setConstant(eqKey, 0.0);
                            lpTable.setConstraintType(eqKey, 1);
                        }
                    }
                }
                // 5
                if (set.getPlayer().equals(follower) && !set.getOutgoingSequences().isEmpty()) {
                    if(relevantForFollowerInP.containsKey(set)){
                        for (Sequence leaderSequence : relevantForFollowerInP.get(set)){
//                    for (Sequence leaderSequence : leaderSequences) {
//                        if (relevantForFollowerInP.containsKey(leaderSequence) && relevantForFollowerInP.get(leaderSequence).contains(set.getPlayersHistory())) {
                            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
                            lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
                            for (Sequence outgoing : set.getOutgoingSequences())
                                lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoing), -1.0);
                            lpTable.setConstant(eqKey, 0.0);
                            lpTable.setConstraintType(eqKey, 1);
                        }
                    }
                }
            }
        }
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

                    lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
                    lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
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

            lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
            lpTable.setConstraint(eqKey, varKey, 1);
            lpTable.setConstraintType(eqKey, 2);
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
                    if (reachableSet.getOutgoingSequences() != null && !reachableSet.getOutgoingSequences().isEmpty())
                        lpTable.setConstraint(eqKey, new Pair<>(reachableSet, followerSequence), -1);
                }
        }
    }

    @Override
    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence) {
        Object varKey = new Pair<>("v", followerSequence);

        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
        lpTable.setConstraintType(followerSequence, 1);
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

                lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
                lpTable.setConstraint(followerSequence, contVarKey, -1);
            }
        }
    }

    protected HashSet<Sequence> findLeaderDeviation(LPData lpData){
//        HashSet<Object> constraints = newlpData.getWatchedDualVariables().keySet();
        HashSet<Sequence> deviations = new HashSet<>();
        HashMap<Object, Double> duals = new HashMap<>();
        Sequence minSequence = null;
        double minCost = Double.POSITIVE_INFINITY;
        try {
            for (Object con : lpData.getWatchedDualVariables().keySet())
                duals.put(con, lpData.getSolver().getDual(lpData.getWatchedDualVariables().get(con)));
        }
        catch (Exception e){e.printStackTrace();}
        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)){
            if(!leaderRG.contains(leaderSequence)){
//                System.out.println(leaderSequence);
                HashMap<Sequence, Double> costs = new HashMap<>();
                for (Sequence followerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)){
//                    double reducedCost = 0.0;
                    Double[] seqCombValue = algConfig.getGenSumSequenceCombinationUtility(leaderSequence, followerSequence);
                    if (seqCombValue != null) {
                        double followerValue = seqCombValue[follower.getId()];
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
                            costs.put(followerSequence, costs.get(followerSequence) - followerValue * duals.get(followerSequence));
//                            reducedCost += -followerValue * duals.get(followerSequence);
//                            lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);

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
                            Object eqKey = new Pair<SequenceInformationSet, Sequence>((SequenceInformationSet) leaderSequence.getLastInformationSet(), followerSequence);
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
                double min = Collections.min(costs.values());
                if ( min <  0.0 * eps) { // !algConfig.getCompatibleSequencesFor(leaderSequence).isEmpty() &&
                    if (!MAX) {
                        for (Sequence prefix : leaderSequence.getAllPrefixes())
                            if (!leaderRG.contains(prefix)) deviations.add(prefix);
                    }
                    else{
                        if (min < minCost){
                            minCost = min;
                            minSequence = leaderSequence;
                        }
                    }
                    if (GREEDY) {
                        System.out.println("Deviation: "+ min );
                        break;
                    }
                }

            }
        }

        if (MAX && minSequence != null) {
            System.out.println("Maximum deviation: " + minCost);
            for (Sequence prefix : minSequence.getAllPrefixes())
                if (!leaderRG.contains(prefix)) deviations.add(prefix);
        }

        return  deviations;

//        return new HashSet<>();
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfig = (StackelbergConfig) algConfig;
        this.expander = (Expander<SequenceInformationSet>)expander;
//        followerBestResponse = new FollowerBestResponse(this.algConfig.getRootState(), expander, this.algConfig, leader, follower);
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

        Pair<HashSet<Sequence>, HashSet<Sequence>> initial = findInitialRG();

        HashSet<Sequence> leaderSequences = initial.getLeft();
        HashSet<Sequence> followerSequences = initial.getRight();
        addLeaderSequencesToLP(leaderSequences);

//        System.exit(0);

//        ((ConstraintGeneratingLPTable)lpTable).watchAllDualVariables();

        boolean updated = true;
        LPData lpData = null;
//        double value = 0.0;
        while(updated){
            // solve LP
            try {
                System.out.println("-----------------------");
                System.out.printf("Watching...");
                lpTable.watchAllDualVariables();
                System.out.println("done.");
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Generating cplex...");
                lpData = lpTable.toCplex();
                System.out.println("done.");
                overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//                lpData.getSolver().exportModel("Iter2pSEFCE.lp");
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
            leaderSequences = findLeaderDeviation(lpData);
            System.out.println("done.");
            // update LP
            if (leaderSequences.isEmpty())
                updated = false;
            else {
                System.out.printf("Adding new sequences...");
                addLeaderSequencesToLP(leaderSequences);
                System.out.println("done.");
            }
            System.out.println("RG size: " +leaderRG.size()+ "/"+((StackelbergConfig) algConfig).getSequencesFor(leader).size());
        }


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
