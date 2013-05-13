package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class MostFrequentAction implements Distribution {

	@Override
	public Map<Action, Double> getDistributionFor(MCTSInformationSet infSet) {
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(infSet.getActionStats().size());
		Action mostFrequentAction = null;
		int count = Integer.MIN_VALUE;
		
		for (Entry<Action, BasicStats> entry : infSet.getActionStats().entrySet()) {
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
