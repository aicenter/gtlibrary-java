package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.backprop.BPStrategy;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;

public abstract class MaxFunctionSelector implements SelectionStrategy {

	@Override
	public Action select(Player player, BPStrategy nodeStats, Map<Action, BPStrategy> nodeActionStats, BPStrategy isStats, Map<Action, BPStrategy> isActionStats) {
		assert isActionStats.size() > 0;

		Action bestAction = null;
		double maxValue = Double.NEGATIVE_INFINITY;

		for (Entry<Action, BPStrategy> entry : isActionStats.entrySet()) {
			double value = evaluate(isStats, entry.getValue());

			if (value > maxValue) {
				maxValue = value;
				bestAction = entry.getKey();
			}
		}
		return bestAction;
	}

	protected abstract double evaluate(BPStrategy nodeStats, BPStrategy actionStats);

}
