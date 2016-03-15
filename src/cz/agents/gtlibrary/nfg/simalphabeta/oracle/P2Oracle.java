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


package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collection;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.Killer;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class P2Oracle extends SimOracleImpl {

	public P2Oracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, rootState.getAllPlayers()[1], utility, data);
	}

	@Override
	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		ActionPureStrategy bestStrategy = null;
		Collection<ActionPureStrategy> possibleActions = getActions();
		double bestValue = -beta - 1e-8;

		for (ActionPureStrategy strategy : possibleActions) {
			double utilityValue = getValueForAction(mixedStrategy, strategy, bestValue);

            if(Killer.kill)
                return null;
			if (bestStrategy == null) {
				if (utilityValue > bestValue ) {
                    bestValue = utilityValue;
					bestStrategy = strategy;
				}
			} else {
				if (utilityValue > bestValue) {
                    bestValue = utilityValue;
					bestStrategy = strategy;
				}
			}
		}
		return new Pair<>(bestStrategy, bestValue);
	}

	protected double getValueForAction(MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, double bestValue) {
		double utilityValue = 0;

		for (ActionPureStrategy action : mixedStrategy.sortStrategies()) {
            double actionProb = mixedStrategy.getProbability(action);

            if(Killer.kill)
                return Double.NaN;
			if (actionProb > 1e-8) {
				Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(action, strategy);
				Double cacheValue = getValueFromCache(strategyPair);
				double cacheWindow = getLowerBoundFromCache(strategyPair);
				double windowValue = Math.max(cacheWindow, getWindowValue(bestValue, actionProb, mixedStrategy, strategy, action));

                if(Killer.kill)
                    return Double.NaN;
				if (cacheValue == null) {
					if (getPesimisticValueFromCache(strategyPair) < windowValue) {
						Stats.getInstance().incrementABCuts();
						return Double.NEGATIVE_INFINITY;
					}
					updateCacheValuesFor(strategyPair, windowValue);
				} else if (cacheValue < windowValue - 1e-8) {
					Stats.getInstance().incrementCacheCuts();
					return Double.NEGATIVE_INFINITY;
				}
				Double util = utility.getUtility(strategy, action);

				if (util.isNaN())
					return Double.NEGATIVE_INFINITY;
				utilityValue += util * actionProb;
			}
		}
		return utilityValue;
	}

	protected void updateCacheValuesFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, double bound) {
		double pesimisticUtilityFromCache = cache.getPesimisticUtilityFor(strategyPair);
		double optimisticUtilityFromCache = cache.getOptimisticUtilityFor(strategyPair);
		double pesimisticUtility = pesimisticUtilityFromCache;
		double optimisticUtility = optimisticUtilityFromCache;
		double utilityValue;

		if (Math.abs(optimisticUtility - pesimisticUtility) > 1e-5) {
			if (USE_INCREASING_BOUND && -bound < optimisticUtility) {
				optimisticUtility = -bound;
				utilityValue = -utility.getUtilityForIncreasedBounds(strategyPair.getRight(), strategyPair.getLeft(), pesimisticUtility, optimisticUtility);
			} else {
				utilityValue = -utility.getUtility(strategyPair.getRight(), strategyPair.getLeft(), pesimisticUtility, optimisticUtility);
			}
            assert optimisticUtility >= pesimisticUtility;
			if (utilityValue == utilityValue) {
				cache.setPesAndOptValueFor(strategyPair, utilityValue);
			} else if (-bound <= optimisticUtilityFromCache && -bound > pesimisticUtilityFromCache) {
				Stats.getInstance().incrementBoundsTightened();
				cache.setPesAndOptValueFor(strategyPair, optimisticUtilityFromCache, -bound);
			}
		}
	}

	protected Double getLowerBoundFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return getOptimisticValueFromCache(strategyPair);
	}

	protected Double getOptimisticValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double cachedValue = cache.getOptimisticUtilityFor(strategyPair);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetOptimistic(strategyPair);
		return -cachedValue;
	}

	protected Double updateCacheAndGetOptimistic(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -alphaBeta.getUnboundedValueAndStoreStrategy(state, cache);
		double optimisticUtility = oppAlphaBeta.getUnboundedValueAndStoreStrategy(state, cache);

        if(Killer.kill)
            return Double.NaN;
		Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(strategyPair, optimisticUtility, pesimisticUtility);
//        storeStrategy(alphaBeta, strategyPair);
		return optimisticUtility;
	}

    protected Double getPesimisticValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double cachedValue = cache.getPesimisticUtilityFor(strategyPair);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetPesimistic(strategyPair);
		return -cachedValue;
	}

	protected Double updateCacheAndGetPesimistic(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -alphaBeta.getUnboundedValueAndStoreStrategy(state, cache);
		double optimisticUtility = oppAlphaBeta.getUnboundedValueAndStoreStrategy(state, cache);

        if(Killer.kill)
            return Double.NaN;
		Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(strategyPair, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}

	protected Double getValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double utility = cache.getUtilityFor(strategyPair);

		if (utility == null)
			return null;
		return -utility;
	}

	protected double getWindowValue(double bestValue, double currProbability, MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, ActionPureStrategy excludeStrategy) {
		double utility = 0;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
            if(Killer.kill)
                return Double.NaN;
            if (entry.getKey().equals(excludeStrategy))
                continue;
			utility += getPesimisticValueFromCache(new Pair<>(entry.getKey(), strategy)) * entry.getValue();
		}
		return (bestValue - utility) / currProbability;
	}

	protected GameState getStateAfter(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = rootState.performAction(strategyPair.getLeft().getAction());

		state.performActionModifyingThisState(strategyPair.getRight().getAction());
		return state;
	}
}
