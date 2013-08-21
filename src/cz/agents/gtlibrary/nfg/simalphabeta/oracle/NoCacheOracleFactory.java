package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.NegativeSimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public class NoCacheOracleFactory implements OracleFactory {

	@Override
	public SimOracle getP1Oracle(GameState state, Data data, SimUtility utility) {
		return new NoCacheP1SimABOracle(state, utility, data);
	}

	@Override
	public SimOracle getP2Oracle(GameState state, Data data, SimUtility utility) {
		return new NoCacheP2SimABOracle(state, new NegativeSimUtility(utility), data);
	}

}
