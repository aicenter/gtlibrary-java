/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop.exp3;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;

/**
 *
 * @author vilo
 */
public class Exp31NodeBPStrategy extends BPStrategy {
    public double gr = -1;
    public double gamma = -1;
    public Exp3BackPropFactory fact;

    public Exp31NodeBPStrategy(Exp3BackPropFactory fact) {
        this.fact = fact;
    }
}
