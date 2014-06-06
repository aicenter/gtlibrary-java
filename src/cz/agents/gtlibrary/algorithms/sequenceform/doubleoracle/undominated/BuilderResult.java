package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.undominated;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class BuilderResult {
    private double gameValue;
    private Map<Sequence,Double> realizationPlan;
    private long lpSolvingTime;

    public BuilderResult(double gameValue, Map<Sequence, Double> realizationPlan, long lpSolvingTime) {
        this.gameValue = gameValue;
        this.realizationPlan = realizationPlan;
        this.lpSolvingTime = lpSolvingTime;
    }

    public double getGameValue() {
        return gameValue;
    }

    public Map<Sequence, Double> getRealizationPlan() {
        return realizationPlan;
    }

    public long getLpSolvingTime() {
        return lpSolvingTime;
    }
}
