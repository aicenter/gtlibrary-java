package cz.agents.gtlibrary.strategy;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Collection;
import java.util.Map;

public class NoMissingSeqStrategy extends StrategyImpl {

    public NoMissingSeqStrategy() {
    }

    public NoMissingSeqStrategy(Map<Sequence, Double> realizationPlan) {
        super(realizationPlan);
    }

    @Override
    protected Map<Action, Double> getMissingSeqDistribution(Collection<Action> actions) {
        throw new UnsupportedOperationException("Missing sequences...");
    }
}