package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.expandconditions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;

public class ExpandConditionImpl implements ExpandCondition {

    @Override
    public boolean validForExpansion(SequenceFormIRConfig config, OracleCandidate candidate) {
        double boundGap = candidate.getUb() - candidate.getLb();

        return boundGap > candidate.getPrecisionError();
    }
}