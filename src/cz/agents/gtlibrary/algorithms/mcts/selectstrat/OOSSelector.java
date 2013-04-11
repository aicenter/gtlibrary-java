/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class OOSSelector implements SelectionStrategy {
    OOSBackPropFactory fact;
    List<Action> actions;
    MCTSInformationSet is;
    /** Current probability of playing this action. */
    double[] p;
    double[] mp;
    /** Cumulative reward. */
    double[] r;
    public OOSSelector(OOSBackPropFactory fact, MCTSInformationSet is){
        this.fact = fact;
        this.is = is;
        actions = is.getAllNodes().iterator().next().getActions();
        p = new double[actions.size()];
        mp = new double[actions.size()];
        r = new double[actions.size()];
    }
    
    protected void updateProb() {
        final int K = actions.size();
        double R = 0;
        for (double ri : r) R += Math.max(0,ri);
        
        if (R <= 0){
            Arrays.fill(p,1.0/K);
        } else {
            for (int i=0; i<p.length; i++) p[i] = Math.max(0,r[i])/R;
        }
    }
    
    @Override
    public Action select(){
        updateProb();
        int iexp = fact.root.getNbSamples() % 2;

        double rand = fact.random.nextDouble();
        
        for (int i=0; i<p.length; i++) {
            double pa;
            //exploring player
            if (iexp == is.getPlayer().getId()){
                pa = (1-fact.gamma)*p[i] + fact.gamma/actions.size();
            } else {
                pa = p[i];
            }
            if (rand > pa) {
                rand -= pa;
            } else {
                fact.pis.add(fact.pi);
                if (iexp == is.getPlayer().getId()) fact.pi *= pa;
                return actions.get(i);
            }
        }
        
        assert false;
        return null;
    }

    @Override
    public double onBackPropagate(Action action, double value) {
        //exploring player
        if (fact.root.getNbSamples() % 2 == is.getPlayer().getId()){
            double vsum = 0;
            double[] v  = new double[p.length];
            int i=0;
            for (Action a : actions){
                if (a.equals(action)){
                    v[i] = value;
                } else {
                    v[i] = is.getActionStats().get(a).getEV();
                }
                vsum += p[i]*v[i];
                i++;
            }
            
            double pi = fact.pis.removeLast();
            for (i=0; i<r.length; i++){
                r[i] += (v[i] - vsum)/pi;
            }
        } else {
            double pi = fact.pis.removeLast();
            for (int i=0; i<p.length; i++) mp[i] += pi*p[i];
        }
        if (fact.pis.isEmpty()) fact.pi = 1;
        return value;
    }

    public List<Action> getActions() {
        return actions;
    }

    public double[] getMp() {
        return mp;
    }
    
    
}
