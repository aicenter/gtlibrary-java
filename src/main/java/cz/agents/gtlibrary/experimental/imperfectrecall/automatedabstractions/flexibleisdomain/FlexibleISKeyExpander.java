package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlexibleISKeyExpander<I extends InformationSet> extends ExpanderImpl<I> {
    private Expander<I> wrappedExpander;
    private Map<ISKey, IRCFRInformationSet> informationSets;


    public FlexibleISKeyExpander(Expander<I> wrappedExpander, AlgorithmConfig<I> config, Map<ISKey, IRCFRInformationSet> informationSets) {
        super(config);
        this.wrappedExpander = wrappedExpander;
        this.informationSets = informationSets;
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        return wrappedExpander.getActions(((FlexibleISKeyGameState) gameState).getWrappedState()).stream().map(a ->
                new FlexibleISAction(getAlgorithmConfig().getInformationSetFor(gameState), a, gameState, informationSets)).collect(Collectors.toList());
    }

    public Expander<I> getWrappedExpander() {
        return wrappedExpander;
    }
}
