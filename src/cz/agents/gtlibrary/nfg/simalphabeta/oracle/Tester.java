package cz.agents.gtlibrary.nfg.simalphabeta.oracle;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P1AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P2AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullAlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.NegativeSimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.UtilityCalculator;

public class Tester {
	public static void main(String[] args) {
		GoofSpielGameState state = new GoofSpielGameState();
		
		state.performActionModifyingThisState(state.getNatureSequence().getFirst());
		DOCache cache = new DOCacheImpl();
		Expander<SimABInformationSet> expander = new GoofSpielExpander<SimABInformationSet>(new SimABConfig());
		Data data = new Data(new P1AlphaBeta(GSGameInfo.FIRST_PLAYER, expander, new AlphaBetaCacheImpl()), new P2AlphaBeta(GSGameInfo.SECOND_PLAYER, expander, new AlphaBetaCacheImpl()), new GSGameInfo(), expander);
		SimUtility utility = new SimUtilityImpl(state, new UtilityCalculator(cache, data));
		SimABOracle firstPlayerOracle = new P1SimABOracle(state, utility, data, cache);
		SimABOracle secondPlayerOracle = new P2SimABOracle(state, new NegativeSimUtility(utility), data, cache);
//		SimABOracle firstPlayerOracle = new SimABOracleImpl(state, GSGameInfo.FIRST_PLAYER, utility, data, cache);
//		SimABOracle secondPlayerOracle = new SimABOracleImpl(state, GSGameInfo.SECOND_PLAYER, new NegativeSimUtility(utility), data, cache);
		SimDoubleOracle oracle = new SimDoubleOracle(firstPlayerOracle, secondPlayerOracle, utility, -data.gameInfo.getMaxUtility(), data.gameInfo.getMaxUtility(), cache, data, state);

		oracle.execute();
		System.out.println("****************");
		System.out.println("game value: " + oracle.getGameValue());
		System.out.println("first player strategy: " + oracle.getFirstPlayerStrategy());
		System.out.println("second player strategy: " + oracle.getSecondPlayerStrategy());
	}

}
