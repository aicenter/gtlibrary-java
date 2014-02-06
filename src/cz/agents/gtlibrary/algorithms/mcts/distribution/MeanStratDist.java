package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.Map;

public class MeanStratDist implements Distribution {
    
	@Override
	public Map<Action, Double> getDistributionFor(MCTSInformationSet infSet) {
                MeanStrategyProvider stat = (MeanStrategyProvider) infSet.selectionStrategy;
                if (stat == null) return null;
                final double[] mp = stat.getMp();
                double sum = 0;
                for (double d : mp) sum += d;
                
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(stat.getActions().size());

                int i=0;
		for (Action a : stat.getActions()) distribution.put(a, mp[i++]/sum);
                
		return distribution;
	}
}
