package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RandomAbstractionExpander<I extends InformationSet> extends ExpanderImpl<I> {
    private Expander<? extends SequenceInformationSet> wrappedExpander;
    private Map<ISKey, List<Action>> actions;

    public RandomAbstractionExpander(Expander<? extends SequenceInformationSet> wrappedExpander, AlgorithmConfig<I> config) {
        super(config);
        this.wrappedExpander = wrappedExpander;
        actions = new HashMap<>();
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        GameState wrappedState = ((RandomAbstractionGameState) gameState).getWrappedGameState();
        List<Action> wrappedActions = wrappedExpander.getActions(wrappedState);
        List<Action> currentActions = actions.getOrDefault(gameState.getISKeyForPlayerToMove(),
                wrappedActions.stream()
                        .map(a -> new RandomAbstractionAction(wrappedState, a, getAlgorithmConfig().getInformationSetFor(gameState)))
                        .collect(Collectors.toList()));
        for (int i = 0; i < currentActions.size(); i++) {
            ((RandomAbstractionAction)currentActions.get(i)).add(wrappedState, wrappedActions.get(i));
        }
        this.actions.put(gameState.getISKeyForPlayerToMove(), currentActions);
        return currentActions;
    }

}
