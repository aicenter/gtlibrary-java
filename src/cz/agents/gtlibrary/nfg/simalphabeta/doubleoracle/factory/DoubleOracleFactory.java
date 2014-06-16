package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;

public interface DoubleOracleFactory {
	public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta, boolean isRoot);
}
