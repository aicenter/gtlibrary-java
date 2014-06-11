/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanValueProvider;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 *
 * @author vilo
 */
public class Exp3MSelector extends Exp3Selector implements MeanValueProvider {
    private BasicStats stats = new BasicStats();
    
    public Exp3MSelector(int N, Exp3BackPropFactory fact) {
        super(N, fact);
    }

    public Exp3MSelector(List<Action> actions, Exp3BackPropFactory fact) {
        super(actions, fact);
    }

    @Override
    public void update(int ai, double value) {
        super.update(ai, value);
        stats.onBackPropagate(value);
    }

    @Override
    public double getMeanValue() {
        return stats.getEV();
    }
    
    
    
}
