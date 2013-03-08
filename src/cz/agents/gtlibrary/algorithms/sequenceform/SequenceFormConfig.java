package cz.agents.gtlibrary.algorithms.sequenceform;

import java.util.*;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;

public class SequenceFormConfig<I extends SequenceInformationSet> extends ConfigImpl<I> {
	private Map<GameState, Double> actualNonZeroUtilityValuesInLeafs = new HashMap<GameState, Double>();
	private Map<Sequence, Set<I>> reachableSetsBySequence = new HashMap<Sequence, Set<I>>();
	private Map<Map<Player, Sequence>, Double> utilityForSequenceCombination = new HashMap<Map<Player, Sequence>, Double>();
	private Map<Sequence, Set<Sequence>> compatibleSequences = new HashMap<Sequence, Set<Sequence>>();
	private Map<Player, Set<Sequence>> playerSequences = new HashMap<Player, Set<Sequence>>();

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
            if (p.getId() == 2) continue; // no fix necessary for nature
            Sequence s = state.getSequenceFor(p);
            if (s.size() == 0) continue;
            I i = getAllInformationSets().get(new Pair<Integer, Sequence>(s.getLast().getInformationSet().hashCode(), s.getLast().getInformationSet().getPlayersHistory()));
            if (i != null) {         // if there is a particular IS in the algConfig for the previous state, we set it to be the IS in the stored sequences
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
			I prevIS = (I) sequence.getLastInformationSet();

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

	private void addSetFor(I set, Sequence sequence) {
		Set<I> reachableSets = reachableSetsBySequence.get(sequence);

		if (reachableSets == null) {
			reachableSets = new HashSet<I>();
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

	protected I createInformationSet(GameState state) {
		I infoSet = getInformationSetFor(state);

		if (infoSet == null) {
			infoSet = (I)new SequenceInformationSet(state); 
		}
		addInformationSetFor(state, infoSet);
		return infoSet;
	}

	public void setUtility(GameState leaf) {
		double utility = leaf.getUtilities()[0] * leaf.getNatureProbability();
		setUtility(leaf, utility);
	}

	public void setUtility(GameState leaf, double utility) {
		

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
		double 	existingUtility = utilityForSequenceCombination.get(activePlayerMap) - utility;
		if (Math.abs(existingUtility) < 0.0000001) {
            utilityForSequenceCombination.put(activePlayerMap, 0d);
        } else {
            utilityForSequenceCombination.put(activePlayerMap, existingUtility);
        }
		actualNonZeroUtilityValuesInLeafs.remove(oldLeaf);		
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
                } else assert (utRes == currentState.getNatureProbability() * currentState.getUtilities()[0]);
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
}
