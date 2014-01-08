package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;

public interface AlphaBetaFactory {
	
	public AlphaBeta getP1AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo);

	public AlphaBeta getP2AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo);
	
}
