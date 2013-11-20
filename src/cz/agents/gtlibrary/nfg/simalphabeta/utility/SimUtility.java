package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.Utility;

public abstract class SimUtility extends Utility<ActionPureStrategy, ActionPureStrategy> {

	public abstract double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta);
	
	public abstract double getUtility(ActionPureStrategy s1, ActionPureStrategy s2);

	public abstract double getUtilityForIncreasedBounds(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta);	
}
