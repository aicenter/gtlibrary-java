package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;

public interface DOCache {

    public void setStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, MixedStrategy<ActionPureStrategy>[] strategy);

    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, MixedStrategy<ActionPureStrategy>[] strategy);

    public MixedStrategy<ActionPureStrategy>[] getStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);

    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double utilityValue);
	
	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double utility);

	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double bound, Double pesimisticUtilityFromCache);

	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double optimisticUtility, Double pesimisticUtility);

	public Double getPesimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public Double getUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);
	
	public Double getUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

	public Double getOptimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);

	public Double getPesimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair);

	public Double getOptimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2);

    public void setTempStrategy(Pair<ActionPureStrategy, ActionPureStrategy> actionPair, MixedStrategy<ActionPureStrategy>[] strategiesFromAlphaBeta);

    public MixedStrategy<ActionPureStrategy>[] getTempStrategy(Pair<ActionPureStrategy, ActionPureStrategy> actionPair);

}
