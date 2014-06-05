/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 *
 * @author vilo
 */
public class Exp3Selector implements Selector, AlgorithmData, MeanStrategyProvider {
    private Exp3BackPropFactory fact;
    private List<Action> actions;
    /** Current probability of playing this action. */
    public double[] p;
    /** Cumulative reward. */
    double[] r;
    /** Mean strategy. */
    double[] mp;

    public Exp3Selector(List<Action> actions, Exp3BackPropFactory fact) {
        this(actions.size(), fact);
        this.actions = actions;
    }
    
    
    public Exp3Selector(int N, Exp3BackPropFactory fact) {
        this.fact = fact;
        p = new double[N];
        r = new double[N];
        mp = new double[N];
    }
    
    protected void updateProb() {
        final int K = r.length;
        final double gamma = fact.gamma;

        for (int i=0; i < r.length; i++) {
            double denom = 1;
            for (int j=0 ; j < r.length; j++) {
                if (i != j) denom += Math.exp((gamma / K) * (r[j] - r[i]));
            }
            final double cp = (1 / denom);
            p[i] = (1 - gamma) * cp + gamma / K;
            if (fact.storeExploration) mp[i]+=p[i];
            else mp[i]+=cp;
        }
    }
    
    
    @Override
    public int select(){
        updateProb();

        double rand = fact.random.nextDouble();
        
        for (int i=0; i<p.length; i++) {
            if (rand > p[i]) {
                rand -= p[i];
            } else {
                return i;
            }
        }

        assert false;
        return -1;
    }
    
    @Override
    public void update(int ai, double value) {
        r[ai] += fact.normalizeValue(value) / p[ai];
    }

    @Override
    public List<Action> getActions() {
        return actions;
    }
    
    @Override
    public double[] getMp() {
        return mp;
    }    
    
    
}
