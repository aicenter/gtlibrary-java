package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

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

public class P2SimABOracle implements SimABOracle {

	public static boolean USE_ALPHABETA = true;
	public static boolean USE_ALPHABETA_CACHE = false;
	private static boolean USE_INCREASING_BOUND = false;

	private HashSet<ActionPureStrategy> actions;
	private GameState rootState;
	private Expander<? extends InformationSet> expander;
	private SimUtility utility;
	private AlphaBeta alphaBeta;
	private DOCache cache;
	private AlphaBeta oppAlphaBeta;
	private AlgorithmConfig<SimABInformationSet> algConfig;
	private Player player;

	public P2SimABOracle(GameState rootState, SimUtility utility, Data data, DOCache cache) {
		this.rootState = rootState;
		this.player = rootState.getAllPlayers()[1];
		this.expander = data.expander;
		this.utility = utility;
		this.alphaBeta = data.getAlphaBetaFor(player);
		this.cache = cache;
		this.oppAlphaBeta = data.alphaBetas[0];
		this.algConfig = data.config;
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

	@Override
	public ActionPureStrategy getForcedBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		return getActions().iterator().next();
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
					else
						assert false;
			assert optimisticUtility >= pesimisticUtility;
			double utilityValue = -utility.getUtility(p2Strategy, p1Strategy, pesimisticUtility - 1e-4, optimisticUtility);

			if (utilityValue == utilityValue) {
				cache.setPesAndOptValueFor(p1Strategy, p2Strategy, utilityValue);
			} else {
				if (-bound <= optimisticUtilityFromCache && -bound > pesimisticUtilityFromCache)
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
				assert windowValue >= getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index);
				if (cacheValue == null) {
					if (getPesimisticValueFromCache(entry.getKey(), strategy) < windowValue) {
//						Info.incrementABCuts();
						return Double.NEGATIVE_INFINITY;
					}
					updateCacheValuesFor(entry.getKey(), strategy, windowValue);
				} else {
					assert !cacheValue.isNaN();
					if (cacheValue < windowValue - 1e-8) {
//						Info.incrementCacheCuts();
						return Double.NEGATIVE_INFINITY;
					}
				}
				Double util = utility.getUtility(strategy, entry.getKey(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

				if (util.isNaN())
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
		double pesimisticUtility = -alphaBeta.getValue(rootState.performAction(p1Strategy.getAction()), p2Strategy.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		double optimisticUtility = oppAlphaBeta.getValue(rootState.performAction(p1Strategy.getAction()), p2Strategy.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

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
		double pesimisticUtility = -alphaBeta.getValue(rootState.performAction(p1Strategy.getAction()), p2Strategy.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		double optimisticUtility = oppAlphaBeta.getValue(rootState.performAction(p1Strategy.getAction()), p2Strategy.getAction(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		
		cache.setPesAndOptValueFor(p1Strategy, p2Strategy, optimisticUtility, pesimisticUtility);
		return pesimisticUtility;
	}

	private Double getValueFromCache(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		Double utility = cache.getUtilityFor(p1Strategy, p2Strategy);
		
		if(utility == null)
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

	private Collection<ActionPureStrategy> getActions() {
		if (actions == null)
			initActions();
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

}
