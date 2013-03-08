package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.RunningStats;

public abstract class MaxFunctionSelector implements SelectionStrategy {

	@Override
	public Action select(RunningStats nodeStats, Map<Action, BackPropagationStrategy> actionStats) {
		assert actionStats.size() > 0;

		Action bestAction = null;
		double maxValue = Double.NEGATIVE_INFINITY;

		for (Entry<Action, BackPropagationStrategy> entry : actionStats.entrySet()) {
			double value = evaluate(nodeStats, entry.getValue());

			if (value > maxValue) {
				maxValue = value;
				bestAction = entry.getKey();
			}
		}
		return bestAction;
	}

	protected abstract double evaluate(RunningStats nodeStats, BackPropagationStrategy actionStats);

}
