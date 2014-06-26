package cz.agents.gtlibrary.nfg.MDP.implementations;

import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 4:08 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MDPExpanderImpl implements MDPExpander {
    @Override
    public abstract List<MDPAction> getActions(MDPState state);

    @Override
    public abstract Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action);

    @Override
    public abstract Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state);
}
