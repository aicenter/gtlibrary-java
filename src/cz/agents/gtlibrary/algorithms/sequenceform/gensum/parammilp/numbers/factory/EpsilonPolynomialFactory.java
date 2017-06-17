package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.Arithmetic;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;

import java.util.Map;

public class EpsilonPolynomialFactory {
    private EpsilonPolynomial one;
    private EpsilonPolynomial zero;
    private EpsilonPolynomial oneNeg;
    private EpsilonPolynomial M;
    private ArithmeticFactory arithmeticFactory;

    public EpsilonPolynomialFactory(ArithmeticFactory arithmeticFactory) {
        this.arithmeticFactory = arithmeticFactory;
        one = new EpsilonPolynomial(1, 1, this);
        zero = new EpsilonPolynomial(0, 1, this);
        oneNeg = one.negate();
        M = new EpsilonPolynomial(1000000, 1, this);
    }

    public EpsilonPolynomial create(int value) {
        return new EpsilonPolynomial(value, 1, this);
    }

    public EpsilonPolynomial create(Map<Integer, Integer> coefMap, int maxCoef) {
        Arithmetic[] polynomial = new Arithmetic[maxCoef + 1];

        for (int i = 0; i < polynomial.length; i++) {
                polynomial[i] = arithmeticFactory.zero();
        }
        for (Map.Entry<Integer, Integer> coefEntry : coefMap.entrySet()) {
            polynomial[coefEntry.getKey()] = arithmeticFactory.create(coefEntry.getValue(), 1);
        }
        return create(polynomial);
//        Integer reward = coefMap.get(0);
//
//        return create(reward == null ? 0 : reward);
    }

    public EpsilonPolynomial create(double value) {
        assert Math.abs(value - Math.round(value)) < 1e-8;
        return new EpsilonPolynomial(Math.round(value), 1, this);
    }

    public EpsilonPolynomial create(int nominator, int denominator) {
        return new EpsilonPolynomial(nominator, denominator, this);
    }

    public EpsilonPolynomial create(Arithmetic[] polynomial) {
        return new EpsilonPolynomial(polynomial, this);
    }

    public EpsilonPolynomial create(Arithmetic arithmetic) {
        return new EpsilonPolynomial(new Arithmetic[]{arithmetic}, this);
    }

    public EpsilonPolynomial one() {
        return one;
    }

    public EpsilonPolynomial zero() {
        return zero;
    }

    public EpsilonPolynomial oneNeg() {
        return oneNeg;
    }

    public EpsilonPolynomial bigM() {
        return M;
    }

    public ArithmeticFactory getArithmeticFactory() {
        return arithmeticFactory;
    }


}
