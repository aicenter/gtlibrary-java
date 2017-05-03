package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp;

import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.*;
import java.util.stream.Collectors;

public class SequenceFormIRConfig extends ConfigImpl<SequenceFormIRInformationSet> {
    protected Map<GameState, Double> actualUtilityValuesInLeafs = new HashMap<>();
    protected Map<Sequence, Set<SequenceFormIRInformationSet>> reachableSetsBySequence = new HashMap<>();
    protected Map<Map<Player, Sequence>, Double> utilityForSequenceCombination = new HashMap<>();
    protected Map<Map<Player, Sequence>, Double> natureProbabilityForSequenceCombination = new HashMap<>();
    protected Map<Sequence, Set<Sequence>> compatibleSequences = new HashMap<>();
    protected Map<Player, Set<Sequence>> playerSequences = new HashMap<>();

    protected Set<GameState> terminalStates = new HashSet<>();
    protected Map<Sequence, Double> p1HighestReachableUtility = new HashMap<>();
    protected Map<Sequence, Double> p2HighestReachableUtility = new HashMap<>();
    protected Map<Sequence, Double> p1LowestReachableUtility = new HashMap<>();
    protected Map<Sequence, Double> p2LowestReachableUtility = new HashMap<>();

    protected int[] countIS = {0, 0};
    protected boolean player2IR = false;

    protected GameInfo gameInfo;

    public SequenceFormIRConfig(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }

    @Override
    public SequenceFormIRInformationSet createInformationSetFor(GameState gameState) {
        return new SequenceFormIRInformationSet(gameState);
    }

    public void addInformationSetFor(GameState state) {
        if (state.isGameEnd()) {
            setUtility(state);
            updateHighestReachableUtilities(state);
            updateLowestReachableUtilities(state);
            terminalStates.add(state);
        } else {
            super.addInformationSetFor(state);
        }
        getOrCreateInformationSet(state);
        fixTheInformationSetInSequences(state);
        setOutgoingSequences(state);
        if (state.isPlayerToMoveNature())
            return;
        setReachableSetBySequence(state);
        addCompatibleSequence(state);
        addPlayerSequences(state);
    }

    protected void updateHighestReachableUtilities(GameState state) {
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[0]).getAllPrefixes()) {
            Double currentValue = p1HighestReachableUtility.get(prefix);
            double p1Utility = state.getUtilities()[0];

            if (currentValue == null || currentValue < p1Utility)
                currentValue = p1Utility;
            p1HighestReachableUtility.put(prefix, currentValue);
        }
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[1]).getAllPrefixes()) {
            Double currentValue = p2HighestReachableUtility.get(prefix);
            double p2Utility = state.getUtilities()[1];

            if (currentValue == null || currentValue < p2Utility)
                currentValue = p2Utility;
            p2HighestReachableUtility.put(prefix, currentValue);
        }
    }

    protected void updateLowestReachableUtilities(GameState state) {
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[0]).getAllPrefixes()) {
            Double currentValue = p1LowestReachableUtility.get(prefix);
            double p1Utility = state.getUtilities()[0];

            if (currentValue == null || currentValue > p1Utility)
                currentValue = p1Utility;
            p1LowestReachableUtility.put(prefix, currentValue);
        }
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[1]).getAllPrefixes()) {
            Double currentValue = p2LowestReachableUtility.get(prefix);
            double p2Utility = state.getUtilities()[1];

            if (currentValue == null || currentValue > p2Utility)
                currentValue = p2Utility;
            p2LowestReachableUtility.put(prefix, currentValue);
        }
    }

    protected void updateP1LowestReachableUtilitiesFromActualUtilities(GameState state) {
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[0]).getAllPrefixes()) {
            Double p1Utility = actualUtilityValuesInLeafs.get(state);

            if (p1Utility == null)
                continue;
            Double currentValue = p1LowestReachableUtility.get(prefix);

            if (currentValue == null || currentValue > p1Utility)
                currentValue = p1Utility;
            p1LowestReachableUtility.put(prefix, currentValue);
        }
    }

    protected void updateP1HighestReachableUtilitiesFromActualUtilities(GameState state) {
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[0]).getAllPrefixes()) {
            Double p1Utility = actualUtilityValuesInLeafs.get(state);

            if (p1Utility == null)
                continue;
            Double currentValue = p1HighestReachableUtility.get(prefix);

            if (currentValue == null || currentValue < p1Utility)
                currentValue = p1Utility;
            p1HighestReachableUtility.put(prefix, currentValue);
        }
    }

    protected void updateP2LowestReachableUtilitiesFromActualUtilities(GameState state) {
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[1]).getAllPrefixes()) {
            Double p2Utility = -actualUtilityValuesInLeafs.get(state);

            if (p2Utility == null)
                continue;
            Double currentValue = p2LowestReachableUtility.get(prefix);

            if (currentValue == null || currentValue > p2Utility)
                currentValue = p2Utility;
            p2LowestReachableUtility.put(prefix, currentValue);
        }
    }

    protected void updateP2HighestReachableUtilitiesFromActualUtilities(GameState state) {
        for (Sequence prefix : state.getSequenceFor(state.getAllPlayers()[1]).getAllPrefixes()) {
            Double p2Utility = actualUtilityValuesInLeafs.get(state);

            if (p2Utility == null)
                continue;
            Double currentValue = p2HighestReachableUtility.get(prefix);

            if (currentValue == null || currentValue < p2Utility)
                currentValue = p2Utility;
            p2HighestReachableUtility.put(prefix, currentValue);
        }
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
//                Set<GameState> oldStates = sequence.getLast().getInformationSet().getAllStates();
//
//                informationSet.addAllStatesToIS(oldStates);
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
            SequenceFormIRInformationSet prevIS = allInformationSets.get(sequence.getLastInformationSet().getISKey());

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
//            countIS[state.getPlayerToMove().getId()]++;
        } else if (!state.isGameEnd() && !infoSet.hasIR()) {
            Player plToMove = infoSet.getPlayer();
            Sequence currentHistory = state.getHistory().getSequenceOf(plToMove);
            for (GameState existingStates : infoSet.getAllStates()) {
                if (existingStates.isGameEnd()) continue;
                if (!existingStates.getHistory().getSequenceOf(plToMove).equals(currentHistory)) {
                    if (plToMove.getId() == 1) player2IR = true;
                    break;
                }
            }
        }
        addInformationSetFor(state, infoSet);
        return infoSet;
    }

    public void setUtility(GameState leaf) {
        final double[] utilities = leaf.getUtilities();

//        assert utilities[0] == -utilities[1] : "not a zero-sum game";
        double utility = utilities[0] * leaf.getNatureProbability();
        setUtility(leaf, utility);
    }

    public void setUtility(GameState leaf, double utility) {
        if (actualUtilityValuesInLeafs.containsKey(leaf)) {
            assert (actualUtilityValuesInLeafs.get(leaf) == utility);
            return; // we have already stored this leaf
        }
//        if (utility == 0) // we do not store zero-utility
//            return;
        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        double existingUtility = utility;

        if (utilityForSequenceCombination.containsKey(activePlayerMap))
            existingUtility += utilityForSequenceCombination.get(activePlayerMap);

        actualUtilityValuesInLeafs.put(leaf, utility);
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
        actualUtilityValuesInLeafs.remove(oldLeaf);
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
        return actualUtilityValuesInLeafs.get(leaf);
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
        return playerSequences.values().stream().flatMap(v -> v.stream()).collect(Collectors.toSet());
//        return compatibleSequences.keySet();
    }

    public Set<SequenceFormIRInformationSet> getReachableSets(Sequence sequence) {
        return reachableSetsBySequence.getOrDefault(sequence, Collections.emptySet());
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

    public int getCountIS(int playerID) {
        return countIS[playerID];
    }

    public boolean isPlayer2IR() {
        return player2IR;
    }

    public double getHighestReachableUtilityFor(Sequence sequence) {
        assert sequence.getPlayer().getId() != 2;
        if (sequence.getPlayer().getId() == 0)
            return p1HighestReachableUtility.getOrDefault(sequence, gameInfo.getMaxUtility());
        return p2HighestReachableUtility.getOrDefault(sequence, gameInfo.getMaxUtility());
    }

    public double getLowestReachableUtilityFor(Sequence sequence) {
        assert sequence.getPlayer().getId() != 2;
        if (sequence.getPlayer().getId() == 0)
            return p1LowestReachableUtility.getOrDefault(sequence, -gameInfo.getMaxUtility());
        return p2LowestReachableUtility.getOrDefault(sequence, -gameInfo.getMaxUtility());
    }

    public Map<GameState, Double> getActualUtilityValuesInLeafs() {
        return actualUtilityValuesInLeafs;
    }

    public void updateUtilitiesReachableBySequences() {
        p1HighestReachableUtility.clear();
        p1LowestReachableUtility.clear();
        p2HighestReachableUtility.clear();
        p2LowestReachableUtility.clear();

        for (GameState terminalState : terminalStates) {
            updateP1HighestReachableUtilitiesFromActualUtilities(terminalState);
            updateP1LowestReachableUtilitiesFromActualUtilities(terminalState);
            updateP2HighestReachableUtilitiesFromActualUtilities(terminalState);
            updateP2LowestReachableUtilitiesFromActualUtilities(terminalState);
        }
    }
}
