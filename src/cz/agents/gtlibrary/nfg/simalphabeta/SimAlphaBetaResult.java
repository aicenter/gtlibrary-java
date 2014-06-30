package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;

public class SimAlphaBetaResult extends Result {
    public DOCache cache;

    public SimAlphaBetaResult(MixedStrategy<ActionPureStrategy> mixedStrategy, DOCache cache, double gameValue) {
        super(gameValue, mixedStrategy);
        this.cache = cache;
    }
}
