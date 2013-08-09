package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.SimDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.P1SimABOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.P2SimABOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABOracle;

public class UtilityCalculator {
	protected Data data;
	protected DOCache cache;

	public UtilityCalculator(DOCache cache, Data data) {
		this.data = data;
		this.cache = cache;
	}

	public double getUtilities(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		Double utility = cache.getUtilityFor(s1, s2);

		if (utility != null)
			return utility;
		if (state.isPlayerToMoveNature())
			return computeUtilityForNature(state, s1, s2, alpha, beta);
		return computeUtilityOf(state, alpha, beta);
	}

	protected double computeUtilityForNature(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		double utilityValue = 0;

		for (Action action : data.expander.getActions(state)) {
			utilityValue += state.getProbabilityOfNatureFor(action) * computeUtilityOf(state.performAction(action), alpha, beta);
		}
		return utilityValue;
	}

	protected double computeUtilityOf(GameState state, double alpha, double beta) {
		DOCache cache = new DOCacheImpl();
		SimUtility utility = new SimUtilityImpl(state, new UtilityCalculator(cache, data));
		SimABOracle p1Oracle = new P1SimABOracle(state, utility, data, cache);
		SimABOracle p2Oracle = new P2SimABOracle(state, new NegativeSimUtility(utility), data, cache);
		SimDoubleOracle doubleOracle = new SimDoubleOracle(p1Oracle, p2Oracle, utility, alpha, beta, cache, data, state);

		doubleOracle.execute();
		return doubleOracle.getGameValue();
	}

	public double getUtilityFromCache(ActionPureStrategy s1, ActionPureStrategy s2) {
		return cache.getUtilityFor(s1, s2);
	}
}
