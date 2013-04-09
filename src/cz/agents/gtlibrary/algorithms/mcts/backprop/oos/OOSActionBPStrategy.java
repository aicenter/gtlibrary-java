/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop.oos;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import java.util.Arrays;

/**
 *
 * @author vilo
 */
public class OOSActionBPStrategy extends BPStrategy {
    /** Current probability of playing this action. */
    public double p = -1;
    /** Cumulative counterfactual regret. */
    public double r = 0;
    public OOSBackPropFactory fact;

    public OOSActionBPStrategy(OOSBackPropFactory fact) {
        this.fact = fact;
    }
    
    @Override
    public void onBackPropagate(double value) {
        value = value / fact.pis.removeLast();
        if (fact.pis.isEmpty()) Arrays.fill(fact.pi,1);
        
        super.onBackPropagate(value);
        
        r += value;
    }
}
