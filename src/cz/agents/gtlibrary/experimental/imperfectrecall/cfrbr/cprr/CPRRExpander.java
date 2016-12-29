package cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.cprr;

import cz.agents.gtlibrary.algorithms.cfr.ir.IRCFRInformationSet;
import cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleisdomain.FlexibleISAction;
import cz.agents.gtlibrary.experimental.imperfectrecall.cfrbr.flexibleisdomain.FlexibleISKeyGameState;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
