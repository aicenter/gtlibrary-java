package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;

public abstract class AlphaBeta {

	protected Player player;
	protected AlphaBetaCache cache;
	protected Expander<SimABInformationSet> expander;
	protected AlgorithmConfig<SimABInformationSet> algConfig;

	public AlphaBeta(Player player, Expander<SimABInformationSet> expander, AlphaBetaCache cache) {
		this.player = player;
		this.expander = expander;
		this.cache = cache;
		this.algConfig = expander.getAlgorithmConfig();
	}

	public double getValue(GameState state, Action action, double alpha, double beta) {
		return getValue(state.performAction((Action) action), alpha, beta);
	}

	public double getValue(GameState state, double alpha, double beta) {
//		if (!OracleImpl.USE_ALPHABETA) {bude kdy�tak vy�e�eno pr�zdnou classou
//			return Double.MAX_VALUE;
//		}
		
		Double value = cache.get(state);
		
		if (value != null)
			return value;

		boolean prune = false;

		algConfig.createInformationSetFor(state);
		if (state.isGameEnd())
			return state.getUtilities()[player.getId()];

		if (state.isPlayerToMoveNature()) {
			return getUtilityForNature(state, alpha, beta);
		} else {
//			if (player.getId() == 0)
//				Info.increaseFPABStates();
//			else
//				Info.increaseSPABStates();
			for (Action minAction : getMinimizingActions(state)) {
				double tempAlpha = getTempAlpha(state, minAction, alpha, beta);
				
				if (beta <= tempAlpha) 
					prune = true;
				beta = Math.min(beta, tempAlpha);
				if (beta <= alpha) {
					prune = true;
					break;
				}

			}
			if (!prune) 
				cache.put(state, beta);
			return beta;
		}

	}

	private double getTempAlpha(GameState state, Action minAction, double alpha, double beta) {
		double tempAlpha = alpha;

		for (Action maxAction : getMaximizingActions(state)) {
			tempAlpha = Math.max(tempAlpha, getValue(performActions(state, minAction, maxAction), tempAlpha, beta));
			if (beta <= tempAlpha) 
				return tempAlpha;
		}
		return tempAlpha;
	}

	public double getUtilityForNature(GameState state, double alpha, double beta) {
		double utility = 0;
		List<Action> actions = expander.getActions(state);

		for (Action action : actions) {
			utility += getValue(state.performAction(action), alpha, beta);
		}
		return utility / actions.size();
	}

	private GameState performActions(GameState state, Action minAction, Action maxAction) {
		if (player.getId() == 0) {
			GameState newState = state.performAction(maxAction);

			newState.performActionModifyingThisState(minAction);
			return newState;
		}
		GameState newState = state.performAction(minAction);

		newState.performActionModifyingThisState(maxAction);
		return newState;
	}

	public int getCacheSize() {
		return cache.size();
	}

	protected abstract List<Action> getMaximizingActions(GameState state);

	protected abstract List<Action> getMinimizingActions(GameState state);
}
