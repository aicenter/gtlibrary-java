package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;

public interface UtilityCalculator {
	
	public double getUtilities(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta);
	
	public double getUtility(GameState state, ActionPureStrategy s1, ActionPureStrategy s2);
	
}
