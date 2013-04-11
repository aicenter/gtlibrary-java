package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.OOSSelector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.Map;

public class OOSMeanStratDist implements Distribution {
    
	@Override
	public Map<Action, Double> getDistributionFor(MCTSInformationSet infSet) {
                OOSSelector oos = (OOSSelector) infSet.selectionStrategy;
                final double[] mp = oos.getMp();
                double sum = 0;
                for (double d : mp) sum += d;
                
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(oos.getActions().size());

                int i=0;
		for (Action a : oos.getActions()) distribution.put(a, mp[i++]/sum);
                
		return distribution;
	}
}
