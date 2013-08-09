package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P1AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P2AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.P1SimABOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.P2SimABOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.NegativeSimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtility;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.SimUtilityImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.UtilityCalculator;

public class SimAlphaBeta {

	public static void main(String[] args) {
		runGoofSpiel();
//		runPursuit();
	}

	public static void runGoofSpiel() {
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		
		simAlphaBeta.runSimAlpabeta(new GoofSpielGameState(), new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
	}
	
	public static void runPursuit() {
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		
		simAlphaBeta.runSimAlpabeta(new PursuitGameState(), new PursuitExpander<SimABInformationSet>(new SimABConfig()));
	}

	public void runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander) {
		if (rootState.isPlayerToMoveNature())
			for (Action action : expander.getActions(rootState)) {
				runSimAlpabeta(rootState.performAction(action), expander);
			}
		else {
			DOCache cache = new DOCacheImpl();
			Data data = new Data(new P1AlphaBeta(GSGameInfo.FIRST_PLAYER, expander, new AlphaBetaCacheImpl()), new P2AlphaBeta(GSGameInfo.SECOND_PLAYER, expander, new AlphaBetaCacheImpl()), new GSGameInfo(), expander);
			SimUtility utility = new SimUtilityImpl(rootState, new UtilityCalculator(cache, data));
			SimABOracle firstPlayerOracle = new P1SimABOracle(rootState, utility, data, cache);
			SimABOracle secondPlayerOracle = new P2SimABOracle(rootState, new NegativeSimUtility(utility), data, cache);
			SimDoubleOracle oracle = new SimDoubleOracle(firstPlayerOracle, secondPlayerOracle, utility, -data.gameInfo.getMaxUtility(), data.gameInfo.getMaxUtility(), cache, data, rootState);

			oracle.execute();
			System.out.println("****************");
			System.out.println("root state: " + rootState);
			System.out.println("game value: " + oracle.getGameValue());
			System.out.println("first player strategy: " + oracle.getFirstPlayerStrategy());
			System.out.println("second player strategy: " + oracle.getSecondPlayerStrategy());
		}
	}
}
