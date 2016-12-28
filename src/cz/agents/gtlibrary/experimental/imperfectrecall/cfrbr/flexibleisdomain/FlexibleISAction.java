package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleisdomain;

import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class FlexibleISAction extends ActionImpl {

    private Action wrappedAction;

    public FlexibleISAction(InformationSet informationSet, Action wrappedAction) {
        super(informationSet);
        this.wrappedAction = wrappedAction;
    }

    @Override
    public void perform(GameState gameState) {
    }

    public Action getWrappedAction() {
        return wrappedAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlexibleISAction)) return false;
        if (!super.equals(o)) return false;

        FlexibleISAction that = (FlexibleISAction) o;

        return wrappedAction.equals(that.wrappedAction);

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(informationSet).append(wrappedAction).toHashCode();
    }

    @Override
    public String toString() {
        return "FISA: " + wrappedAction.toString();
    }
}
