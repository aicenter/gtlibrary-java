package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.utils.Pair;

public interface DOCache {

	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double utilityValue);
	
	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double utility);

	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double bound, Double pesimisticUtilityFromCache);

	public Double getOptUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double optimisticUtility, Double pesimisticUtility);

	public Double getPesimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public Double getUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public Double getOptimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);

	public Double getPesimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);

	
}
