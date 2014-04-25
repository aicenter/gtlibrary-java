package cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.P1LowerBoundComparator;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.P2LowerBoundComparator;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.StrategyComparator;

public class LowerBoundComparatorFactory implements ComparatorFactory {

	@Override
	public StrategyComparator getP1Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data) {
		return new P1LowerBoundComparator(mixedStrategy, state, data);
	}

	@Override
	public StrategyComparator getP2Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data) {
		return new P2LowerBoundComparator(mixedStrategy, state, data);
	}

}
