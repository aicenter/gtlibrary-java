package algorithm.imperfectrecall.digitarray;

import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestExpander;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameInfo;
import cz.agents.gtlibrary.domain.imperfectrecall.brtest.BRTestGameState;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.change.number.DigitArray;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.br.OracleImperfectRecallBestResponse;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.BasicGameBuilder;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DigitArrayTest {

    @Test
    public void DigitArrayAddTest() {
        DigitArray addend = new DigitArray(new int[]{0, 1, 2}, true);
        DigitArray addend1 = new DigitArray(new int[]{0, 1, 2, 3}, true);

        assertArrayEquals(new int[]{0, 2, 4, 3}, addend.add(addend1).getArray());
    }

    @Test
    public void DigitArrayAddTest1() {
        DigitArray addend = new DigitArray(new int[]{0, 1, 2}, true);
        DigitArray addend1 = new DigitArray(new int[]{0, 1, 9, 3}, true);

        assertArrayEquals(new int[]{0, 3, 1, 3}, addend.add(addend1).getArray());
    }

    @Test
    public void DigitArrayAddTest2() {
        DigitArray addend = new DigitArray(new int[]{0, 1, 2}, true);
        DigitArray addend1 = new DigitArray(new int[]{0, 1, 9, 3}, false);

        assertEquals(new DigitArray(new int[]{0, 0, 7, 3}, false), addend.add(addend1));
    }

    @Test
    public void DigitArrayAddTest3() {
        DigitArray addend = new DigitArray(new int[]{0, 1, 2}, false);
        DigitArray addend1 = new DigitArray(new int[]{0, 1, 9, 3}, false);

        assertEquals(new DigitArray(new int[]{0, 3, 1, 3}, false), addend.add(addend1));
    }

    @Test
    public void DigitArraySubtractTest() {
        DigitArray minuend = new DigitArray(new int[]{0, 2, 2}, true);
        DigitArray subtrahend = new DigitArray(new int[]{0, 1, 2, 3}, true);

        assertEquals(new DigitArray(new int[]{0, 0, 9, 7}, true), minuend.subtract(subtrahend));
    }

    @Test
    public void DigitArraySubtractTest1() {
        DigitArray minuend = new DigitArray(new int[]{0, 1, 2}, true);
        DigitArray subtrahend = new DigitArray(new int[]{0, 1, 2, 3}, true);

        assertEquals(new DigitArray(new int[]{0, 0, 0, 3}, false), minuend.subtract(subtrahend));
    }

    @Test
    public void DigitArraySubtractTest2() {
        DigitArray minuend = new DigitArray(new int[]{0, 1, 2}, true);
        DigitArray subtrahend = new DigitArray(new int[]{0, 1, 2, 3}, false);

        assertEquals(new DigitArray(new int[]{0, 2, 4, 3}, true), minuend.subtract(subtrahend));
    }

    @Test
    public void DigitArraySubtractTest3() {
        DigitArray minuend = new DigitArray(new int[]{0, 1, 2}, false);
        DigitArray subtrahend = new DigitArray(new int[]{0, 1, 2, 3}, true);

        assertEquals(new DigitArray(new int[]{0, 2, 4, 3}, false), minuend.subtract(subtrahend));
    }

    @Test
    public void DigitArraySubtractTest4() {
        DigitArray minuend = new DigitArray(new int[]{0, 1, 2}, false);
        DigitArray subtrahend = new DigitArray(new int[]{0, 1, 2, 3}, false);

        assertEquals(new DigitArray(new int[]{0, 0, 0, 3}, true), minuend.subtract(subtrahend));
    }

    @Test
    public void DigitArraySubtractTest5() {
        DigitArray minuend = DigitArray.ONE;
        DigitArray subtrahend = new DigitArray(new int[]{0, 0, 2}, true);

        assertEquals(new DigitArray(new int[]{0, 9, 8}, true), minuend.subtract(subtrahend));
    }
    @Test
    public void DigitArrayIsGreaterThanTest() {
        DigitArray digit = new DigitArray(new int[]{0, 2, 2}, true);
        DigitArray digit1 = new DigitArray(new int[]{0, 2, 2, 0}, true);

        assertEquals(false, digit1.isGreaterThan(digit));
    }

    @Test
    public void DigitArrayIsGreaterThanTest1() {
        DigitArray digit = new DigitArray(new int[]{0, 2, 2}, false);
        DigitArray digit1 = new DigitArray(new int[]{0, 2, 2, 0}, true);

        assertEquals(true, digit1.isGreaterThan(digit));
    }

    @Test
    public void DigitArrayIsGreaterThanTest2() {
        DigitArray digit = new DigitArray(new int[]{0, 2, 2}, true);
        DigitArray digit1 = new DigitArray(new int[]{0, 2, 2, 0}, false);

        assertEquals(false, digit1.isGreaterThan(digit));
    }

    @Test
    public void DigitArrayIsGreaterThanTest3() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, true);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, true);

        assertEquals(false, digit1.isGreaterThan(digit));
    }

    @Test
    public void DigitArrayIsGreaterThanTest4() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, true);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, true);

        assertEquals(false, digit.isGreaterThan(digit1));
    }

    @Test
    public void DigitArrayIsGreaterThanTest5() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, true);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, false);

        assertEquals(false, digit1.isGreaterThan(digit));
    }

    @Test
    public void DigitArrayIsGreaterThanTest6() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, false);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, true);

        assertEquals(false, digit.isGreaterThan(digit1));
    }

    @Test
    public void DigitArrayEqualsTest() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, true);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, true);

        assertEquals(true, digit1.equals(digit));
    }

    @Test
    public void DigitArrayEqualsTest1() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, true);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, false);

        assertEquals(true, digit1.equals(digit));
    }

    @Test
    public void DigitArrayEqualsTest2() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, false);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, false);

        assertEquals(true, digit1.equals(digit));
    }

    @Test
    public void DigitArrayEqualsTest3() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 0}, false);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, true);

        assertEquals(true, digit1.equals(digit));
    }

    @Test
    public void DigitArrayEqualsTest4() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 1}, false);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 0, 0}, true);

        assertEquals(false, digit1.equals(digit));
    }

    @Test
    public void DigitArrayEqualsTest5() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 1}, false);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 1, 0}, true);

        assertEquals(false, digit1.equals(digit));
    }

    @Test
    public void DigitArrayEqualsTest6() {
        DigitArray digit = new DigitArray(new int[]{0, 0, 1}, false);
        DigitArray digit1 = new DigitArray(new int[]{0, 0, 1, 0}, false);

        assertEquals(true, digit1.equals(digit));
    }

    @Test
    public void DigitArrayAverageTest() {
        DigitArray digit = new DigitArray(new int[]{0, 4, 2}, true);
        DigitArray digit1 = new DigitArray(new int[]{1, 7, 7, 0}, true);

        assertArrayEquals(new int[]{1, 0, 9, 5}, DigitArray.getAverage(digit, digit1, 4).getArray());
    }

    @Test
    public void DigitArrayAverageTest1() {
        DigitArray digit = new DigitArray(new int[]{0, 4, 2}, true);
        DigitArray digit1 = new DigitArray(new int[]{1, 2, 7, 0}, true);

        assertArrayEquals(new int[]{0, 8, 5}, DigitArray.getAverage(digit, digit1, 3).getArray());
    }
}
