package cz.agents.gtlibrary.algorithms.crswfabstraction.utils;

import java.math.BigInteger;
import java.util.*;

public class MappingPermutator<V> implements Permutator {

    private List<ListPermutator<V>> permutators;
    private boolean hasNext;

    public MappingPermutator(List<List<V>> mapping) {
        permutators = new ArrayList<>(mapping.size());
        for (List<V> list : mapping) {
            permutators.add(new ListPermutator<>(list));
        }
        hasNext = true;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public void permute() {
        int i = permutators.size() - 1;
        while (i >= 0 && !permutators.get(i).hasNext()) {
            permutators.get(i).reset();
            i -= 1;
        }
        if (i < 0) {
            hasNext = false;
        } else {
            permutators.get(i).permute();
        }
    }

    public BigInteger getNumberOfPermutations() {
        BigInteger numberOfPermutations = BigInteger.ONE;
        for (ListPermutator<V> permutator : permutators) {
            numberOfPermutations = numberOfPermutations.multiply(permutator.getNumberOfPermutations());
        }
        return numberOfPermutations;
    }

    public static void main(String[] args) {
        List<List<Integer>> mapping = new ArrayList<>();
        mapping.add(asList(1, 2));
        mapping.add(asList(1, 2, 3));
        MappingPermutator<Integer> permutator = new MappingPermutator<>(mapping);
        System.out.println("Number of permutations is " + permutator.getNumberOfPermutations());
        int permNumber = 0;
        while (permutator.hasNext()) {
            System.out.println(permNumber++ + "\t" + mapping);
            permutator.permute();
        }
    }

    public static List<Integer> asList(int... arr) {
        List<Integer> list = new ArrayList<>(arr.length);
        for (int i : arr) list.add(i);
        return list;
    }

    public void reset() {
        permutators.forEach(ListPermutator::reset);
    }
}
