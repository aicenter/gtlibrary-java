/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop.oos;

import cz.agents.gtlibrary.algorithms.mcts.backprop.exp3.*;
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
public class OOSBackPropFactory extends DefaultBackPropFactory {
    /** Each player's contribution to the probability of being in current IS. */
    public double[] pi = new double[]{1,1,1};
    public ArrayDeque<Double> pis = new ArrayDeque<Double>();


    public OOSBackPropFactory() {
    }
    
    @Override
    public BPStrategy createForISAction(InformationSet infSet, Action a) {
        if (infSet.getPlayer().getId() > 1) return new BPStrategy();
        return new OOSActionBPStrategy(this);
    }
}
