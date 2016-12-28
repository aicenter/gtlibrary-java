package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ExpectedValues {
    double[] expectedValues;

    public ExpectedValues() {
        expectedValues = new double[4];
    }

    public ExpectedValues(double brExpectedValue, double currentExpectedValue, double realNewExpectedValue) {
        expectedValues = new double[]{brExpectedValue, currentExpectedValue, realNewExpectedValue, 0};
    }

    public ExpectedValues(double[] expectedValues) {
        this.expectedValues = expectedValues;
    }

    public double getBRExpectedValue() {
        return expectedValues[0];
    }

    public double getCurrentExpectedValue() {
        return expectedValues[1];
    }

    public double getRealNewExpectedValue() {
        return expectedValues[2];
    }

    public double getExpectedNewExpectedValue() {
        return expectedValues[3];
    }

    public double getRealvsExpectedDistance() {
        return getRealNewExpectedValue() - getExpectedNewExpectedValue();
    }

    @Override
    public String toString() {
        return Arrays.toString(expectedValues);
    }

    public void add(ExpectedValues expectedValuesForAction) {
        IntStream.range(0, expectedValues.length).forEach(i -> expectedValues[i] += expectedValuesForAction.expectedValues[i]);
    }

    public void updateExpectedExpectedValue(double brWeight) {
        expectedValues[3] = getBRExpectedValue()*brWeight + getCurrentExpectedValue()*(1 - brWeight);
    }

}
