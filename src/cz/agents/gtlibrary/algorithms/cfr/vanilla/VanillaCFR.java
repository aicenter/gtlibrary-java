package cz.agents.gtlibrary.algorithms.cfr.vanilla;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.algorithms.cfr.CFR;
import cz.agents.gtlibrary.algorithms.cfr.CFRConfig;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerAction;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class VanillaCFR extends CFR<VanillaInformationSet> {

	public static void main(String[] args) {
//		GameState rootState = new KuhnPokerGameState();
//		CFRConfig<VanillaInformationSet> config = new CFRConfig<VanillaInformationSet>(new KuhnPokerGameState());
//		VanillaCFR cfr = new VanillaCFR(config);
//
//		cfr.buildGameTree(rootState, new KuhnPokerExpander<VanillaInformationSet>(config));
//		cfr.updateTree(200000);
		
//		GameState rootState = new GenericPokerGameState();
//		CFRConfig<VanillaInformationSet> config = new CFRConfig<VanillaInformationSet>(new GenericPokerGameState());
//		VanillaCFR cfr = new VanillaCFR(config);
//
//		cfr.buildGameTree(rootState, new GenericPokerExpander<VanillaInformationSet>(config));
//		cfr.updateTree(200000);
		
		GameState rootState = new BPGGameState();
		CFRConfig<VanillaInformationSet> config = new CFRConfig<VanillaInformationSet>(new BPGGameState());
		VanillaCFR cfr = new VanillaCFR(config);

		cfr.buildGameTree(rootState, new BPGExpander<VanillaInformationSet>(config));
		cfr.updateTree(200000);
	}

	public VanillaCFR(CFRConfig<VanillaInformationSet> config) {
		super(config);
	}

	@Override
	public VanillaInformationSet createInformationSet(GameState state, List<Action> actions) {
		return new VanillaInformationSet(state, actions);
	}

	@Override
	public void updateTree() {
		config.incrementIterations();
		updateProbabilitiesAndValues();
		updateStrategies();
	}

	private void updateProbabilitiesAndValues() {
		updateStateValues();
		for (VanillaInformationSet set : config.getAllInformationSets().values()) {
			updateProbabilitiesForSet(set);
			updateValuesForSet(set);
		}
	}

	private void updateStateValues() {
		VanillaInformationSet set = config.getInformationSetFor(config.getRootState());
		Map<GameState, float[]> valuesOfChildren = new FixedSizeMap<GameState, float[]>(set.getSuccessorsFor(config.getRootState()).size());
		Map<GameState, float[]> avgValuesOfChildren = new FixedSizeMap<GameState, float[]>(set.getSuccessorsFor(config.getRootState()).size());

		for (GameState successor : set.getSuccessorsFor(config.getRootState())) {
			valuesOfChildren.put(successor, updateStateValuesRecursively(successor));
			avgValuesOfChildren.put(successor, getAverageValues(successor));
		}
		updateState(valuesOfChildren, avgValuesOfChildren, config.getRootState());
	}

	private float[] updateStateValuesRecursively(GameState gameState) {
		VanillaInformationSet set = config.getInformationSetFor(gameState);

		if (set == null) {
			return config.getUtilityFor(gameState.getHistory());
		}
		Set<GameState> successors = set.getSuccessorsFor(gameState);
		Map<GameState, float[]> valuesOfChildren = new FixedSizeMap<GameState, float[]>(successors.size());
		Map<GameState, float[]> avgValuesOfChildren = new FixedSizeMap<GameState, float[]>(successors.size());

		for (GameState successor : successors) {
			valuesOfChildren.put(successor, updateStateValuesRecursively(successor));
			avgValuesOfChildren.put(successor, getAverageValues(successor));
		}
		updateState(valuesOfChildren, avgValuesOfChildren, gameState);
		return set.getValuesForState(gameState);
	}

	private void updateState(Map<GameState, float[]> valuesOfChildren, Map<GameState, float[]> averageValuesOfChildren, GameState gameState) {
		VanillaInformationSet set = config.getInformationSetFor(gameState);
		Map<Action, Float> valuesOfActionsForState = new FixedSizeMap<Action, Float>(set.getActions().size());
		Map<Action, Float> avgValuesOfActionsForState = new FixedSizeMap<Action, Float>(set.getActions().size());
		float[] valuesOfState = new float[config.getRootState().getAllPlayers().length];
		float[] avgValuesOfState = new float[config.getRootState().getAllPlayers().length];
		int indexOfPlayer = getIndexOfPlayer(set.getPlayer());

		for (GameState successor : valuesOfChildren.keySet()) {
			Action action = getLastAction(successor, gameState);
			float[] valueOfChild = valuesOfChildren.get(successor);
			float[] averageValueOfChild = averageValuesOfChildren.get(successor);

			for (int j = 0; j < valuesOfState.length; j++) {
				valuesOfState[j] += set.getStrategyFor(action) * valueOfChild[j];
				avgValuesOfState[j] += set.getAverageStrategyFor(action) * averageValueOfChild[j];
			}
			valuesOfActionsForState.put(action, valueOfChild[indexOfPlayer]);
			avgValuesOfActionsForState.put(action, averageValueOfChild[indexOfPlayer]);
		}
		set.setValuesForState(gameState, valuesOfState);
		set.setValuesOfActionsForState(gameState, valuesOfActionsForState);
		set.setAverageValuesForHistory(gameState, avgValuesOfState);
		set.setAverageValuesOfActionsForState(gameState, avgValuesOfActionsForState);
	}

	private Action getLastAction(GameState successor, GameState gameState) {
		return successor.getHistory().getSequenceOf(gameState.getPlayerToMove()).getLast();
	}

	private int getIndexOfPlayer(Player player) {
		return player.getId() % 2;
	}

	private float[] getAverageValues(GameState gameState) {
		VanillaInformationSet set = config.getInformationSetFor(gameState);

		if (set != null) {
			return set.getAverageValuesForState(gameState);
		}
		return config.getUtilityFor(gameState.getHistory());
	}

	private void updateStrategies() {
		for (VanillaInformationSet set : config.getAllInformationSets().values()) {
			if (set.isSetForNature()) {
				continue;
			}
			set.computeAverageStrategy();

			float setValue = 0;
			float sum = 0;

			for (Action action : set.getActions()) {
				setValue += set.getValueFor(action) * set.getStrategyFor(action);
			}
			for (Action action : set.getActions()) {
				set.addToRegretFor(action, set.getProbabilityOfOccurenceGivenStrategyOfOthers() * (set.getValueFor(action) - setValue));
				sum += Math.max(set.getRegretFor(action) / config.getIterations(), 0);
			}
			for (Action action : set.getActions()) {
				set.setStrategyFor(action, computeNewStrategy(set, sum, action));
			}
		}
	}

	private float computeNewStrategy(VanillaInformationSet set, float sum, Action action) {
		if (sum > 0)
			return Math.max(set.getRegretFor(action) / config.getIterations(), 0) / sum;
		return 1f / set.getActions().size();
	}

	private void updateValuesForSet(VanillaInformationSet set) {
		updateValuesFor(set, getProbabilitiesOfStates(set));
		updateAverageValuesFor(set, getAverageProbabilitiesOfStates(set));
		set.setValueOfGame(getValueOfGame(set.getAverageValues(), set.getAverageStrategy()));
	}

	private void updateProbabilitiesForSet(VanillaInformationSet set) {
		set.setProbabilityOfOccurenceGivenMyStrategy(getProbabilityOfSequence(set.getPlayersHistory()));
		set.setProbabilityOfOccurenceGivenStrategyOfOthers(getProbabilityOfCollectionOfOfOpponentHistories(set.getStates(), set.getPlayer()));
		set.addProbabilityToSum();
	}

	private float getProbabilityOfCollectionOfOfOpponentHistories(Collection<GameState> states, Player player) {
		float probabilityOfHistories = 0;

		for (GameState gameState : states) {
			probabilityOfHistories += getProbabilityOfOpponentHistory(gameState.getHistory(), player);
		}
		return probabilityOfHistories;
	}

	public Map<GameState, Float> getAverageProbabilitiesOfStates(VanillaInformationSet set) {
		Map<GameState, Float> probabilities = new FixedSizeMap<GameState, Float>(set.getStates().size());

		for (GameState gameState : set.getStates()) {
			probabilities.put(gameState, getProbabilityOfOpponentHistoryFromAvgStrat(gameState.getHistory(), set.getPlayer()));
		}
		normalize(probabilities);
		return probabilities;
	}

	public Map<GameState, Float> getProbabilitiesOfStates(VanillaInformationSet set) {
		Map<GameState, Float> probabilities = new FixedSizeMap<GameState, Float>(set.getStates().size());

		for (GameState gameState : set.getStates()) {
			probabilities.put(gameState, getProbabilityOfOpponentHistory(gameState.getHistory(), set.getPlayer()));
		}
		normalize(probabilities);
		return probabilities;
	}

	private void normalize(Map<GameState, Float> map) {
		float sum = getSum(map);

		if (Float.compare(sum, 0) != 0)
			for (GameState key : map.keySet()) {
				map.put(key, map.get(key) / sum);
			}
	}

	private float getSum(Map<GameState, Float> map) {
		float sum = 0;

		for (Float value : map.values()) {
			sum += value;
		}
		return sum;
	}

	private float getProbabilityOfSequence(Sequence playersHistory) {
		float probability = 1;

		for (int i = 0; i < playersHistory.size(); i++) {
			probability *= getStrategyFor(i, playersHistory);
		}
		return probability;
	}

	private float getStrategyFor(int i, Sequence playersHistory) {
		Action action = playersHistory.get(i);
		VanillaInformationSet set = (VanillaInformationSet) action.getInformationSet();

		return set.getStrategyFor(action);
	}

	public float getProbabilityOfOpponentHistoryFromAvgStrat(History history, Player player) {
		float probabilityOfSequences = 1;

		for (Sequence sequence : history.values()) {
			if (!sequence.getPlayer().equals(player))
				probabilityOfSequences *= getProbabilityOfSequenceFromAverageStrategy(sequence);
		}
		return probabilityOfSequences;
	}

	public float getProbabilityOfSequenceFromAverageStrategy(Sequence playersHistory) {
		float probability = 1;

		for (int i = 0; i < playersHistory.size(); i++) {
			probability *= getAverageStrategyFor(i, playersHistory);
		}
		return probability;
	}

	private float getAverageStrategyFor(int i, Sequence playersHistory) {
		Action action = playersHistory.get(i);
		VanillaInformationSet set = (VanillaInformationSet) action.getInformationSet();

		return set.getAverageStrategyFor(action);
	}

	private float getProbabilityOfOpponentHistory(History history, Player player) {
		float probabilityOfSequences = 1;

		for (Sequence sequence : history.values()) {
			if (!sequence.getPlayer().equals(player))
				probabilityOfSequences *= getProbabilityOfSequence(sequence);
		}
		return probabilityOfSequences;
	}

	private void updateValuesFor(VanillaInformationSet set, Map<GameState, Float> probabilities) {
		set.clearValues();
		for (GameState gameState : set.getStates()) {
			Map<Action, Float> actionsForHistory = set.getValuesOfActionsForState(gameState);

			for (Action action : actionsForHistory.keySet()) {
				set.addToValuesFor(action, probabilities.get(gameState) * actionsForHistory.get(action));
			}
		}
	}

	private void updateAverageValuesFor(VanillaInformationSet set, Map<GameState, Float> probabilities) {
		set.clearAverageValues();
		for (GameState gameState : set.getStates()) {
			Map<Action, Float> actionsForHistory = set.getAverageValuesOfActionsForState(gameState);

			for (Action action : actionsForHistory.keySet()) {
				set.addAverageValueFor(action, probabilities.get(gameState) * actionsForHistory.get(action));
			}
		}
	}

	private float getValueOfGame(Map<Action, Float> valuesForSet, Map<Action, Float> averageStrategy) {
		float valueOfGame = 0;

		if (valuesForSet.size() != averageStrategy.size()) {
			throw new UnsupportedOperationException("Different size of arrays in getValueOfGame");
		}

		for (Action action : valuesForSet.keySet()) {
			valueOfGame += valuesForSet.get(action) * averageStrategy.get(action);
		}
		return valueOfGame;
	}
}
