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
        if (y.isOne())
            return this;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].divide(y.polynomial[0]);
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial add(EpsilonPolynomial y) {
        if (y.isZero())
            return this;
        if (isZero())
            return y;
        if(this.polynomial.length >= y.polynomial.length)
            return new EpsilonPolynomial(getAdditionResult(this.polynomial, y.polynomial), factory);
        return new EpsilonPolynomial(getAdditionResult(y.polynomial, this.polynomial), factory);
    }

    private Arithmetic[] getAdditionResult(Arithmetic[] longerPolynomial, Arithmetic[] shorterPolynomial) {
        Arithmetic[] result = new Arithmetic[longerPolynomial.length];

        for (int i = 0; i < shorterPolynomial.length; i++) {
            result[i] = longerPolynomial[i].add(shorterPolynomial[i]);
        }
        for (int i = shorterPolynomial.length; i < longerPolynomial.length; i++) {
            result[i] = longerPolynomial[i];
        }
        return result;
    }

    public EpsilonPolynomial subtract(EpsilonPolynomial y) {
        if (y.isZero())
            return this;
        Arithmetic[] result = new Arithmetic[Math.max(polynomial.length, y.polynomial.length)];

        if(polynomial.length >= y.polynomial.length) {
            for (int i = 0; i < y.polynomial.length; i++) {
                result[i] = this.polynomial[i].subtract(y.polynomial[i]);
            }
            for (int i = y.polynomial.length; i < this.polynomial.length; i++) {
                result[i] = this.polynomial[i];
            }
        } else {
            for (int i = 0; i < polynomial.length; i++) {
                result[i] = polynomial[i].subtract(y.polynomial[i]);
            }
            for (int i = polynomial.length; i < y.polynomial.length; i++) {
                result[i] = y.polynomial[i].negate();
            }
        }
//        clean(result);
        return new EpsilonPolynomial(result, factory);
    }

    private void clean(Arithmetic[] result) {
        for (int i = 0; i < result.length; i++) {
            if(result[i].isZero())
                result[i] = factory.getArithmeticFactory().zero();
            else if(result[i].isOne())
                result[i] = factory.getArithmeticFactory().one();
        }
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
        for (int i = 0; i < Math.min(polynomial.length, y.polynomial.length); i++) {
            int difference = polynomial[i].compareTo(y.polynomial[i]);

            if (difference != 0)
                return difference;
        }
        if(polynomial.length > y.polynomial.length){
            for (int i = y.polynomial.length; i < polynomial.length; i++) {
                int difference = polynomial[i].compareTo(factory.getArithmeticFactory().zero());

                if(difference != 0)
                    return difference;
            }
        } else if( polynomial.length < y.polynomial.length) {
            for (int i = polynomial.length; i < y.polynomial.length; i++) {
                int difference = factory.getArithmeticFactory().zero().compareTo(y.polynomial[i]);

                if(difference != 0)
                    return difference;
            }
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
