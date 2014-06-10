package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import java.util.List;
import java.util.ListIterator;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public abstract class AlphaBetaImpl implements AlphaBeta {

	protected Player player;
	protected AlphaBetaCache cache;
	protected Expander<SimABInformationSet> expander;
	protected AlgorithmConfig<SimABInformationSet> algConfig;
	protected GameInfo gameInfo;
    private Action p1Action;
    private Action p2Action;

    public AlphaBetaImpl(Player player, Expander<SimABInformationSet> expander, AlphaBetaCache cache, GameInfo gameInfo) {
		this.player = player;
		this.expander = expander;
		this.cache = cache;
		this.algConfig = expander.getAlgorithmConfig();
		this.gameInfo = gameInfo;
	}

	public double getUnboundedValue(GameState state) {
		return getValue(state, -gameInfo.getMaxUtility(), gameInfo.getMaxUtility());
	}

	public double getValue(GameState state, double alpha, double beta) {
		Double value = cache.get(state);

		if (value != null)
			return value;

		boolean prune = false;

		if (state.isGameEnd())
			return state.getUtilities()[player.getId()];

		if (state.isPlayerToMoveNature()) {
			return getUtilityForNature(state, alpha, beta);
		} else {
			Stats.getInstance().increaseABStatesFor(player);
			for (Action minAction : getMinimizingActions(state)) {
				double tempAlpha = getTempAlpha(state, minAction, alpha, beta);

                if (tempAlpha < beta) {
                    beta = Math.min(beta, tempAlpha);
                    storeAction(minAction);
                } else {
                    prune = true;
                }
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

    private double getInsideValue(GameState state, double alpha, double beta) {
        Double value = cache.get(state);

        if (value != null)
            return value;

        boolean prune = false;

        if (state.isGameEnd())
            return state.getUtilities()[player.getId()];

        if (state.isPlayerToMoveNature()) {
            return getUtilityForNature(state, alpha, beta);
        } else {
            Stats.getInstance().increaseABStatesFor(player);
            for (Action minAction : getMinimizingActions(state)) {
                double tempAlpha = getInsideTempAlpha(state, minAction, alpha, beta);

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
            double value = getInsideValue(performActions(state, minAction, maxAction), tempAlpha, beta);

            if(value > tempAlpha) {
                tempAlpha = value;
                storeAction(maxAction);
            }
			if (beta <= tempAlpha)
				return tempAlpha;
		}
		return tempAlpha;
	}

    protected void storeAction(Action action) {
        if(action.getInformationSet().getPlayer().getId() == 0)
            p1Action = action;
        else
            p2Action = action;
    }

    private double getInsideTempAlpha(GameState state, Action minAction, double alpha, double beta) {
        double tempAlpha = alpha;

        for (Action maxAction : getMaximizingActions(state)) {
            tempAlpha = Math.max(tempAlpha, getInsideValue(performActions(state, minAction, maxAction), tempAlpha, beta));
            if (beta <= tempAlpha)
                return tempAlpha;
        }
        return tempAlpha;
    }

	public double getUtilityForNature(GameState state, double alpha, double beta) {
		double utility = 0;
		List<Action> actions = expander.getActions(state);
		ListIterator<Action> iterator = actions.listIterator();

		while (iterator.hasNext()) {
			Action action = iterator.next();
			double lowerBound = Math.max(-gameInfo.getMaxUtility(), getLowerBound(actions, state, alpha, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));
			double upperBound = Math.min(gameInfo.getMaxUtility(), getUpperBound(actions, state, beta, state.getProbabilityOfNatureFor(action), utility, iterator.previousIndex()));
					
			utility += state.getProbabilityOfNatureFor(action) * getValue(state.performAction(action), lowerBound, upperBound);
		}
		return utility;
	}

	private double getUpperBound(List<Action> actions, GameState state, double upperBound, double probability, double utilityValue, int index) {
		ListIterator<Action> iterator = actions.listIterator();
		double utility = utilityValue;

		while (iterator.hasNext()) {
			Action action = iterator.next();

			if (iterator.previousIndex() > index)
				utility += state.getProbabilityOfNatureFor(action) * -gameInfo.getMaxUtility();
		}
		return (upperBound - utility) / probability;
	}

	private double getLowerBound(List<Action> actions, GameState state, double lowerBound, double probability, double utilityValue, int index) {
		ListIterator<Action> iterator = actions.listIterator();
		double utility = utilityValue;

		while (iterator.hasNext()) {
			Action action = iterator.next();

			if (iterator.previousIndex() > index)
				utility += state.getProbabilityOfNatureFor(action) * gameInfo.getMaxUtility();
		}
		return (lowerBound - utility) / probability;
	}

    public Action getTopLevelAction(Player player) {
        if(player.getId() == 0)
            return p1Action;
        return p2Action;
    }

	public int getCacheSize() {
		return cache.size();
	}

	protected abstract GameState performActions(GameState state, Action minAction, Action maxAction);

	protected abstract List<Action> getMaximizingActions(GameState state);

	protected abstract List<Action> getMinimizingActions(GameState state);
}
