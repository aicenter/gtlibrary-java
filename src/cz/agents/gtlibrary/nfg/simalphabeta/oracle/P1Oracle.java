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

public class P1Oracle extends SimOracleImpl {

	public P1Oracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, rootState.getAllPlayers()[0], utility, data);
	}

	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		Collection<ActionPureStrategy> possibleActions = getActions();
		ActionPureStrategy bestStrategy = null;
		double bestValue = alpha;
		
		for (ActionPureStrategy strategy : possibleActions) {
			double utilityValue = getValueForAction(mixedStrategy, bestValue, strategy);

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

	protected double getValueForAction(MixedStrategy<ActionPureStrategy> mixedStrategy, double bestValue, ActionPureStrategy strategy) {
		double utilityValue = 0;
		int index = 0;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) { 
			if (entry.getValue() > 1e-8) {
				Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(strategy, entry.getKey());
				Double cacheValue = getValueFromCache(strategyPair);
				double cacheWindow = getLowerBoundFromCache(strategyPair);
				double windowValue = Math.max(cacheWindow, getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index));

				if (cacheValue == null) {
					if (getOptimisticValueFromCache(strategyPair) < windowValue) {
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

	protected void updateCacheValuesFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, double bound) {
		double pesimisticUtilityFromCache = cache.getPesimisticUtilityFor(strategyPair);
		double optimisticUtilityFromCache = cache.getOptimisticUtilityFor(strategyPair);
		double pesimisticUtility = pesimisticUtilityFromCache;
		double optimisticUtility = optimisticUtilityFromCache;

		if (optimisticUtility - pesimisticUtility > 1e-14) {
			if (USE_INCREASING_BOUND)
				if (bound >= pesimisticUtility)
					pesimisticUtility = bound;
			assert optimisticUtility >= pesimisticUtility;
			double utilityValue = utility.getUtility(strategyPair.getLeft(), strategyPair.getRight(), pesimisticUtility - 1e-4, optimisticUtility);

			if (utilityValue == utilityValue) {
				cache.setPesAndOptValueFor(strategyPair, utilityValue);
			} else if (pesimisticUtilityFromCache <= bound && bound < optimisticUtilityFromCache) {
				Stats.incrementBoundsTightened();
				cache.setPesAndOptValueFor(strategyPair, bound, pesimisticUtilityFromCache);
			}
		}
	}

	protected double getWindowValue(double utilityValue, double bestValue, double currProbability, MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, int index) {
		int currentIndex = 0;
		double utility = utilityValue;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (currentIndex > index)
				utility += getOptimisticValueFromCache(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy, entry.getKey())) * entry.getValue();
			currentIndex++;
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
		double pesimisticUtility = -oppAlphaBeta.getUnboundedValue(state);
		double optimisticUtility = alphaBeta.getUnboundedValue(state);

		Stats.addToABTime(System.currentTimeMillis() - time);
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
		double pesimisticUtility = -oppAlphaBeta.getUnboundedValue(state);
		double optimisticUtility = alphaBeta.getUnboundedValue(state);

		Stats.addToABTime(System.currentTimeMillis() - time);
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
