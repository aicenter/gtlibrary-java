package cz.agents.gtlibrary.experimental.stochastic.newexperiment;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;

public class InitialUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

	private NEGameState state;

	public InitialUtility(NEGameState state) {
		this.state = state;
	}

	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		return state.getUtilities()[0];
	}

}
