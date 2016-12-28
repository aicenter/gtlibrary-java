package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleisdomain;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;
import java.util.stream.Collectors;

public class FlexibleISKeyExpander<I extends InformationSet> extends ExpanderImpl<I> {
    private Expander<I> wrappedExpander;

    public FlexibleISKeyExpander(Expander<I> wrappedExpander, AlgorithmConfig<I> config) {
        super(config);
        this.wrappedExpander = wrappedExpander;
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        return wrappedExpander.getActions(((FlexibleISKeyGameState)gameState).getWrappedState()).stream().map(a ->
                new FlexibleISAction(getAlgorithmConfig().getInformationSetFor(gameState), a)).collect(Collectors.toList());
    }
}
