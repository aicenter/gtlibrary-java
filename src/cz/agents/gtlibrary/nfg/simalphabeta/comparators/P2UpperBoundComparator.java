package cz.agents.gtlibrary.nfg.simalphabeta.comparators;

import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class P2UpperBoundComparator extends BoundComparator {

	public P2UpperBoundComparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data) {
		super(mixedStrategy, state, data);
	}

	@Override
	protected double getValue(ActionPureStrategy strategy) {
		double value = 0;

		for (Entry<ActionPureStrategy, Double> p1Entry : mixedStrategy) {
			value += p1Entry.getValue() * getPesimistic(p1Entry.getKey(), strategy);
		}
		return value;
	}

	protected Double getPesimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		Double cachedValue = cache.getOptimisticUtilityFor(p1Strategy, p2Strategy);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetPesimistic(p1Strategy, p2Strategy);
		return -cachedValue;
	}

	protected Double updateCacheAndGetPesimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = getStateAfter(p1Strategy, p2Strategy);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -p2AlphaBeta.getUnboundedValue(state);
		double optimisticUtility = p1AlphaBeta.getUnboundedValue(state);

		Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}
}
