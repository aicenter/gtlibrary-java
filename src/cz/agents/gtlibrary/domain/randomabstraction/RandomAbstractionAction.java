package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameAction;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

public class RandomAbstractionAction extends ActionImpl {

    public Map<GameState, Action> wrappedActions;

    public RandomAbstractionAction(GameState state, Action wrappedAction, InformationSet informationSet) {
        super(informationSet);
        wrappedActions = new HashMap<>();
        wrappedActions.put(state, wrappedAction);
    }
    @Override
    public void perform(GameState gameState) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomAbstractionAction)) return false;
        if (!super.equals(o)) return false;

        RandomAbstractionAction that = (RandomAbstractionAction) o;

        return ((RandomGameAction)wrappedActions.values().stream().findAny().get()).getOrder() ==
                ((RandomGameAction)that.wrappedActions.values().stream().findAny().get()).getOrder();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17,31).append(informationSet).append(((RandomGameAction)wrappedActions.values().stream().findAny().get()).getOrder()).toHashCode();
    }

    @Override
    public String toString() {
        return wrappedActions.toString();
    }

    public void add(GameState gameState, Action action) {
        wrappedActions.put(gameState, action);
    }
}
