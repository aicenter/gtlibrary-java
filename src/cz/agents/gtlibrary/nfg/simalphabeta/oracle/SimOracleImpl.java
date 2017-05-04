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


package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.utils.Pair;

public abstract class SimOracleImpl implements SimOracle {

	public static boolean USE_INCREASING_BOUND = true;

	protected List<ActionPureStrategy> actions;
	protected GameState rootState;
	protected Expander<? extends InformationSet> expander;
	protected Player player;
	protected SimUtility utility;
	protected AlphaBeta alphaBeta;
	protected DOCache cache;
	protected AlphaBeta oppAlphaBeta;
	protected Data data;

	public SimOracleImpl(GameState rootState, Player player, SimUtility utility, Data data) {
		this.rootState = rootState;
		this.expander = data.expander;
		this.player = player;
		this.utility = utility;
		this.alphaBeta = data.getAlphaBetaFor(player);
		this.cache = utility.getUtilityCache();
		this.oppAlphaBeta = data.getAlphaBetaFor(data.gameInfo.getOpponent(player));
		this.data = data;
	}

	@Override
	public ActionPureStrategy getFirstStrategy() {
		return getActions().get(0);
	}

	public List<ActionPureStrategy> getActions() {
		if (actions == null)
			initActions();
		return actions;
	}

	protected void initActions() {
		actions = new ArrayList<ActionPureStrategy>();
		if (player.equals(rootState.getPlayerToMove())) {
			initFotPlayerToMove();
			return;
		}
		initForOtherPlayer();
	}

	protected void initForOtherPlayer() {
		GameState newState = rootState.performAction(expander.getActions(rootState).get(0));

		for (Action action : expander.getActions(newState)) {
			actions.add(new ActionPureStrategy(action));
		}
	}

	protected void initFotPlayerToMove() {
		for (Action action : expander.getActions(rootState)) {
			actions.add(new ActionPureStrategy(action));
		}
	}

}
