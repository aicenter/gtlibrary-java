package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.Action;

public interface Distribution {
	public Map<Action, Double> getDistributionFor(Map<Action, BPStrategy> isActionStats, Map<Action, BPStrategy> nodeActionStats);
}
