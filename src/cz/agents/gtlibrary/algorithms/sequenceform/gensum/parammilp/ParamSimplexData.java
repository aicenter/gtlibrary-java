package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp.numbers.EpsilonPolynomial;
import cz.agents.gtlibrary.interfaces.InformationSet;

import java.util.List;
import java.util.Map;

public class ParamSimplexData {
    public EpsilonPolynomial[][] tableau;
    public Map<Object, Integer> variableIndices;
    public Map<Object, Integer> constraintIndices;
    public List<Integer> basis;

    public ParamSimplexData(EpsilonPolynomial[][] tableau, List<Integer> basis) {
        this.tableau = tableau;
        this.basis = basis;
    }
}
