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

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Result;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.Map;

public class NullDOCache implements DOCache {

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result[] strategy) {
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result[] strategy) {
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, Result p1Strategy, Result p2Strategy) {

    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, Result p1strategy, Result p2strategy) {

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
    public Result[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3) {
        return null;
    }

    @Override
    public Result[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet) {
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

    @Override
    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, Player player, Result strategiesFromAlphaBeta) {
    }

    @Override
    public Result getP1TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return null;
    }

    @Override
    public Result getP2TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return null;
    }

}
