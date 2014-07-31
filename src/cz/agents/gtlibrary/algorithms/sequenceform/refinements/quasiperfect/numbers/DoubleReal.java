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

public class DoubleReal extends Real<DoubleReal> {
	public double val;

	public static DoubleReal ONE  = new DoubleReal(1);
	public static DoubleReal ZERO = new DoubleReal(0);
	public static DoubleReal NEG_INF = new DoubleReal(Double.NEGATIVE_INFINITY);
	public static DoubleReal POS_INF = new DoubleReal(Double.POSITIVE_INFINITY);

	public DoubleReal zero()   { return ZERO; }
	public DoubleReal one()    { return ONE; } 
	public DoubleReal negInf() { return NEG_INF; }
	public DoubleReal posInf() { return POS_INF; }

	public DoubleReal(double d) { val = d; }

	public String toString() { return Double.toString(val); }
	public String toLaTeX()  { return Double.toString(val); }

	public DoubleReal add(DoubleReal y)      { return new DoubleReal(val + y.doubleValue()); }
	public DoubleReal subtract(DoubleReal y) { return new DoubleReal(val - y.doubleValue()); }
	public DoubleReal multiply(DoubleReal y) { return new DoubleReal(val * y.doubleValue()); }
	public DoubleReal divide(DoubleReal y)   { return new DoubleReal(val / y.doubleValue()); }
	public DoubleReal invert()               { return new DoubleReal(1 / val); }
	public DoubleReal negate()               { return new DoubleReal(- val); }
	public DoubleReal abs() { return val < 0 ? negate() : this; }

	public DoubleReal multiplyFrac(Rational y) { return new DoubleReal(val * y.doubleValue()); }

	public double doubleValue() { return val; }
	public DoubleReal fromDouble(double val) { return new DoubleReal(val); }

	public Rational rationalValue() {
		return new Rational(Double.toString(val));
	}
	
	public int compareTo(DoubleReal y){
		double valy = y.doubleValue();
		if (val <  valy) return -1;
		if (val == valy) return 0;
		return 1;
	}
}

