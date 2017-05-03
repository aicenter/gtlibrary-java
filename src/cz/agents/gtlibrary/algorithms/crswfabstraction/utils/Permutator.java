package cz.agents.gtlibrary.algorithms.crswfabstraction.utils;

import java.math.BigInteger;

public interface Permutator {

    boolean hasNext();

    void permute();

    BigInteger getNumberOfPermutations();

    void reset();
}
