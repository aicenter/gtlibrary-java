package cz.agents.gtlibrary.algorithms.sequenceform.gensum.parammilp;

import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.SolverResult;
import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.Rational;

public class Simplex implements Algorithm {

    private GenSumSequenceFormConfig config;
    private Rational[][] tableau;

    @Override
    public SolverResult compute() {
        build();
        return solve();
    }

    private SolverResult solve() {
        return null;
    }

    private void build() {
        

    }
}
