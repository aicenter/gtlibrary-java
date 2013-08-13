package cz.agents.gtlibrary.nfg.experimental.MDP.interfaces;

import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 6/27/13
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MDPExpander {
    public List<MDPAction> getActions(MDPState state);
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action);
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state);
}
