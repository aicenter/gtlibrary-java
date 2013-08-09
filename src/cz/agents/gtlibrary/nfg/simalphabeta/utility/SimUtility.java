package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;

public abstract class SimUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

	public abstract double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta);
	
	public abstract double getUtilityFromCache(ActionPureStrategy s1, ActionPureStrategy s2);
	
	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		return getUtilityFromCache(s1, s2);
	}
}
