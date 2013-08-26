package cz.agents.gtlibrary.nfg.simalphabeta.comparators;

import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class P2LowerBoundComparator extends LowerBoundComparator {

	public P2LowerBoundComparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data) {
		super(mixedStrategy, state, data);
	}

	@Override
	protected double getValue(ActionPureStrategy strategy) {
		double value = 0;

		for (Entry<ActionPureStrategy, Double> p1Entry : mixedStrategy) {
			value += p1Entry.getValue() * getOptimistic(p1Entry.getKey(), strategy);
		}
		return value;
	}

	protected Double getOptimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		Double cachedValue = cache.getOptimisticUtilityFor(p1Strategy, p2Strategy);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetOptimistic(p1Strategy, p2Strategy);
		return -cachedValue;
	}

	protected Double updateCacheAndGetOptimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = getStateAfter(p1Strategy, p2Strategy);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -p2AlphaBeta.getUnboundedValue(state);
		double optimisticUtility = p1AlphaBeta.getUnboundedValue(state);

		Stats.addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return optimisticUtility;
	}

}
