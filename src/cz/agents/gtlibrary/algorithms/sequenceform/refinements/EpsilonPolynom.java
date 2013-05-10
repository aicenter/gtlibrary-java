package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

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

	public EpsilonPolynom negate() {
		negative = negative ? false : true;
		return this;
	}

	@Override
	public String toString() {
		return epsilon + "^" + exponent;
	}

}
