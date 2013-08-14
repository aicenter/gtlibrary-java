package cz.agents.gtlibrary.nfg.experimental.MDP.core;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPUtilityComputer;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import ilog.concert.*;
import ilog.cplex.IloCplex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

    private Map<Player, MDPStrategy> playerStrategy = null;

    private MDPUtilityComputer utilityComputer;
    private MDPConfig config;

    private double finalValue = -Double.MAX_VALUE;

    private Map<Player, IloCplex> lpModels = new HashMap<Player, IloCplex>();
    private Map<Player, IloNumVar> objectives = new HashMap<Player, IloNumVar>();
    private Collection<Player> allPlayers = null;


    public MDPCoreLP(Collection<Player> allPlayers, Map<Player, MDPStrategy> playerStrategy, MDPConfig config) {
        this.allPlayers = allPlayers;
        this.playerStrategy = playerStrategy;
        this.config = config;
        this.utilityComputer = new MDPUtilityComputer(config);
        for (Player p : allPlayers)
            try {

                IloCplex cplex = new IloCplex();
                cplex.setParam(IloCplex.IntParam.RootAlg, IloCplex.Algorithm.Dual);
    //            cplex.setOut(null);
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
        buildLPFromStrategies(player);

        try {
            cplex.exportModel("MDP-LP"+player.getId()+".lp");
            cplex.solve();
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
            for (MDPStateActionMarginal sam : playerStrategy.get(player).getActionStates()) {
                createVariableForStateAction(lpModels.get(player), sam, player);
            }
            for (MDPStateActionMarginal sam : playerStrategy.get(opponent).getActionStates()) {
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

    private IloNumVar createVariableForMDPState(IloCplex cplex, MDPState state) throws IloException {
        IloNumVar result = cplex.numVar(-1, 1, IloNumVarType.Float, "V_" + state.toString());
        variables.put(state,result);
        return result;
    }

    private IloNumVar createVariableForStateAction(IloCplex cplex, MDPStateActionMarginal action, Player player) throws IloException {
        String letter = ((player.getId() == 0) ? "x" : "y") + "_";
        IloNumVar result = cplex.numVar(0, 1, IloNumVarType.Float, letter + action.toString());
        variables.put(action,result);
        return result;
    }

    private void createConstraintForExpValues(IloCplex cplex, Player player, MDPStateActionMarginal opponentsStateAction) throws IloException {
        IloNumExpr sumR = cplex.constant(0);
        IloNumExpr LS = variables.get(opponentsStateAction.getState());
        assert (LS != null);
        Set<MDPState> successors = playerStrategy.get(opponentsStateAction.getPlayer()).getSuccessors(opponentsStateAction).keySet();
        for (MDPState s : successors) {
            if (variables.containsKey(s)) {
                sumR = cplex.sum(sumR, variables.get(s));
            }
        }
        for (MDPStateActionMarginal myActions : playerStrategy.get(player).getStrategy().keySet()) {
            IloNumVar x = variables.get(myActions);
            assert (x != null);
            sumR = cplex.sum(sumR, cplex.prod(x, utilityComputer.getUtility(myActions, opponentsStateAction)));
        }

        if (player.getId() == 0) {
            constraints.put(opponentsStateAction, cplex.addLe(cplex.diff(LS, sumR), 0));
        } else {
            constraints.put(opponentsStateAction, cplex.addGe(cplex.diff(LS, sumR), 0));
        }
    }

    private void createConstraintForStrategy(IloCplex cplex, Player player, MDPState state) throws IloException {
        IloNumExpr LS = cplex.constant(0);
        assert (LS != null);
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
            for (Entry<MDPStateActionMarginal, Double> e : strategy.getPredecessors(state).entrySet()) {
                if (variables.containsKey(e.getKey())) {
                    LS = cplex.sum(LS, cplex.prod(e.getValue(), variables.get(e.getKey())));
                } else {
                    assert true;
                }
            }
        }

        constraints.put(state, cplex.addEq(cplex.diff(LS,RS),0));
    }
}
