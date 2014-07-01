/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class RMBackPropFactory implements BackPropFactory  {
    double gamma = 0.2;
    Random random = new Random();

    private double minUtility=0;
    private double maxUtility=1;

    public RMBackPropFactory(double minUtility, double maxUtility, double gamma) {
        this(gamma);
        this.minUtility = minUtility;
        this.maxUtility = maxUtility;
    }
    
    public RMBackPropFactory(double gamma) {
        this.gamma = gamma;
    }
    
    public double normalizeValue(double value) {       
        assert minUtility <= value + 1e-5 && value <= maxUtility + 1e-5;
        return (value - minUtility) / (maxUtility - minUtility);
//        assert minUtility == 0 && maxUtility == 1;
//        return value;
    }
    
    @Override
    public Selector createSelector(List<Action> actions) {
        return new RMSelector(actions, this);
    }

    @Override
    public Selector createSelector(int N) {
        return new RMSelector(N, this);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
