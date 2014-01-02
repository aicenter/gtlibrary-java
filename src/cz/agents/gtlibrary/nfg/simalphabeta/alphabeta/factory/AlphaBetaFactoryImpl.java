package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P1AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P2AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCacheImpl;

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
