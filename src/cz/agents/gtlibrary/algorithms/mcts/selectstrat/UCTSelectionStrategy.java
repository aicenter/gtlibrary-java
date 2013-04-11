/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class UCTSelectionStrategy implements SelectionStrategy {
    private MCTSInformationSet infSet;
    private UCTBackPropFactory fact;
    
    public UCTSelectionStrategy(UCTBackPropFactory fact, MCTSInformationSet infSet) {
        this.infSet = infSet;
        this.fact = fact;
    }
    
    @Override
    public Action select() {
		Action bestAction = null;
		double maxValue = Double.NEGATIVE_INFINITY;

		for (Map.Entry<Action, BasicStats> en : infSet.getActionStats().entrySet()) {
                    int aCount = en.getValue().getNbSamples();
                    if (aCount == 0) return en.getKey();
                    double value = en.getValue().getEV() + fact.C * Math.sqrt(Math.log(infSet.getInformationSetStats().getNbSamples()) / aCount);
                        
                    if (value > maxValue) {
                            maxValue = value;
                            bestAction = en.getKey();
                    }
		}
		return bestAction;
    }
    
    @Override
    public double onBackPropagate(Action actions, double value) {
        return value;
    }
    
}
