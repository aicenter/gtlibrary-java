package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;

public class NullDOCache implements DOCache {

    @Override
    public void setStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, MixedStrategy<ActionPureStrategy>[] strategy) {
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, MixedStrategy<ActionPureStrategy>[] strategy) {
    }

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
	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double optimisticUtility, Double pesimisticUtility) {
	}

    @Override
    public void setTempStrategy(Pair<ActionPureStrategy, ActionPureStrategy> actionPair, MixedStrategy<ActionPureStrategy>[] strategiesFromAlphaBeta) {
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getTempStrategy(Pair<ActionPureStrategy, ActionPureStrategy> actionPair) {
        return null;
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
        return null;
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
        return null;
    }

    @Override
	public Double getPesimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return null;
	}

	@Override
	public Double getUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
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

	@Override
	public Double getOptimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return null;
	}

}
