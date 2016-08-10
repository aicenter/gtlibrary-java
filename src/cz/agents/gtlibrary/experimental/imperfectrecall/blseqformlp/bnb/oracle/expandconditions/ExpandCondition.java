package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.expandconditions;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.Candidate;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleCandidate;

public interface ExpandCondition {
    public boolean validForExpansion(SequenceFormIRConfig config, OracleCandidate candidate);
}
