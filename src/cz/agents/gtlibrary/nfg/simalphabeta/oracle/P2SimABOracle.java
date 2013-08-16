package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collection;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class P2SimABOracle extends SimOracleImpl {

	public P2SimABOracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, rootState.getAllPlayers()[1], utility, data);
	}

	@Override
	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		ActionPureStrategy bestStrategy = null;
		Collection<ActionPureStrategy> possibleActions = getActions();
		double bestValue = -beta;

		for (ActionPureStrategy strategy : possibleActions) {
			double utilityValue = getValueForAction(mixedStrategy, strategy, bestValue);

			if (bestStrategy == null) {
				if (utilityValue > bestValue - 1e-8) {
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
		return new Pair<ActionPureStrategy, Double>(bestStrategy, bestValue);
	}

	private double getValueForAction(MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, double bestValue) {
		double utilityValue = 0;
		int index = 0;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (entry.getValue() > 1e-8) {
				Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(entry.getKey(), strategy);
				Double cacheValue = getValueFromCache(strategyPair);
				double cacheWindow = getLowerBoundFromCache(strategyPair);
				double windowValue = Math.max(cacheWindow, getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index));

				if (cacheValue == null) {
					if (getPesimisticValueFromCache(strategyPair) < windowValue) {
						Stats.incrementABCuts();
						return Double.NEGATIVE_INFINITY;
					}
					updateCacheValuesFor(strategyPair, windowValue);
				} else if (cacheValue < windowValue - 1e-8) {
					Stats.incrementCacheCuts();
					return Double.NEGATIVE_INFINITY;
				}
				Double util = utility.getUtility(strategy, entry.getKey());
				
				if (util.isNaN())
					return Double.NEGATIVE_INFINITY;
				utilityValue += util * entry.getValue();
			}
			index++;
		}
		return utilityValue;
	}
	
	private void updateCacheValuesFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, double bound) {
		double pesimisticUtilityFromCache = cache.getPesimisticUtilityFor(strategyPair);
		double optimisticUtilityFromCache = cache.getOptimisticUtilityFor(strategyPair);
		double pesimisticUtility = pesimisticUtilityFromCache;
		double optimisticUtility = optimisticUtilityFromCache;

		if (Math.abs(optimisticUtility - pesimisticUtility) > 1e-14) {
			if (USE_INCREASING_BOUND)
				if (-bound <= optimisticUtility)
					optimisticUtility = -bound;
			assert optimisticUtility >= pesimisticUtility;
			double utilityValue = -utility.getUtility(strategyPair.getRight(), strategyPair.getLeft(), pesimisticUtility - 1e-4, optimisticUtility);

			if (utilityValue == utilityValue) {
				cache.setPesAndOptValueFor(strategyPair, utilityValue);
			} else if (-bound <= optimisticUtilityFromCache && -bound > pesimisticUtilityFromCache) {
				Stats.incrementBoundsTightened();
				cache.setPesAndOptValueFor(strategyPair, optimisticUtilityFromCache, -bound);
			}
		}
	}


	private Double getLowerBoundFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		return getOptimisticValueFromCache(strategyPair);
	}

	private Double getOptimisticValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double cachedValue = cache.getOptimisticUtilityFor(strategyPair);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetOptimistic(strategyPair);
		return -cachedValue;
	}

	private Double updateCacheAndGetOptimistic(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -alphaBeta.getUnboundedValue(state);
		double optimisticUtility = oppAlphaBeta.getUnboundedValue(state);
		
		Stats.addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(strategyPair, optimisticUtility, pesimisticUtility);
		return optimisticUtility;
	}

	private Double getPesimisticValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double cachedValue = cache.getPesimisticUtilityFor(strategyPair);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetPesimistic(strategyPair);
		return -cachedValue;
	}

	private Double updateCacheAndGetPesimistic(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -alphaBeta.getUnboundedValue(state);
		double optimisticUtility = oppAlphaBeta.getUnboundedValue(state);

		Stats.addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(strategyPair, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}

	private Double getValueFromCache(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		Double utility = cache.getUtilityFor(strategyPair);

		if (utility == null)
			return null;
		return -utility;
	}

	private double getWindowValue(double utilityValue, double bestValue, double currProbability, MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, int index) {
		int currentIndex = 0;
		double utility = utilityValue;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (currentIndex > index)
				utility += getPesimisticValueFromCache(new Pair<ActionPureStrategy, ActionPureStrategy>(entry.getKey(), strategy)) * entry.getValue();
			currentIndex++;
		}
		return (bestValue - utility) / currProbability;
	}

	private GameState getStateAfter(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = rootState.performAction(strategyPair.getLeft().getAction());

		state.performActionModifyingThisState(strategyPair.getRight().getAction());
		return state;
	}
}
