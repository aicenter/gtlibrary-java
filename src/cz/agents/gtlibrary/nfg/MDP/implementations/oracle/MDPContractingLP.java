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
import cz.agents.gtlibrary.nfg.MDP.DoubleOracleCostPairedMDP;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;

/**
 * Created by bosansky on 1/7/14.
 */
public class MDPContractingLP extends MDPOracleLP {

    protected Set<MDPStateActionMarginal> actionsToRemove = null;
    private Map<MDPState, IloNumVar> fVariables = new HashMap<MDPState, IloNumVar>();
    private Map<MDPState, IloRange> fConstraints = new HashMap<MDPState, IloRange>();
    private Map<MDPState, Double> fValues = new HashMap<MDPState, Double>();

    public MDPContractingLP(Collection<Player> allPlayers, Map<Player, MDPStrategy> playerStrategy, MDPConfig config) {
        super(allPlayers, playerStrategy, config);
    }

    public void setActionsToRemove(Set<MDPStateActionMarginal> actionsToRemove) {
        this.actionsToRemove = actionsToRemove;
    }

    @Override
    protected void updateLPFromStrategies(Player player, Set<MDPStateActionMarginal> newActions) {

        for (MDPStateActionMarginal m : actionsToRemove) {
            Player opponent = config.getOtherPlayer(m.getPlayer());
            try {
                if (variables.containsKey(m)) {
                    getLpModels().get(m.getPlayer()).delete(variables.get(m));
                }
                if (constraints.containsKey(m)) {
                    getLpModels().get(opponent).delete(constraints.get(m));
                }
            }
            catch (IloException e) {
                e.printStackTrace();
            } finally {
                variables.remove(m);
                constraints.remove(m);
            }
        }
        super.updateLPFromStrategies(player, newActions);
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
            for (Map.Entry<MDPStateActionMarginal, Double> e : strategy.getPredecessors(state).entrySet()) {
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

        if (DoubleOracleCostPairedMDP.CONTRACTING) {
            IloNumVar fV = null;
            if (fVariables.containsKey(state)) {
                fV = fVariables.get(state);
                cplex.delete(fConstraints.get(state));
            } else {
                fV = cplex.numVar(0,1, IloNumVarType.Float, "f" + state.toString());
            }

            fVariables.put(state, fV);
            fConstraints.put(state, cplex.addEq(cplex.diff(fV,RS),0));
        }
    }

    public void extractStrategyForPlayer(Player player) {
        super.extractStrategyForPlayer(player);
        for (MDPState state : playerStrategy.get(player).getStates()) {
            IloNumVar fV = fVariables.get(state);
            double v = 0;
            if (fV != null) {
                try {
                    v = getLpModels().get(player).getValue(fV);
                    if (v < 1e-8) v =0;
                } catch (IloException e) {
                    v = 0;
                }
            }
            fValues.put(state, v);
        }
    }

    public Map<MDPState, Double> getfValues() {
        return fValues;
    }
}
