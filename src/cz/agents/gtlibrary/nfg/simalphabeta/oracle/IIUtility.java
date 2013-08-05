package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;

public class IIUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

	protected GameState state;
	protected UtilityCalculator calculator;

	public IIUtility(GameState state, UtilityCalculator calculator) {
		this.state = state.copy();
		this.calculator = calculator;
	}

	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		GameState newState = getStateAfterActions(s1, s2);

		if (newState.isGameEnd()) {
			return newState.getUtilities()[0];
		}
		return calculator.getUtilities(newState, s1, s2, alpha, beta);
	}

	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		GameState newState = getStateAfterActions(s1, s2);

		if (newState.isGameEnd()) {
			return newState.getUtilities()[0];
		}
		return calculator.getUtilities(newState, s1, s2, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}

	protected GameState getStateAfterActions(ActionPureStrategy s1, ActionPureStrategy s2) {
		GameState newState = state.performAction(s1.getAction());
		
		newState.performActionModifyingThisState(s2.getAction());
		return newState;
	}
}
