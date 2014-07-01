/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class UCTBackPropFactory implements BackPropFactory {
    public double C;
    public Random random;

    public UCTBackPropFactory(double C) {
        this(C, new HighQualityRandom());
    }
    
    public UCTBackPropFactory(double C, Random random) {
        this.C = C;
        this.random = random;
    }

    @Override
    public Selector createSelector(List<Action> actions) {
        return new UCTSelector(actions, this);
    }
    
    @Override
    public Selector createSelector(int N) {
        return new UCTSelector(N, this);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
