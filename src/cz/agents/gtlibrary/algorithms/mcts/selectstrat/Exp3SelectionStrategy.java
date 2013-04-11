/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class Exp3SelectionStrategy implements SelectionStrategy {
    Exp3BackPropFactory fact;
    List<Action> actions;
    Player player;
    /** Current probability of playing this action. */
    double[] p;
    /** Cumulative reward. */
    double[] r;
    public Exp3SelectionStrategy(Exp3BackPropFactory fact, InnerNode node){
        this.fact = fact;
        this.player = node.getInformationSet().getPlayer();
        actions = node.getActions();
        p = new double[actions.size()];
        r = new double[actions.size()];
    }

    @Override
    public double onBackPropagate(Action action, double value) {
        double effValue;
        if (fact.useCFRValues){
            effValue = value / fact.pis.removeLast();
            if (fact.pis.isEmpty()) Arrays.fill(fact.pi,1);
        } else {
            effValue = value;
        }
        
        int i = actions.indexOf(action);
        r[i] += fact.normalizeValue(effValue) / p[i];
        return value;
    }
    
    protected void updateProb() {
        final int K = actions.size();
        final double gamma = fact.gamma;

        for (int i=0; i < r.length; i++) {
            double denom = 1;
            for (int j=0 ; j < r.length; j++) {
                if (i != j) {
                    //denom += Math.exp((gamma / K) * (bpj.r - bpi.r));
                    denom += Math.exp((gamma / K) * (r[j] - r[i]));
                }
            }
            p[i] = (1 - gamma) * (1 / denom) + gamma / K;
        }
    }

    @Override
    public Action select(){
        updateProb();

        double rand = fact.random.nextDouble();
        
        for (int i=0; i<p.length; i++) {
            if (rand > p[i]) {
                rand -= p[i];
            } else {
                fact.pis.add(fact.pi[player.getId()]);
                fact.pi[player.getId()] *= p[i];
                return actions.get(i);
            }
        }

        assert false;
        return null;
    }
}
