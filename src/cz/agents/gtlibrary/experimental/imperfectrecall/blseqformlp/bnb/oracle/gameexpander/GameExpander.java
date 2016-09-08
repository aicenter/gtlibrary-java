package cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.gameexpander;

import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.SequenceFormIRConfig;
import cz.agents.gtlibrary.experimental.imperfectrecall.blseqformlp.bnb.oracle.candidate.OracleCandidate;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.Map;

public interface GameExpander {
    public boolean expand(SequenceFormIRConfig config, OracleCandidate candidate);
    public boolean expand(SequenceFormIRConfig config, Map<Action, Double> minPlayerBestResponse);
    public long getBRTime();
    public long getSelfTime();
    public long getBRExpandedNodes();
}
