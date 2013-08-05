package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;

public abstract class SimUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

	public abstract double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta);
	
	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		return getUtility(s1, s2, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
	}
	
}
