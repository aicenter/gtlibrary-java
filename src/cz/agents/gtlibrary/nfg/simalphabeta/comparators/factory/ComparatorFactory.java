package cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.StrategyComparator;

public interface ComparatorFactory {
	
	public StrategyComparator getP1Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data);

	public StrategyComparator getP2Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data);
	
}
