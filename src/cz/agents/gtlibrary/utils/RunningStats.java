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


package cz.agents.gtlibrary.utils;

/**
 * Class to calculate running mean and variance from a set of samples without
 * keeping them all in memory.
 */
public final class RunningStats {

	private int n = 0;
	private double oldM;
	private double newM;

	public void add(double value) {
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			throw new IllegalArgumentException("Bad reward: " + value);
		}
		n++;

		if (n == 1) {
			oldM = newM = value;
		} else {
			newM = oldM + (value - oldM) / n;
		}
		oldM = newM;
	}

	public int getNbSamples() {
		return n;
	}

	public double getMean() {
		return (n > 0) ? newM : 0.0;
	}

	public void reset() {
		n = 0;
		oldM = 0;
		newM = 0;
	}

	@Override
	public String toString() {
		return "Stats: n = " + n + ", mean = " + newM;
	}
}
