package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public class FullLP extends DoubleOracle {
	
	private SimABOracle p1Oracle;
	private SimABOracle p2Oracle;

	public FullLP(GameState rootState, Data data, SimUtility utility) {
		super(rootState, data);
		this.p1Oracle = data.getP1Oracle(rootState, utility, null);
		this.p2Oracle = data.getP2Oracle(rootState, utility, null);
		coreSolver = new ZeroSumGameNESolverImpl<ActionPureStrategy, ActionPureStrategy>(utility);
	}
	
	@Override
	public void generate() {
		PlayerStrategySet<ActionPureStrategy> p1StrategySet = new PlayerStrategySet<ActionPureStrategy>(p1Oracle.getActions());
		PlayerStrategySet<ActionPureStrategy> p2StrategySet = new PlayerStrategySet<ActionPureStrategy>(p2Oracle.getActions());

		Stats.addToP1StrategyCount(p1StrategySet.size());
		Stats.addToP2StrategyCount(p2StrategySet.size());
		coreSolver.addPlayerTwoStrategies(p2StrategySet);
		coreSolver.addPlayerOneStrategies(p1StrategySet);
		computeNE();
		Stats.addToP1NESize(coreSolver.getPlayerOneStrategy());
		Stats.addToP2NESize(coreSolver.getPlayerTwoStrategy());
	}

	public void computeNE() {
		long time = System.currentTimeMillis();
		
		coreSolver.computeNashEquilibrium();
		Stats.addToLPSolveTime(System.currentTimeMillis() - time);
	}

}
