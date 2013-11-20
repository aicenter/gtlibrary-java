package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class CompleteUtilityCalculator implements UtilityCalculator {
	
	private Data data;

	public CompleteUtilityCalculator(Data data) {
		this.data = data;
	}
	
	public double getUtilities(GameState state,ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		throw new UnsupportedOperationException();
	}
	
	protected double computeUtilityForNature(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
		double utilityValue = 0;

		for (Action action : data.expander.getActions(state)) {
			utilityValue += state.getProbabilityOfNatureFor(action) * computeUtilityOf(state.performAction(action));
		}
		return utilityValue;
	}

	protected double computeUtilityOf(GameState state) {
		double p1Bound = data.getAlphaBetaFor(state.getAllPlayers()[0]).getUnboundedValue(state);
		double p2Bound = -data.getAlphaBetaFor(state.getAllPlayers()[1]).getUnboundedValue(state);
		
		if(p1Bound - p2Bound < 1e-8) {
			Stats.getInstance().incrementABCuts();
			return p1Bound;
		}
		DoubleOracle oracle = data.getDoubleOracle(state, 0, 0);
		
		oracle.generate();
		return oracle.getGameValue();
	}

	public double getUtility(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
		if (state.isPlayerToMoveNature())
			return computeUtilityForNature(state, s1, s2);
		return computeUtilityOf(state);
	}

	@Override
	public double getUtilitiesForIncreasedBounds(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		throw new UnsupportedOperationException();
	}
}
