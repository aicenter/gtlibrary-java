package cz.agents.gtlibrary.nfg.simalphabeta.utility;

import java.util.List;
import java.util.ListIterator;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCache;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class DOUtilityCalculator implements UtilityCalculator {

	protected Data data;
	protected NatureCache natureCache;

	public DOUtilityCalculator(Data data, NatureCache natureCache) {
		this.data = data;
		this.natureCache = natureCache;
	}

	public double getUtilities(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		Double utility = data.cache.getUtilityFor(s1, s2);

		if (utility != null)
			return utility;
		if (state.isPlayerToMoveNature())
			return computeUtilityForNature(state, s1, s2, alpha, beta);
		return computeUtilityOf(state, alpha, beta);
	}

	protected double computeUtilityForNature(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		double utilityValue = 0;
		List<Action> actions = data.expander.getActions(state);
		ListIterator<Action> iterator = actions.listIterator();
		
		while (iterator.hasNext()) {
			Action action = iterator.next();
			GameState nextState = state.performAction(action);
			double lowerBound = Math.max(getPesimisticValue(nextState), getLowerBound(actions, state, alpha, state.getProbabilityOfNatureFor(action), utilityValue, iterator.previousIndex()));
			double upperBound = Math.min(getOptimisticValue(nextState), getUpperBound(actions, state, beta, state.getProbabilityOfNatureFor(action), utilityValue, iterator.previousIndex()));
			double currentUtility = computeUtilityOf(nextState, lowerBound, upperBound);

			if(Double.isNaN(currentUtility))
				return Double.NaN;
			data.natureCache.updateBothFor(nextState, currentUtility);
			utilityValue += state.getProbabilityOfNatureFor(action) * currentUtility;
		}
		return utilityValue;
	}

	protected double getUpperBound(List<Action> actions, GameState state, double upperBound, double probability, double utilityValue, int index) {
		ListIterator<Action> iterator = actions.listIterator();
		double utility = utilityValue;

		while (iterator.hasNext()) {
			Action action = iterator.next();

			if (iterator.previousIndex() > index) {
				utility += state.getProbabilityOfNatureFor(action) * getPesimisticValue(state.performAction(action));
			}
		}
		return (upperBound - utility) / probability;
	}

	protected double getLowerBound(List<Action> actions, GameState state, double lowerBound, double probability, double utilityValue, int index) {
		ListIterator<Action> iterator = actions.listIterator();
		double utility = utilityValue;

		while (iterator.hasNext()) {
			Action action = iterator.next();

			if (iterator.previousIndex() > index) {
				utility += state.getProbabilityOfNatureFor(action) * getOptimisticValue(state.performAction(action));
			}
		}
		return (lowerBound - utility) / probability;
	}

	protected double getPesimisticValue(GameState state) {
		Double pesimistic = data.natureCache.getPesimisticFor(state);

		if (pesimistic == null) {
			long time = System.currentTimeMillis();
			
			pesimistic = -data.alphaBetas[1].getUnboundedValue(state);
			Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
			data.natureCache.updatePesimisticFor(state, pesimistic);
		}
		return pesimistic;
	}

	protected double getOptimisticValue(GameState state) {
		Double optimistic = data.natureCache.getOptimisticFor(state);

		if (optimistic == null) {
			long time = System.currentTimeMillis();
			
			optimistic = data.alphaBetas[0].getUnboundedValue(state);
			Stats.getInstance().addToABTime(System.currentTimeMillis() - time);
			data.natureCache.updateOptimisticFor(state, optimistic);
		}
		return optimistic;
	}

	protected double computeUtilityOf(GameState state, double alpha, double beta) {
		DoubleOracle doubleOracle = data.getDoubleOracle(state, alpha, beta);

		doubleOracle.generate();
		return doubleOracle.getGameValue();
	}

	public double getUtility(GameState state, ActionPureStrategy s1, ActionPureStrategy s2) {
		Double utility = data.cache.getUtilityFor(s1, s2);

        if (utility != null)
            return utility;
        if (state.isPlayerToMoveNature())
            return computeUtilityForNature(state, s1, s2, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        return computeUtilityOf(state, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

//		return utility == null ? Double.NaN : utility;
	}

	@Override
	public double getUtilitiesForIncreasedBounds(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		Double utility = data.cache.getUtilityFor(s1, s2);

		if (utility != null)
			return utility;
		if (state.isPlayerToMoveNature())
			return computeUtilityForNatureForIncrBounds(state, s1, s2, alpha - 1e-4, beta);
		return computeUtilityOf(state, alpha - 1e-4, beta);
	}

	private double computeUtilityForNatureForIncrBounds(GameState state, ActionPureStrategy s1, ActionPureStrategy s2, double alpha, double beta) {
		double utilityValue = 0;
		List<Action> actions = data.expander.getActions(state);
		ListIterator<Action> iterator = actions.listIterator();
		
		while (iterator.hasNext()) {
			Action action = iterator.next();
			GameState nextState = state.performAction(action);
			double lowerBound = Math.max(getPesimisticValue(nextState) - 1e-4, getLowerBound(actions, state, alpha, state.getProbabilityOfNatureFor(action), utilityValue, iterator.previousIndex()));
			double upperBound = Math.min(getOptimisticValue(nextState), getUpperBound(actions, state, beta, state.getProbabilityOfNatureFor(action), utilityValue, iterator.previousIndex()));
			double currentUtility = computeUtilityOf(nextState, lowerBound, upperBound);

			if(Double.isNaN(currentUtility))
				return Double.NaN;
			data.natureCache.updateBothFor(nextState, currentUtility);
			utilityValue += state.getProbabilityOfNatureFor(action) * currentUtility;
		}
		return utilityValue;
	}
}
