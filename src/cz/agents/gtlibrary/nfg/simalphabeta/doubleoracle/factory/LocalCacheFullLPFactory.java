package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheRoot;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.FullLP;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.CompleteUtilityCalculator;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;

public class LocalCacheFullLPFactory implements DoubleOracleFactory {

    @Override
    public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta, boolean isRoot) {
        DOCache cache = isRoot ? new DOCacheRoot() : new DOCacheImpl();

        data.setCache(cache);
        return new FullLP(state, data, new SimUtilityImpl(state, new CompleteUtilityCalculator(data), cache), isRoot);
    }

}
