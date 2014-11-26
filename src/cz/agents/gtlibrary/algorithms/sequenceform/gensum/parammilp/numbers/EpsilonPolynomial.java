package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers;


import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.ArithmeticFactory;

public class EpsilonPolynomial implements Comparable<EpsilonPolynomial> {

    private Arithmetic[] polynomial;
    private ArithmeticFactory factory;

    public EpsilonPolynomial(int nominator, int denominator, ArithmeticFactory factory) {
        this.polynomial = new Arithmetic[]{factory.create(nominator, denominator)};
        this.factory = factory;
    }

    public EpsilonPolynomial(Arithmetic[] polynomial, ArithmeticFactory factory) {
        this.polynomial = polynomial;
        this.factory = factory;
    }

    public EpsilonPolynomial(int[] numerators, int[] denominators, ArithmeticFactory factory) {
        polynomial = new Arithmetic[numerators.length];
        for (int i = 0; i < numerators.length; i++) {
            polynomial[i] = factory.create(numerators[i], denominators[i]);
        }
        this.factory = factory;
    }

    public EpsilonPolynomial multiply(EpsilonPolynomial y) {
        assert y.polynomial.length == 1;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].multiply(y.polynomial[0]);
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial divide(EpsilonPolynomial y) {
        assert y.polynomial.length == 1;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].divide(y.polynomial[0]);
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial add(EpsilonPolynomial y) {
        assert this.polynomial.length == y.polynomial.length;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].add(y.polynomial[i]);
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial subtract(EpsilonPolynomial y) {
        assert this.polynomial.length == y.polynomial.length;
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].subtract(y.polynomial[i]);
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public EpsilonPolynomial negate() {
        Arithmetic[] polynomial = new Arithmetic[this.polynomial.length];

        for (int i = 0; i < polynomial.length; i++) {
            polynomial[i] = this.polynomial[i].negate();
        }
        return new EpsilonPolynomial(polynomial, factory);
    }

    public void multiplyThis(EpsilonPolynomial y) {
        assert y.polynomial.length == 1;
        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i].multiplyThis(y.polynomial[0]);
        }
    }

    public void divideThis(EpsilonPolynomial y) {
        assert y.polynomial.length == 1;
        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i].divideThis(y.polynomial[0]);
        }
    }

    public void addToThis(EpsilonPolynomial y) {
        assert this.polynomial.length == y.polynomial.length;
        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i].addToThis(y.polynomial[i]);
        }
    }

    public void subtractFromThis(EpsilonPolynomial y) {
        assert this.polynomial.length == y.polynomial.length;
        for (int i = 0; i < this.polynomial.length; i++) {
            polynomial[i].subtractFromThis(y.polynomial[i]);
        }
    }

    public void negateThis() {
        for (int i = 0; i < polynomial.length; i++) {
            polynomial[i].negateThis();
        }
    }

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
}
