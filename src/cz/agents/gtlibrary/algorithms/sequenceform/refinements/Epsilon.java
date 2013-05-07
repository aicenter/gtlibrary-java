package cz.agents.gtlibrary.algorithms.sequenceform.refinements;

public class Epsilon extends Number {

	private static final long serialVersionUID = 525903728831840335L;
	
	private double value;
	
	public Epsilon() {
		value = -1;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	@Override
	public int intValue() {
		if (value == -1) 
			throw new UnsupportedOperationException("Epsilon not yet initialized");
		return (int) value;
	}

	@Override
	public long longValue() {
		if (value == -1) 
			throw new UnsupportedOperationException("Epsilon not yet initialized");
		return (long) value;
	}

	@Override
	public float floatValue() {
		if (value == -1) 
			throw new UnsupportedOperationException("Epsilon not yet initialized");
		return (float) value;
	}

	@Override
	public double doubleValue() {
		if (value == -1) 
			throw new UnsupportedOperationException("Epsilon not yet initialized");
		return value;
	}

}
