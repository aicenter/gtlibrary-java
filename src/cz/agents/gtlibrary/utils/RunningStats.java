package cz.agents.gtlibrary.utils;

/**
 * Class to calculate running mean and variance from a set of samples 
 * without keeping them all in memory.
 */
public final class RunningStats {

	private static final double default_spread = 0.0;
	
	private int n = 0;
	private double oldM;
	private double newM;
	private double oldS;
	private double newS;

	public void add(double value) {
		if(Double.isInfinite(value) || Double.isNaN(value)){
			throw new IllegalArgumentException("Bad value: " + value);
		}
		n++;

		// See Knuth TAOCP vol 2, 3rd edition, page 232
		if (n == 1) {
			oldM = newM = value;
			oldS = 0.0;
		} else {
			newM = oldM + (value - oldM) / n;
			newS = oldS + (value - oldM) * (value - newM);

			// set up for next iteration
			oldM = newM;
			oldS = newS;
		}
	}

	public int getNbSamples() {
		return n;
	}

	public double getMean() {
		return (n > 0) ? newM : 0.0;
	}

	/**
	 * Variance is positive infinity when n<2.
	 */
	public double getVariance() {
		return ((n > 1) ? newS / (n - 1) : default_spread);
	}

	public double getStdDev() {
		return Math.sqrt(getVariance());
	}

	public double getEVStdDev() {
		if(n==0){
			return default_spread;
		}
		return Math.sqrt(getVariance()/getNbSamples());
	}
	
	public double getEVVar() {
		if(n==0){
			return default_spread;
		}
		return getVariance()/getNbSamples();
	}

	public void reset() {
		n = 0;
	}

}
