/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.List;
import java.util.Random;

/**
 * @author vilo
 */
public class SMConjectureFactory implements SMBackPropFactory {
    Random random;
    double gamma = 0.1;
    private double minUtility = -1;
    private double maxUtility = 1;
    Exp3BackPropFactory fact;

    public SMConjectureFactory(double gamma) {
        this(gamma, new HighQualityRandom());
        this.gamma=gamma;
        fact = new Exp3BackPropFactory(minUtility, maxUtility, gamma);
    }

    public SMConjectureFactory(double gamma, Random random) {
        this.gamma = gamma;
        this.random = random;
    }

    @Override
    public SMSelector createSlector(List<Action> actions1, List<Action> actions2) {
        return new SMConjuctureSelector(actions1, actions2, fact.createSelector(actions1), fact.createSelector(actions2));
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
