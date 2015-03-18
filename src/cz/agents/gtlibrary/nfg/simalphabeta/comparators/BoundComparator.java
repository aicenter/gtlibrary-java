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


package cz.agents.gtlibrary.nfg.simalphabeta.comparators;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;

public abstract class BoundComparator extends StrategyComparator {
	
	protected DOCache cache;
	protected MixedStrategy<ActionPureStrategy> mixedStrategy;
	protected GameState state;
	protected AlphaBeta p1AlphaBeta;
	protected AlphaBeta p2AlphaBeta;
	
	public BoundComparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, Data data, DOCache cache) {
		super();
		this.cache = cache;
		this.mixedStrategy = mixedStrategy;
		this.state = state;
		p1AlphaBeta = data.alphaBetas[0];
		p2AlphaBeta = data.alphaBetas[1];
	}
	
	protected GameState getStateAfter(ActionPureStrategy p1Strategy, ActionPureStrategy p2Strategy) {
		GameState nextState = state.performAction(p1Strategy.getAction());

		nextState.performActionModifyingThisState(p2Strategy.getAction());
		return nextState;
	}

}
