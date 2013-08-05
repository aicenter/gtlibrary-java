package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.utils.Pair;

public class NullDOCache implements DOCache {

	@Override
	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double utilityValue) {
	}

	@Override
	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double utility) {
	}

	@Override
	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double bound, Double pesimisticUtilityFromCache) {
	}

	@Override
	public Double getOptUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return null;
	}

	@Override
	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double optimisticUtility, Double pesimisticUtility) {
	}

	@Override
	public Double getPesimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return null;
	}

	@Override
	public Double getUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return null;
	}

	@Override
	public Double getOptimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return null;
	}

	@Override
	public Double getPesimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return null;
	}
}
