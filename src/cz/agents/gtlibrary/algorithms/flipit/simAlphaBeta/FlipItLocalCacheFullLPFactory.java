package cz.agents.gtlibrary.algorithms.flipit.simAlphaBeta;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheRoot;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheFullLPFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.CompleteUtilityCalculator;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;

/**
 * Created by Jakub Cerny on 14/07/2017.
 */
public class FlipItLocalCacheFullLPFactory extends LocalCacheFullLPFactory {

    @Override
    public DoubleOracle getDoubleOracle(GameState state, Data data, double alpha, double beta, boolean isRoot) {
        DOCache cache = isRoot ? new DOCacheRoot() : new DOCacheImpl();

        data.setCache(cache);
        return new FlipItFullLP(state, data, new SimUtilityImpl(state, new CompleteUtilityCalculator(data), cache), isRoot);
    }

}
