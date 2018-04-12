package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers;

import java.math.BigInteger;

/**
 * A Rational instance represents a single rational number with infinite
 * precision of numerator and denominator.
 *
 * @author Peter Bro Miltersen, February 2002.
 */
public class BigIntRational implements Arithmetic {

    public static BigIntRational ONE = new BigIntRational(BigInteger.ONE);
    public static BigIntRational ZERO = new BigIntRational(BigInteger.ZERO);

    private BigInteger numerator;
    private BigInteger denominator;

    private void normalize() {
        BigInteger g = numerator.gcd(denominator);

        numerator = numerator.divide(g);
        denominator = denominator.divide(g);
        if (denominator.compareTo(BigInteger.ZERO) < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
    }

    /**
     * builds Rational with reward <tt>n</tt>/<tt>d</tt>.
     *
     * @param n is the numerator of a new Rational
     * @param d is the denominator of a new Rational
     */
    public BigIntRational(BigInteger n, BigInteger d) {
        numerator = n;
        denominator = d;
        normalize();
    }

    public BigIntRational(int n, int d) {
        numerator = new BigInteger(Integer.toString(n));
        denominator = new BigInteger(Integer.toString(d));
        normalize();
    }

    public BigIntRational(long n, long d) {
        numerator = new BigInteger(Long.toString(n));
        denominator = new BigInteger(Long.toString(d));
        normalize();
    }

    /**
     * builds Rational with reward <tt>i</tt>.
     */
    public BigIntRational(int i) {
        denominator = BigInteger.ONE;
        numerator = new BigInteger("" + i);
    }

    /**
     * builds Rational with reward <tt>i</tt>.
     */
    public BigIntRational(BigInteger i) {
        denominator = BigInteger.ONE;
        numerator = i;
    }

    /**
     * returns a String representation of this Rational
     */
    public String toString() {
        return numerator.toString() + "/" + denominator.toString();
    }

    /**
     * returns a LaTeX representation of this Rational.
     */
    public String toLaTeX() {
        if (denominator.compareTo(BigInteger.ONE) == 0) {
            return numerator.toString();
        } else {
            return "\\frac{" + numerator.toString() + "}{" + denominator.toString() + "}";
        }
    }

    /**
     * returns a double holding an approximation of this Rational.
     */
    public double doubleValue() {
        if (denominator.equals(BigInteger.ZERO)) {
            int comp = numerator.compareTo(BigInteger.ZERO);
            return comp == 0 ? Double.NaN : comp < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        return numerator.doubleValue() / denominator.doubleValue();
    }

    public Arithmetic multiply(Arithmetic y) {
        BigIntRational rational = (BigIntRational) y;
        BigInteger n = numerator.multiply(rational.numerator);
        BigInteger d = denominator.multiply(rational.denominator);

        return new BigIntRational(n, d);
    }

    public Arithmetic divide(Arithmetic y) {
        BigIntRational rational = (BigIntRational) y;
        BigInteger n = numerator.multiply(rational.denominator);
        BigInteger d = denominator.multiply(rational.numerator);

        return new BigIntRational(n, d);
    }

    public Arithmetic add(Arithmetic y) {
        BigIntRational rational = (BigIntRational) y;
        BigInteger n = numerator.multiply(rational.denominator).add(rational.numerator.multiply(denominator));
        BigInteger d = denominator.multiply(rational.denominator);

        return new BigIntRational(n, d);
    }

    public Arithmetic subtract(Arithmetic y) {
        BigIntRational rational = (BigIntRational) y;
        BigInteger n = numerator.multiply(rational.denominator).subtract(rational.numerator.multiply(denominator));
        BigInteger d = denominator.multiply(rational.denominator);

        return new BigIntRational(n, d);
    }

    /**
     * compareTo compares the current Rational to another Rational.
     *
     * @return -1 if x < y, 0 if x=y, and 1 if x>y where x is this Rational
     */
    public int compareTo(Arithmetic y) {
        return ((BigIntRational) subtract(y)).numerator.compareTo(BigInteger.ZERO);
    }

    public BigIntRational negate() {
        return new BigIntRational(numerator.negate(), denominator);
    }

    public BigIntRational invert() {
        return new BigIntRational(denominator, numerator);
    }

    public BigIntRational abs() {
        return new BigIntRational(numerator.abs(), denominator);
    }

    @Override
    public boolean isZero() {
        return numerator.equals(BigInteger.ZERO);
    }

    @Override
    public boolean isOne() {
        return numerator.equals(denominator);
    }

    public BigInteger getDenominator() {
        return denominator;
    }

    public BigInteger getNumerator() {
        return numerator;
    }

    //    @Override
//    public void multiplyThis(Arithmetic y) {
//        BigIntRational rational = (BigIntRational) y;
//
//        numerator = numerator.multiply(rational.numerator);
//        denominator = denominator.multiply(rational.denominator);
//    }
//
//    @Override
//    public void divideThis(Arithmetic y) {
//        BigIntRational rational = (BigIntRational) y;
//
//        numerator = numerator.multiply(rational.denominator);
//        denominator = denominator.multiply(rational.numerator);
//    }
//
//    @Override
//    public void addToThis(Arithmetic y) {
//        BigIntRational rational = (BigIntRational) y;
//
//        numerator = numerator.multiply(rational.denominator).add(rational.numerator.multiply(denominator));
//        denominator = denominator.multiply(rational.denominator);
//    }
//
//    @Override
//    public void subtractFromThis(Arithmetic y) {
//        BigIntRational rational = (BigIntRational) y;
//        numerator = numerator.multiply(rational.denominator).subtract(rational.numerator.multiply(denominator));
//        denominator = denominator.multiply(rational.denominator);
//    }
//
//    @Override
//    public void negateThis() {
//        numerator = numerator.negate();
//    }
//
//    @Override
//    public void invertThis() {
//        BigInteger temp = numerator;
//
//        numerator = denominator;
//        denominator = temp;
//    }
//
//    @Override
//    public void absThis() {
//        numerator = numerator.abs();
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BigIntRational)) return false;

        BigIntRational rational = (BigIntRational) o;

        if (!denominator.equals(rational.denominator))
            return false;
        if (!numerator.equals(rational.numerator))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = numerator.hashCode();

        result = 31 * result + denominator.hashCode();
        return result;
    }

}
