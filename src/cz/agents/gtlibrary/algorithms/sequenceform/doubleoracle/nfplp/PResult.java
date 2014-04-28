package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.nfplp;


import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class PResult {
    private Map<Sequence, Double> realizationPlan;
    private double gameValue;

    public PResult(Map<Sequence, Double> realizationPlan, double gameValue) {
        this.realizationPlan = realizationPlan;
        this.gameValue = gameValue;
    }

    public double getGameValue() {
        return gameValue;
    }

    public Map<Sequence, Double> getRealizationPlan() {
        return realizationPlan;
    }
}
