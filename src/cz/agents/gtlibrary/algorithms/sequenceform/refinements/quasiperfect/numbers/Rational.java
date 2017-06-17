/************************************************************************\
|* gtf is a framework for analyzing two-player zero-sum games           *|
|* Copyright (C) 2005 Troels Bjerre Sorensen                            *|
|*                                                                      *|
|* This program is free software; you can redistribute it and/or modify *|
|* it under the terms of the GNU General Public License as published by *|
|* the Free Software Foundation; either version 2 of the License, or    *|
|* (at your option) any later version.                                  *|
|*                                                                      *|
|* This program is distributed in the hope that it will be useful, but  *|
|* WITHOUT ANY WARRANTY; without even the implied warranty of           *|
|* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU    *|
|* General Public License for more details.                             *|
|*                                                                      *|
|* You should have received a copy of the GNU General Public License    *|
|* along with this program; if not, write to the                        *|
|* Free Software Foundation, Inc., 59 Temple Place - Suite 330,         *|
|* Boston, MA 02111-1307, USA.                                          *|
\************************************************************************/
package cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.StringTokenizer;

/** A Rational instance represents a single rational number with infinite 
 *  precision of numerator and denominator. 
 *  @author Peter Bro Miltersen, February 2002. */
public class Rational extends Real<Rational> implements Serializable {

	private static BigInteger one = BigInteger.ONE;
	private static BigInteger zero = BigInteger.ZERO;

	public static Rational ONE = new Rational(one);
	public static Rational ZERO = new Rational(zero);
	public static Rational POS_INF = ONE.divide(ZERO);
	public static Rational NEG_INF = POS_INF.negate();

	/** num is the (normalized) numerator of this Rational */
	public BigInteger num;
	/** den is the (normalized) denominator of this Rational */
	public BigInteger den;

	private void Normalize() {
		BigInteger g = num.gcd(den);
		num = num.divide(g);
		den = den.divide(g);
		if (den.compareTo(zero) < 0) {
	    	num = num.negate();
	    	den = den.negate();
		}
	}

	public Rational fromDouble(double val) {
		return new Rational(String.valueOf(val));
	}
	
	public Rational rationalValue() {
		return this;
	}
	
	/** builds Rational with reward <tt>n</tt>/<tt>d</tt>.
	 *  @param n is the numerator of a new Rational
	 *  @param d is the denominator of a new Rational */
	public Rational(BigInteger n, BigInteger d) {
		num = n; den = d;
		Normalize();
	}

    public Rational(int n, int d) {
        num = new BigInteger(Integer.toString(n));
        den = new BigInteger(Integer.toString(d));
        Normalize();
    }

	/** builds Rational described by <tt>rep</tt>, e.g., <tt>rep = "-1/4"</tt>, 
	 * or <tt>rep = "3.14E-1"</tt>.*/
	public Rational(String rep) {
		try {
			if (rep.indexOf('/') != -1) {
				BigInteger d;
				StringTokenizer st = new StringTokenizer(rep, "/");
				num = new BigInteger(st.nextToken().trim());
				den = new BigInteger(st.nextToken().trim());
			} else {
				StringTokenizer st = new StringTokenizer(rep, "Ee");
				int exp = 0;
				rep = st.nextToken();
				if (st.hasMoreTokens()) {
					String expo = st.nextToken();
					if (expo.startsWith("+")) {
						exp = Integer.parseInt(expo.substring(1));
					} else {
						exp = Integer.parseInt(expo);
					}
				}
				st = new StringTokenizer(rep, ".,");
				rep = st.nextToken().trim();
				if (st.hasMoreTokens()) {
					String dec = st.nextToken();
					exp -= dec.length();
					rep += dec;
				}
				if (exp < 0) {
					num = new BigInteger(rep);
					den = BigInteger.TEN.pow(-exp);
				} else {
					num = new BigInteger(rep).multiply(BigInteger.TEN.pow(exp));
					den = BigInteger.ONE;
				}
			}
			assert num != null && den != null;
			Normalize();
		} catch (Exception e) { 
			throw new NumberFormatException("\"" + rep + "\" is not a legal Rational");
		}
	}

	/** builds Rational with reward <tt>i</tt>. */
	public Rational(int i) {
		den = one;
		num = new BigInteger("" + i);
	}

	/** builds Rational with reward <tt>i</tt>. */
	public Rational(BigInteger i) {
		den = one;
		num = i;
	}

	/** returns a Rational whose reward is 0 */
	public Rational zero()   { return ZERO; }
	public Rational one()    { return ONE; }
	public Rational negInf() { return NEG_INF; }
	public Rational posInf() { return POS_INF; }

	/** returns a String representation of this Rational */
	public String toString() {
		if (den.compareTo(one) == 0) {
			return num.toString();
		} else {
			return num.toString() + "/" + den.toString();
		}
	}

	/** returns a LaTeX representation of this Rational. */
	public String toLaTeX() {
		if (den.compareTo(one) == 0) {
			return num.toString();
		} else {
			return "\\frac{" + num.toString() + "}{" + den.toString() + "}";
		}
	}

	/** returns a double holding an approximation of this Rational. */
	public double doubleValue() {
		if (den.equals(zero)) {
			int comp = num.compareTo(zero);
			return comp == 0 ? Double.NaN : comp < 0 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
		}
		return num.doubleValue() / den.doubleValue();
	}

	public Rational multiply(Rational y) {
		BigInteger n = num.multiply(y.num);
		BigInteger d = den.multiply(y.den);
		return new Rational(n, d);
	}
		
	public Rational divide(Rational y) {
		BigInteger n = num.multiply(y.den);
		BigInteger d = den.multiply(y.num);
		return new Rational(n, d);
	}

	public Rational add(Rational y) {
		BigInteger n = num.multiply(y.den).add(y.num.multiply(den));
		BigInteger d = den.multiply(y.den);
		return new Rational(n, d);
	}

	public Rational subtract(Rational y) {
		BigInteger n = num.multiply(y.den).subtract(y.num.multiply(den));
		BigInteger d = den.multiply(y.den);
		return new Rational(n, d);
	}

	public Rational multiplyFrac(Rational y) {
		return multiply(y);
	}
	
	/** compareTo compares the current Rational to another Rational.
	 *  @return -1 if x < y, 0 if x=y, and 1 if x>y where x is this Rational */
	public int compareTo(Rational y) {
		return subtract((Rational) y).num.compareTo(zero);
	}

	public Rational negate() {
		return new Rational(num.negate(), den);
	}

	public Rational invert() {
		return new Rational(den, num);
	}

	public Rational abs() {
		return new Rational(num.abs(), den);
	}

	public int hashCode() {
		return num.hashCode() + den.hashCode();
	}

	public boolean equals(Object o) {
		if (! (o instanceof Rational)) return false;
		Rational other = (Rational) o;
		return num.equals(other.num) && den.equals(other.den);
	}
}
