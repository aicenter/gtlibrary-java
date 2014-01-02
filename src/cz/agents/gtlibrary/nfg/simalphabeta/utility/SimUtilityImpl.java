package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;

public class SimUtilityImpl extends SimUtility {

	protected GameState state;
	protected UtilityCalculator calculator;

	public SimUtilityImpl(GameState state, UtilityCalculator calculator) {
		this.state = state.copy();
		this.calculator = calculator;
	}

	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		GameState newState = getStateAfterActions(s1, s2);

		if (newState.isGameEnd())
			return newState.getUtilities()[0];
		return calculator.getUtilities(newState, s1, s2, alpha, beta);
	}

	protected GameState getStateAfterActions(ActionPureStrategy s1, ActionPureStrategy s2) {
		GameState newState = state.performAction(s1.getAction());
		
		newState.performActionModifyingThisState(s2.getAction());
		return newState;
	}

	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		GameState newState = getStateAfterActions(s1, s2);

		if (newState.isGameEnd())
			return newState.getUtilities()[0];
		return calculator.getUtility(newState, s1, s2);
	}

	@Override
	public double getUtilityForIncreasedBounds(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		GameState newState = getStateAfterActions(s1, s2);

		if (newState.isGameEnd())
			return newState.getUtilities()[0];
		return calculator.getUtilitiesForIncreasedBounds(newState, s1, s2, alpha, beta);
	}
}
