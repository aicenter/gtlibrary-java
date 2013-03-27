/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop.exp3;

/**
 *
 * @author vilo
 */
public class Exp31ActionBPStrategy extends Exp3ActionBPStrategy {
    /** Keeps track of r in case of restarts. Can be removed for optimization. */
    public double global_r = 0;
    public double gr = -1;
    
    public Exp31ActionBPStrategy(Exp3BackPropFactory fact) {
        super(fact);
    }
}
