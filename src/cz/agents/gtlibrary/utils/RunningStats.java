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
			throw new IllegalArgumentException("Bad value: " + value);
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
