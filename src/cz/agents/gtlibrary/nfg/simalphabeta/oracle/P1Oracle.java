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

public class P1Oracle extends SimOracleImpl {

	public P1Oracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, rootState.getAllPlayers()[0], utility, data);
	}

	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		Collection<ActionPureStrategy> possibleActions = getActions();
		ActionPureStrategy bestStrategy = null;
		double bestValue = alpha - 1e-8;

		for (ActionPureStrategy strategy : possibleActions) {
			double utilityValue = getValueForAction(mixedStrategy, bestValue, strategy);

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

	protected double getValueForAction(MixedStrategy<ActionPureStrategy> mixedStrategy, double bestValue, ActionPureStrategy strategy) {
		double utilityValue = 0;

		for (ActionPureStrategy action : mixedStrategy.sortStrategies()) {
            if(Killer.kill)
                return Double.NaN;
            double actionProb = mixedStrategy.getProbability(action);
            if (actionProb > 1e-8) {
				Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(strategy, action);
				Double cacheValue = getValueFromCache(strategyPair);
				double cacheWindow = getLowerBoundFromCache(strategyPair);
				double windowValue = Math.max(cacheWindow, getWindowValue(bestValue, actionProb, mixedStrategy, strategy, action));

                if(Killer.kill)
                    return Double.NaN;
				if (cacheValue == null) {
					if (getOptimisticValueFromCache(strategyPair) < windowValue) {
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

		if (optimisticUtility - pesimisticUtility > 1e-5) {
			if (USE_INCREASING_BOUND && bound >= pesimisticUtility) {
				pesimisticUtility = bound;
				utilityValue = utility.getUtilityForIncreasedBounds(strategyPair.getLeft(), strategyPair.getRight(), pesimisticUtility, optimisticUtility);
			} else {
				utilityValue = utility.getUtility(strategyPair.getLeft(), strategyPair.getRight(), pesimisticUtility, optimisticUtility);
			}
            assert optimisticUtility >= pesimisticUtility;
			if (utilityValue == utilityValue) {
				cache.setPesAndOptValueFor(strategyPair, utilityValue);
			} else if (pesimisticUtilityFromCache <= bound && bound < optimisticUtilityFromCache) {
				Stats.getInstance().incrementBoundsTightened();
				cache.setPesAndOptValueFor(strategyPair, bound, pesimisticUtilityFromCache);
			}
		}
	}

	protected double getWindowValue(double bestValue, double currProbability, MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, ActionPureStrategy excludeStrategy) {
		double utility = 0;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
            if(Killer.kill)
                return Double.NaN;
            if (entry.getKey().equals(excludeStrategy))
                continue;
			utility += getOptimisticValueFromCache(new Pair<>(strategy, entry.getKey())) * entry.getValue();
		}
		return (bestValue - utility) / currProbability;
	}

	protected Double getLowerBoundFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return getPesimisticValueFromCache(strategyPair);
	}

	protected Double getPesimisticValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double cachedValue = cache.getPesimisticUtilityFor(strategyPair);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetPesimistic(strategyPair);
		return cachedValue;
	}

	protected Double updateCacheAndGetPesimistic(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -oppAlphaBeta.getUnboundedValueAndStoreStrategy(state, cache);
		double optimisticUtility = alphaBeta.getUnboundedValueAndStoreStrategy(state, cache);

        if(Killer.kill)
            return Double.NaN;
		Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(strategyPair, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}

	protected Double getOptimisticValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double cachedValue = cache.getOptimisticUtilityFor(strategyPair);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetOptimistic(strategyPair);
		return cachedValue;
	}

	protected Double updateCacheAndGetOptimistic(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -oppAlphaBeta.getUnboundedValueAndStoreStrategy(state, cache);
		double optimisticUtility = alphaBeta.getUnboundedValueAndStoreStrategy(state, cache);

        if(Killer.kill)
            return Double.NaN;
		Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(strategyPair, optimisticUtility, pesimisticUtility);
		return optimisticUtility;
	}

	protected Double getValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return cache.getUtilityFor(strategyPair);
	}

	protected GameState getStateAfter(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = rootState.performAction(strategyPair.getLeft().getAction());

		state.performActionModifyingThisState(strategyPair.getRight().getAction());
		return state;
	}

}
