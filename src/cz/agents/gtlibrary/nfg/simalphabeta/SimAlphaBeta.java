/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


package cz.agents.gtlibrary.nfg.simalphabeta;

import cz.agents.gtlibrary.algorithms.flipit.simAlphaBeta.FlipItLocalCacheFullLPFactory;
import cz.agents.gtlibrary.algorithms.flipit.simAlphaBeta.FlipItSimDoubleOracleFactory;
import cz.agents.gtlibrary.domain.flipit.FlipItExpander;
import cz.agents.gtlibrary.domain.flipit.FlipItGameInfo;
import cz.agents.gtlibrary.domain.flipit.FullInfoFlipItGameState;
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
import cz.agents.gtlibrary.domain.rps.RPSExpander;
import cz.agents.gtlibrary.domain.rps.RPSGameInfo;
import cz.agents.gtlibrary.domain.rps.RPSGameState;
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
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheFullLPFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.SimABDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.OracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SimABOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SortingOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.stats.Stats;

import java.util.ArrayDeque;

public class SimAlphaBeta {

    public static boolean FULLY_COMPUTED = true;

//    public double gameValue = Double.NaN;

    public static void main(String[] args) {
//		runGoofSpielWithNature(true,true,false,false);
//		runGoofSpielWithNatureWithLocalCache();
//        runGoofSpielWithFixedNatureSequence(true, true, false, false, 7);
//		runGoofSpielWithFixedNatureSequenceWithLocalCache();
//	    runPursuit(false,false,false,false);
//        runSimRandomGame(false, false, false, false);
//        runOshiZumo(false,false,false,false);
//        runTron(false,false,false,false);
        runFlipIt(args, true, true, false, true);
//        Stats.getInstance().showSupportCounts();

    }

    public static void runGoofSpielWithFixedNatureSequence(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, int depth) {
        Stats.getInstance().startTime();
        GSGameInfo.useFixedNatureSequence = true;
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new GSGameInfo();
        GoofSpielGameState root = new GoofSpielGameState(depth);

        System.out.println(root.getNatureSequence());
        simAlphaBeta.runSimAlphabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "FixedGoofspielStats.csv", "Full LP");
    }

    public static void runGoofSpielWithNature(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        GSGameInfo.useFixedNatureSequence = false;
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new GSGameInfo();
        GoofSpielGameState root = new GoofSpielGameState();

        simAlphaBeta.runSimAlphabeta(root, new GoofSpielExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
    }


    public static void runFlipIt(String[] args, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        FlipItGameInfo gameInfo;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[0]);
            int graphSize = Integer.parseInt(args[1]);
            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, 1, graphFile, 1);
            FlipItGameInfo.OUTPUT_STRATEGY = true;
        }
        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.FULL;
        FlipItGameInfo.ZERO_SUM_APPROX = true;
        FullInfoFlipItGameState root = new FullInfoFlipItGameState();
        SimABConfig config = new SimABConfig();
        FlipItExpander<SimABInformationSet> expander = new FlipItExpander<>(config);
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        SimAlphaBetaResult result = simAlphaBeta.runSimAlphabetaInAllISs(root, expander, FlipItGameInfo.DEFENDER, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();

//        for (ActionPureStrategy strategy : result.strategy.getPureStrategies())
//            System.out.println(strategy +" : " + result.strategy.getProbability(strategy));

//        System.out.print(config.getAllInformationSets().size());
//        for (SimABInformationSet set : config.getAllInformationSets().values()){
//            if (set.getPlayer().equals(FlipItGameInfo.ATTACKER)) continue;
//            result = simAlphaBeta.runSimAlphabeta(set.getAllStates().iterator().next(), expander, FlipItGameInfo.DEFENDER, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
//            for (ActionPureStrategy strategy : result.strategy.getPureStrategies())
//                System.out.println(strategy +" : " + result.strategy.getProbability(strategy));
//        }
    }

    public static void runPursuit(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        GameInfo gameInfo = new PursuitGameInfo();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        simAlphaBeta.runSimAlphabeta(new PursuitGameState(), new FastImprovedExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
//		CSVExporter.export(Stats.getInstance(), "NatureGoofspielStats.csv",  "Full LP");
    }

    public static void runSimRandomGame(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new RandomGameInfo();
        simAlphaBeta.runSimAlphabeta(new SimRandomGameState(), new RandomGameExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

    public static void runOshiZumo(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new OZGameInfo();
        simAlphaBeta.runSimAlphabeta(new OshiZumoGameState(), new OshiZumoExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

    public static void runTron(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new TronGameInfo();
        simAlphaBeta.runSimAlphabeta(new TronGameState(), new TronExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }
    
    public static void runRPS(boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache) {
        Stats.getInstance().startTime();
        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        GameInfo gameInfo = new RPSGameInfo();
        simAlphaBeta.runSimAlphabeta(new RPSGameState(), new RPSExpander<SimABInformationSet>(new SimABConfig()), alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
        Stats.getInstance().stopTime();
        Stats.getInstance().printOverallInfo();
    }

    public double runSimAlphabeta(GameState rootState, Expander<SimABInformationSet> expander, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, GameInfo gameInfo) {
        double result = 0d;
        if (rootState.isPlayerToMoveNature()) {
            for (Action action : expander.getActions(rootState)) {
                result += rootState.getProbabilityOfNatureFor(action)* runSimAlphabeta(rootState.performAction(action), expander, alphaBetaBounds, doubleOracle, sortingOwnActions, useGlobalCache, gameInfo);
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
            System.out.println("game reward: " + result);
//            System.out.println("P1 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[0]));
//            System.out.println("P2 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[1]));
        }
        return result;
    }

    public static boolean CUT;
    public static boolean COMPUTED;
    public SimAlphaBetaResult runSimAlphabetaInAllISs(GameState rootState, Expander<SimABInformationSet> expander, Player player, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, GameInfo gameInfo) {
        COMPUTED = false;
        AlphaBetaFactory abFactory = (alphaBetaBounds) ? new /*AlphaBetaFactoryImpl()*/NoCacheAlphaBetaFactory() : new NullAlphaBetaFactory();
        DoubleOracleFactory doFactory = (doubleOracle) ? ((useGlobalCache) ? new FlipItSimDoubleOracleFactory() : new LocalCacheDoubleOracleFactory()) : new FlipItLocalCacheFullLPFactory();
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
        double gameValue = oracle.getGameValue();
//        System.out.println("P1 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[0]));
//        System.out.println("P2 strategy: " + oracle.getStrategyFor(rootState.getAllPlayers()[1]));
//        gameValue = oracle.getGameValue();

        SimAlphaBetaResult result;// = new SimAlphaBetaResult(getStrategy(player, player.getId() == 0? p2AlphaBeta : p1AlphaBeta, oracle), oracle.getCache(), (player.getId() == 0?1:-1)*getGameValue(oracle, p1ABBound, p2ABBound));
//        System.out.println(rootState.getSequenceFor(player));
//        System.out.println(rootState.getSequenceFor(gameInfo.getOpponent(player)));
//        for (ActionPureStrategy strategy : result.strategy.getPureStrategies())
//            System.out.println(strategy +" : " + result.strategy.getProbability(strategy));
//        System.out.println();

//        ((P1AlphaBeta)p1AlphaBeta).printCache();
//        System.out.println("Solved.");
        System.out.println("****************");
        System.out.println("root state: " + rootState);
        System.out.println("game reward: " + gameValue);
        if (true) return null;

        COMPUTED = true;
        ArrayDeque<GameState> queue = new ArrayDeque<GameState>();
        queue.add(rootState);
        while (queue.size() > 0) {
            GameState currentState = queue.removeLast();
            CUT = false;
//            System.out.println(currentState);
            if (!currentState.equals(rootState) && currentState.getPlayerToMove().equals(player) && !currentState.isGameEnd()){
//                try {
                    p1ABBound = p1AlphaBeta.getUnboundedValue(currentState);
                    p2ABBound = p2AlphaBeta.getUnboundedValue(currentState);

//                if (p1ABBound == -10000 || p2ABBound == -10000)
//                    continue;
//                }
//                catch (Exception e){
//                    System.out.println("EXCEPTION on ");
//                    System.out.printf("\t %s\n",currentState.getSequenceFor(player).toString());
//                    System.out.printf("\t %s\n",currentState.getSequenceFor(gameInfo.getOpponent(player)).toString());
//                }
//                data.getCache()

                    oracle = data.getDoubleOracle(currentState, -p2ABBound, p1ABBound, false);
                    if (-p2ABBound + 1e-8 < p1ABBound)
                        oracle.generate();

                    result = new SimAlphaBetaResult(getStrategy(player, player.getId() == 0 ? p2AlphaBeta : p1AlphaBeta, oracle), oracle.getCache(), (player.getId() == 0 ? 1 : -1) * getGameValue(oracle, p1ABBound, p2ABBound));
                    if (FlipItGameInfo.OUTPUT_STRATEGY) {
                        System.out.println(currentState.getSequenceFor(player));
                        System.out.println(currentState.getSequenceFor(gameInfo.getOpponent(player)));
                        for (ActionPureStrategy strategy : result.strategy.getPureStrategies())
                            if (result.strategy.getProbability(strategy) > 0.001)
                                System.out.println(strategy + " : " + result.strategy.getProbability(strategy));
                        System.out.println();
                    }
            }

            if (currentState.isGameEnd() || CUT) {
                continue;
            }

            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }

        System.out.println("****************");
        System.out.println("root state: " + rootState);
        System.out.println("game reward: " + gameValue);

        return new SimAlphaBetaResult(getStrategy(player, player.getId() == 0? p2AlphaBeta : p1AlphaBeta, oracle), oracle.getCache(), (player.getId() == 0?1:-1)*getGameValue(oracle, p1ABBound, p2ABBound));
    }

    public SimAlphaBetaResult runSimAlphabeta(GameState rootState, Expander<SimABInformationSet> expander, Player player, boolean alphaBetaBounds, boolean doubleOracle, boolean sortingOwnActions, boolean useGlobalCache, GameInfo gameInfo) {
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
        System.out.println("game reward: " + oracle.getGameValue());
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
