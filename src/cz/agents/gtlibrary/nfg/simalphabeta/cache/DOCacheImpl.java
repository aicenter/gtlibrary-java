/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.simalphabeta.cache;


import java.util.HashMap;
import java.util.Map;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Result;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

public class DOCacheImpl implements DOCache {
	
	private Map<Pair<ActionPureStrategy, ActionPureStrategy>, Double> pesimisticUtilities;
	private Map<Pair<ActionPureStrategy, ActionPureStrategy>, Double> optimisticUtilities;
	private Map<Pair<ActionPureStrategy, ActionPureStrategy>, Double> actualUtilities;

	public DOCacheImpl() {
		pesimisticUtilities = new HashMap<>();
		optimisticUtilities = new HashMap<>();
		actualUtilities = new HashMap<>();
	}

	public Double getPesimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return pesimisticUtilities.get(new Pair<>(strategy1, strategy2));
	}

	public Double getPesimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return pesimisticUtilities.get(strategyPair);
	}

	public Double getOptimisticUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return optimisticUtilities.get(strategyPair);
	}
	
	@Override
	public Double getOptimisticUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return getOptimisticUtilityFor(new Pair<>(strategy1, strategy2));
	}

    @Override
    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, Player player, Result result) {
    }

    @Override
    public Result getP1TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return null;
    }

    @Override
    public Result getP2TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return null;
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result[] results) {
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result[] results) {
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result p1Result, Result p2Result) {
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result p1Result, Result p2Result) {

    }


    @Override
    public Result[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3) {
        return null;
    }

    @Override
    public Result[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyPair) {
        return null;
    }

    @Override
    public Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, Result[]> getStrategies() {
        return null;
    }

    @Override
    public Result[] getTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
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
		setPesAndOptValueFor(new Pair<>(strategy1, strategy2), utility);
	}

	public void setPesAndOptValueFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2, Double optimisticUtility, Double pesimisticUtility) {
		setPesAndOptValueFor(new Pair<>(strategy1, strategy2), optimisticUtility, pesimisticUtility);
	}

	public Double getUtilityFor(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return getUtilityFor(new Pair<>(strategy1, strategy2));
	}
	
	public Double getUtilityFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return actualUtilities.get(strategyPair);
	}


	public void setPesAndOptValueFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, Double optimisticUtility, Double pesimisticUtility) {
		assert optimisticUtility >= pesimisticUtility;
		optimisticUtilities.put(strategyPair, optimisticUtility);
		pesimisticUtilities.put(strategyPair, pesimisticUtility);
		if (optimisticUtility - pesimisticUtility <= 1e-5)
			actualUtilities.put(strategyPair, optimisticUtility);
	}
}
