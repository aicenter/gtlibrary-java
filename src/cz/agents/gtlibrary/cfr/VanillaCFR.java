package cz.agents.gtlibrary.cfr;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerAction;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.History;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Triplet;

public class VanillaCFR extends CFR<VanillaInformationSet> {
	
	public static void main(String[] args) {
		new Scanner(System.in).next();
		GameState rootState = new KuhnPokerGameState();
		CFRConfig<VanillaInformationSet> config = new CFRConfig<VanillaInformationSet>(new KuhnPokerGameState());
		VanillaCFR cfr = new VanillaCFR(config);
		
		cfr.buildGameTree(rootState, new KuhnPokerExpander());
		cfr.updateTree(100000);
//		System.out.println(config.getInformationSetFor(rootState));
		GameState newState = rootState.performAction(new KuhnPokerAction("0", 0, KPGameInfo.NATURE));
		newState.performActionModifyingThisState(new KuhnPokerAction("1", 0, KPGameInfo.NATURE));
//		System.out.println();
//		System.out.println(config.getInformationSetFor(newState));
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
		for (Map<Sequence, VanillaInformationSet> sequenceMap : config.getAllInformationSets().values()) {
			for (VanillaInformationSet set : sequenceMap.values()) {
				updateProbabilitiesForSet(set);
				updateValuesForSet(set);
			}
		}
	}

	private void updateStateValues() {
		Long isHash = config.getRootState().getISEquivalenceForPlayerToMove();
		VanillaInformationSet set = config.getInformationSetFor(config.getRootState());
		Map<Triplet<Long, History, Player>, float[]> valuesOfChildren = 
				new FixedSizeMap<Triplet<Long, History, Player>, float[]>(set.getSuccessorsFor(config.getRootState().getHistory()).size());
		Map<Triplet<Long, History, Player>, float[]> avgValuesOfChildren = 
				new FixedSizeMap<Triplet<Long, History, Player>, float[]>(set.getSuccessorsFor(config.getRootState().getHistory()).size());

		for (Triplet<Long, History, Player> succTriplet : set.getSuccessorsFor(config.getRootState().getHistory())) {
			valuesOfChildren.put(succTriplet, updateStateValuesRecursively(succTriplet));
			avgValuesOfChildren.put(succTriplet, getAverageValues(succTriplet));
		}
		updateState(valuesOfChildren, avgValuesOfChildren, new Triplet<Long, History, Player>(isHash, config.getRootState().getHistory(), config.getRootState().getPlayerToMove()));
	}

	private float[] updateStateValuesRecursively(Triplet<Long, History, Player> triplet) {
		VanillaInformationSet set = getISFor(triplet);

		if (set == null) {
			return config.getUtilityFor(triplet.getSecond());
		}
		Set<Triplet<Long, History, Player>> successors = set.getSuccessorsFor(triplet.getSecond());
		Map<Triplet<Long, History, Player>, float[]> valuesOfChildren = 
				new FixedSizeMap<Triplet<Long, History, Player>, float[]>(successors.size());
		Map<Triplet<Long, History, Player>, float[]> avgValuesOfChildren = 
				new FixedSizeMap<Triplet<Long, History, Player>, float[]>(successors.size());

		for (Triplet<Long, History, Player> succTriplet : successors) {
			valuesOfChildren.put(succTriplet, updateStateValuesRecursively(succTriplet));
			avgValuesOfChildren.put(succTriplet, getAverageValues(succTriplet));
		}
		updateState(valuesOfChildren, avgValuesOfChildren, triplet);
		return set.getValuesForHistory(triplet.getSecond());
	}

	private VanillaInformationSet getISFor(Triplet<Long, History, Player> triplet) {
		return config.getInformationSetFor(triplet.getFirst(), triplet.getSecond().getSequenceOf(triplet.getThird()));
	}

	private void updateState(Map<Triplet<Long, History, Player>, float[]> valuesOfChildren,
			Map<Triplet<Long, History, Player>, float[]> averageValuesOfChildren, Triplet<Long, History, Player> triplet) {
		
		VanillaInformationSet set = getISFor(triplet);
		Map<Action, Float> valuesOfActionsForHistory = new FixedSizeMap<Action, Float>(set.getActions().size());
		Map<Action, Float> avgValuesOfActionsForHistory = new FixedSizeMap<Action, Float>(set.getActions().size());
		float[] valuesOfHistory = new float[config.getRootState().getAllPlayers().length];
		float[] avgValuesOfHistory = new float[config.getRootState().getAllPlayers().length];
		int indexOfPlayer = getIndexOfPlayer(set.getPlayer());

		for (Triplet<Long, History, Player> succTriplet : valuesOfChildren.keySet()) {
			Action action = getLastAction(succTriplet, triplet);
			float[] valueOfChild = valuesOfChildren.get(succTriplet);
			float[] averageValueOfChild = averageValuesOfChildren.get(succTriplet);
			
			for (int j = 0; j < valuesOfHistory.length; j++) {
				valuesOfHistory[j] += set.getStrategyFor(action) * valueOfChild[j];
				avgValuesOfHistory[j] += set.getAverageStrategyFor(action) * averageValueOfChild[j];
			}
			valuesOfActionsForHistory.put(action, valueOfChild[indexOfPlayer]);
			avgValuesOfActionsForHistory.put(action, averageValueOfChild[indexOfPlayer]);
		}
		set.setValuesForHistory(triplet.getSecond(), valuesOfHistory);
		set.setValuesOfActionsForHistory(triplet.getSecond(), valuesOfActionsForHistory);
		set.setAverageValuesForHistory(triplet.getSecond(), avgValuesOfHistory);
		set.setAverageValuesOfActionsForHistory(triplet.getSecond(), avgValuesOfActionsForHistory);
	}

	private Action getLastAction(Triplet<Long, History, Player> succTriplet, Triplet<Long, History, Player> triplet) {
		return succTriplet.getSecond().getSequenceOf(triplet.getThird()).getLast();
	}

	private int getIndexOfPlayer(Player player) {
		return player.getId() % 2;
	}

	private float[] getAverageValues(Triplet<Long, History, Player> triplet) {
		VanillaInformationSet set = getISFor(triplet);

		if (set != null) {
			return set.getAverageValuesForHistory(triplet.getSecond());
		}
		return config.getUtilityFor(triplet.getSecond());
	}

	private void updateStrategies() {
		for (Map<Sequence, VanillaInformationSet> sequenceMap : config.getAllInformationSets().values()) {
			for (VanillaInformationSet set : sequenceMap.values()) {
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
		set.setProbabilityOfOccurenceGivenStrategyOfOthers(getProbabilityOfCollectionOfOfOpponentHistories(set.getHistories(), set.getPlayer()));
		set.addProbabilityToSum();
	}
	
	private float getProbabilityOfCollectionOfOfOpponentHistories(Collection<History> histories, Player player) {
		float probabilityOfHistories = 0;

		for (History history : histories) {
			probabilityOfHistories += getProbabilityOfOpponentHistory(history, player);
		}
		return probabilityOfHistories;
	}

	public Map<History, Float> getAverageProbabilitiesOfStates(VanillaInformationSet set) {
		Map<History, Float> probabilities = new FixedSizeMap<History, Float>(set.getHistories().size());

		for (History history : set.getHistories()) {
			probabilities.put(history, getProbabilityOfOpponentHistoryFromAvgStrat(history, set.getPlayer()));
		}
		normalize(probabilities);
		return probabilities;
	}

	public Map<History, Float> getProbabilitiesOfStates(VanillaInformationSet set) {
		Map<History, Float> probabilities = new FixedSizeMap<History, Float>(set.getHistories().size());

		for (History history : set.getHistories()) {
			probabilities.put(history, getProbabilityOfOpponentHistory(history, set.getPlayer()));
		}
		normalize(probabilities);
		return probabilities;
	}

	private void normalize(Map<History, Float> map) {
		float sum = getSum(map);

		if (Float.compare(sum, 0) != 0)
			for (History key : map.keySet()) {
				map.put(key, map.get(key) / sum);
			}
	}

	private float getSum(Map<History, Float> map) {
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
		VanillaInformationSet set = config.getInformationSetFor(action.getISHash(), playersHistory.getSubSequence(i));

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
		VanillaInformationSet set = config.getInformationSetFor(action.getISHash(), playersHistory.getSubSequence(i));

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

	private void updateValuesFor(VanillaInformationSet set, Map<History, Float> probabilities) {
		set.clearValues();
		for (History history : set.getHistories()) {
			Map<Action, Float> actionsForHistory = set.getValuesOfActionsForHistory(history);

			for (Action action : actionsForHistory.keySet()) {
				set.addToValuesFor(action, probabilities.get(history) * actionsForHistory.get(action));
			}
		}
	}

	private void updateAverageValuesFor(VanillaInformationSet set, Map<History, Float> probabilities) {
		set.clearAverageValues();
		for (History history : set.getHistories()) {
			Map<Action, Float> actionsForHistory = set.getAverageValuesOfActionsForHistory(history);

			for (Action action : actionsForHistory.keySet()) {
				set.addAverageValueFor(action, probabilities.get(history) * actionsForHistory.get(action));//tady se pøièítá pøes ty historie
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
