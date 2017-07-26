package cz.agents.gtlibrary.algorithms.flipit.simAlphaBeta;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.SimDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.SimABDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.DOUtilityCalculator;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;

/**
 * Created by Jakub Cerny on 14/07/2017.
 */
public class FlipItSimDoubleOracleFactory extends SimABDoubleOracleFactory {

    @Override
    public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta, boolean isRoot) {
        SimUtility utility = new SimUtilityImpl(state, new DOUtilityCalculator(data, data.getNatureCache(), data.getCache()), data.getCache());

        return new FlipItSimDoubleOracle(utility, alpha, beta, data, state, data.getCache(), isRoot);
    }

}
