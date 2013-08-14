package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.doubleoracle.NFGDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public abstract class DoubleOracle extends NFGDoubleOracle {

	public DoubleOracle(GameState rootState, Data data) {
		super(rootState, data.expander, data.gameInfo, data.config);
		Stats.incrementStatesVisited();
		Stats.addState(rootState);
	}
	
	public double getGameValue() {
		return coreSolver.getGameValue();
	}
	
	public abstract void generate();

}
