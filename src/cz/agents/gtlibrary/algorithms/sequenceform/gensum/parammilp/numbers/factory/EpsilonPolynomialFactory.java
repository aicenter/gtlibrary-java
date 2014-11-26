package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;

public class EpsilonPolynomialFactory {
    private EpsilonPolynomial one;
    private EpsilonPolynomial zero;

    public EpsilonPolynomialFactory(ArithmeticFactory arithmeticFactory) {
        one = new EpsilonPolynomial(1, 1, arithmeticFactory);
        zero = new EpsilonPolynomial(0, 1, arithmeticFactory);
    }

    public EpsilonPolynomial one() {
        return one;
    }

    public EpsilonPolynomial zero() {
        return zero;
    }

}
