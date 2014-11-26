package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;

import java.util.Map;

public class ParamSimplexData {
    public Rational[][] tableau;
    public Map<Object, Integer> variableIndices;
    public Map<Object, Integer> constraintIndices;

    public ParamSimplexData(Rational[][] tableau) {
        this.tableau = tableau;
    }
}
