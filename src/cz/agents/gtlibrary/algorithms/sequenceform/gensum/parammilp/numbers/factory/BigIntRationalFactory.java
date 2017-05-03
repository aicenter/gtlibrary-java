package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.Arithmetic;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.BigIntRational;

public class BigIntRationalFactory implements ArithmeticFactory {

    @Override
    public Arithmetic create(int numerator, int denominator) {
        return new BigIntRational(numerator, denominator);
    }

    @Override
    public Arithmetic create(long numerator, long denominator) {
        return new BigIntRational(numerator, denominator);
    }

    @Override
    public Arithmetic one() {
        return BigIntRational.ONE;
    }

    @Override
    public Arithmetic zero() {
        return BigIntRational.ZERO;
    }
}
