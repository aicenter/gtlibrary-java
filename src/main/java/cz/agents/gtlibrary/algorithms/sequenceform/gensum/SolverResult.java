package cz.agents.gtlibrary.algorithms.sequenceform.gensum;

import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class SolverResult {
    public Map<Sequence, Double> p1RealPlan;
    public Map<Sequence, Double> p2RealPlan;
    public long time;

    public SolverResult(Map<Sequence, Double> p1RealPlan, Map<Sequence, Double> p2RealPlan, long time) {
        this.p1RealPlan = p1RealPlan;
        this.p2RealPlan = p2RealPlan;
        this.time = time;
    }
}
