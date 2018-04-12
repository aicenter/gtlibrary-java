package cz.agents.gtlibrary.experimental.utils;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.strategy.Strategy;

import java.util.Map;

public class GenSumUtilityCalculator {

    protected GameState rootState;
    protected Expander<? extends InformationSet> expander;

    public GenSumUtilityCalculator(GameState rootState, Expander<? extends InformationSet> expander) {
        super();
        this.rootState = rootState;
        this.expander = expander;
    }

    public double[] computeUtility(Strategy strategy1, Strategy strategy2) {
        return computeUtility(rootState, strategy1, strategy2);
    }

    protected double[] computeUtility(GameState state, Strategy strategy1, Strategy strategy2) {
        if (state.isGameEnd())
            return state.getUtilities();
        if (state.isPlayerToMoveNature()) {
            return computeUtilityForNature(state, strategy1, strategy2);
        }
        return computeUtilityForRegularPlayer(state, strategy1, strategy2);
    }

    protected double[] computeUtilityForRegularPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
        if (state.getPlayerToMove().getId() == 0)
            return getUtilityForFirstPlayer(state, strategy1, strategy2);
        return getUtilityForSecondPlayer(state, strategy1, strategy2);
    }

    protected double[] getUtilityForFirstPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
        Map<Action, Double> contOfStrat = getContOfStrat(state, strategy1);
        double[] utility = new double[2];
        double sum = getProbabilitySumOf(contOfStrat);

        if (contOfStrat.isEmpty())
            throw new UnsupportedOperationException("Missing sequences");
        if (hasOnlyZeros(contOfStrat))
            throw new UnsupportedOperationException("Missing sequences");
        for (Map.Entry<Action, Double> entry : contOfStrat.entrySet()) {
            if (entry.getValue() > 1e-8) {
                double[] utilityAfterAction = computeUtility(state.performAction(entry.getKey()), strategy1, strategy2);

                for (int i = 0; i < utility.length; i++) {
                    utility[i] += utilityAfterAction[i] * entry.getValue() / sum;
                }
            }
        }
        return utility;
    }

    protected double[] getUtilityForSecondPlayer(GameState state, Strategy strategy1, Strategy strategy2) {
        Map<Action, Double> contOfStrat = getContOfStrat(state, strategy2);
        double[] utility = new double[2];
        double sum = getProbabilitySumOf(contOfStrat);

        if (contOfStrat.isEmpty())
            throw new UnsupportedOperationException("Missing sequences");
        if (hasOnlyZeros(contOfStrat))
            throw new UnsupportedOperationException("Missing sequences");
        for (Map.Entry<Action, Double> entry : contOfStrat.entrySet()) {
            if (entry.getValue() > 1e-8) {
                double[] utilityAfterAction = computeUtility(state.performAction(entry.getKey()), strategy1, strategy2);

                for (int i = 0; i < utility.length; i++) {
                    utility[i] += utilityAfterAction[i] * entry.getValue() / sum;
                }
            }
        }
        return utility;
    }

    protected boolean hasOnlyZeros(Map<Action, Double> contOfStrat) {
        for (Double value : contOfStrat.values()) {
            if (value > 0)
                return false;
        }
        return true;
    }

    protected double getProbabilitySumOf(Map<Action, Double> contOfStrat) {
        double probabilitySum = 0;

        for (Double value : contOfStrat.values()) {
            probabilitySum += value;
        }
        return probabilitySum;
    }

    protected double[] computeUtilityForNature(GameState state, Strategy strategy1, Strategy strategy2) {
        double[] utility = new double[2];

        for (Action action : expander.getActions(state)) {
            double[] utilityAfterAction = computeUtility(state.performAction(action), strategy1, strategy2);

            for (int i = 0; i < utility.length; i++) {
                utility[i] += state.getProbabilityOfNatureFor(action) * utilityAfterAction[i];
            }
        }
        return utility;
    }

    protected Map<Action, Double> getContOfStrat(GameState state, Strategy strategy) {
        return strategy.getDistributionOfContinuationOf(state.getSequenceForPlayerToMove(), expander.getActionsForUnknownIS(state));
    }
}
