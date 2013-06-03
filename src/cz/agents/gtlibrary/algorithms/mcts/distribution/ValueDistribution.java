package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import java.util.Map;
import java.util.Map.Entry;

public class ValueDistribution implements Distribution {

	@Override
	public Map<Action, Double> getDistributionFor(MCTSInformationSet infSet) {
                Map<Action, BasicStats> informationSetStats = infSet.getActionStats();
		double sum = getSumOfValues(informationSetStats);
		Map<Action, Double> distribution = new FixedSizeMap<Action, Double>(informationSetStats.size());
		
		for (Entry<Action, BasicStats> entry : informationSetStats.entrySet()) {
			distribution.put(entry.getKey(), entry.getValue().getEV()/sum);
		}
		return distribution;
	}

	private double getSumOfValues(Map<Action, BasicStats> actionStats) {
		double sum = 0;

		for (BasicStats backPropagationStrategy : actionStats.values()) {
			sum += backPropagationStrategy.getEV();
		}
		return sum;
	}

}
