package cz.agents.gtlibrary.algorithms.stackelberg.correlated;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.Arrays;

/**
 * Created by Jakub Cerny on 13/10/2017.
 */
public class LeaderGenerationConfig extends StackelbergConfig {


    public LeaderGenerationConfig(GameState rootState) {
        super(rootState);
    }

    @Override
    public SequenceInformationSet getInformationSetFor(GameState gameState) {
        SequenceInformationSet infoSet = super.getInformationSetFor(gameState);
        if (infoSet == null) {
            infoSet = new SequenceInformationSet(gameState);
        }
        return infoSet;
    }

    @Override
    public void setUtility(GameState leaf, Double[] utility) {
//        if (actualNonZeroUtilityValuesInLeafs.containsKey(leaf)) {
//            assert (Arrays.equals(actualNonZeroUtilityValuesInLeafs.get(leaf), utility));
//            return; // we have already stored this leaf
//        }
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
        utilityForSequenceCombination.put(activePlayerMap, existingUtility);
    }

}
