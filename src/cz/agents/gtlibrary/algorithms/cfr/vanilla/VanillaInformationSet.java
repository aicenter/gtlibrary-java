/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.cfr.vanilla;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.cfr.CFRInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.FixedSizeMap;

/**
 * This class will be removed, the implementation of CFR and OOS is obsolete.
 * Use cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm/CFRISAlgorithm instead.
 */
@Deprecated
public class VanillaInformationSet extends CFRInformationSet {

	private Map<GameState, float[]> valuesForStates;
	private Map<GameState, Map<Action, Float>> valuesOfActionsForStates;
	private Map<GameState, float[]> averageValuesForStates;
	private Map<GameState, Map<Action, Float>> averageValuesOfActionsForStates;

	private Map<Action, Float> values;
	private Map<Action, Float> averageValues;
	private Map<Action, Float> numerators;

	private float probabilityOfOccurenceGivenStrategyOfOthers;
	private float probabilityOfOccurenceGivenMyStrategy;
	private float probabilitySum;

	public VanillaInformationSet(GameState state) {
		super(state);
		valuesForStates = new HashMap<GameState, float[]>();
		valuesOfActionsForStates = new HashMap<GameState, Map<Action, Float>>();
		averageValuesForStates = new HashMap<GameState, float[]>();
		averageValuesOfActionsForStates = new HashMap<GameState, Map<Action, Float>>();
	}
	
	@Override
	public void initializeFor(Collection<Action> actions, GameState state) {
		super.initializeFor(actions, state);
		values = new FixedSizeMap<Action, Float>(actions.size());
		averageValues = new FixedSizeMap<Action, Float>(actions.size());
		numerators = new FixedSizeMap<Action, Float>(actions.size());
	}

	public void addProbabilityToSum() {
		probabilitySum += probabilityOfOccurenceGivenMyStrategy;
	}

	public void setProbabilityOfOccurenceGivenMyStrategy(float probabilityOfOccurenceGivenMyStrategy) {
		this.probabilityOfOccurenceGivenMyStrategy = probabilityOfOccurenceGivenMyStrategy;
	}

	public float getProbabilityOfOccurenceGivenStrategyOfOthers() {
		return probabilityOfOccurenceGivenStrategyOfOthers;
	}

	public float[] getValuesForState(GameState gameState) {
		return valuesForStates.get(gameState);
	}

	public void setValuesForState(GameState gameState, float[] values) {
		valuesForStates.put(gameState, values.clone());
	}

	public void setProbabilityOfOccurenceGivenStrategyOfOthers(float probabilityOfOccurenceGivenStrategyOfOthers) {
		this.probabilityOfOccurenceGivenStrategyOfOthers = probabilityOfOccurenceGivenStrategyOfOthers;
	}

	public float[] getAverageValuesForState(GameState gameState) {
		return averageValuesForStates.get(gameState);
	}

	public Map<GameState, float[]> getAverageValuesForStates() {
		return averageValuesForStates;
	}

	public void setAverageValuesForHistory(GameState gameState, float[] values) {
		averageValuesForStates.put(gameState, values.clone());
	}

	public Map<Action, Float> getValuesOfActionsForState(GameState gameState) {
		return valuesOfActionsForStates.get(gameState);
	}

	public void setValuesOfActionsForState(GameState gameState, Map<Action, Float> values) {
		valuesOfActionsForStates.put(gameState, values);
	}

	public Map<Action, Float> getAverageValuesOfActionsForState(GameState gameState) {
		return averageValuesOfActionsForStates.get(gameState);
	}

	public void setAverageValuesOfActionsForState(GameState gameState, Map<Action, Float> values) {
		averageValuesOfActionsForStates.put(gameState, values);
	}

	public float getValueFor(Action action) {
		return values.get(action);
	}

	public void setValueFor(Action action, float value) {
		values.put(action, value);
	}

	public void addToValuesFor(Action action, float value) {
		Float oldValue = values.get(action);

		if (oldValue == null) {
			values.put(action, value);
		} else {
			values.put(action, oldValue + value);
		}
	}
	
	public void clearValues() {
		values.clear();
	}
	
	public void clearAverageValues() {
		averageValues.clear();
	}
	
	public void addAverageValueFor(Action action, float value) {
		Float oldValue = averageValues.get(action);

		if (oldValue == null) {
			averageValues.put(action, value);
		} else {
			averageValues.put(action, oldValue + value);
		}
	}

	public Map<Action, Float> getAverageValues() {
		return averageValues;
	}

	public void computeAverageStrategy() {
		addStrategyToNumerators();
		for (Action key : averageStrategy.keySet()) {
			averageStrategy.put(key, numerators.get(key) / probabilitySum);
		}
	}

	private void addStrategyToNumerators() {
		for (Action action : strategy.keySet()) {
			Float oldValue = numerators.get(action);
			if (oldValue == null) {
				numerators.put(action, probabilityOfOccurenceGivenMyStrategy * strategy.get(action));
			} else {
				numerators.put(action, oldValue + probabilityOfOccurenceGivenMyStrategy * strategy.get(action));
			}
		}
	}

	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("Game reward: ");
		builder.append(valueOfGame);
		builder.append("\n");
		builder.append("Strategy: ");
		builder.append(strategy);
		builder.append("\n");
		builder.append("Average strategy: ");
		builder.append(averageStrategy);
		builder.append("\n");
		builder.append("Values: ");
		builder.append(values);
		builder.append("\n");
		builder.append("Average values: ");
		builder.append(averageValues);
		builder.append("\n");
		builder.append("Regret: ");
		builder.append(regret);
		builder.append("\n");
		builder.append("Values for histories: ");
		for (Entry<GameState, float[]> entry : valuesForStates.entrySet()) {
			builder.append("[" + entry.getKey() + " = " + Arrays.toString(entry.getValue()) + "]");
		}
		builder.append("\n");
		builder.append("Average values for histories: ");
		for (Entry<GameState, float[]> entry : averageValuesForStates.entrySet()) {
			builder.append("[" + entry.getKey() + " = " + Arrays.toString(entry.getValue()) + "]");
		}
		builder.append("\n");
		builder.append("Values for actions in histories: ");
		builder.append(valuesOfActionsForStates);
		builder.append("\n");
		builder.append("Average values for actions in  histories: ");
		builder.append(averageValuesOfActionsForStates);
		builder.append("\n");
		builder.append("Probability of occurence given my strategy: ");
		builder.append(probabilityOfOccurenceGivenMyStrategy);
		builder.append("\n");
		builder.append("Probability of occurence given opp strategy: ");
		builder.append(probabilityOfOccurenceGivenStrategyOfOthers);
		return builder.toString();
	}
}
