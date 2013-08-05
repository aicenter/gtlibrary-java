package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;

public class NegativeSimUtility extends SimUtility {

	private SimUtility utility;

	public NegativeSimUtility(SimUtility utility) {
		this.utility = utility;
	}

	@Override
	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		return -utility.getUtility(s2, s1, alpha, beta);
	}

}
