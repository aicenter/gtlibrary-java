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
public class OOSSelector implements SelectionStrategy, MeanStrategyProvider {
    OOSBackPropFactory fact;
    List<Action> actions;
    MCTSInformationSet is;
    /** Current probability of playing this action. */
    double[] p;
    double[] mp;
    /** Cumulative regret. */
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
    
    double pa;
    int ai;
    @Override
    public Action select(){
        updateProb();
        fact.putPIs();
        int iexp = fact.root.getNbSamples() % 2;

        double rand = fact.random.nextDouble();
        
        for (int i=0; i<p.length; i++) {
            
            //exploring player
            if (iexp == is.getPlayer().getId()){
                pa = (1-fact.gamma)*p[i] + fact.gamma/actions.size();
            } else {
                pa = p[i];
            }
            if (rand > pa) {
                rand -= pa;
            } else {
                fact.pi[is.getPlayer().getId()] *= p[i];
                fact.s *= pa;
                ai = i;
                return actions.get(i);
            }
        }
        
        assert false;
        return null;
    }

    @Override
    public double onBackPropagate(InnerNode node, Action action, double value) {
        fact.popPIs();
        int curPlayerID = is.getPlayer().getId();
        double c = fact.x;
        fact.x *= p[ai];
        
        //exploring player
        fact.s /= pa;
        if (fact.root.getNbSamples() % 2 == curPlayerID){
            double W = value*fact.pi[1-curPlayerID]/fact.l;
            for (int i=0; i<r.length; i++){
                if (action.equals(actions.get(i))) r[i] += (c-fact.x)*W;
                else r[i] += -fact.x*W;
            }
        } else {
            for (int i=0; i<p.length; i++) mp[i] += p[i]*fact.pi[curPlayerID]/fact.s;
        }
        
        if (node==fact.root){
            assert Math.abs(fact.s-1) < 1e-6;
            assert Math.abs(fact.pi[0]-1) < 1e-6;
            assert Math.abs(fact.pi[1]-1) < 1e-6;
        }
        return value;
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
