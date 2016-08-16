package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.OracleCandidate;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Map;

public interface GameExpander {
    public void expand(SequenceFormIRConfig config, OracleCandidate candidate);
    public void expand(SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse);
    public long getBRTime();
    public long getSelfTime();
}
