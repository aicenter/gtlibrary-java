package cz.agents.gtlibrary.nfg.simalphabeta.cache;


import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

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
    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, Player player, MixedStrategy<ActionPureStrategy> strategiesFromAlphaBeta) {
    }

    @Override
    public MixedStrategy<ActionPureStrategy> getP1TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return null;
    }

    @Override
    public MixedStrategy<ActionPureStrategy> getP2TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return null;
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, MixedStrategy<ActionPureStrategy>[] strategy) {
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, MixedStrategy<ActionPureStrategy>[] strategy) {
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, MixedStrategy<ActionPureStrategy> p1Strategy, MixedStrategy<ActionPureStrategy> p2Strategy) {
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, MixedStrategy<ActionPureStrategy> p1strategy, MixedStrategy<ActionPureStrategy> p2strategy) {

    }


    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3) {
        return null;
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyPair) {
        return null;
    }

    @Override
    public Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> getStrategies() {
        return null;
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
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
