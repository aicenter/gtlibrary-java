package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.NullAlphaBeta;

public class NullAlphaBetaFactory implements AlphaBetaFactory {

	@Override
	public AlphaBeta getP1AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo) {
		return new NullAlphaBeta();
	}

	@Override
	public AlphaBeta getP2AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo) {
		return new NullAlphaBeta();
	}

}
