package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public class DOCacheRoot extends DOCacheImpl {

    private Map<Pair<ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> strategies;
    private Map<Pair<ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> tempStrategies;

    public DOCacheRoot() {
        super();
        this.strategies = new HashMap<>();
        this.tempStrategies = new HashMap<>();
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, MixedStrategy<ActionPureStrategy>[] strategy) {
        setStrategy(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2), strategy);
    }

    @Override
    public void setStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair, MixedStrategy<ActionPureStrategy>[] strategy) {
        strategies.put(strategyPair, strategy);
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2) {
        return getStrategy(new Pair<ActionPureStrategy, ActionPureStrategy>(strategy1, strategy2));
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(Pair<ActionPureStrategy, ActionPureStrategy> strategyPair) {
        assert strategyPair != null;
        return strategies.get(strategyPair);
    }

    @Override
    public void setTempStrategy(Pair<ActionPureStrategy, ActionPureStrategy> actionPair, MixedStrategy<ActionPureStrategy>[] strategiesFromAlphaBeta) {
        tempStrategies.put(actionPair, strategiesFromAlphaBeta);
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getTempStrategy(Pair<ActionPureStrategy, ActionPureStrategy> actionPair) {
        return tempStrategies.get(actionPair);
    }
}
