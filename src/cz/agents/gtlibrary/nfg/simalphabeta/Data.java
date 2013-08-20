package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCache;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.OracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimOracle;
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
	
	public Data(AlphaBeta fpAlphaBeta, AlphaBeta spAlphaBeta, GameInfo gameInfo, Expander<SimABInformationSet> expander,
			DoubleOracleFactory doubleOracleFactory, OracleFactory oracleFactory, DOCache cache, NatureCache natureCache) {
		super();
		this.alphaBetas = new AlphaBeta[]{fpAlphaBeta, spAlphaBeta};
		this.gameInfo = gameInfo;
		this.expander = expander;
		this.config = expander.getAlgorithmConfig();
		this.doubleOracleFactory = doubleOracleFactory;
		this.oracleFactory = oracleFactory;
		this.cache = cache;
		this.natureCache = natureCache;
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
	
	public DoubleOracle getDoubleOracle(GameState state, double alpha, double beta) {
		return doubleOracleFactory.getDoubleOracle(state, this, alpha, beta);
	}
}
