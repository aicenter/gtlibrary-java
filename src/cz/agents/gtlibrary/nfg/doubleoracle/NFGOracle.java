package cz.agents.gtlibrary.nfg.doubleoracle;

import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.utils.Pair;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 5/29/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface NFGOracle {
    public Pair<PureStrategy, Double> getNewStrategy(Utility utilityCalculator, MixedStrategy opponentStrategy);
}
