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
import java.util.Arrays;

public class EpsilonReal extends Real<EpsilonReal> implements Serializable {
	public static EpsilonReal ZERO = new EpsilonReal(0);
	public static EpsilonReal EPSILON = new EpsilonReal(1, 1);
	public static EpsilonReal ONE = new EpsilonReal(1);
	public static EpsilonReal POS_INF = new EpsilonReal(new Rational[]{Rational.POS_INF});
	public static EpsilonReal NEG_INF = new EpsilonReal(new Rational[]{Rational.NEG_INF});

	private Rational[] pol;
	
	public Rational rationalValue() { return pol[0]; }
	
	private void Normalize() {
		if (pol[pol.length - 1].compareTo(Rational.ZERO) == 0) {
			for (int i = pol.length - 2 ; i >= 0 ; i--) {
				if (pol[i].compareTo(Rational.ZERO) != 0) {
					Rational[] temp = new Rational[i + 1];
					System.arraycopy(pol, 0, temp, 0, temp.length);
					pol = temp;
					break;
				}
			}
			if (pol.length > 1 && pol[pol.length - 1].compareTo(Rational.ZERO) == 0) {
				pol = new Rational[1];
				pol[0] = Rational.ZERO;
			}
		}
	}
	
	public EpsilonReal(Rational[] pol) {
		this.pol = pol;
		Normalize();
	}
	
	public EpsilonReal(Rational r) {
		this(new Rational[]{r});
	}
	
	public EpsilonReal(int i) { this(i, 0); }

	public EpsilonReal(int i, int j) {
		pol = new Rational[j + 1];
		Arrays.fill(pol, Rational.ZERO);
		pol[j] = new Rational(i);
	}

	public EpsilonReal zero()   { return ZERO; }
	public EpsilonReal one()    { return ONE; }
	public EpsilonReal negInf() { return NEG_INF; }
	public EpsilonReal posInf() { return POS_INF; }

	public String toString() {
		StringBuffer sb = new StringBuffer().append(pol[0]);
		for (int i = 1 ; i < pol.length ; i++) {
			sb.append("..").append(pol[i]);
		}
		return sb.toString();
	}

	public String toLaTeX() {
		return toString();
	}

	public double doubleValue() {
		return pol[0].doubleValue();
	}

	public EpsilonReal fromDouble(double val) {
		return new EpsilonReal(new Rational[]{pol[0].fromDouble(val)});
	}
	
	public EpsilonReal multiplyFrac(Rational y) {
		return multiply(new EpsilonReal(y));
	}
	
	public EpsilonReal multiply(EpsilonReal y) {
		Rational[] result = new Rational[pol.length + y.pol.length - 1];
		Arrays.fill(result, Rational.ZERO);
		for (int i = 0 ; i < pol.length ; i++) {
			for (int j = 0 ; j < y.pol.length ; j++) {
				result[i + j] = result[i + j].add(pol[i].multiply(y.pol[j]));
			}
		}
		return new EpsilonReal(result);
	}
	
	/** DISCLAIMER: DOES NOT ALWAYS PRODUCE CORRECT RESULT ON DIVISION WITH POLYNOMIA */
	public EpsilonReal divide(EpsilonReal y) {
		if (y.isZero()) throw new ArithmeticException("Division by zero");
		int beginidx = 0;
		while (y.pol[beginidx].isZero()) beginidx++;
		Rational[] result = new Rational[pol.length - beginidx];
		for (int i = 0 ; i < beginidx ; i++) {
			if (!pol[i].isZero()) throw new ArithmeticException("Result is rational divided by power of epsilon.");
		}
		for (int i = 0 ; i < result.length ; i++) {
			result[i] = pol[beginidx + i].divide(y.pol[beginidx]);
		}
		return new EpsilonReal(result);
	}

	public EpsilonReal add(EpsilonReal y) {
		Rational[] result, adder;
		if (pol.length < y.pol.length) {
			result = y.pol.clone();
			adder = pol;
		} else {
			result = pol.clone();
			adder = y.pol;
		}
		for (int i = 0 ; i < adder.length ; i++) {
			result[i] = result[i].add(adder[i]);
		}
		return new EpsilonReal(result);
	}

	public EpsilonReal subtract(EpsilonReal y) {
		return add(y.negate());
	}

	public int compareTo(EpsilonReal y) {
		EpsilonReal temp = subtract(y);
		for (Rational elm : temp.pol) {
			if (elm.isNegative()) return -1;
			if (elm.isPositive()) return 1;
		}
		return 0;
	}
	
	public EpsilonReal negate() {
		Rational[] result = new Rational[pol.length];
		for (int i = 0 ; i < result.length ; i++) {
			result[i] = pol[i].negate();
		}
		return new EpsilonReal(result);
	}

	public EpsilonReal abs() {
		Rational[] result = new Rational[pol.length];
		for (int i = 0 ; i < result.length ; i++) {
			result[i] = pol[i].abs();
		}
		return new EpsilonReal(result);
	}

	public EpsilonReal invert() {
		if (pol.length != 1) throw new UnsupportedOperationException();
		return new EpsilonReal(new Rational[]{pol[0].invert()});
	}
	
	public int hashCode() {
		return pol[0].hashCode();
	}

	public boolean equals(Object o) {
		return compareTo((EpsilonReal) o) == 0;
	}
}
