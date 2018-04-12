/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.MDP.domain.bpg;

import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;

import cz.agents.gtlibrary.nfg.MDP.implementations.MDPExpanderImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;


import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class BPExpander extends MDPExpanderImpl {
    private static int[][] allowedTargetsForDefender;
    private static int[][] startingPositions;
    public static int GOALNODE;

    {
        if (BPConfig.graphFile.startsWith("GridW4")) {
            allowedTargetsForDefender = new int[][]{{3,4,5,6},{11,12,13,14}};
            startingPositions = new int[][]{{0},{17,18}};
            GOALNODE = 16;
        } else if (BPConfig.graphFile.startsWith("GridW5")) {
            allowedTargetsForDefender = new int[][]{{2,3,4,5,6},{12,13,14,15,16}};
            startingPositions = new int[][]{{0},{18,19}};
            GOALNODE = 17;
        } else {
            assert false;
        }
    }

    //    private static int[][] allowedTargetsForDefender = {{4,5,6},{10,11,12}}; // allowed nodes for defender; these must be sorted
//    private static int[][] allowedTargetsForDefender = {{3,4,5,6},{11,12,13,14}}; // allowed nodes for defender; these must be sorted
//    private static int[][] allowedTargetsForDefender = {{2,3,4,5,6},{12,13,14,15,16}}; // allowed nodes for defender; these must be sorted
//    private static int[][] startingPositions = {{0},{17,18}};
//    private static int[][] startingPositions = {{0},{18,19}};

//    public static int GOALNODE = 16;
//    public static int GOALNODE = 17;

    @Override
    public List<MDPAction> getActions(MDPState state) {
        List<MDPAction> result = new ArrayList<MDPAction>();
        if (state.isTerminal())
            return result;
        if (state.isRoot()) {
            if (state.getPlayer().getId() == 0) {
                BPAction.UnitMove m = new BPAction.UnitMove(0, -1, -1);
                result.add(new BPAction(state.getPlayer(), new BPAction.UnitMove[]{m}));
            } else {
                BPAction.UnitMove m1 = new BPAction.UnitMove(0, -1, -1);
                BPAction.UnitMove m2 = new BPAction.UnitMove(0, -1, -1);
                result.add(new BPAction(state.getPlayer(), new BPAction.UnitMove[]{m1,m2}));
            }
        } else {
            BPState s = (BPState)state;
            if (s.getTimeStep() >= BPConfig.getMaxTimeStep()) {
                return result;
            }


            if (state.getPlayer().getId() == 0) {
                Node n = s.getGraph().getNodeByID(s.getUnitNodes()[0]);
                for (Edge e : s.getGraph().getGraph().outgoingEdgesOf(n)) {
                    BPAction.UnitMove m = new BPAction.UnitMove(0, n.getIntID(), e.getTarget().getIntID());
                    result.add(new BPAction(state.getPlayer(), new BPAction.UnitMove[]{m}));
                }
            } else {

                LinkedHashSet<BPAction.UnitMove>[] possibleMoves = new LinkedHashSet[s.getUNITS()];
                for (int unitNumber=0; unitNumber < s.getUNITS(); unitNumber++) {

                    Node n = s.getGraph().getNodeByID(s.getUnitNodes()[unitNumber]);
                    LinkedHashSet<BPAction.UnitMove> set = new LinkedHashSet<BPAction.UnitMove>();
                    for (Edge e : s.getGraph().getGraph().outgoingEdgesOf(n)) {
                        if (Arrays.binarySearch(allowedTargetsForDefender[unitNumber],e.getTarget().getIntID()) < 0)
                            continue;
                        BPAction.UnitMove m = new BPAction.UnitMove(unitNumber, n.getIntID(), e.getTarget().getIntID());
                        set.add(m);
                    }
                    possibleMoves[unitNumber] = set;
                }

                for (BPAction.UnitMove um1 : possibleMoves[0]) {
                    for (BPAction.UnitMove um2 : possibleMoves[1]) {
                        BPAction.UnitMove[] moves = new BPAction.UnitMove[2];
                        moves[0] = um1;
                        moves[1] = um2;
                        result.add(new BPAction(state.getPlayer(), moves));
                    }
                }
            }
        }

        if (BPConfig.SHUFFLE) {
            Collections.shuffle(result, new HighQualityRandom(BPConfig.SHUFFLE_ID));
        }
        return result;
    }

    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {

        Map<MDPState, Double> result = new HashMap<MDPState, Double>();

        if (action.getPlayer().getId() == 0) {
            MDPState state = action.getState().copy().performAction(action.getAction());
            if (state != null) {
                result.put(state,1d);
            }
        } else {
            double cumulProb = 0;
            for (boolean flag1 : new boolean[] {true,false}) {
                for (boolean flag2 : new boolean[] {true,false}) {
                    ((BPAction)action.getAction()).getMoves()[0].setWillSeeTheFlag(flag1);
                    ((BPAction)action.getAction()).getMoves()[1].setWillSeeTheFlag(flag2);
                    MDPState state = action.getState().copy().performAction(action.getAction());
                    if (state != null) {
                        double prob = 1;
                        prob *= (flag1)?BPConfig.getFLAG_PROB():(1-BPConfig.getFLAG_PROB());
                        prob *= (flag2)?BPConfig.getFLAG_PROB():(1-BPConfig.getFLAG_PROB());
                        result.put(state,prob);
                        cumulProb += prob;
                        assert ((action.getState().isRoot() && ((BPState)state).getTimeStep() == 0) ||
                                ((BPState)state).getTimeStep() - 1 == ((BPState)action.getState()).getTimeStep());

                    }
                }
            }
            assert (cumulProb <= 1);
            if (cumulProb < 1) {
                for (MDPState s : result.keySet()) {
                    result.put(s,result.get(s)/cumulProb);
                }
            }
        }
        return result;
    }

    @Override
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {

        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();
        HashSet<MDPStateActionMarginal> predecessors = new HashSet<MDPStateActionMarginal>();


        BPState s = (BPState)state;

        if (s.getTimeStep() == 0) {
            return null;
        }

        if (s.getPlayer().getId() == 0) { // attacker
            int currentNode = s.getUnitNodes()[0];
            if (s.getTimeStep() == 1) {
                BPState oldState = (BPState)s.copy();
                oldState.decTimeStep();
                oldState.undoUnitMove(0, startingPositions[0][0], true);
                result.put(new MDPStateActionMarginal(oldState, new BPAction(oldState.getPlayer(), new BPAction.UnitMove[]{new BPAction.UnitMove(0,startingPositions[0][0],currentNode)})),1d);
            } else {
                for (Edge e : s.getGraph().getGraph().incomingEdgesOf(s.getGraph().getNodeByID(currentNode))) {
                    if (s.getFlaggedNodes().contains(e.getSource().getIntID())) {
                        int fromNode = e.getSource().getIntID();
                        boolean[] set = (s.getFlaggedNodes().size() + 1 <= s.getTimeStep()) ? (new boolean[] {true, false}) : (new boolean[] {true});
                        for (boolean observed : set) {
                            BPState oldState = (BPState)s.copy();
                            oldState.decTimeStep();
                            if (oldState.undoUnitMove(0, fromNode, observed)) {
                                BPAction.UnitMove m = new BPAction.UnitMove(0,fromNode,currentNode);
                                m.setWillSeeTheFlag(observed);
                                predecessors.add(new MDPStateActionMarginal(oldState, new BPAction(oldState.getPlayer(), new BPAction.UnitMove[]{m})));
                            }
                        }
                    }
                }
                for (MDPStateActionMarginal a : predecessors) {
                    assert ((a.getState().isRoot() && (s.getTimeStep() == 0)) ||
                            (s.getTimeStep() - 1 == ((BPState)a.getState()).getTimeStep()));
                    result.put(a,1d);
                }
            }
        } else { // defender
            LinkedHashSet<BPAction.UnitMove>[] possibleMoves = new LinkedHashSet[s.getUNITS()];
            if (s.getTimeStep() == 1) {
                for (int unitNumber=0; unitNumber < s.getUNITS(); unitNumber++) {
                    LinkedHashSet<BPAction.UnitMove> set = new LinkedHashSet<BPAction.UnitMove>();
                    BPAction.UnitMove m = new BPAction.UnitMove(unitNumber, startingPositions[1][unitNumber], s.getUnitNodes()[unitNumber]);
                    set.add(m);
                    possibleMoves[unitNumber] = set;
                }
            } else {
                for (int unitNumber=0; unitNumber < s.getUNITS(); unitNumber++) {
                    Node n = s.getGraph().getNodeByID(s.getUnitNodes()[unitNumber]);
                    LinkedHashSet<BPAction.UnitMove> set = new LinkedHashSet<BPAction.UnitMove>();
                    for (Edge e : s.getGraph().getGraph().incomingEdgesOf(n)) {
                        if (Arrays.binarySearch(allowedTargetsForDefender[unitNumber],e.getSource().getIntID()) < 0)
                            continue;
                        BPAction.UnitMove m = new BPAction.UnitMove(unitNumber, e.getSource().getIntID(), n.getIntID());
                        set.add(m);
                    }
                    possibleMoves[unitNumber] = set;
                }
            }

            for (BPAction.UnitMove um1 : possibleMoves[0]) {
                boolean[] set1 = (s.getFlaggedNodesObservedByPatroller().contains(s.getUnitNodes()[0])) ? (new boolean[] {true, false}) : (new boolean[] {false});
                if (s.getFlaggedNodesObservedByPatroller().headSet(7).size() == s.getTimeStep())
                    set1 = new boolean[] {true};
                for (BPAction.UnitMove um2 : possibleMoves[1]) {
                        boolean[] set2 = (s.getFlaggedNodesObservedByPatroller().contains(s.getUnitNodes()[1])) ? (new boolean[] {true, false}) : (new boolean[] {false});
                        if (s.getFlaggedNodesObservedByPatroller().tailSet(9).size() == s.getTimeStep())
                            set2 = new boolean[] {true};
                        BPAction.UnitMove[] moves = new BPAction.UnitMove[2];
                        moves[0] = um1;
                        moves[1] = um2;
                        for (boolean observed1 : set1)
                            for (boolean observed2 : set2) {
                                um1.setWillSeeTheFlag(observed1);
                                um2.setWillSeeTheFlag(observed2);
                                BPState oldState = (BPState)s.copy();
                                oldState.decTimeStep();
                                boolean r1 = true;
                                r1 &= oldState.undoUnitMove(0, um1.getFromNode(),observed1);
                                r1 &= oldState.undoUnitMove(1, um2.getFromNode(),observed2);
                                if (r1) {
//                                    double prob = 1;
//                                    prob *= (observed1)?BPConfig.getFLAG_PROB():(1-BPConfig.getFLAG_PROB());
//                                    prob *= (observed2)?BPConfig.getFLAG_PROB():(1-BPConfig.getFLAG_PROB());
                                    MDPStateActionMarginal am = new MDPStateActionMarginal(oldState, new BPAction(oldState.getPlayer(), new BPAction.UnitMove[]{um1,um2}));
                                    double prob = getSuccessors(am).get(s);

                                    assert ((am.getState().isRoot() && (s.getTimeStep() == 0)) ||
                                            (s.getTimeStep() - 1 == ((BPState)am.getState()).getTimeStep()));

                                    result.put(am,prob);
                                }
                            }
                }
            }
        }
        return result;
    }

    public static int[][] getAllowedTargetsForDefender() {
        return allowedTargetsForDefender;
    }

    public static int[][] getStartingPositions() {
        return startingPositions;
    }
}
