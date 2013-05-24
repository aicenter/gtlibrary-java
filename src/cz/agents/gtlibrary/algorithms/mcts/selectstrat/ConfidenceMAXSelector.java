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
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 *
 * @author vilo
 */
public class ConfidenceMAXSelector extends UCTMAXSelectionStrategy {
    private static double epsilon = 0.05;

    
    public ConfidenceMAXSelector(UCTBackPropFactory fact, MCTSInformationSet infSet) {
        super(fact, infSet);
    }
    
    @Override
    public Action select() {
		Action bestAction = null;
		double max = Double.NEGATIVE_INFINITY;

                //performing random action
                if (infSet.getInformationSetStats().getNbSamples() < infSet.getActionStats().size()){
                    int idx = fact.rnd.nextInt(infSet.getActionStats().size() - infSet.getInformationSetStats().getNbSamples());
                    for (Map.Entry<Action, BasicStats> en : infSet.getActionStats().entrySet()) {
                        if (en.getValue().getNbSamples() == 0){
                            if (idx == 0) return en.getKey();
                            else idx--;
                        }
                    }
                    assert false;
                }
                
		for (Map.Entry<Action, BasicStats> en : infSet.getActionStats().entrySet()) {
                    int aCount = en.getValue().getNbSamples();
                    //TODO: properly normalize
                    double diff = (maxValue - values.get(en.getKey()) + epsilon)/2;
                    //TODO: some meaningful estimate of variance;
                    double value = 1 - (new NormalDistribution(0,2/Math.sqrt(aCount))).cumulativeProbability(diff);
                    
                    if (value > max) {
                            max = value;
                            bestAction = en.getKey();
                    }
		}
		return bestAction;
    }
}
