package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheRoot;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.FullLP;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.CompleteUtilityCalculator;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;

public class FullLPFactory implements DoubleOracleFactory {

    @Override
    public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta, boolean isRoot) {
        return new FullLP(state, data, new SimUtilityImpl(state, new CompleteUtilityCalculator(data), isRoot ? new DOCacheRoot() : new DOCacheImpl()), isRoot);
    }

}
