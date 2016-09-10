package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;
import java.util.stream.Collectors;

public class RandomAbstractionExpander<I extends InformationSet> extends ExpanderImpl<I> {
    private Expander<I> wrappedExpander;

    public RandomAbstractionExpander(Expander<I> wrappedExpander, AlgorithmConfig<I> config) {
        super(config);
        this.wrappedExpander = wrappedExpander;
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        return wrappedExpander.getActions(((RandomAbstractionGameState)gameState).getWrappedGameState()).stream()
                .map(a -> new RandomAbstractionAction(a, getAlgorithmConfig().getInformationSetFor(gameState))).collect(Collectors.toList());
    }
}
