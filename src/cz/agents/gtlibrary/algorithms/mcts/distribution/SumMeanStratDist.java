package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.Map;

public class SumMeanStratDist implements Distribution {
    
        @Override
	public Map<Action, Double> getDistributionFor(AlgorithmData data) {
                MeanStrategyProvider stat = (MeanStrategyProvider) data;
                if (stat == null) return null;
                final double[] mp = stat.getMp();
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(stat.getActions().size());
                int i=0;
		for (Action a : stat.getActions()) distribution.put(a, mp[i++]);
		return distribution;
	}
}
