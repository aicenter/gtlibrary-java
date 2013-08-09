package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collection;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class P1SimABOracle extends SimABOracleImpl {

	public P1SimABOracle(GameState rootState, SimUtility utility, Data data, DOCache cache) {
		super(rootState, rootState.getAllPlayers()[0], utility, data, cache);
	}

	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta, double hardAlpha, double hardBeta) {
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

	private double getValueForAction(MixedStrategy<ActionPureStrategy> mixedStrategy, double bestValue, ActionPureStrategy strategy) {
		double utilityValue = 0;
		int index = 0;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (entry.getValue() > 1e-8) {
				Double cacheValue = getValueFromCache(strategy, entry.getKey());
				double cacheWindow = getLowerBoundFromCache(strategy, entry.getKey());
				double windowValue = Math.max(cacheWindow, getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index));

				if (cacheValue == null) {
					if (getOptimisticValueFromCache(strategy, entry.getKey()) < windowValue)
						return Double.NEGATIVE_INFINITY;
					updateCacheValuesFor(strategy, entry.getKey(), windowValue);
				} else if (cacheValue < windowValue - 1e-8)
					return Double.NEGATIVE_INFINITY;
				utilityValue += utility.getUtilityFromCache(strategy, entry.getKey()) * entry.getValue();
			}
			index++;
		}
		return utilityValue;
	}

	private void updateCacheValuesFor(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy, double bound) {
		double pesimisticUtilityFromCache = cache.getPesimisticUtilityFor(p1Strategy, p2Strategy);
		double optimisticUtilityFromCache = cache.getOptimisticUtilityFor(p1Strategy, p2Strategy);
		double pesimisticUtility = pesimisticUtilityFromCache;
		double optimisticUtility = optimisticUtilityFromCache;

		if (optimisticUtility - pesimisticUtility > 1e-14) {
			if (USE_INCREASING_BOUND)
				if (bound >= pesimisticUtility)
					pesimisticUtility = bound;
			assert optimisticUtility >= pesimisticUtility;
			double utilityValue = utility.getUtility(p1Strategy, p2Strategy, pesimisticUtility - 1e-4, optimisticUtility);

			if (utilityValue == utilityValue)
				cache.setPesAndOptValueFor(p1Strategy, p2Strategy, utilityValue);
			else if (pesimisticUtilityFromCache <= bound && bound < optimisticUtilityFromCache)
				cache.setPesAndOptValueFor(p1Strategy, p2Strategy, bound, pesimisticUtilityFromCache);
		}
	}

	private double getWindowValue(double utilityValue, double bestValue, double currProbability, MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, int index) {
		int currentIndex = 0;
		double utility = utilityValue;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (currentIndex > index)
				utility += getOptimisticValueFromCache(strategy, entry.getKey()) * entry.getValue();
			currentIndex++;
		}
		return (bestValue - utility) / currProbability;
	}

	private Double getLowerBoundFromCache(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		return getPesimisticValueFromCache(strategy1, strategy2);
	}

	private Double getPesimisticValueFromCache(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		Double cachedValue = cache.getPesimisticUtilityFor(strategy2, strategy1);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetPesimistic(strategy1, strategy2);
		return cachedValue;
	}

	public Double updateCacheAndGetPesimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = getStateAfter(p1Strategy, p2Strategy);
		double pesimisticUtility = -oppAlphaBeta.getUnboundedValue(state);
		double optimisticUtility = alphaBeta.getUnboundedValue(state);

		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}

	private Double getOptimisticValueFromCache(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		Double cachedValue = cache.getOptUtilityFor(strategy1, strategy2);

		if (cachedValue == null)
			cachedValue = updateCacheAndGetOptimistic(strategy1, strategy2);
		return cachedValue;
	}

	public Double updateCacheAndGetOptimistic(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = getStateAfter(p1Strategy, p2Strategy);
		double pesimisticUtility = -oppAlphaBeta.getUnboundedValue(state);
		double optimisticUtility = alphaBeta.getUnboundedValue(state);

		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return optimisticUtility;
	}

	private Double getValueFromCache(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		return cache.getUtilityFor(p1Strategy, p2Strategy);
	}
	
	private GameState getStateAfter(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState state = rootState.performAction(p1Strategy.getAction());
		
		state.performActionModifyingThisState(p2Strategy.getAction());
		return state;
	}

}
