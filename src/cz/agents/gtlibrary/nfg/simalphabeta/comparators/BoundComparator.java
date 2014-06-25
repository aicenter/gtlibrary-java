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
