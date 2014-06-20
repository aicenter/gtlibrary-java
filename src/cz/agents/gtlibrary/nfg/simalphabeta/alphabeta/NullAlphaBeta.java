package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;


public class NullAlphaBeta implements AlphaBeta {


	@Override
	public double getValue(GameState state, double alpha, double beta) {
		return Double.POSITIVE_INFINITY;
	}

    @Override
    public Action getTopLevelAction(Player player) {
        return null;
    }

    @Override
	public double getUnboundedValue(GameState state) {
		return Double.POSITIVE_INFINITY;
	}


}
