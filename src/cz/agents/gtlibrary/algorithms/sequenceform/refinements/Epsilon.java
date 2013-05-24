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
		return value;
	}

	@Override
	public String toString() {
		return value == -1 ? "eps" : Double.toString(doubleValue());
	}

}
