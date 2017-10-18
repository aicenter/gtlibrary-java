package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.domain.flipit.FlipItExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by Jakub Cerny on 11/10/2017.
 */
public class LeaderGeneration2pLessMemSefceLP extends LeaderGenerationTwoPlayerSefceLP {

    protected long deviationIdentificationTime = 0;
    protected long restrictedGameGenerationTime = 0;

    public LeaderGeneration2pLessMemSefceLP(Player leader, GameInfo info) {
        super(leader, info);
    }

    public LeaderGeneration2pLessMemSefceLP(Player leader, GameInfo info, boolean greedy, boolean max) {
        super(leader, info, greedy, max);
    }

    @Override
    public double getRestrictedGameRatio(){
        return 0.0;
//        System.out.println("Best: " + ((double)initialSize) / algConfig.getSequencesFor(leader).size());
//        return ((double)leaderRG.size()) / algConfig.getSequencesFor(leader).size();
    }

    @Override
    protected HashSet<Sequence> findLeaderInitialRG(){

        boolean addAllLeaderSequences = false;
        System.out.printf("all seqs = " + addAllLeaderSequences+ "...");
//        if (DEBUG) return new HashSet<>(algConfig.getSequencesFor(leader));

        HashSet<Sequence> initialSequences = new HashSet<>();
        initialSequences.add(new ArrayListSequenceImpl(leader));

        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(algConfig.getRootState());

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            if (currentState.isGameEnd()) {
                continue;
            }
            {
                if (currentState.getPlayerToMove().equals(leader)) {
                    if (addAllLeaderSequences){
                        for (Action action : expander.getActions(currentState)) {
                            Sequence seq = new ArrayListSequenceImpl(currentState.getSequenceForPlayerToMove());
                            seq.addLast(action);
                            initialSequences.add(seq);
                            queue.add(currentState.performAction(action));
                        }
                    }
                    else {
                        Action defaultAction = expander.getActions(currentState).get(0);
                        Sequence seq = new ArrayListSequenceImpl(currentState.getSequenceForPlayerToMove());
                        seq.addLast(defaultAction);
                        initialSequences.add(seq);
                        queue.add(currentState.performAction(defaultAction));
                    }
                } else {
                    for (Action action : expander.getActions(currentState)) {
                        queue.add(currentState.performAction(action));
                    }
                }
            }
        }


        initialSize = initialSequences.size();
        System.out.printf("RG size = " + initialSize+"...");// + "/" + algConfig.getSequencesFor(leader).size());
//        System.out.println("Initial sequences : ");
//        for (Sequence seq : initialSequences)
//            System.out.println(seq);
        System.gc();
        return initialSequences;
    }

    @Override
    protected HashSet<Sequence> findLeaderDeviation(LPData lpData){
//        HashSet<Object> constraints = newlpData.getWatchedDualVariables().keySet();
        HashSet<Sequence> deviations = new HashSet<>();
        HashMap<Object, Double> duals = new HashMap<>();
        Sequence minSequence = null;
        double minCost = Double.POSITIVE_INFINITY;
        double maxCost = Double.NEGATIVE_INFINITY;
        try {
            for (Object con : lpData.getWatchedDualVariables().keySet())
                duals.put(con, lpData.getSolver().getDual(lpData.getWatchedDualVariables().get(con)));
        }
        catch (Exception e){e.printStackTrace();}
        HashMap<Sequence, HashMap<Sequence,Double>> seqCosts = new HashMap<>();
//        System.out.println("Leafs= " + algConfig.getAllLeafs().size() + "; utilities= " + algConfig.getUtilityForSequenceCombinationGenSum().size());
        for (Map.Entry<Map<Player, Sequence>, Double[]> leaf : algConfig.getUtilityForSequenceCombinationGenSum().entrySet()){
            Sequence leaderSequence = leaf.getKey().get(leader);

//            if(leaderSequence.isEmpty()) continue;
//            if(true){
            if(!leaderRG.contains(leaderSequence)){
//                System.out.println(leaderSequence);
                boolean firstEncountered = false;
                if (!seqCosts.containsKey(leaderSequence)) {
                    firstEncountered = true;
                    seqCosts.put(leaderSequence, new HashMap<Sequence, Double>());
                }
                HashMap<Sequence, Double> costs = seqCosts.get(leaderSequence);

                if (false && firstEncountered){
                    // 4 -> prvni cast
                    if (!leaderSequence.isEmpty()) {
//                        Sequence leaderSubSeq = leaderSequence.getSubSequence(0, leaderSequence.size() - 1);
//                        if (relevantForLeaderInP.containsKey(leaderSequence.getLastInformationSet()))//leaderSubSeq))
//                            for (Sequence followerSequence : relevantForLeaderInP.get(leaderSequence.getLastInformationSet())) {//leaderSubSeq)) {
                        for (Sequence followerSequence : getRelevantSequencesForLeader(leaderSequence.getSubSequence(leaderSequence.size()-1))){
                                Object eqKey = new Pair<SequenceInformationSet, Sequence>((SequenceInformationSet) leaderSequence.getLastInformationSet(), followerSequence);
                                if (!duals.containsKey(eqKey)) continue;
//                            System.out.println("eqkey found");
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - 1 * duals.get(eqKey));
                            }
                    }
                }

                Sequence followerSequence = leaf.getKey().get(follower); {
//                    if (lpTable.exists(createSeqPairVarKey(leaderSequence, followerSequence)) || lpData.getWatchedPrimalVariables().containsKey(createSeqPairVarKey(leaderSequence, followerSequence)))
//                        System.out.println("ERROR!");
//                    double reducedCost = 0.0;
                    Double[] seqCombValue = leaf.getValue();
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
                            for (Action action : followerSequence) {
                                for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
                                    Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
                                    seq.addLast(relevantAction);
                                    Object eqKey = new Triplet<>(followerSequence.getLastInformationSet(), followerSequence, seq);
                                    if (!costs.containsKey(seq))
                                            costs.put(seq, 0.0);
                                    costs.put(seq, costs.get(seq) - followerValue * duals.get(eqKey));
//                                    Object varKey = createSeqPairVarKey(leaderSequence, seq);
                                }
                            }
//                            if (followerSequence.getLastInformationSet() == null) continue;
//                            for (GameState state : followerSequence.getLastInformationSet().getAllStates()){
//                                Sequence stateLeaderSeq = state.getSequenceFor(leader);
////                            System.out.println(stateLeaderSeq);
//                                if (relevantForLeaderInDeviation.containsKey(stateLeaderSeq)) {
//                                    for (Sequence stateFollowerSeq : relevantForLeaderInDeviation.get(stateLeaderSeq)) {
//                                        if (stateFollowerSeq.size() > 0 && followerSequence.size() > 0 && !((SequenceInformationSet)stateFollowerSeq.getLastInformationSet()).getPlayersHistory().isPrefixOf(followerSequence.getSubSequence(followerSequence.size()-1)))
//                                            continue;
//                                        Object eqKey = new Triplet<>(followerSequence.getLastInformationSet(), followerSequence, stateFollowerSeq);
//                                        if(CHECK_EXISTENCE && !lpTable.existsEqKey(eqKey)) continue;
//                                        if (!costs.containsKey(stateFollowerSeq))
//                                            costs.put(stateFollowerSeq, 0.0);
//                                        costs.put(stateFollowerSeq, costs.get(stateFollowerSeq) - followerValue * duals.get(eqKey));
////                                        Object varKey = createSeqPairVarKey(leaderSequence, stateFollowerSeq);
////                                        lpTable.setConstraint(eqKey, varKey, -followerValue);
//                                    }
//                                }
//                            }
                        }
                    }
                }
            }
        }

        HashSet<Integer> blackList = new HashSet<>();
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(algConfig.getRootState());
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            Sequence stateSeq = currentState.getSequenceForPlayerToMove();
            if (currentState.isGameEnd()) {
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                if (currentState.getPlayerToMove().equals(follower)){
                    queue.add(currentState.performAction(action));
                }
                else{
                    if (!blackList.contains(action.getInformationSet().hashCode())) {
                        Sequence seq = new ArrayListSequenceImpl(stateSeq);
                        seq.addLast(action);
                        if (!leaderRG.contains(seq)){
                            if (!seqCosts.containsKey(seq)) {
                                seqCosts.put(seq, new HashMap<Sequence, Double>());
                            }
                            HashMap<Sequence, Double> costs = seqCosts.get(seq);
                            for (Sequence followerSequence : getRelevantSequencesForLeader(stateSeq)){
                                Object eqKey = new Pair<SequenceInformationSet, Sequence>((SequenceInformationSet) action.getInformationSet(), followerSequence);
                                if (!duals.containsKey(eqKey)) continue;
//                            System.out.println("eqkey found");
                                if (!costs.containsKey(followerSequence))
                                    costs.put(followerSequence, 0.0);
                                costs.put(followerSequence, costs.get(followerSequence) - 1 * duals.get(eqKey));
                            }
                        }
                    }
                    queue.add(currentState.performAction(action));
                }
            }
            blackList.add(expander.getActions(currentState).get(0).getInformationSet().hashCode());
        }


        for (Sequence leaderSeq : seqCosts.keySet()){
            if (seqCosts.get(leaderSeq).values().isEmpty()) continue;
            double min = Collections.min(seqCosts.get(leaderSeq).values());
            if ( min <  eps) { // !algConfig.getCompatibleSequencesFor(leaderSequence).isEmpty() &&
                    if (!MAX) {
                        for (Sequence prefix : leaderSeq.getAllPrefixes())
                            if (!leaderRG.contains(prefix)) deviations.add(prefix);
                    }
                    else{
                        if (min < minCost){
                            minCost = min;
                            minSequence = leaderSeq;
                        }
                    }
                    if (GREEDY) {
                        System.out.println("Deviation: "+ min );
                        break;
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
//        System.out.println(deviations.toString());
        System.gc();
        return  deviations;

//        return new HashSet<>();
    }

    protected HashSet<SequenceInformationSet> getReachableFollowerISs(Sequence followerSequence){
        HashSet<SequenceInformationSet> reachable = new HashSet<>();
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(algConfig.getRootState());
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            Sequence stateSeq = currentState.getSequenceForPlayerToMove();
            if (currentState.getPlayerToMove().equals(follower) && stateSeq.equals(followerSequence)) {
                if (!currentState.isGameEnd()){
                    reachable.add((SequenceInformationSet) expander.getActions(currentState).get(0).getInformationSet());
                }
                continue;
            }
                if (currentState.isGameEnd()) {
                    continue;
                }
                for (Action action : expander.getActions(currentState)) {
                    if (currentState.getPlayerToMove().equals(follower)){
                        if (action.equals(followerSequence.get(stateSeq.size()))){
                            queue.add(currentState.performAction(action));
                            break;
                        }
                    }
                    else{
                        queue.add(currentState.performAction(action));
                    }
                }
            }
        System.gc();
        return reachable;
    }

    protected void addFollowerContinuationConstraint(Sequence leaderSequence){
        // leader : for all reachable sets, for all relevant sequences
//        Sequence sequence = new ArrayListSequenceImpl(leaderSequence);

        HashSet<Integer> blackList = new HashSet<>();

        for (SequenceInformationSet set : getReachableFollowerISs(new ArrayListSequenceImpl(follower))) {
            if (blackList.contains(set.hashCode())) continue;
            blackList.add(set.hashCode());
            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
            setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
            lpTable.setConstant(eqKey, 0.0);
            lpTable.setConstraintType(eqKey, 1);
            for (Action outgoing : expander.getActions(set)) {
                Sequence outgoingSeq = new ArrayListSequenceImpl(set.getPlayersHistory());
                outgoingSeq.addLast(outgoing);
                setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoingSeq), -1.0);
            }
        }


        LinkedList<GameState> queue = new LinkedList<>();
//        HashSet<Sequence> relevant = new HashSet<>();
//        HashSet<SequenceInformationSet> reachable = new HashSet<>();
        queue.add(algConfig.getRootState());
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            Sequence stateSeq = currentState.getSequenceForPlayerToMove();
            if (currentState.getPlayerToMove().equals(leader) && stateSeq.equals(leaderSequence)) {
                Sequence followerSequence = currentState.getSequenceFor(follower);
                for (Action action : followerSequence) {
                    for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
                        Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
                        seq.addLast(relevantAction);
                        for (SequenceInformationSet set : getReachableFollowerISs(seq)) {
                            if (blackList.contains(set.hashCode())) continue;
                            blackList.add(set.hashCode());
                            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
                            setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
                            lpTable.setConstant(eqKey, 0.0);
                            lpTable.setConstraintType(eqKey, 1);
                            for (Action outgoing : expander.getActions(set)) {
                                Sequence outgoingSeq = new ArrayListSequenceImpl(set.getPlayersHistory());
                                outgoingSeq.addLast(outgoing);
                                setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoingSeq), -1.0);
                            }
                        }
                    }
                }
//                if (!currentState.isGameEnd()){
//                    reachable.add((SequenceInformationSet) expander.getActions(currentState).get(0).getInformationSet());
//                }
                continue;
            }

            if (currentState.isGameEnd()) {
                continue;
            }
//            Object eqKey = null;
//            if (currentState.getPlayerToMove().equals(follower)){
//                SequenceInformationSet set = (SequenceInformationSet) expander.getActions(currentState).get(0).getInformationSet();
//                eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
//                setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
//                lpTable.setConstant(eqKey, 0.0);
//                lpTable.setConstraintType(eqKey, 1);
//            }
            for (Action action : expander.getActions(currentState)) {
                if (currentState.getPlayerToMove().equals(leader)){
                    if (action.equals(leaderSequence.get(stateSeq.size()))){
                        queue.add(currentState.performAction(action));
//                        sequence.removeFirst();
                        break;
                    }
                }
                else{
//                    Sequence seq = new ArrayListSequenceImpl(stateSeq);
//                    seq.addLast(action);
//                    setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, seq), -1.0);
                    queue.add(currentState.performAction(action));
                }
            }
        }

        System.gc();
//        for (SequenceInformationSet set : reachable){
//            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
//            setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
//            for (Action outgoing : expander.getActions(set)) {
//                Sequence seq = new ArrayListSequenceImpl(set.getPlayersHistory());
//                seq.addLast(outgoing);
//                setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, seq), -1.0);
//            }
//        }
    }



    protected HashSet<Sequence> getRelevantSequencesForLeader(Sequence leaderSequence){
//        Sequence sequence =  new ArrayListSequenceImpl(leaderSequence);
//        Player p = leaderSequence.getPlayer();
        HashSet<Sequence> relevant =  new HashSet<>();
        relevant.add(new ArrayListSequenceImpl(follower));
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(algConfig.getRootState());

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            Sequence stateSeq = currentState.getSequenceForPlayerToMove();
            if (currentState.getPlayerToMove().equals(leader) && stateSeq.equals(leaderSequence)) {
                Sequence followerSequence = currentState.getSequenceFor(follower);
                for (Action action : followerSequence) {
                    for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
                        Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
                        seq.addLast(relevantAction);
                        relevant.add(seq);
                    }
                }
                continue;
            }

            if (currentState.isGameEnd()) {
//                final double[] utilities = currentState.getUtilities();
//                Double[] u = new Double[utilities.length];
//
//                for (Player p : currentState.getAllPlayers()){
//                    if(utilities.length > p.getId())
//                        u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*info.getUtilityStabilizer();
//                }

                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                if (currentState.getPlayerToMove().equals(leader)){
                    if (action.equals(leaderSequence.get(stateSeq.size()))){
                        queue.add(currentState.performAction(action));
//                        sequence.removeFirst();
                        break;
                    }
                }
                else{
//                    Sequence seq = new ArrayListSequenceImpl(stateSeq);
//                    seq.addLast(action);
//                    relevant.add(seq);
                    queue.add(currentState.performAction(action));
                }
            }
        }
        System.gc();
        return relevant;
    }

    @Override
    public double calculateLeaderStrategies(AlgorithmConfig algConfig, Expander expander) {
        this.algConfig = (StackelbergConfig) algConfig;
        this.expander = (Expander<SequenceInformationSet>)expander;
//        followerBestResponse = new FollowerBestResponse(this.algConfig.getRootState(), expander, this.algConfig, leader, follower);
        long startTime = threadBean.getCurrentThreadCpuTime();

        // construct initial LP
        System.out.printf("Generating initial follower constraints...");
        generateFollowerConstraints();
        System.out.println("done.");

        // find relevant sequences
//        generateRelevantSequences();

//        System.out.println("////////////////////////////////////");
//        System.out.println("          RELEVANT SEQUENCES");
//        System.out.println("////////////////////////////////////");
//        for (Sequence seq : relevantForLeaderInP.keySet()){
//            System.out.println(seq);
//            for (Sequence s : relevantForLeaderInP.get(seq))
//                System.out.println("    " + s);
//        }
//        System.out.println("////////////////////////////////////");


        System.out.printf("Finding initial RG...");
        HashSet<Sequence> leaderSequences = findLeaderInitialRG();
        System.out.println("done.");
        startTime = threadBean.getCurrentThreadCpuTime();
        System.out.printf("Adding new sequences...");
        addLeaderSequencesToLP(leaderSequences);
        System.out.println("done.");
        restrictedGameGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;

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
                System.out.printf("Watching...");
                lpTable.watchAllDualVariables();
                System.out.println("done.");
                startTime = threadBean.getCurrentThreadCpuTime();
                System.out.printf("Generating cplex...");
                lpData = lpTable.toCplex();
                System.out.println("done.");
                overallConstraintGenerationTime += threadBean.getCurrentThreadCpuTime() - startTime;
//                lpData.getSolver().exportModel("Iter2pLMSEFCE_" +iteration + ".lp");
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

            System.gc();
            System.out.println("RG size: " +leaderRG.size());//+ "/"+((StackelbergConfig) algConfig).getSequencesFor(leader).size());
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
    protected void addLeaderSequencesToLP(HashSet<Sequence> leaderSequences){
        int numberOfNonExistant = 0;
        System.out.printf("#c - before: "  + lpTable.columnCount());
        boolean originalPContinuation = DEBUG && USE_ORIG_PCONT;
        for (Map.Entry<Map<Player, Sequence>, Double[]> leaf : algConfig.getUtilityForSequenceCombinationGenSum().entrySet()) {
            Sequence leaderSequence = leaf.getKey().get(leader);
            if (!leaderSequences.contains(leaderSequence)) continue;
            leaderRG.add(leaderSequence);
            // update objective, 6, 7 pouze pro sekvence mirici do listu
            Sequence followerSequence = leaf.getKey().get(follower);{


                Double[] seqCombValue = leaf.getValue();
                if (seqCombValue != null) {
                    double followerValue = seqCombValue[follower.getId()];
                    double leaderValue = seqCombValue[leader.getId()];

                    // objective
                    if (leaderValue != 0)
                        setNewObjective(createSeqPairVarKey(leaderSequence, followerSequence), leaderValue);

                    if (followerValue != 0) {
                        // 6:
                        setNewConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -followerValue);

                        // 7:
//                        Sequence outgoingSequence = currentState.getSequenceFor(follower);
                        for (Action action : followerSequence) {
                            for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
                                Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
                                seq.addLast(relevantAction);
                                Object eqKey = new Triplet<>(followerSequence.getLastInformationSet(), followerSequence, seq);
                                Object varKey = createSeqPairVarKey(leaderSequence, seq);
                                setNewConstraint(eqKey, varKey, -followerValue);
                            }
                        }
//                        if(followerSequence.getLastInformationSet() == null) continue;
//                        for (GameState state : followerSequence.getLastInformationSet().getAllStates()){
//                            Sequence stateLeaderSeq = state.getSequenceFor(leader);
////                            System.out.println(stateLeaderSeq);
//                            if (relevantForLeaderInDeviation.containsKey(stateLeaderSeq)) {
//                                for (Sequence stateFollowerSeq : relevantForLeaderInDeviation.get(stateLeaderSeq)) {
//                                    if (stateFollowerSeq.size() > 0 && followerSequence.size() > 0 && !((SequenceInformationSet)stateFollowerSeq.getLastInformationSet()).getPlayersHistory().isPrefixOf(followerSequence.getSubSequence(followerSequence.size()-1)))
//                                        continue;
////                                    lpTable.getEquationIndex()
////                                    if (stateFollowerSeq.size() == 1) continue;
//                                    Object eqKey = new Triplet<>(followerSequence.getLastInformationSet(), followerSequence, stateFollowerSeq);
//                                    if (CHECK_EXISTENCE && !lpTable.existsEqKey(eqKey)){ numberOfNonExistant ++; continue;}
//                                    Object varKey = createSeqPairVarKey(leaderSequence, stateFollowerSeq);
//                                    setNewConstraint(eqKey, varKey, -followerValue);
//                                }
//                            }
//                        }
                    }
                }
            }

            if (!originalPContinuation){

                // 4 -> druha cast
//                if (!leaderSequence.isEmpty()) {
////                    Sequence leaderSubSeq = leaderSequence.getSubSequence(0, leaderSequence.size() - 1);
//                    if (relevantForLeaderInP.containsKey(leaderSequence.getLastInformationSet()))//leaderSubSeq))
//                        for (Sequence followerSeq : relevantForLeaderInP.get(leaderSequence.getLastInformationSet())){//leaderSubSeq)) {
//                            Object eqKey = new Pair<SequenceInformationSet, Sequence>((SequenceInformationSet) leaderSequence.getLastInformationSet(), followerSeq);
//                            setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSeq), -1.0);
//                            lpTable.setConstant(eqKey, 0.0);
//                            lpTable.setConstraintType(eqKey, 1);
//                        }
//                }
            }
        }
        for (Sequence leaderSequence : leaderSequences) {
            leaderRG.add(leaderSequence);
            // 4 -> druha cast
            if (!leaderSequence.isEmpty()) {
//                    Sequence leaderSubSeq = leaderSequence.getSubSequence(0, leaderSequence.size() - 1);
//                if (relevantForLeaderInP.containsKey(leaderSequence.getLastInformationSet()))//leaderSubSeq))
                for (Sequence followerSeq : getRelevantSequencesForLeader(leaderSequence.getSubSequence(leaderSequence.size() - 1))) {//leaderSubSeq)) {
                    Object eqKey = new Pair<SequenceInformationSet, Sequence>((SequenceInformationSet) leaderSequence.getLast().getInformationSet(), followerSeq);
                    setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, followerSeq), -1.0);
                    lpTable.setConstant(eqKey, 0.0);
                    lpTable.setConstraintType(eqKey, 1);
                }
            }

            // 4 -> prvni cast
            addLeaderContinuationConstraint(leaderSequence);

            // follower : for all
            // 5
            addFollowerContinuationConstraint(leaderSequence);



        }

        System.gc();

//        if (originalPContinuation)
//            createPContinuationConstraints();
//        else {
//            for (SequenceInformationSet set : relevantForLeaderInP.keySet()) {// 4 -> prvni cast
//                if (set.getPlayer().equals(leader) && !set.getOutgoingSequences().isEmpty() && leaderSequences.contains(set.getPlayersHistory())) {
////                    if (relevantForLeaderInP.containsKey(set)) {//set.getPlayersHistory())) {
//                        for (Sequence followerSequence : relevantForLeaderInP.get(set)) {//set.getPlayersHistory())) {
//                            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, followerSequence);
//                            setNewConstraint(eqKey, createSeqPairVarKey(set.getPlayersHistory(), followerSequence), 1.0);
//                            lpTable.setConstant(eqKey, 0.0);
//                            lpTable.setConstraintType(eqKey, 1);
//                        }
////                    }
//                }
//            }
//            for (SequenceInformationSet set : relevantForFollowerInP.keySet()) {
//                // 5
//                if (set.getPlayer().equals(follower) && !set.getOutgoingSequences().isEmpty()) {
////                    if(relevantForFollowerInP.containsKey(set)){
//                        for (Sequence leaderSequence : relevantForFollowerInP.get(set)){
//                            if (!leaderSequences.contains(leaderSequence)) continue;
////                    for (Sequence leaderSequence : leaderSequences) {
////                        if (relevantForFollowerInP.containsKey(leaderSequence) && relevantForFollowerInP.get(leaderSequence).contains(set.getPlayersHistory())) {
//                            Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
//                            setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
//                            for (Sequence outgoing : set.getOutgoingSequences())
//                                setNewConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoing), -1.0);
//                            lpTable.setConstant(eqKey, 0.0);
//                            lpTable.setConstraintType(eqKey, 1);
//                        }
////                    }
//                }
//            }
//        }
        System.out.printf(" / after: "  + lpTable.columnCount());
//        System.out.println("# of non-existent: " + numberOfNonExistant);
        System.out.printf("...");
    }

    protected void addLeaderContinuationConstraint(Sequence leaderSequence){
        // leader : for all reachable sets, for all relevant sequences
//        Sequence sequence = new ArrayListSequenceImpl(leaderSequence);
        LinkedList<GameState> queue = new LinkedList<>();
        HashSet<Sequence> relevant = new HashSet<>();
        relevant.add(new ArrayListSequenceImpl(follower));
        HashSet<SequenceInformationSet> reachable = new HashSet<>();
        queue.add(algConfig.getRootState());
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
            Sequence stateSeq = currentState.getSequenceForPlayerToMove();
            if (currentState.getPlayerToMove().equals(leader) && stateSeq.equals(leaderSequence)) {
                Sequence followerSequence = currentState.getSequenceFor(follower);
                for (Action action : followerSequence) {
                    for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
                        Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
                        seq.addLast(relevantAction);
                        relevant.add(seq);
                    }
                }
                if (!currentState.isGameEnd()){
                    reachable.add((SequenceInformationSet) expander.getActions(currentState).get(0).getInformationSet());
                }
                continue;
            }

            if (currentState.isGameEnd()) {
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                if (currentState.getPlayerToMove().equals(leader)){
                    if (leaderSequence.get(stateSeq.size()).equals(action)){
                        queue.add(currentState.performAction(action));
//                        sequence.removeFirst();
                        break;
                    }
                }
                else{
//                    Sequence seq = new ArrayListSequenceImpl(stateSeq);
//                    seq.addLast(action);
//                    relevant.add(seq);
                    queue.add(currentState.performAction(action));
                }
            }
        }
        for (SequenceInformationSet set : reachable)
            for (Sequence followerSequence : relevant){
                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, followerSequence);
                setNewConstraint(eqKey, createSeqPairVarKey(set.getPlayersHistory(), followerSequence), 1.0);
                lpTable.setConstant(eqKey, 0.0);
                lpTable.setConstraintType(eqKey, 1);
            }
        System.gc();
    }

    protected void generateGameTree(){
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(algConfig.getRootState());

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers()){
                    if(utilities.length > p.getId())
                        u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*info.getUtilityStabilizer();
                }
                algConfig.setUtility(currentState, u);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

//    @Override
//    protected void generateRelevantSequences() {
//        if (DEBUG && USE_ORIG_PCONT) return;
//
//        generateGameTree();
//
//        createInitPConstraint();
//        Set<Object> blackList = new HashSet<>();
//        Set<Pair<Sequence, Sequence>> pStops = new HashSet<>();
//
//        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
//            List<Action> actions = expander.getActions(informationSet);
//            Player opponent = info.getOpponent(informationSet.getPlayer());
//
//            for (GameState gameState : informationSet.getAllStates()) {
//                if (!gameState.isGameEnd())
//                    generateRelevantSequence(actions, opponent, gameState, blackList, pStops);
//            }
//        }
//
//        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)) {
//            for (Sequence compatibleFollowerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)) {
//                for (Action action : compatibleFollowerSequence) {
//                    Sequence actionHistory = ((PerfectRecallInformationSet)action.getInformationSet()).getPlayersHistory();
//                    Object eqKeyFollower = new Triplet<>(leaderSequence, actionHistory, action.getInformationSet());
//
//                    if (!blackList.contains(eqKeyFollower)) {
//                        blackList.add(eqKeyFollower);
//                        Pair<Sequence, Sequence> varKey = createSeqPairVarKey(leaderSequence, actionHistory);
//
////                        lpTable.setConstraintType(eqKeyFollower, 1);
////                        lpTable.setConstraint(eqKeyFollower, varKey, -1);
//                        // Leader fixed, follower continuation
////                        if (!relevantForFollowerInP.containsKey(varKey.getLeft())) relevantForFollowerInP.put(varKey.getLeft(), new HashSet<>());
////                        relevantForFollowerInP.get(varKey.getLeft()).add(varKey.getRight());
//                        if (!relevantForFollowerInP.containsKey((SequenceInformationSet) action.getInformationSet())) relevantForFollowerInP.put((SequenceInformationSet) action.getInformationSet(), new HashSet<>());
//                        relevantForFollowerInP.get((SequenceInformationSet) action.getInformationSet()).add(varKey.getLeft());
//
////                        for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
////                            lpTable.setConstraint(eqKeyFollower, createSeqPairVarKey(leaderSequence, followerSequence), 1);
////                        }
//                    }
//
//                    ListIterator<Action> leaderSeqIterator = leaderSequence.listIterator(leaderSequence.size());
//                    Action leaderAction;
//
//                    while (leaderSeqIterator.hasPrevious()) {
//                        leaderAction = leaderSeqIterator.previous();
//                        Sequence leaderHistory = ((PerfectRecallInformationSet)leaderAction.getInformationSet()).getPlayersHistory();
//                        Object eqKeyLeader = new Triplet<>(leaderHistory, actionHistory, leaderAction);
//
//                        if (!blackList.contains(eqKeyLeader)) {
//                            blackList.add(eqKeyLeader);
//                            Pair<Sequence, Sequence> varKey = createSeqPairVarKey(leaderHistory, actionHistory);
//
////                            lpTable.setConstraintType(eqKeyLeader, 1);
////                            lpTable.setConstraint(eqKeyLeader, varKey, -1);
//                            // Follower fixed, leader continuation
////                            if (!relevantForLeaderInP.containsKey(varKey.getLeft())) relevantForLeaderInP.put(varKey.getLeft(), new HashSet<>());
////                            relevantForLeaderInP.get(varKey.getLeft()).add(varKey.getRight());
//                            if (!relevantForLeaderInP.containsKey((SequenceInformationSet) leaderAction.getInformationSet())) relevantForLeaderInP.put((SequenceInformationSet) leaderAction.getInformationSet(), new HashSet<>());
//                            relevantForLeaderInP.get((SequenceInformationSet) leaderAction.getInformationSet()).add(varKey.getRight());
//
//
////                            for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
////                                lpTable.setConstraint(eqKeyLeader, createSeqPairVarKey(leaderContinuation, actionHistory), 1);
////                            }
//                        }
//
//                        for (Sequence followerSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
//                            Object eqKeyLeaderCont = new Triplet<>(leaderHistory, followerSequence, leaderAction.getInformationSet());
//
//                            if (!blackList.contains(eqKeyLeaderCont)) {
//                                blackList.add(eqKeyLeaderCont);
//                                Pair<Sequence, Sequence> varKeyCont = createSeqPairVarKey(leaderHistory, followerSequence);
//
////                                lpTable.setConstraintType(eqKeyLeaderCont, 1);
////                                lpTable.setConstraint(eqKeyLeaderCont, varKeyCont, -1);
//                                // Follower fixed, leader continuation
////                                if (!relevantForLeaderInP.containsKey(varKeyCont.getLeft())) relevantForLeaderInP.put(varKeyCont.getLeft(), new HashSet<>());
////                                relevantForLeaderInP.get(varKeyCont.getLeft()).add(varKeyCont.getRight());
//                                if (!relevantForLeaderInP.containsKey((SequenceInformationSet) leaderAction.getInformationSet())) relevantForLeaderInP.put((SequenceInformationSet) leaderAction.getInformationSet(), new HashSet<>());
//                                relevantForLeaderInP.get((SequenceInformationSet) leaderAction.getInformationSet()).add(varKeyCont.getRight());
//
////                                for (Sequence leaderContinuation : ((SequenceInformationSet) leaderAction.getInformationSet()).getOutgoingSequences()) {
////                                    lpTable.setConstraint(eqKeyLeaderCont, createSeqPairVarKey(leaderContinuation, followerSequence), 1);
////                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
////        for (Sequence followerSequence : relevantForFollower.keySet())
////            for (Sequence leaderSequence : relevantForFollower.get(followerSequence)){
////                if (!relevantForLeader.containsKey(leaderSequence)) relevantForLeader.put(leaderSequence, new HashSet<>());
////                relevantForLeader.get(leaderSequence).add(followerSequence);
////            }
////        for (Sequence leaderSequence : algConfig.getSequencesFor(leader)) {
////            if (!relevantForLeaderInP.containsKey(leaderSequence)) relevantForLeaderInP.put(leaderSequence, new HashSet<>());
////            relevantForLeaderInP.get(leaderSequence).add(new ArrayListSequenceImpl(follower));
////        }
//    }

    @Override
    protected void generateFollowerConstraints(){
        boolean useOriginalFormulation = false;
        boolean checkDeviationStructures = false;
        if (useOriginalFormulation){
            generateGameTree();
            super.generateFollowerConstraints();
            return;
        }

        // generate relevant parts of cons 3,6,7,8

        // 3 :
        lpTable.setConstraint("initP", createSeqPairVarKey(new ArrayListSequenceImpl(leader), new ArrayListSequenceImpl(follower)), 1);
        lpTable.setConstant("initP", 1);
        lpTable.setConstraintType("initP", 1);

        createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower));

        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(algConfig.getRootState());
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

//            algConfig.addStateToSequenceForm(currentState);
            List<Action> actions = expander.getActions(currentState);
            if (currentState.isGameEnd()) {

//                Sequence leaderSeq = currentState.getSequenceFor(leader);
//                if (!relevantForLeaderInDeviation.containsKey(leaderSeq)) {
//                    relevantForLeaderInDeviation.put(leaderSeq, new HashSet<>());
//                    relevantForLeaderInDeviation.get(leaderSeq).add(new ArrayListSequenceImpl(follower));
//                }
//                Sequence outgoingSequence = currentState.getSequenceFor(follower);
//                for (Action action : outgoingSequence) {
//                    for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
//                        Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
//                        seq.addLast(relevantAction);
//                        relevantForLeaderInDeviation.get(leaderSeq).add(seq);
//                    }
//                }


                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers()){
                    if(utilities.length > p.getId())
                        u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*info.getUtilityStabilizer();
                }
                algConfig.setUtility(currentState, u);
                continue;
            }
            else {
                if(currentState.getPlayerToMove().equals(follower)) createReverseISAction((SequenceInformationSet) actions.iterator().next().getInformationSet());
            }
            for (Action action : actions) {
//                System.out.println(expander.getActions((SequenceInformationSet)action.getInformationSet()).size());
//                System.out.println(((SequenceInformationSet)action.getInformationSet()).getPlayersHistory());
                queue.add(currentState.performAction(action));
                if(currentState.getPlayerToMove().equals(follower)) {
                    Sequence followerSequence = new ArrayListSequenceImpl(currentState.getSequenceForPlayerToMove());
                    followerSequence.addLast(action);
//                    System.out.printf("Creating sequence constraint...");
                    createSequenceConstraint(algConfig, followerSequence);
//                    System.out.printf("done.\nCreating IS action constraint...");
                    createISAction(followerSequence, (SequenceInformationSet) action.getInformationSet());
//                    System.out.println("done.");
                }
            }
        }

        System.gc();

//        System.out.println("Tree searched");
//        System.out.println(algConfig.getAllLeafs().size());
//        System.out.println(algConfig.getAllSequences().size());
//        System.out.println(algConfig.getAllInformationSets().size());
//        System.exit(0);

        if (!checkDeviationStructures) return;


        // 6 :
//        createSequenceConstraint(algConfig, new ArrayListSequenceImpl(follower));
//        for (Sequence followerSequence : algConfig.getSequencesFor(follower)) {
//            createSequenceConstraint(algConfig, followerSequence);
//        }

        for (Sequence leaderSeq : relevantForLeaderInDeviation.keySet()){
            System.out.println(leaderSeq + " : " + relevantForLeaderInDeviation.get(leaderSeq).toString());
        }
        generateGameTree();

        // 7 :
        HashMap<Sequence, HashSet<Sequence>> relevantForLeaderInDeviationLocal = new HashMap<>();
        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
            if (informationSet.getPlayer().equals(follower)) {
                if (!informationSet.getOutgoingSequences().isEmpty()) {
                    Sequence outgoingSequence = informationSet.getOutgoingSequences().iterator().next();

                    for (Action action : outgoingSequence) {
                        for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                            for (Sequence sequence : informationSet.getOutgoingSequences()) {
                                for (Sequence leaderSeq : algConfig.getCompatibleSequencesFor(sequence)) {
                                    if (!relevantForLeaderInDeviationLocal.containsKey(leaderSeq))
                                        relevantForLeaderInDeviationLocal.put(leaderSeq, new HashSet<>());
//                                    Double[] seqCombUtilities = algConfig.getGenSumSequenceCombinationUtility(leaderSeq, sequence);
//                                    if (seqCombUtilities!= null && seqCombUtilities[follower.getId()] != 0) {
                                        relevantForLeaderInDeviationLocal.get(leaderSeq).add(relevantSequence);
                                        relevantForLeaderInDeviationLocal.get(leaderSeq).add(new ArrayListSequenceImpl(follower));
//                                    }
                                }
                            }
//                            createISActionConstraint(algConfig, relevantSequence, informationSet);
                        }
                    }
//                    createISActionConstraint(algConfig, new ArrayListSequenceImpl(follower), informationSet);
                }
            }
        }

        for (Sequence leaderSeq : relevantForLeaderInDeviationLocal.keySet()){
            if (!relevantForLeaderInDeviation.containsKey(leaderSeq)) System.out.println("Not present: " + leaderSeq + " : " + relevantForLeaderInDeviationLocal.get(leaderSeq).toString());
            else{
                for (Sequence followerSeq : relevantForLeaderInDeviationLocal.get(leaderSeq))
                    if (!relevantForLeaderInDeviation.get(leaderSeq).contains(followerSeq))
                        System.out.println("Not in list of " + leaderSeq + " : " + followerSeq);
            }
        }
//        relevantForLeaderInDeviation = relevantForLeaderInDeviationLocal;

//        // 8 :
//        for (SequenceInformationSet informationSet : algConfig.getAllInformationSets().values()) {
//            if (informationSet.getPlayer().equals(follower)) {
//                for (Sequence sequence : informationSet.getOutgoingSequences()) {
//                    Object eqKey = new Triplet<>(informationSet, sequence, "eq");
//                    Object varKey = new Pair<>(informationSet, sequence);
//                    Object contVarKey = new Pair<>("v", sequence);
//
//                    if(CONVERT_TO_CANONIC){
//                        Object vKey = new Pair<>("w", varKey);
//                        Object contVKey = new Pair<>("w", sequence);
//                        lpTable.setConstraint(eqKey, vKey, -1);
//                        lpTable.setConstraint(eqKey, contVKey, 1);
//                    }
//                    else {
//                        lpTable.setLowerBound(varKey, Double.NEGATIVE_INFINITY);
//                        lpTable.setLowerBound(contVarKey, Double.NEGATIVE_INFINITY);
//                    }
//                    lpTable.setConstraint(eqKey, varKey, 1);
//                    lpTable.setConstraint(eqKey, contVarKey, -1);
//                    lpTable.setConstraintType(eqKey, 1);
//                }
//            }
//        }
    }

    protected void createISAction(Sequence followerSequence, SequenceInformationSet informationSet){
        for (Action action : followerSequence) {
            for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
                Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
                seq.addLast(relevantAction);
                createISActionConstraint(followerSequence, seq ,informationSet);
            }
        }
        Sequence seq = new ArrayListSequenceImpl(follower);
        createISActionConstraint(followerSequence, seq ,informationSet);
    }

    protected void createReverseISAction(SequenceInformationSet reachableSet){
        Sequence followerSequence = reachableSet.getPlayersHistory();
        for (Action action : followerSequence) {
            for (Action relevantAction : expander.getActions((SequenceInformationSet) action.getInformationSet())) {
                Sequence seq = new ArrayListSequenceImpl(((SequenceInformationSet) action.getInformationSet()).getPlayersHistory());
                seq.addLast(relevantAction);
                Object eqKey = new Triplet<>(followerSequence.getLast().getInformationSet(), followerSequence, seq);
                Object varKey = new Pair<>(reachableSet, seq);
                lpTable.setConstraint(eqKey, varKey, -1);
                if(CONVERT_TO_CANONIC){
                    Object vKey = new Pair<>("w", varKey);
                    lpTable.setConstraint(eqKey, vKey, 1);
                }
            }
        }
        if (followerSequence.isEmpty()) return;

        Sequence seq = new ArrayListSequenceImpl(follower);
        Object eqKey = new Triplet<>(followerSequence.getLast().getInformationSet(), followerSequence, seq);
        Object varKey = new Pair<>(reachableSet, seq);
        lpTable.setConstraint(eqKey, varKey, -1);
        if(CONVERT_TO_CANONIC){
            Object vKey = new Pair<>("w", varKey);
            lpTable.setConstraint(eqKey, vKey, 1);
        }
    }


    protected void createISActionConstraint(Sequence sequence, Sequence followerSequence, SequenceInformationSet informationSet) {
//        for (Sequence sequence : informationSet.getOutgoingSequences()) {
            Object eqKey = new Triplet<>(informationSet, sequence, followerSequence);
            Object varKey = new Pair<>(informationSet, followerSequence);
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
//            if (algConfig.getReachableSets(sequence) != null)
//                for (SequenceInformationSet reachableSet : algConfig.getReachableSets(sequence)) {
//                    if (reachableSet.getOutgoingSequences() != null && !reachableSet.getOutgoingSequences().isEmpty()) {
//                        varKey = new Pair<>(reachableSet, followerSequence);
//                        lpTable.setConstraint(eqKey, varKey, -1);
//                        if(CONVERT_TO_CANONIC){
//                            vKey = new Pair<>("w", varKey);
//                            lpTable.setConstraint(eqKey, vKey, 1);
//                        }
//                    }
//                }
//        }
    }

    @Override
    protected void createSequenceConstraint(StackelbergConfig algConfig, Sequence followerSequence){

        // 6:
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
        if(followerSequence.size() > 0){
            Sequence followerSubSequence = followerSequence.getSubSequence(followerSequence.size() - 1);
            lpTable.setConstraint(followerSubSequence, varKey, -1);
        }

        if (followerSequence.isEmpty()) return;

        // 8:
        InformationSet informationSet = followerSequence.getLast().getInformationSet();
//        System.out.println(informationSet);
        Object eqKey = new Triplet<>(informationSet, followerSequence, "eq");
        varKey = new Pair<>(informationSet, followerSequence);
        Object contVarKey = new Pair<>("v", followerSequence);

        if(CONVERT_TO_CANONIC){
            Object vKey = new Pair<>("w", varKey);
            Object contVKey = new Pair<>("w", followerSequence);
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

    public long getDeviationIdentificationTime() {
        return deviationIdentificationTime;
    }

    public long getRestrictedGameGenerationTime() {
        return restrictedGameGenerationTime;
    }



}
