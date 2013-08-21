package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public class NoCacheP1SimABOracle extends P1Oracle {

	public NoCacheP1SimABOracle(GameState rootState, SimUtility utility, Data data) {
		super(rootState, utility, data);
	}
	
	@Override
	protected void updateCacheValuesFor(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, double bound) {
	}

}
