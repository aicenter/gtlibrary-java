/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class UCTMAXSelectionStrategy implements SelectionStrategy {
    protected MCTSInformationSet infSet;
    protected UCTBackPropFactory fact;
    protected Map<Action, Double> values;
    protected double maxValue = Double.NaN;
    
    public UCTMAXSelectionStrategy(UCTBackPropFactory fact, MCTSInformationSet infSet) {
        this.infSet = infSet;
        this.fact = fact;
        this.values = new FixedSizeMap<Action, Double>(infSet.getActionStats().size());
    }
    
    @Override
    public Action select() {
		Action bestAction = null;
		double maxValue = Double.NEGATIVE_INFINITY;

		for (Map.Entry<Action, BasicStats> en : infSet.getActionStats().entrySet()) {
                    int aCount = en.getValue().getNbSamples();
                    if (aCount == 0) return en.getKey();
                    double value = values.get(en.getKey()) + fact.C * Math.sqrt(Math.log(infSet.getInformationSetStats().getNbSamples()) / aCount);
                        
                    if (value > maxValue) {
                            maxValue = value;
                            bestAction = en.getKey();
                    }
		}
		return bestAction;
    }
    
    @Override
    public double onBackPropagate(InnerNode node, Action action, double value) {
        values.put(action, value);
        double max = -Double.MAX_VALUE;
        for (Map.Entry<Action, Double> en : values.entrySet()){
            if (max < en.getValue()) max = en.getValue();
        }
        maxValue = max;
        return max;
    }
    
    public Action getMaxValueAction(){
        Action out =  infSet.getActionStats().keySet().iterator().next();
        double max = -Double.MAX_VALUE;
        for (Map.Entry<Action, Double> en : values.entrySet()){
            if (max < en.getValue()) {
                max = en.getValue();
                out = en.getKey();
            }
        }
        return out;
    }
    
}
