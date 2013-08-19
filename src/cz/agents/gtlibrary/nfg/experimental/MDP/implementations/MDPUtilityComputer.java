package cz.agents.gtlibrary.nfg.experimental.MDP.implementations;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/24/13
 * Time: 10:04 AM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class MDPUtilityComputer {

//    private MDPConfig config;
//
//    public MDPUtilityComputer(MDPConfig config) {
//        this.config = config;
//    }
//
//    public double getUtility(MDPStrategy firstPlayerStrategy, MDPStrategy secondPlayerStrategy) {
//        double value = 0;
//
//        for (Map.Entry<MDPStateActionMarginal, Double> e1 : firstPlayerStrategy.getStrategy().entrySet()) {
//            for (Map.Entry<MDPStateActionMarginal, Double> e2 : secondPlayerStrategy.getStrategy().entrySet()) {
//                value += getUtility(e1.getKey(), e2.getKey())*e1.getValue()*e2.getValue();
//            }
//        }
//
//        return value;
//    }
//
//    public double getUtility(MDPStateActionMarginal firstPlayerAction, MDPStateActionMarginal secondPlayerAction) {
//        return config.getUtility(firstPlayerAction, secondPlayerAction);
//    }
}
