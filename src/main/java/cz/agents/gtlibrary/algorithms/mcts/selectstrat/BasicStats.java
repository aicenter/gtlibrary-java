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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import java.io.Serializable;

/**
 * 
 * @author Vilo
 */
public class BasicStats implements Serializable {
	private long n = 0;
	private double oldM;
	private double newM;

	public double onBackPropagate(double value) {
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
                return value;
	}

	public double getEV() {
		return (n > 0) ? newM : 0.0;
	}

	public long getNbSamples() {
		return n;
	}

	@Override
	public String toString() {
		return "BP" + n + "(" + newM + ")";
	}
}
