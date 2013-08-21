package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.NoCacheUtilityCalculator;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;

public class NoCacheDoubleOracleFactory implements DoubleOracleFactory {

	@Override
	public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta) {
		SimUtility utility = new SimUtilityImpl(state, new NoCacheUtilityCalculator(data));
		
		return new NoCacheDoubleOracle(utility, alpha, beta, data, state);
	}

}
