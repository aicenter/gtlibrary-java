package epsilonrational;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.DoubleRationalFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.factory.EpsilonPolynomialFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DoubleEpsilonPolynomialTest {

    @Test
    public void oneTest() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());
        EpsilonPolynomial rational = factory.one();

        assertEquals(1, rational.getPolynomial().length);
        assertEquals(1, rational.getPolynomial()[0].doubleValue(), 1e-6);
    }

    @Test
    public void zeroTest() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());
        EpsilonPolynomial rational = factory.zero();

        assertEquals(1, rational.getPolynomial().length);
        assertEquals(0, rational.getPolynomial()[0].doubleValue(), 1e-6);
    }

    @Test
    public void addTest() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());
        EpsilonPolynomial rational = factory.one();

        EpsilonPolynomial result = rational.add(factory.one());
        assertEquals(1, result.getPolynomial().length);
        assertEquals(2, result.getPolynomial()[0].doubleValue(), 1e-6);
    }

    @Test
    public void addLongTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2, 3}, new int[]{3, 2, 1}, new DoubleRationalFactory());
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5, 6}, new int[]{7, 8, 9}, new DoubleRationalFactory());

        EpsilonPolynomial result = rational1.add(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(0.9048, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(1.625, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(3.66666, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void subtractLongTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2, 3}, new int[]{3, 2, 1}, new DoubleRationalFactory());
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5, 6}, new int[]{7, 8, 9}, new DoubleRationalFactory());

        EpsilonPolynomial result = rational1.subtract(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-0.23809, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(0.375, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(2.333333, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void negateTest() {
        EpsilonPolynomial rational = new EpsilonPolynomial(new int[]{1, 2, -3}, new int[]{3, 2, 1}, new DoubleRationalFactory());

        EpsilonPolynomial result = rational.negate();
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-1. / 3, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(-1, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(3, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void multiplyTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{-1, 2, -3}, new int[]{3, 2, 1}, new DoubleRationalFactory());
        EpsilonPolynomial rational2 = new EpsilonPolynomial(1, 3, new DoubleRationalFactory());

        EpsilonPolynomial result = rational1.multiply(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-1. / 9, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(1. / 3, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(-1, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void divideTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{-1, 2, -3}, new int[]{3, 2, 1}, new DoubleRationalFactory());
        EpsilonPolynomial rational2 = new EpsilonPolynomial(1, 3, new DoubleRationalFactory());

        EpsilonPolynomial result = rational1.divide(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-1, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(3, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(-9, result.getPolynomial()[2].doubleValue(), 1e-4);
    }
}
