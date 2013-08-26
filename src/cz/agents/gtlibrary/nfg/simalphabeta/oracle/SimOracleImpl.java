package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;

public abstract class SimOracleImpl implements SimOracle {

	protected static boolean USE_INCREASING_BOUND = false;

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
		this.cache = data.cache;
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
