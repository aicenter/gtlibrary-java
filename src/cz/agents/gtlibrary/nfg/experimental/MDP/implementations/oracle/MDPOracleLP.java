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
public class MDPOracleLP extends MDPCoreLP {

//    protected Map<Object, IloRange> constraints = new HashMap<Object, IloRange>();
//    protected Map<Object, IloNumVar> variables = new HashMap<Object, IloNumVar>();
    protected Set<MDPStateActionMarginal> newActions = null;

    public MDPOracleLP(Collection<Player> allPlayers, Map<Player, MDPStrategy> playerStrategy, MDPConfig config) {
        super(allPlayers, playerStrategy, config);
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
            for (MDPStateActionMarginal sam : playerStrategy.get(player).getActionStates()) {
                createVariableForStateAction(getLpModels().get(player), sam, player);
            }
            for (MDPStateActionMarginal sam : playerStrategy.get(opponent).getActionStates()) {
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
                MDPState s = mdpStateActionMarginal.getState();
                if (playerStrategy.get(opponent).hasStateASuccessor(s) && !s.isRoot())
                    createVariableForMDPState(getLpModels().get(player), s);
            }
            for (MDPStateActionMarginal mdpStateActionMarginal : newActions) {
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
        } catch (IloException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setNewActions(Set<MDPStateActionMarginal> newActions) {
        this.newActions = newActions;
    }
}
