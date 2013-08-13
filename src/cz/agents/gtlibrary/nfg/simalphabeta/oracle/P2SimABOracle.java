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

public class P2SimABOracle extends SimABOracleImpl {

	public P2SimABOracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, rootState.getAllPlayers()[1], utility, data);
	}

	@Override
	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta, double hardAlpha, double hardBeta) {
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

	private void updateCacheValuesFor(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy, double bound) {
		double pesimisticUtilityFromCache = cache.getPesimisticUtilityFor(p1Strategy, p2Strategy);
		double optimisticUtilityFromCache = cache.getOptimisticUtilityFor(p1Strategy, p2Strategy);
		double pesimisticUtility = pesimisticUtilityFromCache;
		double optimisticUtility = optimisticUtilityFromCache;

		if (optimisticUtility - pesimisticUtility > 1e-14) {
			if (USE_INCREASING_BOUND)
				if (-bound <= optimisticUtility)
					optimisticUtility = -bound;
			assert optimisticUtility >= pesimisticUtility;
			double utilityValue = -utility.getUtility(p2Strategy, p1Strategy, pesimisticUtility - 1e-4, optimisticUtility);

			if (utilityValue == utilityValue) {
				cache.setPesAndOptValueFor(p1Strategy, p2Strategy, utilityValue);
			} else if (-bound <= optimisticUtilityFromCache && -bound > pesimisticUtilityFromCache) {
				Stats.incrementBoundsTightened();
				cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtilityFromCache, -bound);
			}
		}
	}

	private double getValueForAction(MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, double bestValue) {
		double utilityValue = 0;
		int index = 0;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (entry.getValue() > 1e-8) {
				Double cacheValue = getValueFromCache(entry.getKey(), strategy);
				double cacheWindow = getLowerBoundFromCache(entry.getKey(), strategy);
				double windowValue = Math.max(cacheWindow, getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index));

				if (cacheValue == null) {
					if (getPesimisticValueFromCache(entry.getKey(), strategy) < windowValue) {
						Stats.incrementABCuts();
						return Double.NEGATIVE_INFINITY;
					}
					updateCacheValuesFor(entry.getKey(), strategy, windowValue);
				} else if (cacheValue < windowValue - 1e-8) {
					Stats.incrementCacheCuts();
					return Double.NEGATIVE_INFINITY;
				}
				Double util = utility.getUtility(strategy, entry.getKey());
				
				if (util == null)
					return Double.NEGATIVE_INFINITY;
				utilityValue += util * entry.getValue();
			}
			index++;
		}
		return utilityValue;
	}

	private Double getLowerBoundFromCache(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		return getOptimisticValueFromCache(p1Strategy, p2Strategy);
	}

	private Double getOptimisticValueFromCache(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		Double cachedValue = cache.getOptUtilityFor(p1Strategy, p2Strategy);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetOptimistic(p1Strategy, p2Strategy);
		return -cachedValue;
	}

	private Double updateCacheAndGetOptimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = getStateAfter(p1Strategy, p2Strategy);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -alphaBeta.getUnboundedValue(state);
		double optimisticUtility = oppAlphaBeta.getUnboundedValue(state);
		
		Stats.addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return optimisticUtility;
	}

	private Double getPesimisticValueFromCache(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		Double cachedValue = cache.getPesimisticUtilityFor(p1Strategy, p2Strategy);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetPesimistic(p1Strategy, p2Strategy);
		return -cachedValue;
	}

	private Double updateCacheAndGetPesimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = getStateAfter(p1Strategy, p2Strategy);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -alphaBeta.getUnboundedValue(state);
		double optimisticUtility = oppAlphaBeta.getUnboundedValue(state);

		Stats.addToABTime(System.currentTimeMillis() - time);
		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}

	private Double getValueFromCache(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		Double utility = cache.getUtilityFor(p1Strategy, p2Strategy);

		if (utility == null)
			return null;
		return -utility;
	}

	private double getWindowValue(double utilityValue, double bestValue, double currProbability, MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, int index) {
		int currentIndex = 0;
		double utility = utilityValue;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (currentIndex > index)
				utility += getPesimisticValueFromCache(entry.getKey(), strategy) * entry.getValue();
			currentIndex++;
		}
		return (bestValue - utility) / currProbability;
	}

	private GameState getStateAfter(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = rootState.performAction(p1Strategy.getAction());

		state.performActionModifyingThisState(p2Strategy.getAction());
		return state;
	}
}
