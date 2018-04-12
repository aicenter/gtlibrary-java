package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.flexibleisdomain;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.iinodes.ActionImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;


public class FlexibleISAction extends ActionImpl {

    private Action wrappedAction;
    private GameState associatedState;
    private Map<ISKey, IRCFRInformationSet> informationSets;

    public FlexibleISAction(InformationSet informationSet, Action wrappedAction, GameState associatedState, Map<ISKey, IRCFRInformationSet> informationSets) {
        super(informationSet);
        this.wrappedAction = wrappedAction;
        this.associatedState = associatedState;
        this.informationSets = informationSets;
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
        updateIS();
        if (!super.equals(o)) return false;
        FlexibleISAction that = (FlexibleISAction) o;

        return wrappedAction.equals(that.wrappedAction);
    }

    @Override
    public int hashCode() {
        updateIS();
        return new HashCodeBuilder(17, 31).append(informationSet).append(wrappedAction).toHashCode();
    }

    private void updateIS() {
        informationSet = informationSets.get(associatedState.getISKeyForPlayerToMove());
        assert associatedState.isPlayerToMoveNature() ||  informationSet != null;
    }

    @Override
    public String toString() {
        return "FISA: " + wrappedAction.toString();
    }
}
