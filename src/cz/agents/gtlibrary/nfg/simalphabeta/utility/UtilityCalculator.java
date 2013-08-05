package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABOracleImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimDoubleOracle;

public class UtilityCalculator {
	protected Data data;
	protected DOCache cache;

	public UtilityCalculator(DOCache cache, Data data) {
		this.data = data;
		this.cache = cache;
	}
	
	public double getUtilities(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		Double utility = cache.getUtilityFor(s1, s2);
		
		if(utility != null) {
			assert !utility.isNaN();
			return utility;
		}
		if(cache != null && alpha == Double.NEGATIVE_INFINITY && beta == Double.POSITIVE_INFINITY)
			return Double.NaN;
		if(cache != null && alpha == Double.NEGATIVE_INFINITY && beta == Double.NEGATIVE_INFINITY) {
//			Info.fullRunFromNESolver++;
			if (state.isPlayerToMoveNature())
				return computeUtilityForNature(state, s1, s2, cache.getPesimisticUtilityFor(s1, s2), cache.getPesimisticUtilityFor(s1, s2));
			return computeUtilityOf(state, cache.getPesimisticUtilityFor(s1, s2), cache.getPesimisticUtilityFor(s1, s2));
		}
		if (state.isPlayerToMoveNature()) 
			return computeUtilityForNature(state, s1, s2, alpha, beta);
		return computeUtilityOf(state, alpha, beta);
	}

	protected double computeUtilityForNature(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		double utilityValue = 0;

		for (Action action : data.expander.getActions(state)) {
			GameState newState = state.performAction(action);
			
			utilityValue += state.getProbabilityOfNatureFor(action) * computeUtilityOf(newState, alpha, beta);
		}
//		cache.setPesAndOptValueFor(s1, s2, utilityValue);//nevim jestli je tohle ok
		return utilityValue;
	}

	protected double computeUtilityOf(GameState state, double alpha, double beta) {
		DOCache cache = new DOCacheImpl();
		SimUtility utility = new SimUtilityImpl(state, new UtilityCalculator(cache, data));
		SimABOracleImpl  p1Oracle = new SimABOracleImpl(state, state.getAllPlayers()[0], utility, data, cache);
		SimABOracleImpl p2Oracle = new SimABOracleImpl(state, state.getAllPlayers()[1], new NegativeSimUtility(utility), data, cache);
//		SimABOracle p1Oracle = new P1SimABOracle(state, utility, data, cache);
//		SimABOracle p2Oracle = new P2SimABOracle(state, new NegativeSimUtility(utility), data, cache);
		SimDoubleOracle doubleOracle = new SimDoubleOracle(p1Oracle, p2Oracle, utility, alpha, beta, cache, data, state);
		
		doubleOracle.execute();
		return doubleOracle.getGameValue();
	}

}
