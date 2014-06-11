package cz.agents.gtlibrary.nfg.MDP.implementations.pureStrategyOracle;

import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;

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
