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


package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCache;

public class P2AlphaBeta extends AlphaBetaImpl {

	public P2AlphaBeta(Player player, Expander<SimABInformationSet> expander, AlphaBetaCache cache, GameInfo gameInfo) {
		super(player, expander, cache, gameInfo);
	}


	@Override
	protected List<Action> getMaximizingActions(GameState state) {

		GameState newState = state.performAction(expander.getActions(state).get(0));

		return expander.getActions(newState);
	}

	@Override
	protected List<Action> getMinimizingActions(GameState state) {
		return expander.getActions(state);
	}

	@Override
	protected GameState performActions(GameState state, Action minAction, Action maxAction) {
		GameState newState = state.performAction(minAction);

		newState.performActionModifyingThisState(maxAction);
		return newState;
	}
	
}
