package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.AlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NoCacheAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NullAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullDOCache;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NullNatureCache;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.LowerBoundComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.UpperBoundComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.DoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.FullLPFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.SimABDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.OracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SimABOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SortingOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.utils.io.CSVExporter;
import cz.agents.gtlibrary.utils.io.GambitEFG;

public class SimAlphaBeta {

	public static void main(String[] args) {
//		runGoofSpielWithNature();
//		runGoofSpielWithNatureWithLocalCache();
//		runGoofSpielWithFixedNatureSequence(true, true);
//		runGoofSpielWithFixedNatureSequenceWithLocalCache();
//	    runPursuit(true,true);
        runSimRandomGame(false, true, false);
	}

	public static void runGoofSpielWithFixedNatureSequenceWithLocalCache(boolean alphaBetaBounds, boolean doubleOracle) {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = true;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabetaWithLocalCache(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "FixedGoofspielStats.csv", "Local Cache DO, Sorting Oracle, Bounds tightening");
	}
	
	public static void runGoofSpielWithNatureWithLocalCache(boolean alphaBetaBounds, boolean doubleOracle) {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = false;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabetaWithLocalCache(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
		Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv", "Local Cache DO, Sorting Oracle, Bounds tightening");
		
	}

	public static void runGoofSpielWithFixedNatureSequence(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions) {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = true;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new GSGameInfo();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, gameInfo);
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "FixedGoofspielStats.csv", "Full LP");
	}
	
	public static void runGoofSpielWithNature(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions) {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = false;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, new GSGameInfo());
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
	}
	
	public static void runPursuit(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions) {
		Stats.getInstance().startTime();
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new PursuitGameInfo();
		simAlphaBeta.runSimAlpabeta(new PursuitGameState(), new PursuitExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, gameInfo);
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
	}

    public static void runSimRandomGame(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new RandomGameInfo();
        simAlphaBeta.runSimAlpabeta(new SimRandomGameState(), new RandomGameExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

	public void runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, GameInfo gameInfo) {
		if (rootState.isPlayerToMoveNature()) {
			for (Action action : expander.getActions(rootState)) {
				runSimAlpabeta(rootState.performAction(action), expander, alphaBetaBounds, doubleOracle, sortingOwnActions, gameInfo);
			}
		} else {
            AlphaBetaFactory abFactory = (alphaBetaBounds) ? new NoCacheAlphaBetaFactory() : new NullAlphaBetaFactory();
            DoubleOracleFactory doFactory = (doubleOracle) ? new LocalCacheDoubleOracleFactory() : new FullLPFactory();
            OracleFactory oracleFactory = (sortingOwnActions) ? new SortingOracleFactory() : new SimABOracleFactory();
			Data data = new Data(abFactory, gameInfo, expander,
					doFactory,
                    oracleFactory,
                    new DOCacheImpl(),
                    new NatureCacheImpl(),
                    new LowerBoundComparatorFactory());
            System.out.println(data.gameInfo.getInfo());
            DoubleOracle oracle = data.getDoubleOracle(rootState, -data.getAlphaBetaFor(rootState.getAllPlayers()[1]).getUnboundedValue(rootState), data.getAlphaBetaFor(rootState.getAllPlayers()[0]).getUnboundedValue(rootState));

			oracle.generate();
			System.out.println("****************");
//			System.out.println("root state: " + rootState);
			System.out.println("game value: " + oracle.getGameValue());
		}
	}
	
	public void runSimAlpabetaWithLocalCache(GameState rootState, Expander<SimABInformationSet> expander) {
		if (rootState.isPlayerToMoveNature()) {
			for (Action action : expander.getActions(rootState)) {
				runSimAlpabetaWithLocalCache(rootState.performAction(action), expander);
			}
		} else {

			long time = System.currentTimeMillis();
			Data data = new Data(new NoCacheAlphaBetaFactory(), new GSGameInfo(), expander, 
					new LocalCacheDoubleOracleFactory(), new SortingOracleFactory(), new DOCacheImpl(), new NatureCacheImpl(), new UpperBoundComparatorFactory());

            System.out.println(data.gameInfo.getInfo());
            DoubleOracle oracle = data.getDoubleOracle(rootState, -data.getAlphaBetaFor(rootState.getAllPlayers()[1]).getUnboundedValue(rootState), data.getAlphaBetaFor(rootState.getAllPlayers()[0]).getUnboundedValue(rootState));

			oracle.generate();
			System.out.println("****************");
			System.out.println("root state: " + rootState);
			System.out.println("game value: " + oracle.getGameValue());
			System.out.println("time: " + (System.currentTimeMillis() - time));
		}
	}
}
