package cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle;

import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.doubleoracle.NFGDoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public abstract class DoubleOracle extends NFGDoubleOracle {

    public DoubleOracle(GameState rootState, Data data) {
        super(rootState, data.expander, data.gameInfo, data.config);
        Stats.getInstance().incrementStatesVisited();
//		Stats.getInstance().addState(rootState);
    }

    public MixedStrategy<ActionPureStrategy> getStrategyFor(Player player) {
        if (player.getId() == 0)
            return coreSolver.getPlayerOneStrategy();
        return coreSolver.getPlayerTwoStrategy();
    }

    public abstract double getGameValue();

    public abstract void generate();

    public abstract DOCache getCache();

}
