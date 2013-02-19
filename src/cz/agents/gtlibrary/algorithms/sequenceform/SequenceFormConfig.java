package cz.agents.gtlibrary.algorithms.sequenceform;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

public class SequenceFormConfig extends ConfigImpl<SequenceInformationSet> {
	private Map<GameState, Double> actualNonZeroUtilityValuesInLeafs = new HashMap<GameState, Double>();
	private Map<Sequence, Set<SequenceInformationSet>> reachableSetsBySequence = new HashMap<Sequence, Set<SequenceInformationSet>>();
	private Map<Map<Player, Sequence>, Double> utilityForSequenceCombination = new HashMap<Map<Player, Sequence>, Double>();
	private Map<Sequence, Set<Sequence>> compatibleSequences = new HashMap<Sequence, Set<Sequence>>();
	private Map<Player, Set<Sequence>> playerSequences = new HashMap<Player, Set<Sequence>>();

	public void addStateToSequenceForm(GameState state) {
		if (state.isPlayerToMoveNature())
			return;
		setOutgoingSequences(state);
		createInformationSet(state);
		setReachableSetBySequence(state);
		addCompatibleSequence(state);
		addPlayerSequences(state);
	}

	public void addCompatibleSequence(GameState state) {
		Sequence sequenceOfPlrToMove = state.getSequenceForPlayerToMove();
		Sequence opponentSequence = state.getSequenceFor(state.getAllPlayers()[1 - state.getPlayerToMove().getId()]);

		addCompatibleSequence(state, sequenceOfPlrToMove, opponentSequence);
		addCompatibleSequence(state, opponentSequence, sequenceOfPlrToMove);
	}

	private void addCompatibleSequence(GameState state, Sequence sequence1, Sequence sequence2) {
		Set<Sequence> sequences = compatibleSequences.get(sequence1);

		if (sequences == null)
			sequences = new HashSet<Sequence>();
		sequences.add(sequence2);
		this.compatibleSequences.put(sequence1, sequences);
	}

	public Set<Sequence> getCompatibleSequencesFor(Sequence sequence) {
		return compatibleSequences.get(sequence);
	}

	private void setOutgoingSequences(GameState state) {
		addOutgoingSequenceFor(state, state.getAllPlayers()[0]);
		addOutgoingSequenceFor(state, state.getAllPlayers()[1]);
	}

	private void addOutgoingSequenceFor(GameState state, Player player) {
		Sequence sequence = state.getSequenceFor(player);

		if (sequence != null && sequence.size() > 0) {
			SequenceInformationSet prevIS = (SequenceInformationSet) sequence.getLastInformationSet();

			prevIS.addOutgoingSequences(sequence);
		}
	}

	private void setReachableSetBySequence(GameState state) {
		if (state.isGameEnd()) {
			addSetFor(getInformationSetFor(state), state.getSequenceFor(state.getAllPlayers()[0]));
			addSetFor(getInformationSetFor(state), state.getSequenceFor(state.getAllPlayers()[1]));
		} else {
			addSetFor(getInformationSetFor(state), state.getSequenceForPlayerToMove());
		}
	}

	private void addSetFor(SequenceInformationSet set, Sequence sequence) {
		Set<SequenceInformationSet> reachableSets = reachableSetsBySequence.get(sequence);

		if (reachableSets == null) {
			reachableSets = new HashSet<SequenceInformationSet>();
			reachableSetsBySequence.put(sequence, reachableSets);
		}
		reachableSets.add(set);
	}

	private void addPlayerSequences(GameState state) {
		addSequenceForPlayer(state.getSequenceFor(state.getAllPlayers()[0]), state.getAllPlayers()[0]);
		addSequenceForPlayer(state.getSequenceFor(state.getAllPlayers()[1]), state.getAllPlayers()[1]);
	}

	private void addSequenceForPlayer(Sequence sequence, Player player) {
		Set<Sequence> sequencesForPlayer = playerSequences.get(player);

		if (sequencesForPlayer == null) {
			sequencesForPlayer = new HashSet<Sequence>();
			playerSequences.put(player, sequencesForPlayer);
		}
		sequencesForPlayer.add(sequence);
	}

	private SequenceInformationSet createInformationSet(GameState state) {
		SequenceInformationSet infoSet = getInformationSetFor(state);

		if (infoSet == null) {
			infoSet = new SequenceInformationSet(state);
		}
		addInformationSetFor(state, infoSet);
		return infoSet;
	}

	public void setUtility(GameState leaf) {
		double utility = leaf.getUtilities()[0] * leaf.getNatureProbability();

		if (actualNonZeroUtilityValuesInLeafs.containsKey(leaf)) {
			assert (actualNonZeroUtilityValuesInLeafs.get(leaf) == utility);
			return; // we have already stored this leaf
		}

		FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
		double existingUtility = utility;
		if (utilityForSequenceCombination.containsKey(activePlayerMap)) existingUtility += utilityForSequenceCombination.get(activePlayerMap);

		actualNonZeroUtilityValuesInLeafs.put(leaf, utility);
		utilityForSequenceCombination.put(activePlayerMap, existingUtility);
	}

	private FixedSizeMap<Player, Sequence> createActivePlayerMap(GameState leaf) {
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

	public Double getUtilityForSequenceCombination(Map<Player, Sequence> sequenceCombination) {
		return utilityForSequenceCombination.get(sequenceCombination);
	}

	public Collection<Sequence> getAllSequences() {
		return reachableSetsBySequence.keySet();
	}

	public Set<SequenceInformationSet> getReachableSets(Sequence sequence) {
		return reachableSetsBySequence.get(sequence);
	}

	public Collection<Sequence> getSequencesFor(Player player) {
		return playerSequences.get(player);
	}

	public Map<Sequence, Set<Sequence>> getCompatibleSequences() {
		return compatibleSequences;
	}

	public Map<Sequence, Set<SequenceInformationSet>> getReachableSets() {
		return reachableSetsBySequence;
	}

}
