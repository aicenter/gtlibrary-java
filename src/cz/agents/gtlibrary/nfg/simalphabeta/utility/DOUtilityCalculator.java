package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;

public class DOUtilityCalculator implements UtilityCalculator {
	protected Data data;

	public DOUtilityCalculator(Data data) {
		this.data = data;
	}

	public double getUtilities(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		Double utility = data.cache.getUtilityFor(s1, s2);

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
		DoubleOracle doubleOracle = data.getDoubleOracle(state, alpha, beta);

		doubleOracle.generate();
		return doubleOracle.getGameValue();
	}

	public double getUtility(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
		Double utility = data.cache.getUtilityFor(s1, s2);

		return utility == null ? Double.NaN : utility;
	}
}
