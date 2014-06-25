package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;

public class SimAlphaBetaResult {
    public MixedStrategy<ActionPureStrategy> mixedStrategy;
    public DOCache cache;
    public double gameValue;

    public SimAlphaBetaResult(MixedStrategy<ActionPureStrategy> mixedStrategy, DOCache cache, double gameValue) {
        this.mixedStrategy = mixedStrategy;
        this.cache = cache;
        this.gameValue = gameValue;
    }
}
