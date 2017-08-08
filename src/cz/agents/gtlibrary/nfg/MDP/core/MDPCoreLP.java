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


package cz.agents.gtlibrary.nfg.MDP.core;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/24/13
 * Time: 9:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class MDPCoreLP {

    protected Map<Object, IloRange> constraints = new HashMap<Object, IloRange>();
    protected Map<Object, IloNumVar> variables = new HashMap<Object, IloNumVar>();

    protected Map<Player, MDPStrategy> playerStrategy = null;

//    private MDPUtilityComputer utilityComputer;
    protected MDPConfig config;

    private double finalValue = -Double.MAX_VALUE;

    private Map<Player, IloCplex> lpModels = new HashMap<Player, IloCplex>();
    private Map<Player, IloNumVar> objectives = new HashMap<Player, IloNumVar>();
    protected long BUILDING_LP_TIME = 0;
    protected long SOLVING_LP_TIME = 0;
//    private Collection<Player> allPlayers = null;
    protected ThreadMXBean threadBean;
    
    public static boolean USE_BARRIER = false;



    public MDPCoreLP(Collection<Player> allPlayers, Map<Player, MDPStrategy> playerStrategy, MDPConfig config) {
        this.threadBean = ManagementFactory.getThreadMXBean();
//        this.allPlayers = allPlayers;
        this.playerStrategy = playerStrategy;
        this.config = config;
//        this.utilityComputer = new MDPUtilityComputer(config);
        for (Player p : allPlayers)
            try {

                IloCplex cplex = new IloCplex();
                if (USE_BARRIER){
                    cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Barrier);
                } else {
                    cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Primal);
                }
                String seed = System.getProperty("CPLEX_SEED");
//                if (seed != null){
//                    cplex.setParam(IloCplex.IntParam.RandomSeed, Integer.parseInt(seed));
//                }
                
//                cplex.setParam(IloCplex.DoubleParam.BarEpComp, 0.1);
//                cplex.setParam(IloCplex.DoubleParam.EpOpt, 0.1);
//                cplex.setParam(IloCplex.BooleanParam.PerInd, true);
//                cplex.setParam(IloCplex.IntParam.PerLim, 1000000);
//                cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Auto);
                cplex.setParam(IloCplex.IntParam.Threads, 1);
//                cplex.setParam(IloCplex.IntParam.CraInd, 1);
//                cplex.setParam(IloCplex.IntParam.ParallelMode, 1);
//                cplex.setParam(IloCplex.IntParam.AuxRootThreads, -1);

                        cplex.setOut(null);
                IloNumVar obj = createVariableForMDPState(cplex, playerStrategy.get(config.getOtherPlayer(p)).getRootState());
                if (p.getId() == 0) {
                  cplex.addMaximize(obj);
                } else if (p.getId() == 1) {
                    cplex.addMinimize(obj);
                } else {
                    assert false;
                }
                objectives.put(p,obj);
                lpModels.put(p,cplex);

            } catch (IloException e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
    }

    public double solveForPlayer(Player player) {
        IloCplex cplex = lpModels.get(player);
        long start = threadBean.getCurrentThreadCpuTime();
        buildLPFromStrategies(player);
        BUILDING_LP_TIME += threadBean.getCurrentThreadCpuTime() - start;
        try {
//            cplex.exportModel("MDP-LP"+player.getId()+".lp");
            start = System.nanoTime();
            cplex.solve();
            SOLVING_LP_TIME += System.nanoTime() - start;

            if (cplex.getStatus() != IloCplex.Status.Optimal) {
                System.out.println(cplex.getStatus());
                assert false;
            }
            finalValue = cplex.getValue(objectives.get(player));
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        return finalValue;
    }

    private void buildLPFromStrategies(Player player) {
        Player opponent = config.getOtherPlayer(player);
        try {
            for (MDPState s : playerStrategy.get(opponent).getStates()) {
                if (playerStrategy.get(opponent).hasStateASuccessor(s) && !s.isRoot())
                    createVariableForMDPState(lpModels.get(player), s);
            }
            for (MDPStateActionMarginal sam : playerStrategy.get(player).getAllMarginalsInStrategy()) {
                createVariableForStateAction(lpModels.get(player), sam, player);
            }
            for (MDPStateActionMarginal sam : playerStrategy.get(opponent).getAllMarginalsInStrategy()) {
                createConstraintForExpValues(lpModels.get(player), player, sam);
            }
            for (MDPState s : playerStrategy.get(player).getStates()) {
                createConstraintForStrategy(lpModels.get(player), player, s);
            }
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    protected IloNumVar createVariableForMDPState(IloCplex cplex, MDPState state) throws IloException {
        if (variables.containsKey(state)) {
            return variables.get(state);
        }
        IloNumVar result = cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, IloNumVarType.Float, "V_" + state.toString());
        variables.put(state,result);
        return result;
    }

    protected IloNumVar createVariableForStateAction(IloCplex cplex, MDPStateActionMarginal action, Player player) throws IloException {
        if (variables.containsKey(action)) {
            return variables.get(action);
        }
        String letter = ((player.getId() == 0) ? "x" : "y") + "_";
        IloNumVar result = cplex.numVar(0, 1, IloNumVarType.Float, letter + action.toString());
        variables.put(action,result);
        return result;
    }

    protected void createConstraintForExpValues(IloCplex cplex, Player player, MDPStateActionMarginal opponentsStateAction) throws IloException {
        IloNumExpr sumR = cplex.constant(0);
        IloNumExpr LS = variables.get(opponentsStateAction.getState());
        if (LS == null)
            assert false;
        Map<MDPState, Double> successors = playerStrategy.get(opponentsStateAction.getPlayer()).getSuccessors(opponentsStateAction);
        for (MDPState s : successors.keySet()) {
            if (variables.containsKey(s)) {
                sumR = cplex.sum(sumR, cplex.prod(successors.get(s), variables.get(s)));
            }
        }

        for (MDPStateActionMarginal myActions : playerStrategy.get(player).getAllMarginalsInStrategy()) {
            IloNumVar x = variables.get(myActions);
            assert (x != null);
//            sumR = cplex.sum(sumR, cplex.prod(x, playerStrategy.get(player).getUtilityFromCache(myActions, opponentsStateAction)));
            sumR = cplex.sum(sumR, cplex.prod(x, playerStrategy.get(player).getUtility(myActions, opponentsStateAction)));
        }

        if (player.getId() == 0) {
            constraints.put(opponentsStateAction, cplex.addLe(cplex.diff(LS, sumR), 0, opponentsStateAction.toString()));
        } else {
            constraints.put(opponentsStateAction, cplex.addGe(cplex.diff(LS, sumR), 0, opponentsStateAction.toString()));
        }
    }

    protected void createConstraintForStrategy(IloCplex cplex, Player player, MDPState state) throws IloException {
        IloNumExpr LS = cplex.constant(0);
        IloNumExpr RS = cplex.constant(0);

        MDPStrategy strategy = playerStrategy.get(player);
        boolean hasSuccessors = false;

        for (MDPAction a : strategy.getActions(state)) {
            MDPStateActionMarginal am = new MDPStateActionMarginal(state,a);
            if (variables.containsKey(am)) {
                hasSuccessors = true;
                RS = cplex.sum(RS, variables.get(am));
            }
        }

        if (!hasSuccessors) return;

        if (state.equals(strategy.getRootState())) {
            LS = cplex.constant(1);
        } else {
            boolean hasLS = false;
            for (Entry<MDPStateActionMarginal, Double> e : strategy.getPredecessors(state).entrySet()) {
                if (variables.containsKey(e.getKey())) {
//                    assert (e.getValue() > 0);
                    hasLS = true;
                    LS = cplex.sum(LS, cplex.prod(e.getValue(), variables.get(e.getKey())));
                } else {
                    assert true;
                }
            }
            assert hasLS;
        }

        constraints.put(state, cplex.addEq(cplex.diff(LS,RS),0, state.toString()));
    }

    public void extractStrategyForPlayer(Player player) {
        for (MDPStateActionMarginal map : playerStrategy.get(player).getAllMarginalsInStrategy()) {
            double v = 0;
            if (variables.containsKey(map)) {
                try {
                    v = lpModels.get(player).getValue(variables.get(map));
                    if (v < 1e-8) v =0;
                } catch (IloException e) {
                    v = 0;
                }
            }
            playerStrategy.get(player).putStrategy(map, v);
        }
    }

    public Map<Player, IloCplex> getLpModels() {
        return lpModels;
    }

    public Map<Player, IloNumVar> getObjectives() {
        return objectives;
    }

    public double getFinalValue() {
        return finalValue;
    }

    public void setFinalValue(double finalValue) {
        this.finalValue = finalValue;
    }

    public long getBUILDING_LP_TIME() {
        return BUILDING_LP_TIME;
    }

    public long getSOLVING_LP_TIME() {
        return SOLVING_LP_TIME;
    }
}
