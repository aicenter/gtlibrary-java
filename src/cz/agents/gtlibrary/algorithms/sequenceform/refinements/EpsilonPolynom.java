package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

public class EpsilonPolynom extends Number {
	
	private static final long serialVersionUID = 4222738743015373008L;
	
	private Epsilon epsilon;
	private final double exponent;
	private double value = Double.NaN;
	
	public EpsilonPolynom(Epsilon epsilon, int exponent) {
		this.epsilon = epsilon;
		this.exponent = exponent;
	}
	
	@Override
	public int intValue() {
		if(value != value)
			value = Math.pow(epsilon.doubleValue(), exponent);
		return (int) value;
	}
	@Override
	public long longValue() {
		if(value != value)
			value = Math.pow(epsilon.doubleValue(), exponent);
		return (long) value;
	}
	@Override
	public float floatValue() {
		if(value != value)
			value = Math.pow(epsilon.doubleValue(), exponent);
		return (float) value;
	}
	@Override
	public double doubleValue() {
		if(value != value)
			value = Math.pow(epsilon.doubleValue(), exponent);
		return value;
	}
	
	@Override
	public String toString() {
		return "eps^" + exponent;
	}

}
