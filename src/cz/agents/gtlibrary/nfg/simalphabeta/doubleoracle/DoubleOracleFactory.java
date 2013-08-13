package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;

public interface DoubleOracleFactory {
	public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta);
}
