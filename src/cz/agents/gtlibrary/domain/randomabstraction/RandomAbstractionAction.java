package cz.agents.gtlibrary.domain.randomabstraction;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class RandomAbstractionAction extends ActionImpl {

    public Action wrappedAction;

    public RandomAbstractionAction(Action wrappedAction, InformationSet informationSet) {
        super(informationSet);
        this.wrappedAction = wrappedAction;
    }
    @Override
    public void perform(GameState gameState) {
        wrappedAction.perform(((RandomAbstractionGameState)gameState).getWrappedGameState());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RandomAbstractionAction)) return false;
        if (!super.equals(o)) return false;

        RandomAbstractionAction that = (RandomAbstractionAction) o;

        return wrappedAction != null ? wrappedAction.equals(that.wrappedAction) : that.wrappedAction == null;

    }

    @Override
    public int hashCode() {
        return wrappedAction != null ? wrappedAction.hashCode() : 0;
    }

    @Override
    public String toString() {
        return wrappedAction.toString();
    }
}
