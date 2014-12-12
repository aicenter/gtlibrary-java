package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

public class FirstPhaseParametricSolverResult {
    Map<Sequence, Double> p1Rp;
    Map<Sequence, Double> p2Rp;
    EpsilonPolynomial value;

    public FirstPhaseParametricSolverResult(Map<Sequence, Double> p1Rp, Map<Sequence, Double> p2Rp, EpsilonPolynomial value) {
        this.p1Rp = p1Rp;
        this.p2Rp = p2Rp;
        this.value = value;
    }
}
