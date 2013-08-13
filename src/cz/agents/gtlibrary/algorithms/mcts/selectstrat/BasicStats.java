/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

/**
 * 
 * @author Vilo
 */
public class BasicStats {
	private int n = 0;
	private double oldM;
	private double newM;

	public double onBackPropagate(double value) {
		if (Double.isInfinite(value) || Double.isNaN(value)) {
			throw new IllegalArgumentException("Bad value: " + value);
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

	public int getNbSamples() {
		return n;
	}

	@Override
	public String toString() {
		return "BP" + n + "(" + newM + ")";
	}
}
