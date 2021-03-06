package cz.agents.gtlibrary.algorithms.stackelberg.correlated;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.util.*;

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

    public boolean isStateInConfig(GameState gameState) {
        SequenceInformationSet infoSet = super.getInformationSetFor(gameState);
        return infoSet != null && infoSet.getAllStates().contains(gameState);
    }

    public void setOutgoingSequencesImmediately(GameState state, Expander<SequenceInformationSet> expander){
        SequenceInformationSet set = getInformationSetFor(state);
//        if(isStateInConfig(state)) return;
        if (!set.getOutgoingSequences().isEmpty()) return;
        for(Action a : expander.getActions(state)){
            Sequence seq = new ArrayListSequenceImpl(state.getSequenceForPlayerToMove());
            seq.addLast(a);
            set.addOutgoingSequences(seq);
        }
    }

    @Override
    public void addStateToSequenceForm(GameState state) {
        if (state.isPlayerToMoveNature() || isStateInConfig(state))
            return;
        fixTheInformationSetInSequences(state);
        setOutgoingSequences(state);
        createInformationSet(state);
        setReachableSetBySequence(state);
        addCompatibleSequence(state);
        addPlayerSequences(state);
    }

    @Override
    public void setUtility(GameState leaf, Double[] utility) {
//        if (isStateInConfig(leaf)) return;
//        if (actualNonZeroUtilityValuesInLeafs.containsKey(leaf)) {
//            assert (Arrays.equals(actualNonZeroUtilityValuesInLeafs.get(leaf), utility));
//            return; // we have already stored this leaf
//        }
        if (isZeroForAllPlayers(utility))
            return; // we do not store zero-utility
        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        Double[] existingUtility = utility;

        if (false && utilityForSequenceCombination.containsKey(activePlayerMap)) {
            Double[] storedUV = utilityForSequenceCombination.get(activePlayerMap);

            for (int i = 0; i < existingUtility.length; i++) {
                existingUtility[i] += storedUV[i];
            }
        }
        utilityForSequenceCombination.put(activePlayerMap, existingUtility);
    }

    public Double getActualNonzeroUtilityValues(GameState leaf, Player player) {
        FixedSizeMap<Player, Sequence> activePlayerMap = createActivePlayerMap(leaf);
        return utilityForSequenceCombination.get(activePlayerMap)[player.getId()];
    }

    public void removeSequence(Sequence seq) {
        System.out.println("removing seq: " + seq);
        compatibleSequences.remove(seq);
//        playerSequences.get(seq.getPlayer()).remove(seq);
        if (!playerSequences.get(seq.getPlayer()).remove(seq)) System.out.println("MISSING");
        if (playerSequences.get(seq.getPlayer()).contains(seq)) System.out.println("ERROR!");
//        ((SequenceInformationSet)seq.getLastInformationSet()).getOutgoingSequences().clear();
    }

    public void removeSequencesFrom(GameState state) {
        HashSet<Map> outcomesToDelete = new HashSet<>();
        for (Sequence seq : getInformationSetFor(state).getOutgoingSequences()) {
//            System.out.println("removing seq: " + seq + " #= " + seq.hashCode());
            for (Sequence sseq : compatibleSequences.keySet())
                compatibleSequences.get(sseq).remove(seq);
            compatibleSequences.remove(seq);
            if (!playerSequences.get(seq.getPlayer()).remove(seq)) System.out.println("MISSING");
            if (playerSequences.get(seq.getPlayer()).contains(seq)) System.out.println("ERROR!");
//            if (playerSequences.size() == )
            for (Map<Player, Sequence> map : utilityForSequenceCombination.keySet())
                if (map.get(seq.getPlayer()).equals(seq))
                    outcomesToDelete.add(map);
            reachableSetsBySequence.remove(seq);
        }
        for(Map map : outcomesToDelete)
            utilityForSequenceCombination.remove(map);
        getInformationSetFor(state).getOutgoingSequences().clear();
    }

    public Collection<Sequence> getSequencesFor(Player player) {
        return playerSequences.get(player);
    }

}
