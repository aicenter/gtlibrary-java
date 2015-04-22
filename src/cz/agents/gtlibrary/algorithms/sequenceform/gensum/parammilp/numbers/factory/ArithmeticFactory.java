package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.Arithmetic;

public interface ArithmeticFactory {
    public Arithmetic create(int numerator, int denominator);
    public Arithmetic create(long numerator, long denominator);
    public Arithmetic one();
    public Arithmetic zero();
}
