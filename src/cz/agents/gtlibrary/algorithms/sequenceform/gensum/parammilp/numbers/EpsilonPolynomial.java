package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.EpsilonPolynomialFactory;

import java.util.Arrays;

public class EpsilonPolynomial implements Comparable<EpsilonPolynomial> {

    private Arithmetic[] polynomial;
    private EpsilonPolynomialFactory factory;

    public EpsilonPolynomial(int nominator, int denominator, EpsilonPolynomialFactory factory) {
        this.polynomial = new Arithmetic[]{factory.getArithmeticFactory().create(nominator, denominator)};
        this.factory = factory;
    }

    public EpsilonPolynomial(long nominator, long denominator, EpsilonPolynomialFactory factory) {
        this.polynomial = new Arithmetic[]{factory.getArithmeticFactory().create(nominator, denominator)};
        this.factory = factory;
    }

    public EpsilonPolynomial(Arithmetic[] polynomial, EpsilonPolynomialFactory factory) {
        this.polynomial = polynomial;
        this.factory = factory;
    }

    public EpsilonPolynomial(int[] numerators, int[] denominators, EpsilonPolynomialFactory factory) {
        polynomial = new Arithmetic[numerators.length];
        for (int i = 0; i < numerators.length; i++) {
            polynomial[i] = factory.getArithmeticFactory().create(numerators[i], denominators[i]);
        }
        this.factory = factory;
    }

    public EpsilonPolynomial multiply(EpsilonPolynomial y) {
        assert y.polynomial.length == 1;
        if (y.isOne())
            return this;
        if (isOne())
            return y;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].multiply(y.polynomial[0]);
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial divide(EpsilonPolynomial y) {
        assert y.polynomial.length == 1;
        if (isOne())
            return this;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].divide(y.polynomial[0]);
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial add(EpsilonPolynomial y) {
        if (polynomial.length < y.polynomial.length)
            adjustPolynomial(y);
        if (y.isZero())
            return this;
        if (isZero())
            return y;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < y.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].add(y.polynomial[i]);
        }
        for (int i = y.polynomial.length; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i];
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial subtract(EpsilonPolynomial y) {
        if (polynomial.length < y.polynomial.length)
            adjustPolynomial(y);
        if (isZero())
            return this;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < y.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].subtract(y.polynomial[i]);
        }
        for (int i = y.polynomial.length; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i];
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    private void adjustPolynomial(EpsilonPolynomial y) {
        Arithmetic[] newPolynomial = new Arithmetic[y.polynomial.length];

        for (int i = 0; i < polynomial.length; i++) {
            newPolynomial[i] = polynomial[i];
        }
        for (int i = polynomial.length; i < y.polynomial.length; i++) {
            newPolynomial[i] = factory.getArithmeticFactory().zero();
        }
        polynomial = newPolynomial;
    }

    public EpsilonPolynomial negate() {
        if (isZero())
            return this;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].negate();
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

//    public void multiplyThis(EpsilonPolynomial y) {
//        assert y.polynomial.length == 1;
//        for (int i = 0; i < this.polynomial.length; i++) {
//            polynomial[i].multiplyThis(y.polynomial[0]);
//        }
//    }
//
//    public void divideThis(EpsilonPolynomial y) {
//        assert y.polynomial.length == 1;
//        for (int i = 0; i < this.polynomial.length; i++) {
//            polynomial[i].divideThis(y.polynomial[0]);
//        }
//    }
//
//    public void addToThis(EpsilonPolynomial y) {
//        assert this.polynomial.length == y.polynomial.length;
//        for (int i = 0; i < this.polynomial.length; i++) {
//            polynomial[i].addToThis(y.polynomial[i]);
//        }
//    }
//
//    public void subtractFromThis(EpsilonPolynomial y) {
//        assert this.polynomial.length == y.polynomial.length;
//        for (int i = 0; i < this.polynomial.length; i++) {
//            polynomial[i].subtractFromThis(y.polynomial[i]);
//        }
//    }
//
//    public void negateThis() {
//        for (int i = 0; i < polynomial.length; i++) {
//            polynomial[i].negateThis();
//        }
//    }

    public Arithmetic[] getPolynomial() {
        return polynomial;
    }

    public EpsilonPolynomial copy() {
        return new EpsilonPolynomial(polynomial, factory);
    }

    public int compareTo(EpsilonPolynomial y) {
        assert polynomial.length == y.polynomial.length;
        for (int i = 0; i < polynomial.length; i++) {
            int difference = polynomial[i].compareTo(y.polynomial[i]);

            if (difference != 0)
                return difference;
        }
        return 0;
    }

    public boolean isZero() {
        for (Arithmetic arithmetic : polynomial) {
            if (!arithmetic.isZero())
                return false;
        }
        return true;
    }

    public boolean isOne() {
        if (!polynomial[0].isOne())
            return false;
        for (int i = 1; i < polynomial.length; i++) {
            if (!polynomial[i].isZero())
                return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EpsilonPolynomial)) return false;

        EpsilonPolynomial that = (EpsilonPolynomial) o;

        if (!Arrays.equals(polynomial, that.polynomial)) return false;

        return true;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(polynomial);
    }

    @Override
    public String toString() {
        return Arrays.toString(polynomial);
    }
}
