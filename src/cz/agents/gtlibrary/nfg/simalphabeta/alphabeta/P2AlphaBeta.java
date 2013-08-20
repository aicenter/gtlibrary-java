package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;

public class P2AlphaBeta extends AlphaBetaImpl {

	public P2AlphaBeta(Player player, Expander<SimABInformationSet> expander, AlphaBetaCache cache, GameInfo gameInfo) {
		super(player, expander, cache, gameInfo);
	}


	@Override
	protected List<Action> getMaximizingActions(GameState state) {
		algConfig.createInformationSetFor(state);

		GameState newState = state.performAction(expander.getActions(state).get(0));

		algConfig.createInformationSetFor(newState);
		return expander.getActions(newState);
	}

	@Override
	protected List<Action> getMinimizingActions(GameState state) {
		algConfig.createInformationSetFor(state);
		return expander.getActions(state);
	}

	@Override
	protected GameState performActions(GameState state, Action minAction, Action maxAction) {
		GameState newState = state.performAction(minAction);

		newState.performActionModifyingThisState(maxAction);
		return newState;
	}
	
}
