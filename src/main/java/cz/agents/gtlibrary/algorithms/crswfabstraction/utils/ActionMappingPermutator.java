package cz.agents.gtlibrary.algorithms.crswfabstraction.utils;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;

import javax.sound.midi.MidiDevice;
import java.math.BigInteger;
import java.util.*;

public class ActionMappingPermutator implements Permutator {

    List<Permutator> permutators;
    List<InformationSet> sets;
    private boolean hasNext;
    private int lastPermutationIndex;

    public ActionMappingPermutator(Set<InformationSet> partition, Map<InformationSet, List<List<Action>>> actionMapping) {
        permutators = new ArrayList<>();
        sets = new ArrayList<>(partition.size() - 1);
        Iterator<InformationSet> iterator = partition.iterator();
        iterator.next(); //Do not permute first
        while (iterator.hasNext()) {
            InformationSet informationSet = iterator.next();
            permutators.add(new MappingPermutator<>(actionMapping.get(informationSet)));
            sets.add(informationSet);
        }
        hasNext = true;
        lastPermutationIndex = -1;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public void permute() {
        int i = permutators.size() - 1;
        while (i >= 0 && !permutators.get(i).hasNext()) {
            permutators.get(i).reset();
            i -= 1;
        }
        if (i < 0) {
            hasNext = false;
        } else {
            lastPermutationIndex = i;
            permutators.get(i).permute();
        }
    }

    @Override
    public BigInteger getNumberOfPermutations() {
        BigInteger numberOfPermutations = BigInteger.ONE;
        for (Permutator permutator : permutators) {
            numberOfPermutations = numberOfPermutations.multiply(permutator.getNumberOfPermutations());
        }
        return numberOfPermutations;
    }

    @Override
    public void reset() {
        for (Permutator permutator : permutators) permutator.reset();
    }

    public InformationSet getLastPermutedSet() {
        return sets.get(lastPermutationIndex);
    }
}
