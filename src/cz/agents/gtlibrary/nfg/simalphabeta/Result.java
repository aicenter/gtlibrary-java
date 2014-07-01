package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;

public class Result {
    public double gameValue;
    public MixedStrategy<ActionPureStrategy> strategy;

    public Result(double gameValue, MixedStrategy<ActionPureStrategy> strategy) {
        this.gameValue = gameValue;
        this.strategy = strategy;
    }
}
