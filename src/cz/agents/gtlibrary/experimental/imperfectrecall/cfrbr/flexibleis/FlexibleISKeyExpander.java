package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleis;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;

public class FlexibleISKeyExpander<I extends InformationSet> extends ExpanderImpl<I> {
    private Expander<I> wrappedExpander;

    public FlexibleISKeyExpander(Expander<I> wrappedExpander) {
        super(wrappedExpander.getAlgorithmConfig());
        this.wrappedExpander = wrappedExpander;
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        return wrappedExpander.getActions(((FlexibleISKeyGameState)gameState).getWrappedState());
    }
}
