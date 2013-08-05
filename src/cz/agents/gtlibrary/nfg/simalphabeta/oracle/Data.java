package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;


public class Data {
	
	private AlphaBeta[] alphaBetas;
	public GameInfo gameInfo;
	public Expander<SimABInformationSet> expander;
	public AlgorithmConfig<SimABInformationSet> config;
	
	public Data(AlphaBeta fpAlphaBeta, AlphaBeta spAlphaBeta, GameInfo gameInfo, Expander<SimABInformationSet> expander) {
		super();
		this.alphaBetas = new AlphaBeta[]{fpAlphaBeta, spAlphaBeta};
		this.gameInfo = gameInfo;
		this.expander = expander;
		this.config = expander.getAlgorithmConfig();
	}
	
	public AlphaBeta getAlphaBetaFor(Player player) {
		return alphaBetas[player.getId()];
	}
}
