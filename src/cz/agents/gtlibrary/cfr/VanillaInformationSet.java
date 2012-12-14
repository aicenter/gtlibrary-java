package cz.agents.gtlibrary.cfr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class VanillaInformationSet extends CFRInformationSet {

	private Map<History, float[]> valuesForHistories;
	private Map<History, Map<Action, Float>> valuesOfActionsForHistories;
	private Map<History, float[]> averageValuesForHistories;
	private Map<History, Map<Action, Float>> averageValuesOfActionsForHistories;

	private Map<Action, Float> values;
	private Map<Action, Float> averageValues;
	private Map<Action, Float> numerators;

	private float probabilityOfOccurenceGivenStrategyOfOthers;
	private float probabilityOfOccurenceGivenMyStrategy;
	private float probabilitySum;

	public VanillaInformationSet(GameState state, List<Action> actions) {
		super(state, actions);
		valuesForHistories = new HashMap<History, float[]>();
		valuesOfActionsForHistories = new HashMap<History, Map<Action, Float>>();
		averageValuesForHistories = new HashMap<History, float[]>();
		averageValuesOfActionsForHistories = new HashMap<History, Map<Action, Float>>();
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

	public float[] getValuesForHistory(History history) {
		return valuesForHistories.get(history);
	}

	public void setValuesForHistory(History history, float[] values) {
		valuesForHistories.put(history, values.clone());
	}

	public void setProbabilityOfOccurenceGivenStrategyOfOthers(float probabilityOfOccurenceGivenStrategyOfOthers) {
		this.probabilityOfOccurenceGivenStrategyOfOthers = probabilityOfOccurenceGivenStrategyOfOthers;
	}

	public float[] getAverageValuesForHistory(History history) {
		return averageValuesForHistories.get(history);
	}

	public Map<History, float[]> getAverageValuesForHistorys() {
		return averageValuesForHistories;
	}

	public void setAverageValuesForHistory(History history, float[] values) {
		averageValuesForHistories.put(history, values.clone());
	}

	public Map<Action, Float> getValuesOfActionsForHistory(History history) {
		return valuesOfActionsForHistories.get(history);
	}

	public void setValuesOfActionsForHistory(History history, Map<Action, Float> values) {
		valuesOfActionsForHistories.put(history, values);
	}

	public Map<Action, Float> getAverageValuesOfActionsForHistory(History history) {
		return averageValuesOfActionsForHistories.get(history);
	}

	public void setAverageValuesOfActionsForHistory(History history, Map<Action, Float> values) {
		averageValuesOfActionsForHistories.put(history, values);
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
	

//	public void addToAverageValuesFor(Action action, float value) {
//		Float oldValue = averageValues.get(action);
//
//		if (oldValue == null) {
//			averageValues.put(action, value);
//		} else {
//			averageValues.put(action, oldValue + value);
//		}
//	}

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
		
		builder.append("Game value: ");
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
		for (Entry<History, float[]> entry : valuesForHistories.entrySet()) {
			builder.append("[" + entry.getKey() + " = " + Arrays.toString(entry.getValue()) + "]");
		}
		builder.append("\n");
		builder.append("Average values for histories: ");
		for (Entry<History, float[]> entry : averageValuesForHistories.entrySet()) {
			builder.append("[" + entry.getKey() + " = " + Arrays.toString(entry.getValue()) + "]");
		}
		builder.append("\n");
		builder.append("Values for actions in histories: ");
		builder.append(valuesOfActionsForHistories);
		builder.append("\n");
		builder.append("Average values for actions in  histories: ");
		builder.append(averageValuesOfActionsForHistories);
		builder.append("\n");
		builder.append("Probability of occurence given my strategy: ");
		builder.append(probabilityOfOccurenceGivenMyStrategy);
		builder.append("\n");
		builder.append("Probability of occurence given opp strategy: ");
		builder.append(probabilityOfOccurenceGivenStrategyOfOthers);
		return builder.toString();
	}
}
