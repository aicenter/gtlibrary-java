package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BackPropagationStrategy;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.RunningStats;

public abstract class MaxFunctionSelector implements SelectionStrategy {

	@Override
	public int select(RunningStats nodeStats, Map<Action, BackPropagationStrategy> actionStats) {
		assert actionStats.size() > 0;

		int maxIndex = -1;
		int currentIndex = 0;
		double maxValue = Double.NEGATIVE_INFINITY;

		for (Entry<Action, BackPropagationStrategy> entry : actionStats.entrySet()) {
			double value = evaluate(nodeStats, entry.getValue());

			if (value > maxValue) {
				maxValue = value;
				maxIndex = currentIndex;
			}
			currentIndex++;
		}

//		if (maxIndex == -1) {
//			//fall back on max value selector which can't fail;
//			return (new MaxValueSelector()).select(nodeStats, actionStats);
//		}
		return maxIndex;
	}

	protected abstract double evaluate(RunningStats nodeStats, BackPropagationStrategy actionStats);

}
