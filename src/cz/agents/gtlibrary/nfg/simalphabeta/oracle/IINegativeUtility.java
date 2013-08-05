package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;

public class IINegativeUtility extends IIUtility {

	public IINegativeUtility(GameState state, UtilityCalculator calculator) {
		super(state, calculator);
	}

	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
//		GameState newState = getStateAfterActions(s2, s1);
//
//		if (newState.isGameEnd()) {
//			return newState.getUtilities()[1];
//		}
//		return -calculator.getUtilities(newState, s2, s1, alpha, beta);
		return -super.getUtility(s2, s1, alpha, beta);
	}

	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		return -super.getUtility(s2, s1);
	}

}
