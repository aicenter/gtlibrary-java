/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class RMSelector implements Selector, MeanStrategyProvider {
    private RMBackPropFactory fact;
    private List<Action> actions;
    MCTSInformationSet is;
    Action lastSelected = null;
    /** Current probability of playing this action. */
    double[] p;
    double[] mp;
    /** Cumulative regret. */
    double[] r;
    public RMSelector(List<Action> actions, RMBackPropFactory fact){
        this(actions.size(), fact);
        this.actions = actions;
    }
    
    public RMSelector(int N, RMBackPropFactory fact){
        this.fact = fact;
        p = new double[N];
        mp = new double[N];
        r = new double[N];
        
    }
    
    protected void updateProb() {
        final int K = r.length;
        double R = 0;
        for (double ri : r) R += Math.max(0,ri);
        
        if (R <= 0){
            Arrays.fill(p,1.0/K);
        } else {
            for (int i=0; i<p.length; i++) p[i] = Math.max(0,r[i])/R;
        }
        for (int i=0; i<p.length; i++) mp[i] += p[i];
    }
    
    @Override
    public int select(){
        updateProb();

        double rand = fact.random.nextDouble();
        for (int i=0; i<p.length; i++) {
            double pa = (1-fact.gamma)*p[i] + fact.gamma/p.length;
            
            if (rand > pa) {
                rand -= pa;
            } else {
                return i;
            }
        }
        assert false;
        return -1;
    }

    @Override
    public void update(int selection, double value) {
        double pa = (1-fact.gamma)*p[selection] + fact.gamma/p.length;
        r[selection] += (1-p[selection])*fact.normalizeValue(value)/pa;
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }

    @Override
    public double[] getMp() {
        return mp;
    }
    
    private double valueEstimate(Action exp, InnerNode node){
        double out = 0;
        return out;
    }
    
    
}
