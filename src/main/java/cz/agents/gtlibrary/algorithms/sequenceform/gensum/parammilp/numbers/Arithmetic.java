package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers;

public interface Arithmetic extends Comparable<Arithmetic> {

    public Arithmetic multiply(Arithmetic y);

    public Arithmetic divide(Arithmetic y);

    public Arithmetic add(Arithmetic y);

    public Arithmetic subtract(Arithmetic y);

    public Arithmetic negate();

    public Arithmetic invert();

    public Arithmetic abs();

    public boolean isZero();

    public boolean isOne();

    public double doubleValue();

}
