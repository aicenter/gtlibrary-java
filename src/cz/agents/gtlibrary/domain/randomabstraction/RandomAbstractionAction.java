package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.domain.randomgameimproved.RandomGameAction;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

public class RandomAbstractionAction extends ActionImpl {

    public Map<ISKey, Action> wrappedActions;
    int hashCode = -1;

    public RandomAbstractionAction(GameState state, Action wrappedAction, InformationSet informationSet) {
        super(informationSet);
        wrappedActions = new HashMap<>();
        wrappedActions.put(state.getISKeyForPlayerToMove(), wrappedAction);
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

        return ((RandomGameAction) wrappedActions.values().stream().findAny().get()).getOrder() ==
                ((RandomGameAction) that.wrappedActions.values().stream().findAny().get()).getOrder();
    }

    @Override
    public int hashCode() {
        if (hashCode == -1)
            hashCode = new HashCodeBuilder(17, 31).append(informationSet).append(((RandomGameAction) wrappedActions.values().stream().findAny().get()).getOrder()).toHashCode();
        return hashCode;
    }

    @Override
    public String toString() {
        return informationSet.hashCode() +
                "OA:" + ((RandomGameAction) wrappedActions.values().stream().findAny().get()).getOrder();
    }

    public void add(GameState gameState, Action action) {
        wrappedActions.put(gameState.getISKeyForPlayerToMove(), action);
        assert wrappedActions.values().stream().map(a -> ((RandomGameAction) a).getOrder()).distinct().count() == 1;
    }
}
