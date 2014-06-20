package cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.P1UpperBoundComparator;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.P2UpperBoundComparator;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.StrategyComparator;

public class UpperBoundComparatorFactory implements ComparatorFactory {

	@Override
	public StrategyComparator getP1Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data, DOCache cache) {
		return new P1UpperBoundComparator(mixedStrategy, state, data, cache);
	}

	@Override
	public StrategyComparator getP2Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data, DOCache cache) {
		return new P2UpperBoundComparator(mixedStrategy, state, data, cache);
	}

}
