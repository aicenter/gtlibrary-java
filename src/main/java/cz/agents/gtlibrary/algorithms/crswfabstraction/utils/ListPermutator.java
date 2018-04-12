package cz.agents.gtlibrary.algorithms.crswfabstraction.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListPermutator<I> implements Permutator{

    private List<I> list;

    private int[] counters;
    private int N;
    private int counterSum;
    private final int endSum;

    public ListPermutator(List<I> list) {
        this.list = list;
        counters = new int[list.size() + 1];
        N = list.size() - 1;
        counterSum = 0;
        endSum = list.size()*(list.size() - 1)/2;
    }

    public boolean hasNext() {
        return counterSum != endSum;
    }

    public void permute() {
        int n = 0;
        while (counters[n] >= n) {
            counterSum -= counters[n];
            counters[n] = 0;
            n += 1;
        }
        if (n <= N) {
            if (n % 2 == 1) {
                I tmp = list.get(counters[n]);
                list.set(counters[n], list.get(n));
                list.set(n, tmp);
            } else {
                I tmp = list.get(0);
                list.set(0, list.get(n));
                list.set(n, tmp);
            }
        }
        counters[n] += 1;
        counterSum += 1;
    }

    public void reset() {
        for (int i = 0; i < counters.length; i++) {
            counters[i] = 0;
        }
        counterSum = 0;
    }

    @Override
    public String toString() {
        return "ListPermutator{" +
                "list=" + list +
                ", counters=" + Arrays.toString(counters) +
                ", N=" + N +
                '}';
    }

    public BigInteger getNumberOfPermutations() {
        BigInteger factorial = BigInteger.ONE;
        for (int i = 1; i <= list.size(); i++) {
            factorial = factorial.multiply(BigInteger.valueOf(i));
        }
        return factorial;
    }

    public static void main(String[] args) {
        List<Integer> list = asList(1, 2, 3);
        ListPermutator<Integer> permutator = new ListPermutator<>(list);
        System.out.println("Number of permutations is " + permutator.getNumberOfPermutations());
        int permNumber = 0;
        System.out.println(permNumber++);
        System.out.println("\t" + list);
        while (permutator.hasNext()) {
            permutator.permute();
            System.out.println(permNumber++);
            System.out.println("\t" + list);
        }
    }

    public static List<Integer> asList(int... arr) {
        List<Integer> list = new ArrayList<>(arr.length);
        for (int i : arr) list.add(i);
        return list;
    }
}
