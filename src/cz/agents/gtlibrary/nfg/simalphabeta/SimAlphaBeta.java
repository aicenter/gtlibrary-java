package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.pursuit.EvaderPursuitAction;
import cz.agents.gtlibrary.domain.pursuit.FastImprovedExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.domain.rps.RPSExpander;
import cz.agents.gtlibrary.domain.rps.RPSGameInfo;
import cz.agents.gtlibrary.domain.rps.RPSGameState;
import cz.agents.gtlibrary.domain.tron.TronExpander;
import cz.agents.gtlibrary.domain.tron.TronGameInfo;
import cz.agents.gtlibrary.domain.tron.TronGameState;
import cz.agents.gtlibrary.iinodes.PlayerImpl;
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
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheFullLPFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.SimABDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.OracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SimABOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SortingOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

public class SimAlphaBeta {

    public static boolean FULLY_COMPUTED = true;

//    public double gameValue = Double.NaN;

    public static void main(String[] args) {
//		runGoofSpielWithNature(true,false,false,false);
//		runGoofSpielWithNatureWithLocalCache();
        runGoofSpielWithFixedNatureSequence(false, false, false, false, 7);
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
    
    public static void runRPS(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new RPSGameInfo();
        simAlphaBeta.runSimAlpabeta(new RPSGameState(), new RPSExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

    public double runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, GameInfo gameInfo) {
        double result = 0d;
        if (rootState.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(rootState)) {
                result += rootState.getProbabilityOfNatureFor(action)*runSimAlpabeta(rootState.performAction(action), expander, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
            }
        } else {
            AlphaBetaFactory abFactory = (alphaBetaBounds) ? new NoCacheAlphaBetaFactory() : new NullAlphaBetaFactory();
            DoubleOracleFactory doFactory = (doubleOracle) ? ((useGlobalCache) ? new SimABDoubleOracleFactory() : new LocalCacheDoubleOracleFactory()) : new LocalCacheFullLPFactory();
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

            if (beta + 1e-8 < alpha) {
                oracle.generate();
                result = oracle.getGameValue();
            } else {
                result = alpha;
            }

            System.out.println("****************");
			System.out.println("root state: " + rootState);
            System.out.println("game value: " + result);
//            System.out.println("P1 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[0]));
//            System.out.println("P2 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[1]));
        }
        return result;
    }

    public SimAlphaBetaResult runSimAlpabeta(GameState rootState, Expander<SimABInformationSet> expander, Player player, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, GameInfo gameInfo) {
        AlphaBetaFactory abFactory = (alphaBetaBounds) ? new NoCacheAlphaBetaFactory() : new NullAlphaBetaFactory();
        DoubleOracleFactory doFactory = (doubleOracle) ? ((useGlobalCache) ? new SimABDoubleOracleFactory() : new LocalCacheDoubleOracleFactory()) : new LocalCacheFullLPFactory();
        OracleFactory oracleFactory = (sortingOwnActions) ? new SortingOracleFactory() : new SimABOracleFactory();
        Data data = new Data(abFactory, gameInfo, expander,
                doFactory,
                oracleFactory,
                new DOCacheImpl(),
                new NatureCacheImpl(),
                new LowerBoundComparatorFactory());
        System.out.println(data.gameInfo.getInfo());
        AlphaBeta p1AlphaBeta = abFactory.getP1AlphaBeta(expander, gameInfo);
        AlphaBeta p2AlphaBeta = abFactory.getP2AlphaBeta(expander, gameInfo);
        double p1ABBound = p1AlphaBeta.getUnboundedValue(rootState);
        double p2ABBound = p2AlphaBeta.getUnboundedValue(rootState);

        DoubleOracle oracle = data.getDoubleOracle(rootState, -p2ABBound, p1ABBound, true);

        if (-p2ABBound + 1e-8 < p1ABBound)
            oracle.generate();
        if(Killer.kill)
            return null;
        System.out.println("****************");
		System.out.println("root state: " + rootState);
        System.out.println("game value: " + oracle.getGameValue());
//        System.out.println("P1 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[0]));
//        System.out.println("P2 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[1]));
//        gameValue = oracle.getGameValue();

        return new SimAlphaBetaResult(getStrategy(player, player.getId() == 0? p2AlphaBeta : p1AlphaBeta, oracle), oracle.getCache(), (player.getId() == 0?1:-1)*getGameValue(oracle, p1ABBound, p2ABBound));
    }

    private double getGameValue(DoubleOracle oracle, double p1ABBound, double p2ABBound) {
        if (-p2ABBound + 1e-8 > p1ABBound)
            return p1ABBound;
        return oracle.getGameValue();
    }

    private MixedStrategy<ActionPureStrategy> getStrategy(Player player, AlphaBeta p1AlphaBeta, DoubleOracle oracle) {
        MixedStrategy<ActionPureStrategy> strategy = oracle.getStrategyFor(player);

        if(strategy == null) {
            strategy = new MixedStrategy<>();

            strategy.put(new ActionPureStrategy(p1AlphaBeta.getTopLevelAction(player)), 1d);
//            System.out.println("Strategy " + strategy + " extracted from alpha-beta");
        }
        return strategy;
    }

}
