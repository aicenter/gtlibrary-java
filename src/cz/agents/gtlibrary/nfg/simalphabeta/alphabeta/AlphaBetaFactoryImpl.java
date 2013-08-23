package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;

public class AlphaBetaFactoryImpl implements AlphaBetaFactory {

	@Override
	public AlphaBeta getP1AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo) {
		return new P1AlphaBeta(gameInfo.getAllPlayers()[0], expander, new AlphaBetaCacheImpl(), gameInfo);
	}

	@Override
	public AlphaBeta getP2AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo) {
		return new P2AlphaBeta(gameInfo.getAllPlayers()[1], expander, new AlphaBetaCacheImpl(), gameInfo);
	}

}
