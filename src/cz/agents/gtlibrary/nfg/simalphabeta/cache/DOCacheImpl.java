package cz.agents.gtlibrary.nfg.simalphabeta.cache;


import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;

public class DOCacheImpl implements DOCache {
	
	private Map<Pair<ActionPureStrategy, ActionPureStrategy>, Double> pesimisticUtilities;
	private Map<Pair<ActionPureStrategy, ActionPureStrategy>, Double> optimisticUtilities;
	private Map<Pair<ActionPureStrategy, ActionPureStrategy>, Double> actualUtilities;

	public DOCacheImpl() {
		pesimisticUtilities = new HashMap<Pair<ActionPureStrategy, ActionPureStrategy>, Double>();
		optimisticUtilities = new HashMap<Pair<ActionPureStrategy, ActionPureStrategy>, Double>();
		actualUtilities = new HashMap<Pair<ActionPureStrategy, ActionPureStrategy>, Double>();
	}

	public Double getPesimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return pesimisticUtilities.get(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2));
	}

	public Double getPesimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return pesimisticUtilities.get(strategyPair);
	}

	public Double getOptimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return optimisticUtilities.get(strategyPair);
	}
	
	@Override
	public Double getOptimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return getOptimisticUtilityFor(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2));
	}

    @Override
    public void setStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, MixedStrategy<ActionPureStrategy>[] strategy) {
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, MixedStrategy<ActionPureStrategy>[] strategy) {
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
        return null;
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
        return null;
    }

    public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double utility) {
		if (!utility.isNaN()) {
			optimisticUtilities.put(strategyPair, utility);
			pesimisticUtilities.put(strategyPair, utility);
		}
		actualUtilities.put(strategyPair, utility);
	}

	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double utility) {
		setPesAndOptValueFor(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2), utility);
	}

	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double optimisticUtility, Double pesimisticUtility) {
		setPesAndOptValueFor(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2), optimisticUtility, pesimisticUtility);
	}

	public Double getUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return getUtilityFor(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2));
	}
	
	public Double getUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return actualUtilities.get(strategyPair);
	}


	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double optimisticUtility, Double pesimisticUtility) {
		assert optimisticUtility >= pesimisticUtility;
		optimisticUtilities.put(strategyPair, optimisticUtility);
		pesimisticUtilities.put(strategyPair, pesimisticUtility);
		if (optimisticUtility - pesimisticUtility < 1e-14)
			actualUtilities.put(strategyPair, optimisticUtility);
	}
}
