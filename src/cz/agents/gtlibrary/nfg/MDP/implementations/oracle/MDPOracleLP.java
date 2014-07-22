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


package cz.agents.gtlibrary.nfg.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.core.MDPCoreLP;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/24/13
 * Time: 9:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class MDPOracleLP extends MDPCoreLP {

    private static boolean SAVELP = false;

//    protected Map<Object, IloRange> constraints = new HashMap<Object, IloRange>();
//    protected Map<Object, IloNumVar> variables = new HashMap<Object, IloNumVar>();
    protected Set<MDPStateActionMarginal> newActions = null;
//    protected Map<Player, Map<MDPStateActionMarginal, Set<MDPStateActionMarginal>>> whereIsMyAction = new HashMap<Player, Map<MDPStateActionMarginal, Set<MDPStateActionMarginal>>>();

    public MDPOracleLP(Collection<Player> allPlayers, Map<Player, MDPStrategy> playerStrategy, MDPConfig config) {
        super(allPlayers, playerStrategy, config);
//        whereIsMyAction.put(config.getAllPlayers().get(0), new HashMap<MDPStateActionMarginal, Set<MDPStateActionMarginal>>());
//        whereIsMyAction.put(config.getAllPlayers().get(1), new HashMap<MDPStateActionMarginal, Set<MDPStateActionMarginal>>());
    }

    public double solveForPlayer(Player player) {
        IloCplex cplex = getLpModels().get(player);
        long start = threadBean.getCurrentThreadCpuTime();
        if (newActions == null) {
            buildLPFromStrategies(player);
        } else {
            updateLPFromStrategies(player, newActions);
        }
        BUILDING_LP_TIME += threadBean.getCurrentThreadCpuTime() - start;
        try {
            if (SAVELP) cplex.exportModel("MDP-LP"+player.getId()+".lp");
            start = System.nanoTime();
            cplex.solve();
            SOLVING_LP_TIME += System.nanoTime() - start;
            if (cplex.getStatus() != IloCplex.Status.Optimal) {
                System.out.println(cplex.getStatus());
                assert false;
            }
            setFinalValue(cplex.getValue(getObjectives().get(player)));
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return getFinalValue();
    }

    private void buildLPFromStrategies(Player player) {
        Player opponent = config.getOtherPlayer(player);
        try {
            for (MDPState s : playerStrategy.get(opponent).getStates()) {
                if (playerStrategy.get(opponent).hasStateASuccessor(s) && !s.isRoot())
                    createVariableForMDPState(getLpModels().get(player), s);
            }
            for (MDPStateActionMarginal sam : playerStrategy.get(player).getAllMarginalsInStrategy()) {
                createVariableForStateAction(getLpModels().get(player), sam, player);
            }
            for (MDPStateActionMarginal sam : playerStrategy.get(opponent).getAllMarginalsInStrategy()) {
                createConstraintForExpValues(getLpModels().get(player), player, sam);
            }
            for (MDPState s : playerStrategy.get(player).getStates()) {
                createConstraintForStrategy(getLpModels().get(player), player, s);
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    protected void updateLPFromStrategies(Player player, Set<MDPStateActionMarginal> newActions) {
        Player opponent = config.getOtherPlayer(player);
        HashSet<MDPStateActionMarginal> opponentMarginalsGenerate = new HashSet<MDPStateActionMarginal>();
        HashSet<Object[]> opponentMarginalsUpdate = new HashSet<Object[]>();
        HashSet<MDPState> myStates = new HashSet<MDPState>();
        try {
            for (MDPStateActionMarginal mdpStateActionMarginal : newActions) {
                if (mdpStateActionMarginal.getPlayer().equals(player)) { // my new Action
                    createVariableForStateAction(getLpModels().get(player), mdpStateActionMarginal, player);
                    myStates.add(mdpStateActionMarginal.getState());
                    for (MDPState state : playerStrategy.get(player).getSuccessors(mdpStateActionMarginal).keySet()) {
                        if (!playerStrategy.get(player).hasStateASuccessor(state)) continue;
                        myStates.add(state);
                    }

                    for (MDPStateActionMarginal opp : playerStrategy.get(opponent).getAllMarginalsInStrategy()) {
                           if (constraints.containsKey(opp)) {
                               double curUtility = playerStrategy.get(player).getUtility(mdpStateActionMarginal, opp);
                               if (curUtility != 0) {
                                   opponentMarginalsUpdate.add(new Object[]{mdpStateActionMarginal, opp, curUtility});
                               }
//                                    for (MDPStateActionMarginal precedingAction : playerStrategy.get(player).getPredecessors(mdpStateActionMarginal.getState()).keySet()) {
//                                        opponentMarginalsUpdate.add(new Object[]{precedingAction, opp});
//                                    }
                           } else
                            {
                                opponentMarginalsGenerate.add(opp);
                            }
                    }

                } else { // opponent's new action
                    MDPState s = mdpStateActionMarginal.getState();
                    if (playerStrategy.get(opponent).hasStateASuccessor(s) && !s.isRoot())
                        createVariableForMDPState(getLpModels().get(player), s);
                    opponentMarginalsGenerate.add(mdpStateActionMarginal);

                    for (MDPStateActionMarginal precedingAction : playerStrategy.get(opponent).getPredecessors(mdpStateActionMarginal.getState()).keySet()) {
                        opponentMarginalsGenerate.add(precedingAction);
                    }
                }
            }

            for (MDPStateActionMarginal removedLastAction : MDPIterativeStrategy.getRemovedLastActions(player)) {
                if (!variables.containsKey(removedLastAction)) continue;
                for (MDPStateActionMarginal opp : playerStrategy.get(opponent).getAllMarginalsInStrategy()) {
                    if (constraints.containsKey(opp)) {
                        double curUtility = playerStrategy.get(player).getUtility(removedLastAction, opp);
                        opponentMarginalsUpdate.add(new Object[]{removedLastAction, opp, curUtility});
                    } else
                    {
                        opponentMarginalsGenerate.add(opp);
                    }
                }
            }

            for (MDPState state : myStates) {
                if (constraints.containsKey(state)) {
                    getLpModels().get(player).delete(constraints.get(state));
                }
                createConstraintForStrategy(getLpModels().get(player), player, state);
            }

            for (Object[] pair : opponentMarginalsUpdate) {
                updateCoefficient((MDPStateActionMarginal)pair[0], (MDPStateActionMarginal)pair[1], (Double)pair[2]);
            }

            for (MDPStateActionMarginal opp : opponentMarginalsGenerate) {
                IloConstraint cc = constraints.remove(opp);
                if (cc != null) {
                    getLpModels().get(player).delete(cc);
                }
                createConstraintForExpValues(getLpModels().get(player), player, opp);

            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setNewActions(Set<MDPStateActionMarginal> newActions) {
        this.newActions = newActions;
    }

    @Override
    protected void createConstraintForExpValues(IloCplex cplex, Player player, MDPStateActionMarginal opponentsStateAction) throws IloException {
        IloNumExpr sumR = cplex.constant(0);
        IloNumExpr LS = variables.get(opponentsStateAction.getState());
//        HashSet<MDPStateActionMarginal> rememberThisConstraintForActions = new HashSet<MDPStateActionMarginal>();
        assert (LS != null);
        Map<MDPState, Double> successors = playerStrategy.get(opponentsStateAction.getPlayer()).getSuccessors(opponentsStateAction);
        for (MDPState s : successors.keySet()) {
            if (variables.containsKey(s)) {
                sumR = cplex.sum(sumR, cplex.prod(successors.get(s), variables.get(s)));
            }
        }

        for (MDPStateActionMarginal myActions : playerStrategy.get(player).getAllMarginalsInStrategy()) {
            IloNumVar x = variables.get(myActions);
            assert (x != null);
            double utValue = playerStrategy.get(player).getUtility(myActions, opponentsStateAction);
            if (utValue != 0) {
                sumR = cplex.sum(sumR, cplex.prod(x, utValue));
            }

//            if (!playerStrategy.get(player).isActionFullyExpandedInRG(myActions) || !playerStrategy.get(config.getOtherPlayer(player)).isActionFullyExpandedInRG(opponentsStateAction)) {
                // we remember only actions that currently do not have a successor, but there is one in the original game
//                rememberThisConstraintForActions.add(myActions);
//            }
        }

        IloRange c = null;
        if (player.getId() == 0) {
            c = cplex.addLe(cplex.diff(LS, sumR), 0, opponentsStateAction.toString());
            constraints.put(opponentsStateAction, c);
        } else {
            c = cplex.addGe(cplex.diff(LS, sumR), 0, opponentsStateAction.toString());
            constraints.put(opponentsStateAction, c);
        }

//        for (MDPStateActionMarginal m : rememberThisConstraintForActions) {
//            Set<MDPStateActionMarginal> tmp = whereIsMyAction.get(player).get(m);
//            if (tmp == null) tmp = new HashSet<MDPStateActionMarginal>();
//            tmp.add(opponentsStateAction);
//            whereIsMyAction.get(player).put(m,tmp);
//        }
    }

    private void updateCoefficient(MDPStateActionMarginal myAction, MDPStateActionMarginal oppAction, double utility) {
        MDPIterativeStrategy myStrategy = (MDPIterativeStrategy)playerStrategy.get(myAction.getPlayer());
        Player player = myAction.getPlayer();
        IloRange c = constraints.get(oppAction);
//        assert (whereIsMyAction.get(player).get(myAction).contains(oppAction));
//        double utility = myStrategy.getUtility(myAction, oppAction);
        try {
//            int col = getLpModels().get(player).LPMatrix().getIndex(variables.get(myAction));
//            int row = getLpModels().get(player).LPMatrix().getIndex(c);
//            if (col != -1 && row != -1)
//                getLpModels().get(player).LPMatrix().setNZ(row,col,-utility);
//            else
                getLpModels().get(player).setLinearCoef(c, variables.get(myAction), -utility);       // must be "-" because it is on the other side of the inequality
        } catch (IloException e) {
            e.printStackTrace();
            assert false;
        }
    }

//    @Override
//    protected IloNumVar createVariableForStateAction(IloCplex cplex, MDPStateActionMarginal action, Player player) throws IloException {
//        if (variables.containsKey(action)) {
//            return variables.get(action);
//        }
//        String letter = ((player.getId() == 0) ? "x" : "y") + "_";
//        double bound = action.getState().isRoot() ? 0 : action.getState().horizon()*0.00001;
//        IloNumVar result = cplex.numVar(bound, 1, IloNumVarType.Float, letter + action.toString());
//        variables.put(action,result);
//        return result;
//    }
}
