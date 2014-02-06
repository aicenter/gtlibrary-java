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
            for (int i=0; i<p.length; i++) p[i] = 0.99*Math.max(0,r[i])/R + 0.01/K;
        }
    }
    
    int ai;
    @Override   
    public Action select(){
        updateProb();
        fact.putPIs();
        fact.putSs();
        //on biassed path
        int iexp = fact.root.getNbSamples() % 2;
        int inBiasActions=0;
        double biasedSum=0;
        //still on path to current IS
        if (fact.bs > 0 && !fact.underTargetIs) {
            if (fact.getCurrentIS().equals(is)){
                fact.underTargetIs = true;
            } else if (fact.getCurrentIS().getPlayer().equals(is.getPlayer())){
                //this IS is above the current IS, returning the action from the history
                if (fact.getCurrentIS().getPlayersHistory().size() > is.getPlayersHistory().size()){
                    ai = actions.indexOf(fact.getCurrentIS().getPlayersHistory().get(is.getPlayersHistory().size()));
                    biasedSum += p[ai];
                    p[ai]*=-1;//na;egative values at p represent the actions to bias to
                    inBiasActions=1;
                }
            } else {
                if (is.getPlayersHistory().size() < fact.opponentMaxSequenceLength){
                    int i=0;
                    for (Action a : actions){
                        if (fact.opponentAllowedActions.contains(a)){
                            biasedSum += p[i];
                            p[i] *= -1; //negative prob. indicates action to bias towards
                            inBiasActions++;
                        }
                        i++;
                    }
                }
            }
        }
        
        double bpa, upa, pa;
        double rand = fact.random.nextDouble();
        for (int i=0; i<p.length; i++) {
            //exploring player
            if (iexp == is.getPlayer().getId()){
                if (Double.compare(p[i],0.0) != -1) bpa=0;
                else if (biasedSum > 0) bpa = (1-fact.gamma)*-1*p[i]/biasedSum + fact.gamma/inBiasActions;
                else bpa=1.0/inBiasActions;
                upa = (1-fact.gamma)*Math.abs(p[i]) + fact.gamma/actions.size();
            } else {
                if (Double.compare(p[i],0.0) != -1) bpa=0;
                else if (biasedSum > 0) bpa = -1*p[i]/biasedSum;
                else bpa=1.0/inBiasActions;
                upa = Math.abs(p[i]);
            }
            if (fact.underTargetIs) bpa=upa;
            pa = (fact.isBiasedIteration() ? bpa : upa);
            if (rand > pa) {
                rand -= pa;
            } else {
                ai = i;
                for (i=0; i<p.length; i++) if (Double.compare(p[i],0.0) == -1) p[i]*=-1;
                Action a = actions.get(ai);
                fact.pi[is.getPlayer().getId()] *= p[ai];
                fact.bs *= bpa;
                fact.us *= upa;
                return a;
            }
        }
        assert false;
        return null;
    }

    @Override
    public double onBackPropagate(InnerNode node, Action action, double value) {
        fact.popPIs();
        fact.popSs();
        double s = fact.delta*fact.bs + (1-fact.delta)*fact.us;
        int curPlayerID = is.getPlayer().getId();
        double c = fact.x;
        fact.x *= p[ai];
        
        //exploring player
        if (fact.root.getNbSamples() % 2 == curPlayerID){
            double W = value*fact.pi[1-curPlayerID]/fact.l;
            for (int i=0; i<r.length; i++){
                if (action.equals(actions.get(i))) r[i] += (c-fact.x)*W;
                else r[i] += -fact.x*W;
            }
        } else {
            for (int i=0; i<p.length; i++) mp[i] += p[i]*fact.pi[curPlayerID]/s;
        }
        
        if (node==fact.root){
            if (fact.root.getNbSamples() % 2 == 0) fact.biassedSample = fact.random.nextDouble() < fact.delta;
            fact.underTargetIs = false;
            assert Math.abs(fact.s-1) < 1e-6;
            assert Math.abs(fact.bs-1) < 1e-6;
            assert Math.abs(fact.us-1) < 1e-6;
            assert Math.abs(fact.pi[0]-1) < 1e-6;
            assert Math.abs(fact.pi[1]-1) < 1e-6;
            assert fact.pis[0].isEmpty();
            assert fact.pis[1].isEmpty();
            assert fact.ss.isEmpty();
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
