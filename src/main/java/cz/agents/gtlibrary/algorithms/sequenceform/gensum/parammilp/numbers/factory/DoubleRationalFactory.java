package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.Arithmetic;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.DoubleRational;

public class DoubleRationalFactory implements ArithmeticFactory {

    @Override
    public Arithmetic create(int numerator, int denominator) {
        return new DoubleRational((double)numerator / denominator);
    }

    @Override
    public Arithmetic create(long numerator, long denominator) {
        return new DoubleRational((double)numerator / denominator);
    }

    @Override
    public Arithmetic one() {
        return DoubleRational.ONE;
    }

    @Override
    public Arithmetic zero() {
        return DoubleRational.ZERO;
    }
}
