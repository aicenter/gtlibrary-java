package cz.agents.gtlibrary.algorithms.mcts.distribution;

import java.util.Map;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.interfaces.Action;

public interface Distribution {
	public Map<Action, Double> getDistributionFor(Map<Action, BackPropagationStrategy> informationSetStats, Map<Action, BackPropagationStrategy> nodeStats);
}
