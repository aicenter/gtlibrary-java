package cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.P1Oracle;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.P2Oracle;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.NegativeSimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public class SimABOracleFactory implements OracleFactory {

	@Override
	public SimOracle getP1Oracle(GameState state, Data data, SimUtility utility) {
		return new P1Oracle(state, utility, data);

	}

	@Override
	public SimOracle getP2Oracle(GameState state, Data data, SimUtility utility) {
		return new P2Oracle(state, new NegativeSimUtility(utility), data);
	}

}
