/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P1AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P2AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullAlphaBetaCache;

public class NoCacheAlphaBetaFactory implements AlphaBetaFactory {

	@Override
	public AlphaBeta getP1AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo) {
		return new P1AlphaBeta(gameInfo.getAllPlayers()[0], expander, new NullAlphaBetaCache(), gameInfo);
	}

	@Override
	public AlphaBeta getP2AlphaBeta(Expander<SimABInformationSet> expander, GameInfo gameInfo) {
		return new P2AlphaBeta(gameInfo.getAllPlayers()[1], expander, new NullAlphaBetaCache(), gameInfo);
	}

}
