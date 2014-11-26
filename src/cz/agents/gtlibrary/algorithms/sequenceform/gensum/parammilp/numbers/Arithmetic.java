package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers;

public interface Arithmetic extends Comparable<Arithmetic> {

    public Arithmetic multiply(Arithmetic y);

    public Arithmetic divide(Arithmetic y);

    public Arithmetic add(Arithmetic y);

    public Arithmetic subtract(Arithmetic y);

    public Arithmetic negate();

    public Arithmetic invert();

    public Arithmetic abs();

    public void multiplyThis(Arithmetic y);

    public void divideThis(Arithmetic y);

    public void addToThis(Arithmetic y);

    public void subtractFromThis(Arithmetic y);

    public void negateThis();

    public void invertThis();

    public void absThis();

    public double doubleValue();

}
