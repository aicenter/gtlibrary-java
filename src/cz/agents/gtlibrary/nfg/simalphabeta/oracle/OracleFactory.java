package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public interface OracleFactory {

	public SimABOracle getP1Oracle(GameState state, Data data, SimUtility utility);
	
	public SimABOracle getP2Oracle(GameState state, Data data, SimUtility utility);
	
}
