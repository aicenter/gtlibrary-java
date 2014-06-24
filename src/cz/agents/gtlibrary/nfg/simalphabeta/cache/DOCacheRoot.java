package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.HashMap;
import java.util.Map;

public class DOCacheRoot extends DOCacheImpl {

    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> strategies;
    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> tempStrategies;

    public DOCacheRoot() {
        super();
        this.strategies = new HashMap<>();
        this.tempStrategies = new HashMap<>();
    }

    @Override
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, MixedStrategy<ActionPureStrategy>[] strategy) {
        setStrategy(new Triplet<>(strategy1, strategy2, strategy3), strategy);
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, MixedStrategy<ActionPureStrategy>[] strategy) {
        assert strategy != null;
        strategies.put(strategyTriplet, strategy);
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3) {
        return getStrategy(new Triplet<>(strategy1, strategy2, strategy3));
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet) {
        return strategies.get(strategyTriplet);
    }

    @Override
    public Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> getStrategies() {
        return strategies;
    }

    @Override
    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, MixedStrategy<ActionPureStrategy>[] strategiesFromAlphaBeta) {
        tempStrategies.put(actionTriplet, strategiesFromAlphaBeta);
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return tempStrategies.get(actionTriplet);
    }
}
