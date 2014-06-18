package cz.agents.gtlibrary.experimental.stochastic.smallgame;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;

public class InitialUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

	private SGGameState state;

	public InitialUtility(SGGameState state) {
		this.state = state;
	}

	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		return state.getCurrentUtilities().get(s1.getAction()).get(s2.getAction());
	}

}
