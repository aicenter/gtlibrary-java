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


package cz.agents.gtlibrary.algorithms.sequenceform;

import java.util.*;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.iinodes.PerfectRecallISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;

public class SequenceFormConfig<I extends SequenceInformationSet> extends ConfigImpl<I> {
	protected Map<GameState, Double> actualNonZeroUtilityValuesInLeafs = new HashMap<GameState, Double>();
	protected Map<Sequence, Set<I>> reachableSetsBySequence = new HashMap<Sequence, Set<I>>();
	protected Map<Map<Player, Sequence>, Double> utilityForSequenceCombination = new HashMap<Map<Player, Sequence>, Double>();
	protected Map<Map<Player, Sequence>, Double> natureProbabilityForSequenceCombination = new HashMap<Map<Player, Sequence>, Double>();
	protected Map<Sequence, Set<Sequence>> compatibleSequences = new HashMap<Sequence, Set<Sequence>>();
	protected Map<Player, Set<Sequence>> playerSequences = new HashMap<Player, Set<Sequence>>();
	protected Map<Map<Player, Sequence>, Double> allUtilitiesForSeqComb = new HashMap<Map<Player, Sequence>, Double>();

	//TODO: This method should override addInformationSetFor(GameState)
	public void addStateToSequenceForm(GameState state) {
		if (state.isPlayerToMoveNature())
			return;
		fixTheInformationSetInSequences(state);
		setOutgoingSequences(state);
		createInformationSet(state);
		setReachableSetBySequence(state);
		addCompatibleSequence(state);
		addPlayerSequences(state);
	}

	public void fixTheInformationSetInSequences(GameState state) {
		for (Player p : state.getAllPlayers()) {
			if (p.getId() == 2)
				continue; // no fix necessary for nature
			Sequence s = state.getSequenceFor(p);
			if (s.size() == 0)
				continue;
			I i = getAllInformationSets().get(s.getLastInformationSet().getISKey());
			if (i != null) { // if there is a particular IS in the algConfig for the previous state, we set it to be the IS in the stored sequences
				Set<GameState> oldStates = s.getLast().getInformationSet().getAllStates();
				i.addAllStateToIS(oldStates);
				s.getLast().setInformationSet(i);
			} else {
				System.out.print("");
			}
		}
	}

	public void addCompatibleSequence(GameState state) {
		Sequence sequenceOfPlrToMove = state.getSequenceForPlayerToMove();
		Sequence opponentSequence = state.getSequenceFor(state.getAllPlayers()[1 - state.getPlayerToMove().getId()]);

		addCompatibleSequence(state, sequenceOfPlrToMove, opponentSequence);
		addCompatibleSequence(state, opponentSequence, sequenceOfPlrToMove);
	}

	protected void addCompatibleSequence(GameState state, Sequence sequence1, Sequence sequence2) {
		Set<Sequence> sequences = compatibleSequences.get(sequence1);

		if (sequences == null)
			sequences = new HashSet<Sequence>();
		sequences.add(sequence2);
		this.compatibleSequences.put(sequence1, sequences);
	}
	
	public Map<Map<Player, Sequence>, Double> getUtilityForSequenceCombination() {
		return utilityForSequenceCombination;
	}

	public Set<Sequence> getCompatibleSequencesFor(Sequence sequence) {
		return compatibleSequences.get(sequence);
	}

	protected void setOutgoingSequences(GameState state) {
		addOutgoingSequenceFor(state, state.getAllPlayers()[0]);
		addOutgoingSequenceFor(state, state.getAllPlayers()[1]);
	}

	protected void addOutgoingSequenceFor(GameState state, Player player) {
		Sequence sequence = state.getSequenceFor(player);

		if (sequence != null && sequence.size() > 0) {
			I prevIS = (I) sequence.getLastInformationSet();

			prevIS.addOutgoingSequences(sequence);
		}
	}
	
	public Map<GameState, Double> getActualNonZeroUtilityValuesInLeafs() {
		return actualNonZeroUtilityValuesInLeafs;
	}
	
	protected void setReachableSetBySequence(GameState state) {
		if (state.isGameEnd()) {
			addSetFor(getInformationSetFor(state), state.getSequenceFor(state.getAllPlayers()[0]));
			addSetFor(getInformationSetFor(state), state.getSequenceFor(state.getAllPlayers()[1]));
		} else {
			addSetFor(getInformationSetFor(state), state.getSequenceForPlayerToMove());
		}
	}

	protected void addSetFor(I set, Sequence sequence) {
		Set<I> reachableSets = reachableSetsBySequence.get(sequence);

		if (reachableSets == null) {
			reachableSets = new HashSet<I>();
			reachableSetsBySequence.put(sequence, reachableSets);
		}
		reachableSets.add(set);
	}

	protected void addPlayerSequences(GameState state) {
		addSequenceForPlayer(state.getSequenceFor(state.getAllPlayers()[0]), state.getAllPlayers()[0]);
		addSequenceForPlayer(state.getSequenceFor(state.getAllPlayers()[1]), state.getAllPlayers()[1]);
	}

	protected void addSequenceForPlayer(Sequence sequence, Player player) {
		Set<Sequence> sequencesForPlayer = playerSequences.get(player);

		if (sequencesForPlayer == null) {
			sequencesForPlayer = new HashSet<Sequence>();
			playerSequences.put(player, sequencesForPlayer);
		}
		sequencesForPlayer.add(sequence);
	}

	protected I createInformationSet(GameState state) {
		I infoSet = getInformationSetFor(state);

		if (infoSet == null) {
			infoSet = (I) new SequenceInformationSet(state);
		}
		addInformationSetFor(state, infoSet);
		return infoSet;
	}

	public void setUtility(GameState leaf) {
		final double[] utilities = leaf.getUtilities();
		assert utilities[0] == -utilities[1] : "not a zero-sum game";
		double utility = utilities[0] * leaf.getNatureProbability();
		setUtility(leaf, utility);
	}

	public void setUtility(GameState leaf, double utility) {
		if (actualNonZeroUtilityValuesInLeafs.containsKey(leaf)) {
			assert (actualNonZeroUtilityValuesInLeafs.get(leaf) == utility);
			return; // we have already stored this leaf
		}
		updateAllUtilitiesForSeqComb(leaf, utility);
		if (utility == 0) { // we do not store zero-utility
			return;
		}

		FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
		double existingUtility = utility;

		if (utilityForSequenceCombination.containsKey(activePlayerMap))
			existingUtility += utilityForSequenceCombination.get(activePlayerMap);

		actualNonZeroUtilityValuesInLeafs.put(leaf, utility);
		utilityForSequenceCombination.put(activePlayerMap, existingUtility);
	}

	protected void updateAllUtilitiesForSeqComb(GameState leaf, double utility) {
		FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
		double existingUtility = utility;

		updateProbabilitiesForSeqComb(leaf, activePlayerMap);
		if (utilityForSequenceCombination.containsKey(activePlayerMap))
			existingUtility += utilityForSequenceCombination.get(activePlayerMap);
		allUtilitiesForSeqComb.put(activePlayerMap, existingUtility);
	}

	public void updateProbabilitiesForSeqComb(GameState leaf, FixedSizeMap<Player, Sequence> activePlayerMap) {
		Double oldProb = natureProbabilityForSequenceCombination.get(activePlayerMap);

		if (oldProb == null)
			natureProbabilityForSequenceCombination.put(activePlayerMap, leaf.getNatureProbability());
		else
			natureProbabilityForSequenceCombination.put(activePlayerMap, oldProb + leaf.getNatureProbability());
	}

	public void removeUtility(GameState oldLeaf) {
		Double utility = getActualNonzeroUtilityValues(oldLeaf);

		if (utility == null) {
			return; // leaf not stored
		}

		FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(oldLeaf);

		if (!utilityForSequenceCombination.containsKey(activePlayerMap)) {
			assert false;
			return;
		}
		double existingUtility = utilityForSequenceCombination.get(activePlayerMap) - utility;
		if (Math.abs(existingUtility) < 0.0000001) {
			utilityForSequenceCombination.put(activePlayerMap, 0d);
		} else {
			utilityForSequenceCombination.put(activePlayerMap, existingUtility);
		}
		actualNonZeroUtilityValuesInLeafs.remove(oldLeaf);
	}

	protected FixedSizeMap<Player, Sequence> createActivePlayerMap(GameState leaf) {
		FixedSizeMap<Player, Sequence> activePlayerMap = new FixedSizeMap<Player, Sequence>(2);

		for (int playerIndex = 0; playerIndex < 2; playerIndex++) {
			Player player = leaf.getAllPlayers()[playerIndex];

			activePlayerMap.put(player, leaf.getSequenceFor(player));
		}
		return activePlayerMap;
	}

	public Double getActualNonzeroUtilityValues(GameState leaf) {
		return actualNonZeroUtilityValuesInLeafs.get(leaf);
	}

	public Double getUtilityFor(Map<Player, Sequence> sequenceCombination) {
		return utilityForSequenceCombination.get(sequenceCombination);
	}

	public Double getUtilityFor(Sequence sequence1, Sequence sequence2) {
		return getUtilityFor(getPlayerSequenceMap(sequence1, sequence2));
	}

	public Double getUtilityFromAllFor(Sequence sequence1, Sequence sequence2) {
		return getUtilityFromAllFor(getPlayerSequenceMap(sequence1, sequence2));
	}

	protected Double getUtilityFromAllFor(Map<Player, Sequence> sequenceMap) {
		return allUtilitiesForSeqComb.get(sequenceMap);
	}

	public Double getNatureProbabilityFor(Map<Player, Sequence> sequenceCombination) {
		return natureProbabilityForSequenceCombination.get(sequenceCombination);
	}

	public Double getNatureProbabilityFor(Sequence sequence1, Sequence sequence2) {
		return getNatureProbabilityFor(getPlayerSequenceMap(sequence1, sequence2));
	}

	public Collection<Sequence> getAllSequences() {
		return compatibleSequences.keySet();
	}

	public Set<I> getReachableSets(Sequence sequence) {
		return reachableSetsBySequence.get(sequence);
	}

	public Collection<Sequence> getSequencesFor(Player player) {
		return playerSequences.get(player);
	}

	public Map<Sequence, Set<Sequence>> getCompatibleSequences() {
		return compatibleSequences;
	}

	public Map<Sequence, Set<I>> getReachableSets() {
		return reachableSetsBySequence;
	}

	public int getSizeForPlayer(Player player) {
		return getSequencesFor(player).size();
	}

	public void validateGameStructure(GameState rootState, Expander<SequenceInformationSet> expander) {
		Player player1 = rootState.getAllPlayers()[0];
		Player player2 = rootState.getAllPlayers()[1];

		HashSet<GameState> visitedStates = new HashSet<GameState>();
		HashSet<SequenceInformationSet> visitedISs = new HashSet<SequenceInformationSet>();

		LinkedList<GameState> queue = new LinkedList<GameState>();
		queue.add(rootState);

		while (!queue.isEmpty()) {
			GameState currentState = queue.poll();

			if (!currentState.isPlayerToMoveNature()) {
				visitedStates.add(currentState);
				assert (getAllInformationSets().containsKey(currentState.getISKeyForPlayerToMove()));
				visitedISs.add(getAllInformationSets().get(currentState.getISKeyForPlayerToMove()));
			}

			if (currentState.isGameEnd()) {
				assert (getInformationSetFor(currentState).getOutgoingSequences().size() == 0);
				Double utRes = getActualNonzeroUtilityValues(currentState);
				if (utRes == null) {
					assert ((currentState.getNatureProbability() * currentState.getUtilities()[0]) == 0);
				} else
					assert (utRes == currentState.getNatureProbability() * currentState.getUtilities()[0]);
				continue;
			}

			if (currentState.isPlayerToMoveNature()) { // nature moving
			} else {
				assert (getActualNonzeroUtilityValues(currentState) == null);
				assert (getAllSequences().contains(currentState.getSequenceFor(player1)));
				assert (getAllSequences().contains(currentState.getSequenceFor(player2)));
				assert (getAllInformationSets().containsKey(currentState.getISKeyForPlayerToMove()));
				assert (getAllInformationSets().get(currentState.getISKeyForPlayerToMove()).getAllStates().contains(currentState));
				assert (getAllInformationSets().get(currentState.getISKeyForPlayerToMove()).getOutgoingSequences().size() > 0);
			}

			List<Action> moves = expander.getActions(currentState);
			assert (moves.size() > 0); // there must be moves
			for (Action action : moves) {
				GameState newState = currentState.performAction(action);
				assert (newState != null);
				queue.add(newState);
			}

		}
		assert (visitedISs.size() == getAllInformationSets().values().size());

		HashSet<Sequence> rqSequences = new HashSet<Sequence>();
		for (GameState state : visitedStates) {
			rqSequences.add(state.getSequenceFor(player1));
			rqSequences.add(state.getSequenceFor(player2));
		}
		assert (getAllSequences().size() == rqSequences.size());

	}

	@Override
	public I createInformationSetFor(GameState gameState) {
		return (I) new SequenceInformationSet(gameState);
	}

    protected Map<Player, Sequence> getPlayerSequenceMap(Sequence sequence1, Sequence sequence2) {
        Map<Player, Sequence> sequenceCombination = new HashMap<>(2);

        sequenceCombination.put(sequence1.getPlayer(), sequence1);
        sequenceCombination.put(sequence2.getPlayer(), sequence2);
        return sequenceCombination;
    }
}
