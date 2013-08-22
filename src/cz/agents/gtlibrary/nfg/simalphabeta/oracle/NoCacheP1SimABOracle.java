package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class NoCacheP1SimABOracle extends P1Oracle {

	public NoCacheP1SimABOracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, utility, data);
	}

	@Override
	protected double getValueForAction(MixedStrategy<ActionPureStrategy> mixedStrategy, double bestValue, ActionPureStrategy strategy) {
		double utilityValue = 0;
		int index = 0;

		for (Entry<ActionPureStrategy, Double> entry : mixedStrategy) {
			if (entry.getValue() > 1e-8) {
				Pair<ActionPureStrategy, ActionPureStrategy> strategyPair = new Pair<ActionPureStrategy, ActionPureStrategy>(strategy, entry.getKey());
				double cacheWindow = getLowerBound(strategyPair);
				double windowValue = Math.max(cacheWindow, getWindowValue(utilityValue, bestValue, entry.getValue(), mixedStrategy, strategy, index));

				if (getOptimisticValue(strategyPair) < windowValue) {
					Stats.incrementABCuts();
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

	private double getLowerBound(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double pesimisticUtility = -oppAlphaBeta.getUnboundedValue(state);
		
		Stats.addToABTime(System.currentTimeMillis() - time);
		return pesimisticUtility;
	}

	private double getOptimisticValue(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
		GameState state = getStateAfter(strategyPair);
		long time = System.currentTimeMillis();
		double optimisticUtility = alphaBeta.getUnboundedValue(state);
		
		Stats.addToABTime(System.currentTimeMillis() - time);
		return optimisticUtility;
	}

}
