package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.expandconditions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleCandidate;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExpandConditionImpl implements ExpandCondition {

    @Override
    public boolean validForExpansion(SequenceFormIRConfig config, OracleCandidate candidate) {
        double boundGap = candidate.getUb() - candidate.getLb();

        return boundGap > candidate.getPrecisionError();
    }
}