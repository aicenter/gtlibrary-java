package cz.agents.gtlibrary.nfg.simalphabeta.cache;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.utils.Triplet;

import java.util.HashMap;
import java.util.Map;

public class DOCacheRoot extends DOCacheImpl {

    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> strategies;
    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>> p1TempStrategies;
    private Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>> p2TempStrategies;

    public DOCacheRoot() {
        super();
        this.strategies = new HashMap<>();
        this.p1TempStrategies = new HashMap<>();
        this.p2TempStrategies = new HashMap<>();
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
    public void setStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3, MixedStrategy<ActionPureStrategy> p1strategy, MixedStrategy<ActionPureStrategy> p2strategy) {
        setStrategy(new Triplet<>(strategy1, strategy2, strategy3), new MixedStrategy[]{p1strategy, p2strategy});
    }

    @Override
    public void setStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet, MixedStrategy<ActionPureStrategy> p1Strategy, MixedStrategy<ActionPureStrategy> p2Strategy) {
        setStrategy(strategyTriplet, new MixedStrategy[]{p1Strategy, p2Strategy});
    }

    @Override
    public void setTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet, Player player, MixedStrategy<ActionPureStrategy> strategyFromAlphaBeta) {
        if (player.getId() == 0)
            p1TempStrategies.put(actionTriplet, strategyFromAlphaBeta);
        else
            p2TempStrategies.put(actionTriplet, strategyFromAlphaBeta);
    }

    @Override
    public MixedStrategy<ActionPureStrategy> getP1TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return p1TempStrategies.get(actionTriplet);
    }

    @Override
    public MixedStrategy<ActionPureStrategy> getP2TempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        return p2TempStrategies.get(actionTriplet);
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(ActionPureStrategy strategy1, ActionPureStrategy strategy2, ActionPureStrategy strategy3) {
        return getStrategy(new Triplet<>(strategy1, strategy2, strategy3));
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getTempStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> actionTriplet) {
        MixedStrategy<ActionPureStrategy> p1Strategy = getP1TempStrategy(actionTriplet);
        MixedStrategy<ActionPureStrategy> p2Strategy = getP2TempStrategy(actionTriplet);

        assert (p1Strategy != null && p2Strategy != null) || (p1Strategy == null && p2Strategy == null);
        return new MixedStrategy[]{p1Strategy, p2Strategy};
    }

    @Override
    public MixedStrategy<ActionPureStrategy>[] getStrategy(Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy> strategyTriplet) {
        return strategies.get(strategyTriplet);
    }

    @Override
    public Map<Triplet<ActionPureStrategy, ActionPureStrategy, ActionPureStrategy>, MixedStrategy<ActionPureStrategy>[]> getStrategies() {
        return strategies;
    }

}
