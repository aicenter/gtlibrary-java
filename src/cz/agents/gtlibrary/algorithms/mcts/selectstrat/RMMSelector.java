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
public class RMMSelector implements SelectionStrategy, MeanStrategyProvider {
    RMBackPropFactory fact;
    List<Action> actions;
    MCTSInformationSet is;
    Action lastSelected = null;
    /** Current probability of playing this action. */
    double[] p;
    double[] mp;
    /** Cumulative regret. */
    double[] r;
    public RMMSelector(RMBackPropFactory fact, MCTSInformationSet is){
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
        for (int i=0; i<p.length; i++) mp[i] += p[i];
    }
    
    @Override
    public Action select(){
        updateProb();

        double rand = fact.random.nextDouble();
        for (int i=0; i<p.length; i++) {
            double pa = (1-fact.gamma)*p[i] + fact.gamma/actions.size();
            
            if (rand > pa) {
                rand -= pa;
            } else {
                lastSelected = actions.get(i);
                return lastSelected;
            }
        }
        assert false;
        return null;
    }

    @Override
    public double onBackPropagate(InnerNode node, Action action, double value) {
        try {
            if (node.getInformationSet().getPlayer().getId() == 0){
                assert node.getInformationSet().getAllNodes().size() == 1;
                InnerNode child = (InnerNode) node.getChildOrNull(action);
                Action oppAct = ((RMMSelector) child.getInformationSet().selectionStrategy).lastSelected;
                
                int i=0;
                for (Action a : node.getActions()){
                    if (!a.equals(action)){
                        r[i] += ((InnerNode)node.getChildOrNull(a)).getChildOrNull(oppAct).getEV()[node.getInformationSet().getPlayer().getId()] - value;
                    }
                    i++;
                }
            } else {
                int i=0;
                for (Action a : node.getActions()){
                    if (!a.equals(action)) r[i] += node.getChildOrNull(a).getEV()[node.getInformationSet().getPlayer().getId()] - value;
                    i++;
                }
            }
        } catch (NullPointerException ex){
            //intentionally empty
        }
        
        if (node.getInformationSet().getAllNodes().size() == 1) return node.getEV()[0];
        else return value;
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
        try {
            if (node.getInformationSet().getAllNodes().size() == 1){
                InnerNode child = (InnerNode) node.getChildOrNull(exp);
                double[] p = ((RMMSelector) child.getInformationSet().selectionStrategy).p;
                int i=0;
                for (Action a : child.getActions()){
                    out += p[i++] * ((InnerNode)child.getChildOrNull(a)).getEV()[node.getInformationSet().getPlayer().getId()];
                }
            } else {
                InnerNode parent = (InnerNode) node.getParent();
                double[] p = ((RMMSelector) parent.getInformationSet().selectionStrategy).p;
                int i=0;
                for (Action a : parent.getActions()){
                    out += p[i++] * ((InnerNode)parent.getChildOrNull(a)).getChildOrNull(exp).getEV()[node.getInformationSet().getPlayer().getId()];
                }
            }
        } catch (NullPointerException ex){
            return 0;
        }
        return out;
    }
    
    
}
