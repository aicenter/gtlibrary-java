package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.SimDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.DOUtilityCalculator;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;

public class SimABDoubleOracleFactory implements DoubleOracleFactory {

	@Override
	public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta) {
		SimUtility utility = new SimUtilityImpl(state, new DOUtilityCalculator(data, data.natureCache));
		
		return new SimDoubleOracle(utility, alpha, beta, data, state, data.cache);
	}

}