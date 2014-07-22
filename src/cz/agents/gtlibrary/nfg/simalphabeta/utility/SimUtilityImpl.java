/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.SimAlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;

public class SimUtilityImpl extends SimUtility {

	protected GameState state;
	protected UtilityCalculator calculator;
    protected DOCache cache;

	public SimUtilityImpl(GameState state, UtilityCalculator calculator, DOCache cache) {
		this.state = state.copy();
		this.calculator = calculator;
        this.cache = cache;
	}

	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		GameState newState = getStateAfterActions(s1, s2);

		if (newState.isGameEnd()) {
            if(newState instanceof SimultaneousGameState)
                SimAlphaBeta.FULLY_COMPUTED &= ((SimultaneousGameState)newState).isActualGameEnd();
            return newState.getUtilities()[0];
        }
		return calculator.getUtilities(newState, s1, s2, alpha, beta);
	}

	protected GameState getStateAfterActions(ActionPureStrategy s1, ActionPureStrategy s2) {
		GameState newState = state.performAction(s1.getAction());
		
		newState.performActionModifyingThisState(s2.getAction());
		return newState;
	}

	public double getUtility(ActionPureStrategy s1, ActionPureStrategy s2) {
		GameState newState = getStateAfterActions(s1, s2);


        if (newState.isGameEnd()) {
            if(newState instanceof SimultaneousGameState)
                SimAlphaBeta.FULLY_COMPUTED &= ((SimultaneousGameState)newState).isActualGameEnd();
            return newState.getUtilities()[0];
        }
		return calculator.getUtility(newState, s1, s2);
	}

	@Override
	public double getUtilityForIncreasedBounds(ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		GameState newState = getStateAfterActions(s1, s2);

        if (newState.isGameEnd()) {
            if(newState instanceof SimultaneousGameState)
                SimAlphaBeta.FULLY_COMPUTED &= ((SimultaneousGameState)newState).isActualGameEnd();
            return newState.getUtilities()[0];
        }
		return calculator.getUtilitiesForIncreasedBounds(newState, s1, s2, alpha, beta);
	}

    public DOCache getUtilityCache() {
        return cache;
    }
}
