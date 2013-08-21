package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public class NoCacheDoubleOracle extends SimDoubleOracle {

	public NoCacheDoubleOracle(SimUtility utility, double alpha, double beta, Data data, GameState state) {
		super(utility, alpha, beta, data, state);
	}

	@Override
	protected void updateCacheValues(PlayerStrategySet<ActionPureStrategy> p1StrategySet, PlayerStrategySet<ActionPureStrategy> p2StrategySet) {
	}

}
