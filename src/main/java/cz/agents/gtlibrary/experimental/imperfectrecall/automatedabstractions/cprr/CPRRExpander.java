package cz.agents.gtlibrary.experimental.imperfectrecall.automatedabstractions.cprr;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;

public class CPRRExpander<I extends InformationSet> extends ExpanderImpl<I> {
    private Expander<I> wrappedExpander;

    public CPRRExpander(Expander<I> wrappedExpander) {
        super(wrappedExpander.getAlgorithmConfig());
        this.wrappedExpander = wrappedExpander;
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        return wrappedExpander.getActions(((CPRRGameState) gameState).getWrappedState());
    }
}
