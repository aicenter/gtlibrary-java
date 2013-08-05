package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;

public class P1AlphaBeta extends AlphaBeta {
	
	public P1AlphaBeta(Player player, Expander<SimABInformationSet> expander, AlphaBetaCache cache) {
		super(player, expander, cache);
	}

	@Override
	protected List<Action> getMaximizingActions(GameState state) {
		algConfig.createInformationSetFor(state);
		return expander.getActions(state);		
	}
	
	@Override
	protected List<Action> getMinimizingActions(GameState state) {
		algConfig.createInformationSetFor(state);
		
		GameState newState = state.performAction(expander.getActions(state).get(0));
				
		algConfig.createInformationSetFor(newState);
		return expander.getActions(newState);
	}

}
