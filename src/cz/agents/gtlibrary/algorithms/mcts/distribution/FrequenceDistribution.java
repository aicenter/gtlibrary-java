package cz.agents.gtlibrary.algorithms.mcts.distribution;

import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class FrequenceDistribution implements Distribution {

	@Override
	public Map<Action, Double> getDistributionFor(Map<Action, BackPropagationStrategy> informationSetStats, Map<Action, BackPropagationStrategy> nodeStats) {
		long sum = getSumOfVisits(informationSetStats);
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(informationSetStats.size());
		
		for (Entry<Action, BackPropagationStrategy> entry : informationSetStats.entrySet()) {
			distribution.put(entry.getKey(), (double)entry.getValue().getNbSamples()/sum);
		}
		return distribution;
	}

	private long getSumOfVisits(Map<Action, BackPropagationStrategy> actionStats) {
		long sum = 0;

		for (BackPropagationStrategy backPropagationStrategy : actionStats.values()) {
			sum += backPropagationStrategy.getNbSamples();
		}
		return sum;
	}

}
