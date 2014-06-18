package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.pursuit.FastImprovedExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.domain.tron.TronExpander;
import cz.agents.gtlibrary.domain.tron.TronGameInfo;
import cz.agents.gtlibrary.domain.tron.TronGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.AlphaBeta;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.AlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NoCacheAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NullAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.LowerBoundComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.DoubleOracle;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.DoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.FullLPFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.SimABDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.OracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SimABOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SortingOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class SimAlphaBeta {

    public double gameValue = Double.NaN;

    public static void main(String[] args) {
//		runGoofSpielWithNature();
//		runGoofSpielWithNatureWithLocalCache();
        runGoofSpielWithFixedNatureSequence(true, false, false, false, 7);
//		runGoofSpielWithFixedNatureSequenceWithLocalCache();
//	    runPursuit(true,true);
//        runSimRandomGame(false, true, false, false);
//        runOshiZumo(true,false,false,false);
    }

    public static void runGoofSpielWithFixedNatureSequence(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, int depth) {
        Stats.getInstance().startTime();
        GSGameInfo.useFixedNatureSequence = true;
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new GSGameInfo();
        GoofSpielGameState root = new GoofSpielGameState(depth);

        System.out.println(root.getNatureSequence());
        simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "FixedGoofspielStats.csv", "Full LP");
    }

    public static void runGoofSpielWithNature(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        GSGameInfo.useFixedNatureSequence = false;
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GoofSpielGameState root = new GoofSpielGameState();

        simAlphaBeta.runSimAlpabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, new GSGameInfo());
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
    }

    public static void runPursuit(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new PursuitGameInfo();
        simAlphaBeta.runSimAlpabeta(new PursuitGameState(), new FastImprovedExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
    }

    public static void runSimRandomGame(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new RandomGameInfo();
        simAlphaBeta.runSimAlpabeta(new SimRandomGameState(), new RandomGameExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

    public static void runOshiZumo(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new OZGameInfo();
        simAlphaBeta.runSimAlpabeta(new OshiZumoGameState(), new OshiZumoExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

    public static void runTron(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new TronGameInfo();
        simAlphaBeta.runSimAlpabeta(new TronGameState(), new TronExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

    public void runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, GameInfo gameInfo) {
        if (rootState.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(rootState)) {
                runSimAlpabeta(rootState.performAction(action), expander, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
            }
        } else {
            AlphaBetaFactory abFactory = (alphaBetaBounds) ? new NoCacheAlphaBetaFactory() : new NullAlphaBetaFactory();
            DoubleOracleFactory doFactory = (doubleOracle) ? ((useGlobalCache) ? new SimABDoubleOracleFactory() : new LocalCacheDoubleOracleFactory()) : new FullLPFactory();
            OracleFactory oracleFactory = (sortingOwnActions) ? new SortingOracleFactory() : new SimABOracleFactory();
            Data data = new Data(abFactory, gameInfo, expander,
                    doFactory,
                    oracleFactory,
                    new DOCacheImpl(),
                    new NatureCacheImpl(),
                    new LowerBoundComparatorFactory());
            System.out.println(data.gameInfo.getInfo());
            double beta = -data.getAlphaBetaFor(rootState.getAllPlayers()[1]).getUnboundedValue(rootState);
            double alpha = data.getAlphaBetaFor(rootState.getAllPlayers()[0]).getUnboundedValue(rootState);
            DoubleOracle oracle = data.getDoubleOracle(rootState, beta, alpha);

            if (beta + 1e-8 < alpha)
                oracle.generate();
            System.out.println("****************");
//			System.out.println("root state: " + rootState);
            System.out.println("game value: " + oracle.getGameValue());
            System.out.println("P1 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[0]));
            System.out.println("P2 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[1]));
        }
    }

    public MixedStrategy<ActionPureStrategy> runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander, Player player, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, GameInfo gameInfo) {
        AlphaBetaFactory abFactory = (alphaBetaBounds) ? new NoCacheAlphaBetaFactory() : new NullAlphaBetaFactory();
        DoubleOracleFactory doFactory = (doubleOracle) ? ((useGlobalCache) ? new SimABDoubleOracleFactory() : new LocalCacheDoubleOracleFactory()) : new FullLPFactory();
        OracleFactory oracleFactory = (sortingOwnActions) ? new SortingOracleFactory() : new SimABOracleFactory();
        Data data = new Data(abFactory, gameInfo, expander,
                doFactory,
                oracleFactory,
                new DOCacheImpl(),
                new NatureCacheImpl(),
                new LowerBoundComparatorFactory());
//        System.out.println(data.gameInfo.getInfo());
        AlphaBeta p1AlphaBeta = abFactory.getP1AlphaBeta(expander, gameInfo);
        AlphaBeta p2AlphaBeta = abFactory.getP2AlphaBeta(expander, gameInfo);

        DoubleOracle oracle = data.getDoubleOracle(rootState, -p2AlphaBeta.getUnboundedValue(rootState), p1AlphaBeta.getUnboundedValue(rootState));

        if (-p2AlphaBeta.getUnboundedValue(rootState) + 1e-8 < p1AlphaBeta.getUnboundedValue(rootState))
            oracle.generate();
        if(Killer.kill)
            return null;
//        System.out.println("****************");
//		System.out.println("root state: " + rootState);
//        System.out.println("game value: " + oracle.getGameValue());
//        System.out.println("P1 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[0]));
//        System.out.println("P2 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[1]));
        gameValue = oracle.getGameValue();
        return getStrategy(player, p1AlphaBeta, oracle);
    }

    private MixedStrategy<ActionPureStrategy> getStrategy(Player player, AlphaBeta p1AlphaBeta, DoubleOracle oracle) {
        MixedStrategy<ActionPureStrategy> strategy = oracle.getStrategyFor(player);

        if(strategy == null) {
            strategy = new MixedStrategy<ActionPureStrategy>();

            strategy.put(new ActionPureStrategy(p1AlphaBeta.getTopLevelAction(player)), 1d);
//            System.out.println("Strategy " + strategy + " extracted from alpha-beta");
        }
        return strategy;
    }

}
