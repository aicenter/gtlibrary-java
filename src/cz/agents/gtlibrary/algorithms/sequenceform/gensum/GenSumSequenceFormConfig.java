package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GenSumSequenceFormConfig extends SequenceFormConfig<SequenceInformationSet> {
    protected Map<Map<Player, Sequence>, Double[]> utilityForSequenceCombination;
    protected Map<GameState, Double[]> actualNonZeroUtilityValuesInLeafs;

    public GenSumSequenceFormConfig() {
        super();
        utilityForSequenceCombination = new HashMap<>();
        actualNonZeroUtilityValuesInLeafs = new HashMap<>();
    }

    public void setUtility(GameState leaf) {
        final double[] utilities = leaf.getUtilities();
        Double[] u = new Double[utilities.length];

        for (Player p : leaf.getAllPlayers())
            u[p.getId()] = utilities[p.getId()] * leaf.getNatureProbability();
        setUtility(leaf, u);
    }

    public void setUtility(GameState leaf, Double[] utility) {
        if (actualNonZeroUtilityValuesInLeafs.containsKey(leaf)) {
            assert (Arrays.equals(actualNonZeroUtilityValuesInLeafs.get(leaf), utility));
            return; // we have already stored this leaf
        }
        if (isZeroForAllPlayers(utility))
            return; // we do not store zero-utility
        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        Double[] existingUtility = utility;

        if (utilityForSequenceCombination.containsKey(activePlayerMap)) {
            Double[] storedUV = utilityForSequenceCombination.get(activePlayerMap);

            for (int i = 0; i < existingUtility.length; i++) {
                existingUtility[i] += storedUV[i];
            }
        }
        actualNonZeroUtilityValuesInLeafs.put(leaf, utility);
        utilityForSequenceCombination.put(activePlayerMap, existingUtility);
    }

    private boolean isZeroForAllPlayers(Double[] utilities) {
        for (Double utility : utilities) {
            if (utility != 0)
                return false;
        }
        return true;
    }

    public Double getActualNonzeroUtilityValues(GameState leaf, Player player) {
        return actualNonZeroUtilityValuesInLeafs.get(leaf)[player.getId()];
    }

    public Double getUtilityFor(Map<Player, Sequence> sequenceCombination, Player player) {
        if (!utilityForSequenceCombination.containsKey(sequenceCombination))
            return null;
        return utilityForSequenceCombination.get(sequenceCombination)[player.getId()];
    }

    public Double getUtilityFor(Sequence sequence1, Sequence sequence2, Player player) {
        Map<Player, Sequence> sequenceCombination = new HashMap<>(2);

        sequenceCombination.put(sequence1.getPlayer(), sequence1);
        sequenceCombination.put(sequence2.getPlayer(), sequence2);
        return getUtilityFor(sequenceCombination, player);
    }

    public Map<GameState, Double[]> getActualNonZeroUtilityValuesInLeafsGenSum() {
        return actualNonZeroUtilityValuesInLeafs;
    }

    public Map<Map<Player, Sequence>, Double[]> getUtilityForSequenceCombinationGenSum() {
        return utilityForSequenceCombination;
    }
}
