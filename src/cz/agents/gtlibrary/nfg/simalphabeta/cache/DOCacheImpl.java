package cz.agents.gtlibrary.nfg.simalphabeta.cache;


import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
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

	public Double getOptUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return optimisticUtilities.get(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2));
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
		Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2);

		assert (optimisticUtility >= pesimisticUtility);
		optimisticUtilities.put(strategyPair, optimisticUtility);
		pesimisticUtilities.put(strategyPair, pesimisticUtility);
		if (optimisticUtility - pesimisticUtility < 1e-14)
			actualUtilities.put(strategyPair, optimisticUtility);
	}

	public Double getUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return actualUtilities.get(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2));
	}

	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double optimisticUtility, Double pesimisticUtility) {
		assert optimisticUtility >= pesimisticUtility;
		optimisticUtilities.put(strategyPair, optimisticUtility);
		pesimisticUtilities.put(strategyPair, pesimisticUtility);
		if (optimisticUtility - pesimisticUtility < 1e-14)
			actualUtilities.put(strategyPair, optimisticUtility);
	}

	

}
