package cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPCoreLP;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/24/13
 * Time: 9:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class MDPOracleLP extends MDPCoreLP {

//    protected Map<Object, IloRange> constraints = new HashMap<Object, IloRange>();
//    protected Map<Object, IloNumVar> variables = new HashMap<Object, IloNumVar>();
    protected Set<MDPStateActionMarginal> newActions = null;
    protected Map<Player, Map<MDPStateActionMarginal, Map<IloConstraint, MDPStateActionMarginal>>> whereIsMyAction = new HashMap<Player, Map<MDPStateActionMarginal, Map<IloConstraint, MDPStateActionMarginal>>>();

    public MDPOracleLP(Collection<Player> allPlayers, Map<Player, MDPStrategy> playerStrategy, MDPConfig config) {
        super(allPlayers, playerStrategy, config);
        whereIsMyAction.put(config.getAllPlayers().get(0), new HashMap<MDPStateActionMarginal, Map<IloConstraint, MDPStateActionMarginal>>());
        whereIsMyAction.put(config.getAllPlayers().get(1), new HashMap<MDPStateActionMarginal, Map<IloConstraint, MDPStateActionMarginal>>());
    }

    public double solveForPlayer(Player player) {
        IloCplex cplex = getLpModels().get(player);
        if (newActions == null) {
            buildLPFromStrategies(player);
        } else {
            updateLPFromStrategies(player, newActions);
        }
        try {
            cplex.exportModel("MDP-LP"+player.getId()+".lp");
            cplex.solve();
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

    private void updateLPFromStrategies(Player player, Set<MDPStateActionMarginal> newActions) {
        Player opponent = config.getOtherPlayer(player);
        try {
            for (MDPStateActionMarginal mdpStateActionMarginal : newActions) {
                if (mdpStateActionMarginal.getPlayer().equals(player)) { // my new Action
                    createVariableForStateAction(getLpModels().get(player), mdpStateActionMarginal, player);
                } else { // opponent's new action
                    MDPState s = mdpStateActionMarginal.getState();
                    if (playerStrategy.get(opponent).hasStateASuccessor(s) && !s.isRoot())
                        createVariableForMDPState(getLpModels().get(player), s);
                }
            }
            for (MDPStateActionMarginal mdpStateActionMarginal : newActions) {
                if (mdpStateActionMarginal.getPlayer().equals(player)) { // my new Action
                    if (constraints.containsKey(mdpStateActionMarginal.getState())) {
                        getLpModels().get(player).delete(constraints.get(mdpStateActionMarginal.getState()));
                    }
                    createConstraintForStrategy(getLpModels().get(player), player, mdpStateActionMarginal.getState());

                    for (MDPState state : playerStrategy.get(player).getSuccessors(mdpStateActionMarginal).keySet()) {
                        if (!playerStrategy.get(player).hasStateASuccessor(state)) continue;
                        if (constraints.containsKey(state)) {
                            getLpModels().get(player).delete(constraints.get(state));
                        }
                        createConstraintForStrategy(getLpModels().get(player), player, state);
                    }
                    for (MDPStateActionMarginal opp : playerStrategy.get(opponent).getAllMarginalsInStrategy()) {
                        if (playerStrategy.get(player).getUtilityFromCache(mdpStateActionMarginal, opp) != 0) {
                            IloConstraint c = constraints.remove(opp);
                            if (c != null) {
                                getLpModels().get(player).delete(c);
                            }
                            createConstraintForExpValues(getLpModels().get(player), player, opp);
                        }
                    }
                    for (MDPStateActionMarginal precedingAction : playerStrategy.get(player).getPredecessors(mdpStateActionMarginal.getState()).keySet()) {
                        if (!whereIsMyAction.get(player).containsKey(precedingAction)) continue;
                        for (IloConstraint c : new HashSet<IloConstraint>(whereIsMyAction.get(player).get(precedingAction).keySet())) {
                            MDPStateActionMarginal opp = whereIsMyAction.get(player).get(precedingAction).remove(c);
                            IloConstraint cc = constraints.remove(opp);
                            if (cc != null) {
                                getLpModels().get(player).delete(cc);
                            }
                            createConstraintForExpValues(getLpModels().get(player), player, opp);
                        }
                    }
                } else { // opponent's new action
                    IloConstraint c = constraints.remove(mdpStateActionMarginal);
                    if (c != null) {
                        getLpModels().get(player).delete(c);
                    }
                    createConstraintForExpValues(getLpModels().get(player), player, mdpStateActionMarginal);
                    for (MDPStateActionMarginal precedingAction : playerStrategy.get(opponent).getPredecessors(mdpStateActionMarginal.getState()).keySet()) {
                        IloConstraint p = constraints.remove(precedingAction);
                        if (p != null) {
                            getLpModels().get(player).delete(p);
                        }
                        createConstraintForExpValues(getLpModels().get(player), player, precedingAction);
                    }
                }
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
        HashSet<MDPStateActionMarginal> rememberThisConstraintForActions = new HashSet<MDPStateActionMarginal>();
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
            double utValue = playerStrategy.get(player).getUtilityFromCache(myActions, opponentsStateAction);
            if (utValue != 0) {
                sumR = cplex.sum(sumR, cplex.prod(x, utValue));
            }
            if (playerStrategy.get(player).getSuccessors(myActions) == null && playerStrategy.get(player).getAllSuccessors(myActions) != null) {
                // we remember only actions that currently do not have a successor, but there is one in the original game
                rememberThisConstraintForActions.add(myActions);
            }
        }

        IloRange c = null;
        if (player.getId() == 0) {
            c = cplex.addLe(cplex.diff(LS, sumR), 0);
            constraints.put(opponentsStateAction, c);
        } else {
            c = cplex.addGe(cplex.diff(LS, sumR), 0);
            constraints.put(opponentsStateAction, c);
        }

        for (MDPStateActionMarginal m : rememberThisConstraintForActions) {
            Map<IloConstraint, MDPStateActionMarginal> tmp = whereIsMyAction.get(player).get(m);
            if (tmp == null) tmp = new HashMap<IloConstraint, MDPStateActionMarginal>();
            tmp.put(c, opponentsStateAction);
            whereIsMyAction.get(player).put(m,tmp);
        }
    }

}
