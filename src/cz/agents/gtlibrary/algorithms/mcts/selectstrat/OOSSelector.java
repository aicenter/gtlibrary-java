/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import cz.agents.gtlibrary.algorithms.mcts.backprop.exp3.Exp3ActionBPStrategy;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class OOSSelector implements SelectionStrategy {
    public Random random = new Random();
    private double gamma;

    public OOSSelector(double gamma) {
        this.gamma = gamma;
    }

    private void updateProb(Map<Action, Exp3ActionBPStrategy> actionStats) {
        final int K = actionStats.size();

        double sum = 0;
        for (Exp3ActionBPStrategy bpi : actionStats.values()) {
            sum += bpi.r;
            bpi.p = (1 - gamma) * (bpi.r / sum) + gamma / K;
        }
        for (Exp3ActionBPStrategy bpi : actionStats.values()) {
            if (sum > 0) bpi.p = (1 - gamma) * (bpi.r / sum) + gamma / K;
            else bpi.p = 1 / K;
            
        }
    }

    @Override
    public Action select(Player player, BPStrategy nodeStats, Map<Action, BPStrategy> nodeActionStats, BPStrategy isStats, Map<Action, BPStrategy> isActionStats){
        Map<Action, Exp3ActionBPStrategy> actionStats = (Map<Action, Exp3ActionBPStrategy>) (Map) isActionStats;
        updateProb(actionStats);

        double rand = random.nextDouble();
        
        for (Map.Entry<Action,Exp3ActionBPStrategy> en : actionStats.entrySet()) {
            final Exp3ActionBPStrategy bps = en.getValue();
            if (rand > bps.p) {
                rand -= bps.p;
            } else {
                bps.fact.pis.add(bps.fact.pi[player.getId()]);
                bps.fact.pi[player.getId()] *= bps.p;
                return en.getKey();
            }
        }

        assert false;
        return null;
    }
}
