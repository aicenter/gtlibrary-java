package cz.agents.gtlibrary.algorithms.crswfabstraction.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Uses Heaps algorithm (see e.g. Wikipedia)
 */
public class Permutations<I> implements Iterable<List<I>> {

    private List<I> originalList;

    public Permutations(List<I> list) {
        this.originalList = list;
    }

    @Override
    public Iterator<List<I>> iterator() {
        return new PermutationIterator(originalList);
    }

    private class PermutationIterator implements Iterator<List<I>> {

        private List<I> currentPermutation;

        private int[] counters;
        private int N;

        private PermutationIterator(List<I> list) {
            currentPermutation = new ArrayList<>(list);
            counters = new int[currentPermutation.size() + 1];
            N = currentPermutation.size() - 1;
        }

        @Override
        public boolean hasNext() {
            return counters[N + 1] == 0;
        }

        @Override
        public List<I> next() {
            List<I> nextPermutation = new ArrayList<>(currentPermutation);
            int n = 1;
            while (counters[n] >= n) {
                counters[n] = 0;
                n += 1;
            }
            if (n <= N) {
                if (n % 2 == 1) {
                    I tmp = currentPermutation.get(counters[n]);
                    currentPermutation.set(counters[n], currentPermutation.get(n));
                    currentPermutation.set(n, tmp);
                } else {
                    I tmp = currentPermutation.get(0);
                    currentPermutation.set(0, currentPermutation.get(n));
                    currentPermutation.set(n, tmp);
                }
            }
            counters[n] += 1;
            return nextPermutation;
        }
    }
}
