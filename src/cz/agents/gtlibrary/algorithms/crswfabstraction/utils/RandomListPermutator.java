package cz.agents.gtlibrary.algorithms.crswfabstraction.utils;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;

public class RandomListPermutator<V> implements Permutator {

    private List<V> list;
    private int capacity;
    private int currentPermutation;

    public RandomListPermutator(List<V> list, int capacity) {
        this.list = list;
        this.capacity = capacity;
        this.currentPermutation = 0;
    }

    @Override
    public boolean hasNext() {
        return currentPermutation < capacity;
    }

    @Override
    public void permute() {
        Collections.shuffle(list);
        currentPermutation += 1;
    }

    @Override
    public BigInteger getNumberOfPermutations() {
        return BigInteger.valueOf(capacity);
    }

    public void reset() {
        currentPermutation = 0;
    }
}
