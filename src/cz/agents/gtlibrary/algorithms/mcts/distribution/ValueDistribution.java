package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class ValueDistribution implements Distribution {

	@Override
	public Map<Action, Double> getDistributionFor(Map<Action, BPStrategy> informationSetStats, Map<Action, BPStrategy> nodeActionStats) {
		double sum = getSumOfValues(informationSetStats);
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(informationSetStats.size());
		
		for (Entry<Action, BPStrategy> entry : informationSetStats.entrySet()) {
			distribution.put(entry.getKey(), entry.getValue().getEV()/sum);
		}
		return distribution;
	}

	private double getSumOfValues(Map<Action, BPStrategy> actionStats) {
		double sum = 0;

		for (BPStrategy backPropagationStrategy : actionStats.values()) {
			sum += backPropagationStrategy.getEV();
		}
		return sum;
	}

}
