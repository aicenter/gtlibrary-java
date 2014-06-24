package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;

public interface AlphaBeta {
	
	public double getUnboundedValue(GameState state);

    public double getUnboundedValueAndStoreStrategy(GameState state, DOCache doCache);

	public double getValue(GameState state, double alpha, double beta);

    public Action getTopLevelAction(Player player);

}
