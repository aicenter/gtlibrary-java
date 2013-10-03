package cz.agents.gtlibrary.nfg.experimental.domain.randomgame;

import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPExpanderImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/12/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RGMDPExpander extends MDPExpanderImpl {
    @Override
    public List<MDPAction> getActions(MDPState state) {
        List<MDPAction> result = new ArrayList<MDPAction>(RGMDPConfig.BF_ACTIONS);

        if (state.isRoot()) {
            result.add(new RGMDPAction(state.getPlayer(), 0));
        } else if (state.isTerminal()) {
            return result;
        }  else {
            for (int i=0; i<RGMDPConfig.BF_ACTIONS; i++) {
                result.add(new RGMDPAction(state.getPlayer(), i));
            }
        }
        return result;
    }

    @Override
    public Map<MDPState, Double> getSuccessors(MDPStateActionMarginal action) {
        Map<MDPState, Double> result = new HashMap<MDPState, Double>();

        if (action.getState().isRoot()) {
            result.put(new RGMDPState(action.getPlayer()),1d);
            return result;
        }

        int changedActions = 0;
        for (int i=0; i<RGMDPConfig.BF_ACTIONS; i++) {
            if (((RGMDPAction)action.getAction()).getID() == i) {
                MDPState newState = action.getState().copy().performAction(action.getAction());
                result.put(newState, RGMDPConfig.NATURE_PROB[0]);
            } else {
                RGMDPAction changedAction = new RGMDPAction(action.getPlayer(), i);
                MDPState newState = action.getState().copy().performAction(changedAction);
                if (RGMDPConfig.NATURE_PROB[changedActions+1] > 0) {
                    result.put(newState, RGMDPConfig.NATURE_PROB[changedActions+1]);
                }
                changedActions++;
            }
        }

        return result;
    }

    @Override
    public Map<MDPStateActionMarginal, Double> getPredecessors(MDPState state) {
        Map<MDPStateActionMarginal, Double> result = new HashMap<MDPStateActionMarginal, Double>();

        RGMDPState s = ((RGMDPState)state);
        if (s.getStep() == 0)
            return null;

        int mainActionID = s.getID() - ((s.getID() >> RGMDPConfig.SHIFT) << RGMDPConfig.SHIFT);

        RGMDPState previousState = new RGMDPState(state.getPlayer(), s.getID() >> RGMDPConfig.SHIFT, s.getStep()-1);

        for (int i=0; i<RGMDPConfig.BF_ACTIONS; i++) {
            if (i == mainActionID) {
                result.put(new MDPStateActionMarginal(previousState, new RGMDPAction(s.getPlayer(), i)), RGMDPConfig.NATURE_PROB[0]);
            } else if (i < mainActionID) {
                if (RGMDPConfig.NATURE_PROB[mainActionID] > 0)
                    result.put(new MDPStateActionMarginal(previousState, new RGMDPAction(s.getPlayer(), i)), RGMDPConfig.NATURE_PROB[mainActionID]);
            } else if (i > mainActionID) {
                if (RGMDPConfig.NATURE_PROB[mainActionID+1] > 0)
                    result.put(new MDPStateActionMarginal(previousState, new RGMDPAction(s.getPlayer(), i)), RGMDPConfig.NATURE_PROB[mainActionID+1]);
            }
        }


        return result;
    }
}
