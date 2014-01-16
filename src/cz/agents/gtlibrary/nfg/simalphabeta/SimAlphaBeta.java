package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NoCacheAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.LowerBoundComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.UpperBoundComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.FullLPFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.SimABDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SimABOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SortingOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;
import cz.agents.gtlibrary.utils.io.CSVExporter;

public class SimAlphaBeta {

	public static void main(String[] args) {
//		runGoofSpielWithNature();
//		runGoofSpielWithNatureWithLocalCache();
//		runGoofSpielWithFixedNatureSequence();
//		runGoofSpielWithFixedNatureSequenceWithLocalCache();
				runPursuit();
	}

	public static void runGoofSpielWithFixedNatureSequenceWithLocalCache() {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = true;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabetaWithLocalCache(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
		CSVExporter.export(Stats.getInstance(), "FixedGoofspielStats.csv", "Local Cache DO, Sorting Oracle, Bounds tightening");
	}
	
	public static void runGoofSpielWithNatureWithLocalCache() {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = false;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabetaWithLocalCache(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
		Stats.getInstance().printOverallInfo();
		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv", "Local Cache DO, Sorting Oracle, Bounds tightening");
		
	}

	public static void runGoofSpielWithFixedNatureSequence() {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = true;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		System.out.println(root.getNatureSequence());
		simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
		CSVExporter.export(Stats.getInstance(), "FixedGoofspielStats.csv", "Full LP");
	}
	
	public static void runGoofSpielWithNature() {
		Stats.getInstance().startTime();
		GSGameInfo.useFixedNatureSequence = false;
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		GoofSpielGameState root = new GoofSpielGameState();
		
		simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()));
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
	}
	
	public static void runPursuit() {
		Stats.getInstance().startTime();
		SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
		
		simAlphaBeta.runSimAlpabeta(new PursuitGameState(), new PursuitExpander<SimABInformationSet>(new SimABConfig()));
		Stats.getInstance().stopTime();
		Stats.getInstance().printOverallInfo();
		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
	}

	public void runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander) {
		if (rootState.isPlayerToMoveNature()) {
			for (Action action : expander.getActions(rootState)) {
				runSimAlpabeta(rootState.performAction(action), expander);
			}
		} else {
			Data data = new Data(new NoCacheAlphaBetaFactory(), new GSGameInfo(), expander, 
					new FullLPFactory(), new SimABOracleFactory(), new DOCacheImpl(), new NatureCacheImpl(), new LowerBoundComparatorFactory());
			DoubleOracle oracle = data.getDoubleOracle(rootState, -data.getAlphaBetaFor(rootState.getAllPlayers()[1]).getUnboundedValue(rootState), data.getAlphaBetaFor(rootState.getAllPlayers()[0]).getUnboundedValue(rootState));

			oracle.generate();
			System.out.println("****************");
			System.out.println("root state: " + rootState);
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
			DoubleOracle oracle = data.getDoubleOracle(rootState, -data.getAlphaBetaFor(rootState.getAllPlayers()[1]).getUnboundedValue(rootState), data.getAlphaBetaFor(rootState.getAllPlayers()[0]).getUnboundedValue(rootState));

			oracle.generate();
			System.out.println("****************");
			System.out.println("root state: " + rootState);
			System.out.println("game value: " + oracle.getGameValue());
			System.out.println("time: " + (System.currentTimeMillis() - time));
		}
	}
}