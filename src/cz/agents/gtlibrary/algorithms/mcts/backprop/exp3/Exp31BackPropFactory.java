/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.backprop.exp3;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 *
 * @author vilo
 */
public class Exp31BackPropFactory extends Exp3BackPropFactory {
    public Exp31BackPropFactory(double minUtility, double maxUtility) {
        super(minUtility, maxUtility);
    }  
    
    @Override
    public BPStrategy createForIS(InformationSet infSet) {
        return new Exp31NodeBPStrategy(this);
    }

    @Override
    public BPStrategy createForISAction(InformationSet infSet, Action a) {
        return new Exp31ActionBPStrategy(this);
    }
}
