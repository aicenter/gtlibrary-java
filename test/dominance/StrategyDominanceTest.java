package dominance;

import cz.agents.gtlibrary.utils.StrategyDominance;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class StrategyDominanceTest {

    @Test
    public void pureDominanceTest1() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 2, 3});
        utilities.put(2, new double[]{1, 2, 5});
        utilities.put(3, new double[]{0, 0, 0});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();

        expectedWeaklyDominatedStrategies.add(1);
        expectedWeaklyDominatedStrategies.add(3);

        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();

        expectedStronglyDominatedStrategies.add(3);

        StrategyDominance.DominanceResult result = dominance.computePureDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }

    @Test
    public void pureDominanceTest2() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 2, 3});
        utilities.put(2, new double[]{0, 2, 5});
        utilities.put(3, new double[]{2, 0, 0});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();
        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();
        StrategyDominance.DominanceResult result = dominance.computePureDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }

    @Test
    public void pureDominanceTest3() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 0});
        utilities.put(2, new double[]{1, 0});
        utilities.put(3, new double[]{1, 0});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();
        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();
        StrategyDominance.DominanceResult result = dominance.computePureDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }

    @Test
    public void mixedDominanceTest1() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 0});
        utilities.put(2, new double[]{0, 3});
        utilities.put(3, new double[]{3, -1});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();

        expectedWeaklyDominatedStrategies.add(1);
        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();

        expectedStronglyDominatedStrategies.add(1);
        StrategyDominance.DominanceResult result = dominance.computeMixedDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }

    @Test
    public void mixedDominanceTest2() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 0});
        utilities.put(2, new double[]{1, 0});
        utilities.put(3, new double[]{1, 0});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();
        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();
        StrategyDominance.DominanceResult result = dominance.computeMixedDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }

    @Test
    public void mixedDominanceTest3() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 0});
        utilities.put(2, new double[]{1.2, -0.2});
        utilities.put(3, new double[]{0.8, 0.2});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();
        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();
        StrategyDominance.DominanceResult result = dominance.computeMixedDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }

    @Test
    public void mixedDominanceTest4() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 0});
        utilities.put(2, new double[]{1.201, -0.2});
        utilities.put(3, new double[]{0.8, 0.2});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();

        expectedWeaklyDominatedStrategies.add(1);
        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();

        expectedStronglyDominatedStrategies.add(1);
        StrategyDominance.DominanceResult result = dominance.computeMixedDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }


    @Test
    public void mixedDominanceTest5() {
        StrategyDominance<Integer> dominance = new StrategyDominance<>();
        Map<Integer, double[]> utilities = new HashMap<>();

        utilities.put(1, new double[]{1, 0, 1});
        utilities.put(2, new double[]{1.2, -0.2, 2});
        utilities.put(3, new double[]{0.8, 0.2, 2});
        Set<Integer> expectedWeaklyDominatedStrategies = new HashSet<>();

        expectedWeaklyDominatedStrategies.add(1);
        Set<Integer> expectedStronglyDominatedStrategies = new HashSet<>();
        StrategyDominance.DominanceResult result = dominance.computeMixedDominance(utilities);

        assertEquals(expectedWeaklyDominatedStrategies, result.weaklyDominatedStrategies);
        assertEquals(expectedStronglyDominatedStrategies, result.stronglyDominatedStrategies);
    }
}
