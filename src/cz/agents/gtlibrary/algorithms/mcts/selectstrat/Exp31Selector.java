/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import cz.agents.gtlibrary.algorithms.mcts.backprop.exp3.Exp31ActionBPStrategy;
import cz.agents.gtlibrary.algorithms.mcts.backprop.exp3.Exp31NodeBPStrategy;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class Exp31Selector  implements SelectionStrategy {
    public Random random = new Random();
    private boolean clearWeights = false;

    private void initialize(Exp31NodeBPStrategy nodeStats, Map<Action, Exp31ActionBPStrategy> actionStats){
        final int K = actionStats.size();
        nodeStats.gamma = 1;
        nodeStats.gr = K*Math.log(K)/(Math.E-1);
        
        for(Exp31ActionBPStrategy bp : actionStats.values()){
            bp.p = 1.0/K;
        }
    }
    
    private void updateProb(Exp31NodeBPStrategy nodeStats, Map<Action, Exp31ActionBPStrategy> actionStats){
        final int K = actionStats.size();
        final double gamma = nodeStats.gamma;

        for(Exp31ActionBPStrategy bpi : actionStats.values()){
            double denom = 1;
            for(Exp31ActionBPStrategy bpj : actionStats.values()){
                if (bpi!=bpj) denom += Math.exp((gamma/K)*(bpj.r-bpi.r));
            }
            bpi.p = (1-gamma)*(1/denom) + gamma/K;
        }
    }
    
    
    @Override
        public Action select(Player player, BPStrategy inNodeStats, Map<Action, BPStrategy> nodeActionStats, BPStrategy isStats, Map<Action, BPStrategy> isActionStats){
        Map<Action, Exp31ActionBPStrategy> actionStats = (Map<Action, Exp31ActionBPStrategy>) (Map) isActionStats;
        Exp31NodeBPStrategy nodeStats = (Exp31NodeBPStrategy) inNodeStats;
        
        if (nodeStats.gamma == -1) initialize(nodeStats, actionStats);
        else updateProb(nodeStats, actionStats);
        
        double rand = random.nextDouble();
        Action out = null; 
        double grMax = -Double.MAX_VALUE;
        //compute max comulative reward and select a random action
        for (Map.Entry<Action,Exp31ActionBPStrategy> en : actionStats.entrySet()) {
            final Exp31ActionBPStrategy bps = en.getValue();
            if (rand > bps.p) {
                rand-=bps.p;
            } else {
                if (out == null){
                    out = en.getKey();
                }
            }
            if (bps.r + bps.global_r > grMax) grMax = bps.r + bps.global_r;
        }

        if (grMax > nodeStats.gr - actionStats.size()/nodeStats.gamma){
              nodeStats.gr *= 4; nodeStats.gamma /= 2;
              //try to reset weights
              if (clearWeights){
                  System.out.println("Clearing weights gamma=" + nodeStats.gamma);
                  for (Exp31ActionBPStrategy bps : actionStats.values()) {
                      bps.global_r += bps.r;
                      bps.r = 0;
                  }
              }
        }
        
        return out;
    }
    
    
}
