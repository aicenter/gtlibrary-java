package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collections;
import java.util.List;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class P2SortingOracle extends P2Oracle {
	
	public P2SortingOracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, utility, data);
	}

	@Override
	public Pair<ActionPureStrategy, Double> getBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		ActionPureStrategy bestStrategy = null;
		List<ActionPureStrategy> possibleActions = getActions();
		double bestValue = -beta;
		
		Collections.sort(possibleActions, data.getP2Comparator(mixedStrategy, rootState, utility.getUtilityCache()));
		
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
}
