package cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by bosansky on 1/7/14.
 */
public class MDPContractingLP extends MDPOracleLP {

    protected Set<MDPStateActionMarginal> actionsToRemove = null;

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
}
