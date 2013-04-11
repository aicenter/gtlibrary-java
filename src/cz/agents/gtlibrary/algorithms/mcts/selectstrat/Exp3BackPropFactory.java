/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.SelectionStrategy;
import java.util.ArrayDeque;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class Exp3BackPropFactory implements BackPropFactory  {
    public double gamma = 0.05;
    public boolean useCFRValues = false;
    Random random = new Random();
    /** Each player's contribution to the probability of being in current IS. */
    double[] pi = new double[]{1,1,1};
    ArrayDeque<Double> pis = new ArrayDeque<Double>();
    
    private double minUtility;
    private double maxUtility;

    public Exp3BackPropFactory(double minUtility, double maxUtility, double gamma) {
        this.minUtility = minUtility;
        this.maxUtility = maxUtility;
        this.gamma = gamma;
    }
    
    public double normalizeValue(double value) {
        assert minUtility <= value && value <= maxUtility;
        return (value - minUtility) / (maxUtility - minUtility);
    }

    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        return new Exp3SelectionStrategy(this, infSet.getAllNodes().iterator().next());
    }

    @Override
    public SelectionStrategy createForNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
}
