/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop.exp3;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import cz.agents.gtlibrary.algorithms.mcts.backprop.DefaultBackPropFactory;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 *
 * @author vilo
 */
public class Exp3BackPropFactory extends DefaultBackPropFactory {
    public boolean useCFRValues = false;
    /** Each player's contribution to the probability of being in current IS. */
    public double[] pi = new double[]{1,1,1};
    public ArrayDeque<Double> pis = new ArrayDeque<Double>();
    
    private double minUtility;
    private double maxUtility;

    public Exp3BackPropFactory(double minUtility, double maxUtility) {
        this.minUtility = minUtility;
        this.maxUtility = maxUtility;
    }
    
    public double normalizeValue(double value) {
        assert minUtility <= value && value <= maxUtility;
        return (value - minUtility) / (maxUtility - minUtility);
    }

    @Override
    public BPStrategy createForISAction(InformationSet infSet, Action a) {
        if (infSet.getPlayer().getId() > 1) return new BPStrategy();
        return new Exp3ActionBPStrategy(this);
    }
}
