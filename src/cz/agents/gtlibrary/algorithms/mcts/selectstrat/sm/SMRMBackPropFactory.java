/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.List;
import java.util.Random;

/**
 * @author vilo
 */
public class SMRMBackPropFactory implements SMBackPropFactory {
    Random random;
    double gamma = 0.2;
    private double minUtility = 0;
    private double maxUtility = 1;

    public SMRMBackPropFactory(double gamma) {
        this(gamma, new HighQualityRandom());
    }

    public SMRMBackPropFactory(double gamma, Random random) {
        this.gamma = gamma;
        this.random = random;
    }

    @Override
    public SMSelector createSlector(List<Action> actions1, List<Action> actions2) {
        return new SMRMSelector(this, actions1, actions2);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
