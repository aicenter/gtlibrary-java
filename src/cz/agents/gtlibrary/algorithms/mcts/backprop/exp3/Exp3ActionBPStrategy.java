/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop.exp3;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import java.util.Arrays;

/**
 *
 * @author vilo
 */
public class Exp3ActionBPStrategy extends BPStrategy {
    /** Current probability of playing this action. */
    public double p = -1;
    /** Cumulative reward. */
    public double r = 0;
    public Exp3BackPropFactory fact;

    public Exp3ActionBPStrategy(Exp3BackPropFactory fact) {
        this.fact = fact;
    }
    
    @Override
    public void onBackPropagate(double value) {
        if (fact.useCFRValues){
            value = value / fact.pis.removeLast();
            if (fact.pis.isEmpty()) Arrays.fill(fact.pi,1);
        }
        super.onBackPropagate(value);
        double normValue = fact.normalizeValue(value);
        r += normValue / p;
    }
}
