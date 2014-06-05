package cz.agents.gtlibrary.algorithms.sequenceform.refinements.nfp.reusing;


import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class PResultReuse {
    private Map<Sequence, Double> realizationPlan;
    private double gameValue;

    public PResultReuse(Map<Sequence, Double> realizationPlan, double gameValue) {
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
