package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.expandconditions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;

public class DummyExpandCondition implements ExpandCondition {
    @Override
    public boolean validForExpansion(SequenceFormIRConfig config, Candidate candidate) {
        return false;
    }
}
