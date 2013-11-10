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
	public DOCache cache;
	public NatureCache natureCache;
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
	
	public StrategyComparator getP1Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state) {
		return comparatorFactory.getP1Comparator(mixedStrategy, state, this);
	}
	
	public StrategyComparator getP2Comparator(MixedStrategy<ActionPureStrategy> mixedStrategy, GameState state) {
		return comparatorFactory.getP2Comparator(mixedStrategy, state, this);
	}
	
	public DoubleOracle getDoubleOracle(GameState state, double alpha, double beta) {
		return doubleOracleFactory.getDoubleOracle(state, this, alpha, beta);
	}
}
