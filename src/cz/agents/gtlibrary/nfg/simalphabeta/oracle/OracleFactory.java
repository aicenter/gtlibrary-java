package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public interface OracleFactory {

	public SimOracle getP1Oracle(GameState state, Data data, SimUtility utility);
	
	public SimOracle getP2Oracle(GameState state, Data data, SimUtility utility);
	
}
