package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPData;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Jakub Cerny on 11/10/2017.
 */
public class LeaderGeneration2pDualComparingSefceLP extends LeaderGenerationTwoPlayerSefceLP {

    HashMap<Object, Double> lastDuals = null;

    public LeaderGeneration2pDualComparingSefceLP(Player leader, GameInfo info) {
        super(leader, info);
        MAX = false;
    }

    public LeaderGeneration2pDualComparingSefceLP(Player leader, GameInfo info, boolean greedy, boolean max) {
        super(leader, info, greedy, max);
        MAX = false;
    }

    @Override
    protected HashSet<Sequence> findLeaderDeviation(LPData lpData){
//        HashSet<Object> constraints = newlpData.getWatchedDualVariables().keySet();
        HashSet<Sequence> deviations = new HashSet<>();
        HashMap<Object, Double> duals = new HashMap<>();
        Sequence minSequence = null;
        double minCost = Double.POSITIVE_INFINITY;
        double maxCost = Double.NEGATIVE_INFINITY;
        HashSet<Sequence> potentialDeviations;
        if (lastDuals == null) potentialDeviations = new HashSet<>(algConfig.getSequencesFor(leader));
        else potentialDeviations = new HashSet<>();
        int diff = 0;
        try {
            for (Object con : lpData.getWatchedDualVariables().keySet()) {
                duals.put(con, lpData.getSolver().getDual(lpData.getWatchedDualVariables().get(con)));
                if (lastDuals != null){
                    if (!lastDuals.containsKey(con) || Math.abs(lastDuals.get(con) - duals.get(con)) > eps) {
                        diff++;
                        if (con instanceof Sequence) {
                            for (Sequence leaderSequence  : algConfig.getCompatibleSequencesFor((Sequence) con))
                                if (!leaderRG.contains(leaderSequence))
                                    potentialDeviations.add(leaderSequence);
//                            potentialDeviations.addAll(algConfig.getCompatibleSequencesFor((Sequence) con));
                        }
                        if (con instanceof Pair && ((Pair)con).getLeft() instanceof SequenceInformationSet && ((SequenceInformationSet)((Pair) con).getLeft()).getPlayer().equals(leader)) {
                            for (Sequence leaderSequence : ((SequenceInformationSet) ((Pair) con).getLeft()).getOutgoingSequences())
                                if (!leaderRG.contains(leaderSequence))
                                    potentialDeviations.add(leaderSequence);
//                            potentialDeviations.addAll(((SequenceInformationSet)((Pair) con).getLeft()).getOutgoingSequences());
                        }
                        if (con instanceof Triplet && ((Triplet)con).getFirst() instanceof InformationSetImpl && ((Triplet)con).getSecond() instanceof Sequence && ((Sequence)((Triplet)con).getSecond()).getPlayer().equals(follower)) {
                            for (Sequence leaderSequence : algConfig.getCompatibleSequencesFor((Sequence) ((Triplet) con).getSecond()))
                                if (!leaderRG.contains(leaderSequence))
                                    potentialDeviations.add(leaderSequence);
//                            potentialDeviations.addAll(algConfig.getCompatibleSequencesFor((Sequence) ((Triplet) con).getSecond()));
                        }
                    }
                }
            }

        }
        catch (Exception e){e.printStackTrace();}
//        if (lastDuals != null){
//            potentialDeviations = new HashSet<>();
//            for (Object o : duals.keySet()) {
////                System.out.println(o + " : last = " + lastDuals.get(o) + "; current = " + duals.get(o));
//                if (!lastDuals.containsKey(o) || Math.abs(lastDuals.get(o) - duals.get(o)) > eps) {
//                    diff++;
//                    if (o instanceof Sequence) {
//                        potentialDeviations.addAll(algConfig.getCompatibleSequencesFor((Sequence) o));
//                    }
//                    if (o instanceof Pair && ((Pair)o).getLeft() instanceof SequenceInformationSet && ((SequenceInformationSet)((Pair) o).getLeft()).getPlayer().equals(leader))
//                        potentialDeviations.addAll(((SequenceInformationSet)((Pair) o).getLeft()).getOutgoingSequences());
//                    if (o instanceof Triplet && ((Triplet)o).getFirst() instanceof InformationSetImpl && ((Triplet)o).getSecond() instanceof Sequence && ((Sequence)((Triplet)o).getSecond()).getPlayer().equals(follower))
//                        potentialDeviations.addAll(algConfig.getCompatibleSequencesFor((Sequence) ((Triplet)o).getSecond()));
//                }
//            }
////            System.out.println("Diffs = " + diff + " / " + lastDuals.size());
////            System.out.println(potentialDeviations.size() + " / " + algConfig.getSequencesFor(leader).size());
//        }
//        else{
////            MAX = false;
//            potentialDeviations =  new HashSet<>(algConfig.getSequencesFor(leader));
//        }
//        for (Sequence seq : potentialDeviations) if (!seq.getPlayer().equals(leader)) System.out.println("CHYBA: " + seq);
        lastDuals = duals;
        System.out.printf(potentialDeviations.size() + "/" + (algConfig.getSequencesFor(leader).size()-leaderRG.size()) + "...");
        for (Sequence leaderSequence : potentialDeviations){
//            if(leaderSequence.isEmpty()) continue;
//            if(true){
//            if(!leaderRG.contains(leaderSequence)){
//                System.out.println(leaderSequence);
                HashMap<Sequence, Double> costs = new HashMap<>();
                for (Sequence followerSequence : algConfig.getCompatibleSequencesFor(leaderSequence)){
//                    if (lpTable.exists(createSeqPairVarKey(leaderSequence, followerSequence)) || lpData.getWatchedPrimalVariables().containsKey(createSeqPairVarKey(leaderSequence, followerSequence)))
//                        System.out.println("ERROR!");
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
                                        if(CHECK_EXISTENCE && !lpTable.existsEqKey(eqKey)) continue;
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


                // In use only for tests on existing variables
//                for (SequenceInformationSet set : algConfig.getAllInformationSets().values()) {
//                    // 4 -> prvni cast
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
//                    // 5
//                    if (set.getPlayer().equals(follower) && !set.getOutgoingSequences().isEmpty()) {
//                        if(relevantForFollowerInP.containsKey(set)){
//                            if (relevantForFollowerInP.get(set).contains(leaderSequence)){
////                                if (!leaderSequences.contains(leaderSequence)) continue;
////                    for (Sequence leaderSequence : leaderSequences) {
////                        if (relevantForFollowerInP.containsKey(leaderSequence) && relevantForFollowerInP.get(leaderSequence).contains(set.getPlayersHistory())) {
//                                Object eqKey = new Pair<SequenceInformationSet, Sequence>(set, leaderSequence);
//                                if (!duals.containsKey(eqKey)) continue;
//                                if(!leaderRG.contains(leaderSequence)) System.exit(0);
//                                if (!costs.containsKey(set.getPlayersHistory()))
//                                    costs.put(set.getPlayersHistory(), 0.0);
//                                costs.put(set.getPlayersHistory(), costs.get(set.getPlayersHistory()) + 1 * duals.get(eqKey));
////                                lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, set.getPlayersHistory()), 1.0);
//                                for (Sequence outgoing : set.getOutgoingSequences()) {
////                                    lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, outgoing), -1.0);
//                                    if (!costs.containsKey(outgoing))
//                                        costs.put(outgoing, 0.0);
//                                    costs.put(outgoing, costs.get(outgoing) - 1 * duals.get(eqKey));
//                                }
////                                lpTable.setConstant(eqKey, 0.0);
////                                lpTable.setConstraintType(eqKey, 1);
//                            }
//                        }
//                    }
//                }


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
                double max = Collections.max(costs.values());
//                if (Math.abs(min) > 0 || Math.abs(max) > 0) {
//                    System.out.println();
//                    System.out.println("Leader sequence: " + leaderSequence);
//                    System.out.println("Corresponding follower sequences and RVs: ");
//                    for (Sequence seq : costs.keySet())
//                        if (Math.abs(costs.get(seq)) > 0) System.out.println(seq + " : " + costs.get(seq));
//                }
                if (max > maxCost) maxCost = max;
//                if ( max >  0.0 * -eps) {
                if ( min <  0.0 * -eps) { // !algConfig.getCompatibleSequencesFor(leaderSequence).isEmpty() &&
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

//            }
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

        for (Sequence seq : deviations)
            if (!potentialDeviations.contains(seq))
                System.out.println("Not containing : " + seq);

        return  deviations;

//        return new HashSet<>();
    }

}
