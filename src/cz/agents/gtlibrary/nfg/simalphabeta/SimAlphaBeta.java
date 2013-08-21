package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P1AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.P2AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.AlphaBetaCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullAlphaBetaCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullDOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullNatureCache;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.NoCacheDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.SimABDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.NoCacheOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.SimABOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class SimAlphaBeta {

	public static void main(String[] args) {
		runGoofSpielWithNature();
//		runGoofSpielWithFixedNatureSequence();
//		runGoofSpielWithFixedNatureSequenceWithoutCache();
		//		runPursuit();
	}

	public static void runGoofSpielWithFixedNatureSequenceWithoutCache() {
		GSGameInfo.useFixedNatureSequence = true;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabetaWithoutCache(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
		
	}

	public static void runGoofSpielWithFixedNatureSequence() {
		GSGameInfo.useFixedNatureSequence = true;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
	}
	
	public static void runGoofSpielWithNature() {
		GSGameInfo.useFixedNatureSequence = false;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
	}
	
	public static void runPursuit() {
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		
		simAlphaBeta.runSimAlpabeta(new PursuitGameState(), new PursuitExpander<SimABInformationSet>(new SimABConfig()));
	}

	public void runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander) {
		if (rootState.isPlayerToMoveNature()) {
			long time = System.currentTimeMillis();
			
			for (Action action : expander.getActions(rootState)) {
				runSimAlpabeta(rootState.performAction(action), expander);
			}
			Stats.printOverallInfo();
			Stats.resetOverall();
			System.out.println("Time: " + (System.currentTimeMillis() - time));
		} else {
			long time = System.currentTimeMillis();
			Data data = new Data(new P1AlphaBeta(rootState.getAllPlayers()[0], expander, new AlphaBetaCacheImpl(), new GSGameInfo()), new P2AlphaBeta(rootState.getAllPlayers()[1], expander, new AlphaBetaCacheImpl(), new GSGameInfo()), new GSGameInfo(), expander, 
					new SimABDoubleOracleFactory(), new SimABOracleFactory(), new DOCacheImpl(), new NatureCacheImpl());
			DoubleOracle oracle = data.getDoubleOracle(rootState, -data.getAlphaBetaFor(rootState.getAllPlayers()[1]).getUnboundedValue(rootState), data.getAlphaBetaFor(rootState.getAllPlayers()[0]).getUnboundedValue(rootState));

			oracle.generate();
			System.out.println("****************");
			System.out.println("root state: " + rootState);
			System.out.println("game value: " + oracle.getGameValue());
			System.out.println("time: " + (System.currentTimeMillis() - time));
			Stats.printInfo();
			Stats.reset();
		}
	}
	
	public void runSimAlpabetaWithoutCache(GameState rootState, Expander<SimABInformationSet> expander) {
		if (rootState.isPlayerToMoveNature()) {
			long time = System.currentTimeMillis();
			
			for (Action action : expander.getActions(rootState)) {
				runSimAlpabetaWithoutCache(rootState.performAction(action), expander);
			}
			Stats.printOverallInfo();
			Stats.resetOverall();
			System.out.println("Time: " + (System.currentTimeMillis() - time));
		} else {
			long time = System.currentTimeMillis();
			Data data = new Data(new P1AlphaBeta(rootState.getAllPlayers()[0], expander, new NullAlphaBetaCache(), new GSGameInfo()), new P2AlphaBeta(rootState.getAllPlayers()[1], expander, new NullAlphaBetaCache(), new GSGameInfo()), new GSGameInfo(), expander, 
					new NoCacheDoubleOracleFactory(), new NoCacheOracleFactory(), new NullDOCache(), new NullNatureCache());
			DoubleOracle oracle = data.getDoubleOracle(rootState, -data.getAlphaBetaFor(rootState.getAllPlayers()[1]).getUnboundedValue(rootState), data.getAlphaBetaFor(rootState.getAllPlayers()[0]).getUnboundedValue(rootState));

			oracle.generate();
			System.out.println("****************");
			System.out.println("root state: " + rootState);
			System.out.println("game value: " + oracle.getGameValue());
			System.out.println("time: " + (System.currentTimeMillis() - time));
			Stats.printInfo();
			Stats.reset();
			System.out.println(((ConfigImpl<SimABInformationSet>)expander.getAlgorithmConfig()).getAllInformationSets().size());
		}
	}
}
