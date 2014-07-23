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


package cz.agents.gtlibrary.nfg.simalphabeta.comparators;

import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class P2UpperBoundComparator extends BoundComparator {

	public P2UpperBoundComparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data, DOCache cache) {
		super(mixedStrategy, state, data, cache);
	}

	@Override
	protected double getValue(ActionPureStrategy strategy) {
		double value = 0;

		for (Entry<ActionPureStrategy, Double> p1Entry : mixedStrategy) {
			value += p1Entry.getValue() * getPesimistic(p1Entry.getKey(), strategy);
		}
		return value;
	}

	protected Double getPesimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		Double cachedValue = cache.getOptimisticUtilityFor(p1Strategy, p2Strategy);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetPesimistic(p1Strategy, p2Strategy);
		return -cachedValue;
	}

	protected Double updateCacheAndGetPesimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = getStateAfter(p1Strategy, p2Strategy);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -p2AlphaBeta.getUnboundedValue(state);
		double optimisticUtility = p1AlphaBeta.getUnboundedValue(state);

		Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}
}
