package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;

public class NoCacheUtilityCalculator extends DOUtilityCalculator {

	public NoCacheUtilityCalculator(Data data) {
		super(data);
	}

	@Override
	public double getUtility(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
		if (state.isPlayerToMoveNature())
			return computeUtilityForNature(state, s1, s2, getPesimisticValue(state), getOptimisticValue(state));
		return computeUtilityOf(state, getPesimisticValue(state), getOptimisticValue(state));
	}
}
