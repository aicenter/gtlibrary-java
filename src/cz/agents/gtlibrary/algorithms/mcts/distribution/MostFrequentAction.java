package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class MostFrequentAction implements Distribution {

	@Override
	public Map<Action, Double> getDistributionFor(Map<Action, BPStrategy> isActionStats, Map<Action, BPStrategy> nodeActionStats) {
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(isActionStats.size());
		Action mostFrequentAction = null;
		int count = Integer.MIN_VALUE;
		
		for (Entry<Action, BPStrategy> entry : isActionStats.entrySet()) {
			if(entry.getValue().getNbSamples() > count) {
				mostFrequentAction = entry.getKey();
				count = entry.getValue().getNbSamples();
			}				
			distribution.put(entry.getKey(), 0d);
		}
		distribution.put(mostFrequentAction, 1d);
		return distribution;
	}

}
