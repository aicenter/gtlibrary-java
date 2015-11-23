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
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2, 3}, new int[]{3, 2, 1}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5, 6}, new int[]{7, 8, 9}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        EpsilonPolynomial result = rational1.add(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(0.9048, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(1.625, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(3.66666, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void addDifferentLengthsTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2, 3}, new int[]{3, 2, 1}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5}, new int[]{7, 8}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        EpsilonPolynomial result = rational1.add(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(0.9048, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(1.625, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(3, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void addDifferentLengths1Test() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2}, new int[]{3, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5, 6}, new int[]{7, 8, 9}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        EpsilonPolynomial result = rational1.add(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(0.9048, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(1.625, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(0.66666, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void subtractLongTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2, 3}, new int[]{3, 2, 1}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5, 6}, new int[]{7, 8, 9}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        EpsilonPolynomial result = rational1.subtract(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-0.23809, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(0.375, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(2.333333, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void subtractDifferentLengthsTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2, 3}, new int[]{3, 2, 1}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5}, new int[]{7, 8}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial result = rational1.subtract(rational2);

        assertEquals(3, result.getPolynomial().length);
        assertEquals(-0.23809, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(0.375, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(3, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void subtractDifferentLengths1Test() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{1, 2}, new int[]{3, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(new int[]{4, 5, 6}, new int[]{7, 8, 9}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial result = rational1.subtract(rational2);

        assertEquals(3, result.getPolynomial().length);
        assertEquals(-0.23809, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(0.375, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(-0.6666666, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void negateTest() {
        EpsilonPolynomial rational = new EpsilonPolynomial(new int[]{1, 2, -3}, new int[]{3, 2, 1}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        EpsilonPolynomial result = rational.negate();
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-1. / 3, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(-1, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(3, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void multiplyTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{-1, 2, -3}, new int[]{3, 2, 1}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(1, 3, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        EpsilonPolynomial result = rational1.multiply(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-1. / 9, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(1. / 3, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(-1, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void divideTest() {
        EpsilonPolynomial rational1 = new EpsilonPolynomial(new int[]{-1, 2, -3}, new int[]{3, 2, 1}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial rational2 = new EpsilonPolynomial(1, 3, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        EpsilonPolynomial result = rational1.divide(rational2);
        assertEquals(3, result.getPolynomial().length);
        assertEquals(-1, result.getPolynomial()[0].doubleValue(), 1e-4);
        assertEquals(3, result.getPolynomial()[1].doubleValue(), 1e-4);
        assertEquals(-9, result.getPolynomial()[2].doubleValue(), 1e-4);
    }

    @Test
    public void isZeroSimpleTest() {
        assertEquals(true, new EpsilonPolynomialFactory(new DoubleRationalFactory()).zero().isZero());
    }

    @Test
    public void isZeroSimpleTest2() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());

        assertEquals(true, factory.one().subtract(factory.one()).isZero());
    }

    @Test
    public void isZeroSimpleTest3() {
        assertEquals(false, new EpsilonPolynomialFactory(new DoubleRationalFactory()).one().isZero());
    }

    @Test
    public void isZeroTest() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());

        assertEquals(true, factory.one().add(factory.one()).multiply(factory.create(2)).divide(factory.create(4)).subtract(factory.one()).isZero());
    }

    @Test
    public void isOneSimpleTest() {
        assertEquals(true, new EpsilonPolynomialFactory(new DoubleRationalFactory()).one().isOne());
    }

    @Test
    public void isOneSimpleTest2() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());

        assertEquals(true, factory.one().multiply(factory.one()).isOne());
    }

    @Test
    public void isOneSimpleTest3() {
        assertEquals(false, new EpsilonPolynomialFactory(new DoubleRationalFactory()).zero().isOne());
    }

    @Test
    public void isOneTest() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());

        assertEquals(true, factory.one().add(factory.one()).multiply(factory.create(2)).divide(factory.create(4)).subtract(factory.create(2)).negate().isOne());
    }

    @Test
    public void  compareToSimpleTest() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());

        assertEquals(true, factory.one().compareTo(factory.zero()) > 0);
    }

    @Test
    public void  compareToSimpleTest1() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());

        assertEquals(true, factory.one().compareTo(factory.create(5)) < 0);
    }

    @Test
    public void  compareToSimpleTest2() {
        EpsilonPolynomialFactory factory = new EpsilonPolynomialFactory(new DoubleRationalFactory());

        assertEquals(true, factory.one().compareTo(factory.one()) == 0);
    }

    @Test
    public void  compareToLongTest() {
        EpsilonPolynomial polynomial1 = new EpsilonPolynomial(new int[]{2, 3, 4}, new int[]{6, 7, 8}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial polynomial2 = new EpsilonPolynomial(new int[]{1, 3, 1}, new int[]{3, 7, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        assertEquals(true, polynomial1.compareTo(polynomial2) == 0);
    }

    @Test
    public void  compareToLongTest1() {
        EpsilonPolynomial polynomial1 = new EpsilonPolynomial(new int[]{2, 4, 4}, new int[]{6, 7, 8}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial polynomial2 = new EpsilonPolynomial(new int[]{1, 3, 1}, new int[]{3, 7, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        assertEquals(true, polynomial1.compareTo(polynomial2) > 0);
    }

    @Test
    public void  compareToLongTest2() {
        EpsilonPolynomial polynomial1 = new EpsilonPolynomial(new int[]{2, 3, 4}, new int[]{6, 7, 9}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial polynomial2 = new EpsilonPolynomial(new int[]{1, 3, 1}, new int[]{3, 7, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        assertEquals(true, polynomial1.compareTo(polynomial2) < 0);
    }

    @Test
    public void  compareToDifferentLengthsTest() {
        EpsilonPolynomial polynomial1 = new EpsilonPolynomial(new int[]{2, 3}, new int[]{6, 7}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial polynomial2 = new EpsilonPolynomial(new int[]{1, 3, 1}, new int[]{3, 7, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        assertEquals(true, polynomial1.compareTo(polynomial2) < 0);
    }

    @Test
    public void  compareToDifferentLengthsTest1() {
        EpsilonPolynomial polynomial1 = new EpsilonPolynomial(new int[]{2, 3}, new int[]{6, 7}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial polynomial2 = new EpsilonPolynomial(new int[]{1, 3, -1}, new int[]{3, 7, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        assertEquals(true, polynomial1.compareTo(polynomial2) > 0);
    }

    @Test
    public void  compareToDifferentLengthsTest2() {
        EpsilonPolynomial polynomial1 = new EpsilonPolynomial(new int[]{2, 2}, new int[]{6, 7}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial polynomial2 = new EpsilonPolynomial(new int[]{1, 3, -1}, new int[]{3, 7, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        assertEquals(true, polynomial1.compareTo(polynomial2) < 0);
    }

    @Test
    public void  compareToDifferentLengthsTest3() {
        EpsilonPolynomial polynomial1 = new EpsilonPolynomial(new int[]{2, 3}, new int[]{6, 7}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));
        EpsilonPolynomial polynomial2 = new EpsilonPolynomial(new int[]{1, 3, 0}, new int[]{3, 7, 2}, new EpsilonPolynomialFactory(new DoubleRationalFactory()));

        assertEquals(true, polynomial1.compareTo(polynomial2) == 0);
    }
}
