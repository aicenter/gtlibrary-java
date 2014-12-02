package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers;

import org.apfloat.Apint;
import org.apfloat.ApintMath;

public class ApIntRational implements Arithmetic {

    public static ApIntRational ONE = new ApIntRational(Apint.ONE);
    public static ApIntRational ZERO = new ApIntRational(Apint.ZERO);

    private Apint numerator;
    private Apint denominator;

    public ApIntRational(Apint n, Apint d) {
        numerator = n;
        denominator = d;
    }

    public ApIntRational(int n, int d) {
        numerator = new Apint(n);
        denominator = new Apint(d);
    }

    public ApIntRational(long n, long d) {
        numerator = new Apint(n);
        denominator = new Apint(d);
    }

    public ApIntRational(int i) {
        denominator = Apint.ONE;
        numerator = new Apint(i);
    }

    public ApIntRational(Apint i) {
        denominator = Apint.ONE;
        numerator = i;
    }

    /**
     * returns a String representation of this Rational
     */
    public String toString() {
        if (denominator.compareTo(Apint.ONE) == 0) {
            return numerator.toString();
        } else {
            return numerator.toString() + "/" + denominator.toString();
        }
    }

    /**
     * returns a LaTeX representation of this Rational.
     */
    public String toLaTeX() {
        if (denominator.compareTo(Apint.ONE) == 0) {
            return numerator.toString();
        } else {
            return "\\frac{" + numerator.toString() + "}{" + denominator.toString() + "}";
        }
    }

    /**
     * returns a double holding an approximation of this Rational.
     */
    public double doubleValue() {
        if (denominator.equals(Apint.ZERO)) {
            int comp = numerator.compareTo(Apint.ZERO);

            return comp == 0 ? Double.NaN : comp < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        return numerator.doubleValue() / denominator.doubleValue();
    }

    public Arithmetic multiply(Arithmetic y) {
        ApIntRational rational = (ApIntRational) y;
        Apint n = numerator.multiply(rational.numerator);
        Apint d = denominator.multiply(rational.denominator);

        return new ApIntRational(n, d);
    }

    public Arithmetic divide(Arithmetic y) {
        ApIntRational rational = (ApIntRational) y;
        Apint n = numerator.multiply(rational.denominator);
        Apint d = denominator.multiply(rational.numerator);

        return new ApIntRational(n, d);
    }

    public Arithmetic add(Arithmetic y) {
        ApIntRational rational = (ApIntRational) y;
        Apint n = numerator.multiply(rational.denominator).add(rational.numerator.multiply(denominator));
        Apint d = denominator.multiply(rational.denominator);

        return new ApIntRational(n, d);
    }

    public Arithmetic subtract(Arithmetic y) {
        ApIntRational rational = (ApIntRational) y;
        Apint n = numerator.multiply(rational.denominator).subtract(rational.numerator.multiply(denominator));
        Apint d = denominator.multiply(rational.denominator);

        return new ApIntRational(n, d);
    }

    /**
     * compareTo compares the current Rational to another Rational.
     *
     * @return -1 if x < y, 0 if x=y, and 1 if x>y where x is this Rational
     */
    public int compareTo(Arithmetic y) {
        return subtract(y).compareTo(ZERO);
    }

    public ApIntRational negate() {
        return new ApIntRational(numerator.negate(), denominator);
    }

    public ApIntRational invert() {
        return new ApIntRational(denominator, numerator);
    }

    public ApIntRational abs() {
        return new ApIntRational(ApintMath.abs(numerator), denominator);
    }

    @Override
    public boolean isZero() {
        return numerator.equals(Apint.ZERO);
    }

    @Override
    public boolean isOne() {
        return numerator.equals(denominator);
    }

    //    @Override
//    public void multiplyThis(Arithmetic y) {
//        ApIntRational rational = (ApIntRational) y;
//
//        numerator = numerator.multiply(rational.numerator);
//        denominator = denominator.multiply(rational.denominator);
//    }
//
//    @Override
//    public void divideThis(Arithmetic y) {
//        ApIntRational rational = (ApIntRational) y;
//
//        numerator = numerator.multiply(rational.denominator);
//        denominator = denominator.multiply(rational.numerator);
//    }
//
//    @Override
//    public void addToThis(Arithmetic y) {
//        ApIntRational rational = (ApIntRational) y;
//
//        numerator = numerator.multiply(rational.denominator).add(rational.numerator.multiply(denominator));
//        denominator = denominator.multiply(rational.denominator);
//    }
//
//    @Override
//    public void subtractFromThis(Arithmetic y) {
//        ApIntRational rational = (ApIntRational) y;
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
//        Apint temp = numerator;
//
//        numerator = denominator;
//        denominator = temp;
//    }
//
//    @Override
//    public void absThis() {
//        numerator = ApintMath.abs(numerator);
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApIntRational)) return false;

        ApIntRational rational = (ApIntRational) o;

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
