/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class Exp3BackPropFactory implements BackPropFactory  {
    public double gamma = 0.05;
    public boolean useCFRValues = false;
    public boolean storeExploration = false;
    Random random;
    /** Each player's contribution to the probability of being in current IS. */
    double[] pi = new double[]{1,1,1};
    ArrayDeque<Double> pis = new ArrayDeque<Double>();
    
    private double minUtility;
    private double maxUtility;


    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma, boolean storeExploration) {
        this(minUtility, maxUtility, gamma, storeExploration, new HighQualityRandom());
    }

    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma, boolean storeExploration, Random random) {
        this(minUtility, maxUtility, gamma, random);
        this.storeExploration = storeExploration;
    }

    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma) {
        this(minUtility, maxUtility, gamma, new HighQualityRandom());
    }
    
    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma, Random random) {
        this.minUtility = minUtility;
        this.maxUtility = maxUtility;
        this.gamma = gamma;
        this.random = random;
    }
    
    public double normalizeValue(double value) {       
        assert minUtility <= value + 1e-5 && value <= maxUtility + 1e-5;
        return (value - minUtility) / (maxUtility - minUtility);
//        assert minUtility == 0 && maxUtility == 1;
//        return value;
    }
    
    public double valuesSpread() {
        return maxUtility - minUtility;
    }

    @Override
    public Selector createSelector(List<Action> actions) {
        return new Exp3Selector(actions,this);
    }
    
    @Override
    public Selector createSelector(int N) {
        return new Exp3Selector(N,this);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
