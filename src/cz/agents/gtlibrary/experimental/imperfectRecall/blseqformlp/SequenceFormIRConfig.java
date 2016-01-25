package cz.agents.gtlibrary.experimental.imperfectRecall.blseqformlp;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.*;

public class SequenceFormIRConfig extends ConfigImpl<SequenceFormIRInformationSet> {
    protected Map<GameState, Double> actualNonZeroUtilityValuesInLeafs = new HashMap<>();
    protected Map<Sequence, Set<SequenceFormIRInformationSet>> reachableSetsBySequence = new HashMap<>();
    protected Map<Map<Player, Sequence>, Double> utilityForSequenceCombination = new HashMap<>();
    protected Map<Map<Player, Sequence>, Double> natureProbabilityForSequenceCombination = new HashMap<>();
    protected Map<Sequence, Set<Sequence>> compatibleSequences = new HashMap<>();
    protected Map<Player, Set<Sequence>> playerSequences = new HashMap<>();

    private Set<GameState> terminalStates = new HashSet<>();

    @Override
    public SequenceFormIRInformationSet createInformationSetFor(GameState gameState) {
        return new SequenceFormIRInformationSet(gameState);
    }

    public void addInformationSetFor(GameState state) {
        if (state.isGameEnd()) {
            setUtility(state);
            terminalStates.add(state);
        } else
            super.addInformationSetFor(state);
        if (state.isPlayerToMoveNature())
            return;
        fixTheInformationSetInSequences(state);
        setOutgoingSequences(state);
        getOrCreateInformationSet(state);
        setReachableSetBySequence(state);
        addCompatibleSequence(state);
        addPlayerSequences(state);
    }

    public void fixTheInformationSetInSequences(GameState state) {
        for (Player player : state.getAllPlayers()) {
            if (player.getId() == 2)
                continue; // no fix necessary for nature
            Sequence sequence = state.getSequenceFor(player);

            if (sequence.size() == 0)
                continue;
            SequenceFormIRInformationSet informationSet = getAllInformationSets().get(sequence.getLastInformationSet().getISKey());

            if (informationSet != null) { // if there is a particular IS in the algConfig for the previous state, we set it to be the IS in the stored sequences
                Set<GameState> oldStates = sequence.getLast().getInformationSet().getAllStates();

                informationSet.addAllStatesToIS(oldStates);
                sequence.getLast().setInformationSet(informationSet);
            } else {
                System.out.print("");
            }
        }
    }

    public void addCompatibleSequence(GameState state) {
        Sequence sequenceOfPlayerToMove = state.getSequenceForPlayerToMove();
        Sequence opponentSequence = state.getSequenceFor(state.getAllPlayers()[1 - state.getPlayerToMove().getId()]);

        addCompatibleSequence(sequenceOfPlayerToMove, opponentSequence);
        addCompatibleSequence(opponentSequence, sequenceOfPlayerToMove);
    }

    protected void addCompatibleSequence(Sequence sequence1, Sequence sequence2) {
        Set<Sequence> sequences = compatibleSequences.get(sequence1);

        if (sequences == null)
            sequences = new HashSet<>();
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
            SequenceFormIRInformationSet prevIS = (SequenceFormIRInformationSet) sequence.getLastInformationSet();

            prevIS.addOutgoingSequenceFor(sequence.getSubSequence(sequence.size() - 1), sequence);
        }
    }

    protected void setReachableSetBySequence(GameState state) {
        if (state.isGameEnd()) {
            addSetFor(getInformationSetFor(state), state.getSequenceFor(state.getAllPlayers()[0]));
            addSetFor(getInformationSetFor(state), state.getSequenceFor(state.getAllPlayers()[1]));
        } else {
            addSetFor(getInformationSetFor(state), state.getSequenceForPlayerToMove());
        }
    }

    protected void addSetFor(SequenceFormIRInformationSet set, Sequence sequence) {
        Set<SequenceFormIRInformationSet> reachableSets = reachableSetsBySequence.get(sequence);

        if (reachableSets == null) {
            reachableSets = new HashSet<>();
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
            sequencesForPlayer = new HashSet<>();
            playerSequences.put(player, sequencesForPlayer);
        }
        sequencesForPlayer.add(sequence);
    }

    protected SequenceFormIRInformationSet getOrCreateInformationSet(GameState state) {
        SequenceFormIRInformationSet infoSet = getInformationSetFor(state);

        if (infoSet == null) {
            infoSet = createInformationSetFor(state);
        } else if (!state.isGameEnd() && !infoSet.isHasIR()) {
            Player plToMove = infoSet.getPlayer();
            Sequence currentHistory = state.getHistory().getSequenceOf(plToMove);
            for (GameState existingStates : infoSet.getAllStates()) {
                if (existingStates.isGameEnd()) continue;
                if (!existingStates.getHistory().getSequenceOf(plToMove).equals(currentHistory)) {
                    infoSet.setHasIR(true);
                    break;
                }
            }
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
        if (utility == 0) // we do not store zero-utility
            return;
        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        double existingUtility = utility;

        if (utilityForSequenceCombination.containsKey(activePlayerMap))
            existingUtility += utilityForSequenceCombination.get(activePlayerMap);

        actualNonZeroUtilityValuesInLeafs.put(leaf, utility);
        utilityForSequenceCombination.put(activePlayerMap, existingUtility);
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

    public Double getNatureProbabilityFor(Map<Player, Sequence> sequenceCombination) {
        return natureProbabilityForSequenceCombination.get(sequenceCombination);
    }

    public Double getNatureProbabilityFor(Sequence sequence1, Sequence sequence2) {
        return getNatureProbabilityFor(getPlayerSequenceMap(sequence1, sequence2));
    }

    public Collection<Sequence> getAllSequences() {
        return compatibleSequences.keySet();
    }

    public Set<SequenceFormIRInformationSet> getReachableSets(Sequence sequence) {
        return reachableSetsBySequence.get(sequence);
    }

    public Collection<Sequence> getSequencesFor(Player player) {
        return playerSequences.get(player);
    }

    public Map<Sequence, Set<Sequence>> getCompatibleSequences() {
        return compatibleSequences;
    }

    public Map<Sequence, Set<SequenceFormIRInformationSet>> getReachableSets() {
        return reachableSetsBySequence;
    }

    public int getSizeForPlayer(Player player) {
        return getSequencesFor(player).size();
    }

    protected Map<Player, Sequence> getPlayerSequenceMap(Sequence sequence1, Sequence sequence2) {
        Map<Player, Sequence> sequenceCombination = new HashMap<>(2);

        sequenceCombination.put(sequence1.getPlayer(), sequence1);
        sequenceCombination.put(sequence2.getPlayer(), sequence2);
        return sequenceCombination;
    }

    public Set<GameState> getTerminalStates() {
        return terminalStates;
    }
}
