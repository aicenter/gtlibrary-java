package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.List;
import java.util.Map;

public class SimplexSolverResult {

    Map<Object, EpsilonPolynomial> variableValues;
    Map<Object, Integer> variableIndices;
    Map<Sequence, Double> p1Rp;
    Map<Sequence, Double> p2Rp;
    EpsilonPolynomial value;
    EpsilonPolynomial[][] tableau;
    List<Integer> basis;
    Indices firstPhaseSlacks;

    public SimplexSolverResult(Map<Sequence, Double> p1Rp, Map<Sequence, Double> p2Rp, EpsilonPolynomial value,
                               EpsilonPolynomial[][] tableau, List<Integer> basis,  Map<Object, EpsilonPolynomial> variableValues,
                               Indices firstPhaseSlacks, Map<Object, Integer> variableIndices) {
        this.p1Rp = p1Rp;
        this.p2Rp = p2Rp;
        this.value = value;
        this.tableau = tableau;
        this.basis = basis;
        this.variableValues = variableValues;
        this.firstPhaseSlacks = firstPhaseSlacks;
        this.variableIndices = variableIndices;
    }
}
