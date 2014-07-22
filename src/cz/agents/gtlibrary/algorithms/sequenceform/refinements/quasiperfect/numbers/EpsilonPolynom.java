/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers;

public class EpsilonPolynom extends Number {

	private static final long serialVersionUID = 4222738743015373008L;

	private Epsilon epsilon;
	private final double exponent;
	private double value = Double.NaN;
	private boolean negative;

	public EpsilonPolynom(Epsilon epsilon, int exponent) {
		this.epsilon = epsilon;
		this.exponent = exponent;
		negative = false;
	}

	@Override
	public int intValue() {
		return (int) doubleValue();
	}

	@Override
	public long longValue() {
		return (long) doubleValue();
	}

	@Override
	public float floatValue() {
		return (float) doubleValue();
	}

	@Override
	public double doubleValue() {
		if (epsilon.doubleValue() != -1 && value != value)
			value = (negative?-1:1)*Math.pow(epsilon.doubleValue(), exponent);
		return value;
	}

    public double getExponent() {
        return exponent;
    }

    public EpsilonPolynom negate() {
		negative = negative ? false : true;
		return this;
	}

	@Override
	public String toString() {
		return epsilon + "^" + exponent;
	}
	
	

}
