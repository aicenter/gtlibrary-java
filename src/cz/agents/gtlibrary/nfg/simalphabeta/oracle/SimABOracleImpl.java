package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import cz.agents.gtlibrary.iinodes.PlayerImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class SimABOracleImpl implements SimABOracle {

	public static boolean USE_ALPHABETA = true;
	public static boolean USE_ALPHABETA_CACHE = false;
	private static boolean USE_INCREASING_BOUND = false;
	private final boolean USE_CACHED_VALUES = true;

	private HashSet<ActionPureStrategy> actions;
	private GameState rootState;
	private Expander<? extends InformationSet> expander;
	private Player player;
	private SimUtility utility;
	public AlphaBeta alphaBeta;
	private DOCache cache;
	private AlphaBeta oppAlphaBeta;
	private AlgorithmConfig<SimABInformationSet> algConfig;

	public SimABOracleImpl(GameState rootState, Player player, SimUtility utility, Data data, DOCache cache) {
		this.rootState = rootState;
		this.expander = data.expander;
		this.player = player;
		this.utility = utility;
		this.alphaBeta = data.getAlphaBetaFor(player);
		this.cache = cache;
		this.oppAlphaBeta = data.getAlphaBetaFor(new PlayerImpl((player.getId() + 1) % 2));
		this.algConfig = data.config;
	}

	public Collection<ActionPureStrategy> getCurrentStrategies() {
		return getActions();
	}

	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta, double hardAlpha, double hardBeta) {
		ActionPureStrategy bestStrategy = null;
		double bestValue = player.getId() == 0 ? alpha : -beta;
		Collection<ActionPureStrategy> possibleActions = getActions();

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
				Double cacheValue = getValueFromCache(entry.getKey(), strategy);
				double cacheWindow = getLowerBoundFromCache(strategy, entry.getKey());

				double windowValue = Math.max(cacheWindow, getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index));
				assert windowValue >= getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index);
				if (cacheValue == null) {
					if ((player.getId() == 0 && getOptimisticValueFromCache(entry.getKey(), strategy) < windowValue) || (player.getId() == 1 && getPesimisticValueFromCache(entry.getKey(), strategy) < windowValue)) {
//						Info.incrementABCuts();
						return Double.NEGATIVE_INFINITY;
					}
					updateCacheValuesFor(strategy, entry.getKey(), windowValue);
				} else {
					if (cacheValue.isNaN()) {
						assert false;
						return Double.NEGATIVE_INFINITY;
					}
					if (cacheValue < windowValue - 1e-8) {
//						Info.incrementCacheCuts();
						return Double.NEGATIVE_INFINITY;
					}
				}
				Double util = utility.getUtility(strategy, entry.getKey(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

				if (util.isNaN()) {
					return Double.NEGATIVE_INFINITY;
				}
				utilityValue += util * entry.getValue();
			}
			index++;

		}

		return utilityValue;
	}

	private Double getLowerBoundFromCache(ActionPureStrategy strategy, ActionPureStrategy strategy1) {
		return player.getId() == 0 ? getPesimisticValueFromCache(strategy1, strategy) : getOptimisticValueFromCache(strategy1, strategy);
	}

	private void updateCacheValuesFor(ActionPureStrategy fpStrategy, ActionPureStrategy spStrategy, double bound) {
		Pair<ActionPureStrategy, ActionPureStrategy> strategyPair;
		if (player.getId() == 0)
			strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(fpStrategy, spStrategy);
		else
			strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(spStrategy, fpStrategy);
		double pesimisticUtilityFromCache = cache.getPesimisticUtilityFor(strategyPair);
		double optimisticUtilityFromCache = cache.getOptimisticUtilityFor(strategyPair);
		double pesimisticUtility = pesimisticUtilityFromCache;
		double optimisticUtility = optimisticUtilityFromCache;

		if (optimisticUtility - pesimisticUtility > 1e-14) {
			if (USE_INCREASING_BOUND)
				if (player.getId() == 0) {
					if (bound >= pesimisticUtility)
						pesimisticUtility = bound;
					else
						assert false;
				} else {
					if (-bound <= optimisticUtility)
						optimisticUtility = -bound;
					else
						assert false;
				}
			assert optimisticUtility >= pesimisticUtility;
			double utilityValue = (player.getId() == 0 ? 1 : -1) * utility.getUtility(fpStrategy, spStrategy, pesimisticUtility - 1e-4, optimisticUtility);

			if (utilityValue == utilityValue) {
				cache.setPesAndOptValueFor(strategyPair, utilityValue);
			} else {
				if (player.getId() == 0 && pesimisticUtilityFromCache <= bound && bound < optimisticUtilityFromCache) {
//					Info.incrementBoundsTightened();
					cache.setPesAndOptValueFor(strategyPair, bound, pesimisticUtilityFromCache);
				} else if (player.getId() == 1 && -bound <= optimisticUtilityFromCache && -bound > pesimisticUtilityFromCache) {
//					Info.incrementBoundsTightened();
					cache.setPesAndOptValueFor(strategyPair, optimisticUtilityFromCache, -bound);
				}
			}
		}
	}

	private Double getOptimisticValueFromCache(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		Double cachedValue = null;

		if (USE_CACHED_VALUES) {
			if (player.getId() == 0)
				cachedValue = cache.getOptUtilityFor(strategy2, strategy1);
			else
				cachedValue = cache.getOptUtilityFor(strategy1, strategy2);

			if (cachedValue == null) {
				double pesimisticUtility = 0;
				double optimisticUtility = 0;
				if (player.getId() == 0) {
//					long time = System.currentTimeMillis();
					pesimisticUtility = -oppAlphaBeta.getValue(rootState.performAction(strategy2.getAction()), strategy1.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					optimisticUtility = alphaBeta.getValue(rootState.performAction(strategy2.getAction()), strategy1.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//					Info.addToABTime(System.currentTimeMillis() - time);
					cache.setPesAndOptValueFor(strategy2, strategy1, optimisticUtility, pesimisticUtility);
				} else {
//					long time = System.currentTimeMillis();
					optimisticUtility = oppAlphaBeta.getValue(rootState.performAction(strategy1.getAction()), strategy2.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					pesimisticUtility = -alphaBeta.getValue(rootState.performAction(strategy1.getAction()), strategy2.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//					Info.addToABTime(System.currentTimeMillis() - time);
					cache.setPesAndOptValueFor(strategy1, strategy2, optimisticUtility, pesimisticUtility);
				}

				cachedValue = optimisticUtility;
			}

			if (cachedValue != null && player.getId() == 1)
				cachedValue = -cachedValue;

		}
		return cachedValue;
	}

	private Double getPesimisticValueFromCache(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		Double cachedValue = null;

		if (USE_CACHED_VALUES) {
			if (player.getId() == 0)
				cachedValue = cache.getPesimisticUtilityFor(strategy2, strategy1);
			else
				cachedValue = cache.getPesimisticUtilityFor(strategy1, strategy2);

			if (cachedValue == null) {
				double pesimisticUtility = 0;
				double optimisticUtility = 0;
				if (player.getId() == 0) {
//					long time = System.currentTimeMillis();
					pesimisticUtility = -oppAlphaBeta.getValue(rootState.performAction(strategy2.getAction()), strategy1.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					optimisticUtility = alphaBeta.getValue(rootState.performAction(strategy2.getAction()), strategy1.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//					Info.addToABTime(System.currentTimeMillis() - time);
					cache.setPesAndOptValueFor(strategy2, strategy1, optimisticUtility, pesimisticUtility);
				} else {
//					long time = System.currentTimeMillis();
					optimisticUtility = oppAlphaBeta.getValue(rootState.performAction(strategy1.getAction()), strategy2.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
					pesimisticUtility = -alphaBeta.getValue(rootState.performAction(strategy1.getAction()), strategy2.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//					Info.addToABTime(System.currentTimeMillis() - time);
					cache.setPesAndOptValueFor(strategy1, strategy2, optimisticUtility, pesimisticUtility);
				}

				cachedValue = pesimisticUtility;
			}

			if (cachedValue != null && player.getId() == 1)
				cachedValue = -cachedValue;
		}
		return cachedValue;
	}

	private Double getValueFromCache(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
		Double cachedValue = null;

		if (USE_CACHED_VALUES) {
			if (player.getId() == 0)
				cachedValue = cache.getUtilityFor(strategy2, strategy1);
			else
				cachedValue = cache.getUtilityFor(strategy1, strategy2);

			if (cachedValue != null && player.getId() == 1)
				cachedValue = -cachedValue;
		}
		return cachedValue;
	}

	private double getWindowValue(double utilityValue, double bestValue, double currProbability, MixedStrategy<ActionPureStrategy> mixedStrategy, ActionPureStrategy strategy, int index) {
		int currentIndex = 0;
		double utility = utilityValue;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (currentIndex > index) {
				if (player.getId() == 0)
					utility += getOptimisticValueFromCache(entry.getKey(), strategy) * entry.getValue();
				else
					utility += getPesimisticValueFromCache(entry.getKey(), strategy) * entry.getValue();
			}
			currentIndex++;
		}
		return (bestValue - utility) / currProbability;
	}

	private Collection<ActionPureStrategy> getActions() {
		if (actions == null) {
			initActions();
		}
		return actions;
	}

	private void initActions() {
		actions = new LinkedHashSet<ActionPureStrategy>();
		if (player.equals(rootState.getPlayerToMove())) {
			initFotPlayerToMove();
			return;
		}
		initForOtherPlayer();
	}

	private void initForOtherPlayer() {
		GameState newState = rootState.performAction(expander.getActions(rootState).get(0));

		algConfig.createInformationSetFor(newState);
		for (Action action : expander.getActions(newState)) {
			actions.add(new ActionPureStrategy(action));
		}
	}

	private void initFotPlayerToMove() {
		algConfig.createInformationSetFor(rootState);
		for (Action action : expander.getActions(rootState)) {
			actions.add(new ActionPureStrategy(action));
		}
	}

	public GameState getGameState() {
		return rootState;
	}

	public ActionPureStrategy getForcedBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		return getActions().iterator().next();
	}

}
