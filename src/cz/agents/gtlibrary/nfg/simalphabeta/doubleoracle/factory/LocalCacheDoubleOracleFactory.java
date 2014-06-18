package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheRoot;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.SimDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.DOUtilityCalculator;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;

public class LocalCacheDoubleOracleFactory implements DoubleOracleFactory {

    @Override
    public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta, boolean isRoot) {
        DOCache cache = isRoot ? new DOCacheRoot() : new DOCacheImpl();
        SimUtility utility = new SimUtilityImpl(state, new DOUtilityCalculator(data, new NatureCacheImpl(), cache), cache);

        return new SimDoubleOracle(utility, alpha, beta, data, state, utility.getUtilityCache(), isRoot);
    }

}
