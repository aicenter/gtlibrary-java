package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.expandconditions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;

public interface ExpandCondition {
    public boolean validForExpansion(SequenceFormIRConfig config, Candidate candidate);
}
