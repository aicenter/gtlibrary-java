package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import java.util.List;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;

public abstract class AlphaBetaImpl implements AlphaBeta {

	protected Player player;
	protected AlphaBetaCache cache;
	protected Expander<SimABInformationSet> expander;
	protected AlgorithmConfig<SimABInformationSet> algConfig;

	public AlphaBetaImpl(Player player, Expander<SimABInformationSet> expander, AlphaBetaCache cache) {
		this.player = player;
		this.expander = expander;
		this.cache = cache;
		this.algConfig = expander.getAlgorithmConfig();
	}

	public double getUnboundedValue(GameState state) {
		return getValue(state, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	public double getValue(GameState state, double alpha, double beta) {
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

	public int getCacheSize() {
		return cache.size();
	}
	
	protected abstract GameState performActions(GameState state, Action minAction, Action maxAction);

	protected abstract List<Action> getMaximizingActions(GameState state);

	protected abstract List<Action> getMinimizingActions(GameState state);
}
