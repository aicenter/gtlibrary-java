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

public abstract class Real<T extends Real> implements Comparable<T>, Serializable {
	public abstract T negInf();
	public abstract T posInf();
	public abstract T zero();
	public abstract T one(); 

	public abstract double doubleValue();
	public abstract T fromDouble(double val);

	public abstract Rational rationalValue();

	public abstract T add(T y);
	public abstract T subtract(T y);
	public abstract T multiply(T y);
	public abstract T divide(T y);
	public abstract T invert();
	public abstract T negate();
	public abstract T abs();
	public abstract T multiplyFrac(Rational y);

	public abstract int compareTo(T y);

	public abstract String toLaTeX();
	
	public DoubleReal approxAdd(Real y)      { return new DoubleReal(doubleValue() + y.doubleValue()); }
	public DoubleReal approxSubtract(Real y) { return new DoubleReal(doubleValue() - y.doubleValue()); }
	public DoubleReal approxMultiply(Real y) { return new DoubleReal(doubleValue() * y.doubleValue()); }
	public DoubleReal approxDivide(Real y)   { return new DoubleReal(doubleValue() / y.doubleValue()); }

	public boolean equals(T y)  { return compareTo(y) == 0; }
	public boolean less(T y)    { return compareTo(y) <  0; }
	public boolean leq(T y)     { return compareTo(y) <= 0; } 
	public boolean greater(T y) { return compareTo(y) >  0; }
	public boolean geq(T y)     { return compareTo(y) >= 0; }
	
	public boolean isZero()     { return compareTo(zero()) == 0; }
	public boolean isOne()      { return compareTo(one())  == 0; }
	public boolean isNegative() { return compareTo(zero()) <  0; }
	public boolean isPositive() { return compareTo(zero()) >  0; }
}
