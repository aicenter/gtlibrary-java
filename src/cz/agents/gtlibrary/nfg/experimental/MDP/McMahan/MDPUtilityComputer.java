package cz.agents.gtlibrary.nfg.experimental.MDP.McMahan;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle.DefaultStrategyType;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.utils.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/24/13
 * Time: 10:04 AM
 * To change this template use File | Settings | File Templates.
 */

public class MDPUtilityComputer extends Utility<MDPStrategy, MDPStrategy> {

    MDPConfig config;

    public MDPUtilityComputer(MDPConfig config) {
        this.config = config;
    }

    @Override
    public double getUtility(MDPStrategy s1, MDPStrategy s2) {
        double result = 0d;
        for (MDPStateActionMarginal m1 : s1.getAllMarginalsInStrategy()) {
            result += s1.getStrategyProbability(m1) * s1.getUtility(m1, s2);
        }
        return result;
    }

}
