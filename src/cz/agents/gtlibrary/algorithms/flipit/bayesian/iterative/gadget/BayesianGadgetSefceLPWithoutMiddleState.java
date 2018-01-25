package cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.gadget;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetAction;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets.GadgetInformationSet;
import cz.agents.gtlibrary.domain.flipit.types.FollowerType;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Quadruple;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by Jakub Cerny on 13/12/2017.
 */
public class BayesianGadgetSefceLPWithoutMiddleState extends BayesianGadgetSefceLP {

    public BayesianGadgetSefceLPWithoutMiddleState(Player leader, GameInfo info) {
        super(leader, info);
    }


    @Override
    protected void makeGadget(GameState state, FollowerType type){
//        if (gadgetRoots.contains(set)) return;
        HashMap<Object, HashSet<Object>> varsToDeleteForState = new HashMap<>();
        varsToDelete.get(type).put(state, varsToDeleteForState);
        HashSet<Object> utilityToDeleteForState = new HashSet<>();
        utilityToDelete.get(type).put(state, utilityToDeleteForState);
        HashSet<Object> blackList = new HashSet<>();
        Sequence followerSequence = state.getSequenceFor(follower);

        // zpracuj i root !!
        createPContinuationConstraint(blackList, state.getSequenceFor(leader), followerSequence, null, type);
        createSequenceConstraint(algConfigs.get(type.getID()), followerSequence, type);

        // update constraints and OBJECTIVE
        // remember which were updated so that they can be later discarded

        // 1. layer (4,5)
        Sequence leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
//        GadgetAction middleAction = new GadgetAction(algConfig.getInformationSetFor(state), state.getISKeyForPlayerToMove());
//        leaderSequence.addLast(middleAction);
//        createPContinuationConstraintInState(blackList, new ArrayList<Action>(){{add(middleAction);}}, state.getSequenceForPlayerToMove(), followerSequence, algConfig.getInformationSetFor(state));
//        createPContinuationConstraint(blackList, leaderSequence, followerSequence, null);

        gadgetRootsSequences.get(type).add(leaderSequence);
        if (!gadgetRoots.get(type).containsKey(leaderSequence))
            gadgetRoots.get(type).put(leaderSequence, new HashSet<>());
        gadgetRoots.get(type).get(leaderSequence).add(state);
//        System.out.println(gadgetRoots.get(leaderSequence).size());


        // 2. layer (4,5,6,7)
        SequenceInformationSet gadgetSet = new GadgetInformationSet(state, leaderSequence);

        // 6, 7 :
        ArrayList<double[]> leavesUnder = getLeavesUnder(state, type);
        ArrayList<Action> actions = new ArrayList<>();
        LinkedHashSet<Sequence> outgoingSeqs = new LinkedHashSet<>();
        for (int i = 0; i < leavesUnder.size(); i++){
            leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
            GadgetAction leafAction = new GadgetAction(gadgetSet, state, i);
            actions.add(new GadgetAction(gadgetSet, state, i));
//            leaderSequence.addLast(middleAction);
            leaderSequence.addLast(leafAction);
            outgoingSeqs.add(leaderSequence);
            double[] u = leavesUnder.get(i);//.getUtilities();

            lpTable.setObjective(createSeqPairVarKey(leaderSequence, followerSequence, type), u[leader.getId()]);
            utilityToDeleteForState.add(createSeqPairVarKey(leaderSequence, followerSequence, type));

            if (u[follower.getId()] != 0.0){

                Pair<Sequence, FollowerType> seqKey = new Pair<>(followerSequence, type);

                lpTable.setConstraint(seqKey, createSeqPairVarKey(leaderSequence, followerSequence, type), -u[follower.getId()]);

                if (!varsToDeleteForState.containsKey(seqKey))
                    varsToDeleteForState.put(seqKey, new HashSet<>());
                varsToDeleteForState.get(seqKey).add(createSeqPairVarKey(leaderSequence, followerSequence, type));


                // 7 :
                for (Action action : followerSequence) {
                    for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                        Object eqKey = new Quadruple<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, relevantSequence, type);
                        lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, relevantSequence, type), -u[follower.getId()]);

                        if (!varsToDeleteForState.containsKey(eqKey))
                            varsToDeleteForState.put(eqKey, new HashSet<>());
                        varsToDeleteForState.get(eqKey).add(createSeqPairVarKeyCheckExistence(leaderSequence, relevantSequence, type));
                    }
                }

                Object eqKey = new Quadruple<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, new ArrayListSequenceImpl(follower), type);
                lpTable.setConstraint(eqKey, createSeqPairVarKeyCheckExistence(leaderSequence, new ArrayListSequenceImpl(follower),type), -u[follower.getId()]);
                if (!varsToDeleteForState.containsKey(eqKey))
                    varsToDeleteForState.put(eqKey, new HashSet<>());
                varsToDeleteForState.get(eqKey).add(createSeqPairVarKeyCheckExistence(leaderSequence, new ArrayListSequenceImpl(follower), type));

            }
        }
        leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
//        leaderSequence.addLast(middleAction);
        createPContinuationConstraintInState(blackList, actions, leaderSequence, followerSequence, state.getISKeyForPlayerToMove(), type);
//        System.out.println(outgoingSeqs.size() + " " + actions.size());
        for(Sequence outgoing : outgoingSeqs){
            createPContinuationConstraint(blackList, outgoing, followerSequence, outgoingSeqs, type);
        }

        eqsToDelete.get(type).put(state, blackList);
//        System.out.println("MAKING : " + state.hashCode() + " / " + blackList.size());
//        System.out.println("BS///");
//        for (Object o : blackList)
//            System.out.println(o);
//        System.out.println("BE///");

    }


}
