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


package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.AlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCache;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.StrategyComparator;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.ComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.DoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.OracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;


public class Data {
	
	public AlphaBeta[] alphaBetas;
	public GameInfo gameInfo;
	public Expander<SimABInformationSet> expander;
	public AlgorithmConfig<SimABInformationSet> config;
	private DOCache cache;
	private NatureCache natureCache;
	private DoubleOracleFactory doubleOracleFactory;
	private OracleFactory oracleFactory;
	private ComparatorFactory comparatorFactory;
	
	public Data(AlphaBetaFactory aBFactory, GameInfo gameInfo, Expander<SimABInformationSet> expander, DoubleOracleFactory doubleOracleFactory,
			OracleFactory oracleFactory, DOCache cache, NatureCache natureCache, ComparatorFactory compFactory) {
		super();
		this.alphaBetas = new AlphaBeta[]{aBFactory.getP1AlphaBeta(expander, gameInfo), aBFactory.getP2AlphaBeta(expander, gameInfo)};
		this.gameInfo = gameInfo;
		this.expander = expander;
		this.config = expander.getAlgorithmConfig();
		this.doubleOracleFactory = doubleOracleFactory;
		this.oracleFactory = oracleFactory;
		this.cache = cache;
		this.natureCache = natureCache;
		this.comparatorFactory = compFactory;
	}
	
	public AlphaBeta getAlphaBetaFor(Player player) {
		return alphaBetas[player.getId()];
	}
	
	public SimOracle getP1Oracle(GameState state, SimUtility utility, DOCache cache) {
		return oracleFactory.getP1Oracle(state, this, utility);
	}
	
	public SimOracle getP2Oracle(GameState state, SimUtility utility, DOCache cache) {
		return oracleFactory.getP2Oracle(state, this, utility);
	}
	
	public StrategyComparator getP1Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, DOCache cache) {
		return comparatorFactory.getP1Comparator(mixedStrategy, state, this, cache);
	}
	
	public StrategyComparator getP2Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state, DOCache cache) {
		return comparatorFactory.getP2Comparator(mixedStrategy, state, this, cache);
	}
	
	public DoubleOracle getDoubleOracle(GameState state, double alpha, double beta) {
		return doubleOracleFactory.getDoubleOracle(state, this, alpha, beta, false);
	}

    public DoubleOracle getDoubleOracle(GameState state, double alpha, double beta, boolean isRoot) {
        return doubleOracleFactory.getDoubleOracle(state, this, alpha, beta, isRoot);
    }

    public DOCache getCache() {
        return cache;
    }

    public void setCache(DOCache cache) {
        this.cache = cache;
    }

    public NatureCache getNatureCache() {
        return natureCache;
    }
}
