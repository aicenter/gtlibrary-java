package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.doubleoracle.DOCandidate;

public interface GameExpander {
    public void expand(SequenceFormIRConfig config, DOCandidate candidate);
}
