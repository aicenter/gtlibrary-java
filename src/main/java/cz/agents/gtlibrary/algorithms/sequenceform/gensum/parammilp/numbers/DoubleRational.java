package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers;

public class DoubleRational implements Arithmetic {

    public static final DoubleRational ZERO = new DoubleRational(0);
    public static final DoubleRational ONE = new DoubleRational(1);

    private double value;

    public DoubleRational(double value) {
        this.value = value;
    }

    @Override
    public Arithmetic multiply(Arithmetic y) {
        return new DoubleRational(value * ((DoubleRational) y).value);
    }

    @Override
    public Arithmetic divide(Arithmetic y) {
        return new DoubleRational(value / ((DoubleRational) y).value);
    }

    @Override
    public Arithmetic add(Arithmetic y) {
        return new DoubleRational(value + ((DoubleRational) y).value);
    }

    @Override
    public Arithmetic subtract(Arithmetic y) {
        return new DoubleRational(value - ((DoubleRational) y).value);
    }

    @Override
    public Arithmetic negate() {
        return new DoubleRational(-value);
    }

    @Override
    public Arithmetic invert() {
        return new DoubleRational(1/value);
    }

    @Override
    public Arithmetic abs() {
        return new DoubleRational(Math.abs(value));
    }

    @Override
    public boolean isZero() {
        return value == 0;
    }

    @Override
    public boolean isOne() {
        return value == 1;
    }

//    @Override
//    public void multiplyThis(Arithmetic y) {
//          reward = reward*((DoubleRational)y).reward;
//    }
//
//    @Override
//    public void divideThis(Arithmetic y) {
//        reward = reward/((DoubleRational)y).reward;
//    }
//
//    @Override
//    public void addToThis(Arithmetic y) {
//        reward = reward+((DoubleRational)y).reward;
//    }
//
//    @Override
//    public void subtractFromThis(Arithmetic y) {
//        reward = reward-((DoubleRational)y).reward;
//    }
//
//    @Override
//    public void negateThis() {
//        reward = -reward;
//    }
//
//    @Override
//    public void invertThis() {
//        reward = 1/reward;
//    }
//
//    @Override
//    public void absThis() {
//         reward = Math.abs(reward);
//    }

    @Override
    public int compareTo(Arithmetic o) {
        return Double.compare(value, ((DoubleRational)o).value);
    }

    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public String toString() {
        return value + "";
    }
}
