package cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.gadgets;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by Jakub Cerny on 13/12/2017.
 */
public class GadgetSefceLPWithoutMiddleState extends GadgetSefceLP {

    public GadgetSefceLPWithoutMiddleState(Player leader, GameInfo info) {
        super(leader, info);
    }


    @Override
    protected void makeGadget(GameState state){
        createdGadgets.add(state);
        gadgetsCreated++;
//        if (gadgetRoots.contains(set)) return;
        HashMap<Object, HashSet<Object>> varsToDeleteForState = new HashMap<>();
        varsToDelete.put(state, varsToDeleteForState);
        HashSet<Object> utilityToDeleteForState = new HashSet<>();
        utilityToDelete.put(state, utilityToDeleteForState);
        HashSet<Object> blackList = new HashSet<>();
        Sequence followerSequence = state.getSequenceFor(follower);

        // zpracuj i root !!
        createPContinuationConstraint(blackList, state.getSequenceFor(leader), followerSequence, null);
        createSequenceConstraint(algConfig, followerSequence);

        // update constraints and OBJECTIVE
        // remember which were updated so that they can be later discarded

        // 1. layer (4,5)
        Sequence leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
//        GadgetAction middleAction = new GadgetAction(algConfig.getInformationSetFor(state), state.getISKeyForPlayerToMove());
//        leaderSequence.addLast(middleAction);
//        createPContinuationConstraintInState(blackList, new ArrayList<Action>(){{add(middleAction);}}, state.getSequenceForPlayerToMove(), followerSequence, algConfig.getInformationSetFor(state));
//        createPContinuationConstraint(blackList, leaderSequence, followerSequence, null);

        gadgetRootsSequences.add(leaderSequence);
        if (!gadgetRoots.containsKey(leaderSequence))
            gadgetRoots.put(leaderSequence, new HashSet<>());
        gadgetRoots.get(leaderSequence).add(state);
//        System.out.println(gadgetRoots.get(leaderSequence).size());


        // 2. layer (4,5,6,7)
        SequenceInformationSet gadgetSet = new GadgetInformationSet(state, leaderSequence);

        // 6, 7 :
        ArrayList<double[]> leavesUnder = getLeavesUnder(state);
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

            if (DISCOUNT_GADGETS)
                lpTable.setObjective(createSeqPairVarKey(leaderSequence, followerSequence), u[leader.getId()] - GADGET_DISCOUNT);
            else
                lpTable.setObjective(createSeqPairVarKey(leaderSequence, followerSequence), u[leader.getId()]);
            utilityToDeleteForState.add(createSeqPairVarKey(leaderSequence, followerSequence));

            if (u[follower.getId()] != 0.0){
                lpTable.setConstraint(followerSequence, createSeqPairVarKey(leaderSequence, followerSequence), -u[follower.getId()]);

                if (!varsToDeleteForState.containsKey(followerSequence))
                    varsToDeleteForState.put(followerSequence, new HashSet<>());
                varsToDeleteForState.get(followerSequence).add(createSeqPairVarKey(leaderSequence, followerSequence));


                // 7 :
                for (Action action : followerSequence) {
                    for (Sequence relevantSequence : ((SequenceInformationSet) action.getInformationSet()).getOutgoingSequences()) {
                        Object eqKey = new Triplet<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, relevantSequence);
                        lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, relevantSequence), -u[follower.getId()]);

                        if (!varsToDeleteForState.containsKey(eqKey))
                            varsToDeleteForState.put(eqKey, new HashSet<>());
                        varsToDeleteForState.get(eqKey).add(createSeqPairVarKey(leaderSequence, relevantSequence));
                    }
                }

                Object eqKey = new Triplet<>(followerSequence.getLastInformationSet().getISKey(), followerSequence, new ArrayListSequenceImpl(follower));
                lpTable.setConstraint(eqKey, createSeqPairVarKey(leaderSequence, new ArrayListSequenceImpl(follower)), -u[follower.getId()]);
                if (!varsToDeleteForState.containsKey(eqKey))
                    varsToDeleteForState.put(eqKey, new HashSet<>());
                varsToDeleteForState.get(eqKey).add(createSeqPairVarKey(leaderSequence, new ArrayListSequenceImpl(follower)));

            }
        }
        leaderSequence = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
//        leaderSequence.addLast(middleAction);
        createPContinuationConstraintInState(blackList, actions, leaderSequence, followerSequence, state.getISKeyForPlayerToMove());
//        System.out.println(outgoingSeqs.size() + " " + actions.size());
        for(Sequence outgoing : outgoingSeqs){
            createPContinuationConstraintInGadget(blackList, outgoing, followerSequence, outgoingSeqs);
        }

        eqsToDelete.put(state, blackList);
//        System.out.println("MAKING : " + state.hashCode() + " / " + blackList.size());
//        System.out.println("BS///");
//        for (Object o : blackList)
//            System.out.println(o);
//        System.out.println("BE///");

    }


}
