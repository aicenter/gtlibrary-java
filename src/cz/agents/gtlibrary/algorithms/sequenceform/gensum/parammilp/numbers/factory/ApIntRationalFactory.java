package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.ApIntRational;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.Arithmetic;

public class ApIntRationalFactory implements ArithmeticFactory {

    @Override
    public Arithmetic create(int numerator, int denominator) {
        return new ApIntRational(numerator, denominator);
    }

    @Override
    public Arithmetic create(long numerator, long denominator) {
        return new ApIntRational(numerator, denominator);
    }

    @Override
    public Arithmetic one() {
        return ApIntRational.ONE;
    }

    @Override
    public Arithmetic zero() {
        return ApIntRational.ZERO;
    }
}
