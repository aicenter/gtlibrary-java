package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
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

public abstract class SimABOracleImpl implements SimABOracle {
	
	protected static boolean USE_INCREASING_BOUND = false;
	
	protected HashSet<ActionPureStrategy> actions;
	protected GameState rootState;
	protected Expander<? extends InformationSet> expander;
	protected Player player;
	protected SimUtility utility;
	protected AlphaBeta alphaBeta;
	protected DOCache cache;
	protected AlphaBeta oppAlphaBeta;
	protected AlgorithmConfig<SimABInformationSet> algConfig;

	public SimABOracleImpl(GameState rootState, Player player, SimUtility utility, Data data, DOCache cache) {
		this.rootState = rootState;
		this.expander = data.expander;
		this.player = player;
		this.utility = utility;
		this.alphaBeta = data.getAlphaBetaFor(player);
		this.cache = cache;
		this.oppAlphaBeta = data.getAlphaBetaFor(data.gameInfo.getOpponent(player));
		this.algConfig = data.config;
	}
	

	@Override
	public ActionPureStrategy getForcedBestResponse(MixedStrategy<ActionPureStrategy> mixedStrategy, double alpha, double beta) {
		return getActions().iterator().next();
	}
	
	protected Collection<ActionPureStrategy> getActions() {
		if (actions == null)
			initActions();
		return actions;
	}

	protected void initActions() {
		actions = new LinkedHashSet<ActionPureStrategy>();
		if (player.equals(rootState.getPlayerToMove())) {
			initFotPlayerToMove();
			return;
		}
		initForOtherPlayer();
	}

	protected void initForOtherPlayer() {
		GameState newState = rootState.performAction(expander.getActions(rootState).get(0));

		algConfig.createInformationSetFor(newState);
		for (Action action : expander.getActions(newState)) {
			actions.add(new ActionPureStrategy(action));
		}
	}

	protected void initFotPlayerToMove() {
		algConfig.createInformationSetFor(rootState);
		for (Action action : expander.getActions(rootState)) {
			actions.add(new ActionPureStrategy(action));
		}
	}
}
