package cz.agents.gtlibrary.utils;

import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABConfig;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.AlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NoCacheAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NullAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.LowerBoundComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.DoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheDoubleOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.LocalCacheFullLPFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.OracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.oracle.factory.SimABOracleFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.utility.CompleteUtilityCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimGameStrategyDominance {

    public static void main(String[] args) {
        Map<Player, StrategyDominance.DominanceResult> result = getDominatedStrategies(new OshiZumoGameState(), new OshiZumoExpander<>(new SimABConfig()), new OZGameInfo());

        System.out.println(result);
    }

    public static Map<Player, StrategyDominance.DominanceResult> getDominatedStrategies(SimultaneousGameState rootState, Expander<SimABInformationSet> expander, GameInfo gameInfo) {
        AlphaBetaFactory abFactory = new NullAlphaBetaFactory();
        DoubleOracleFactory doFactory = new LocalCacheFullLPFactory();
        OracleFactory oracleFactory = new SimABOracleFactory();
        Data data = new Data(abFactory, gameInfo, expander,
                doFactory,
                oracleFactory,
                new DOCacheImpl(),
                new NatureCacheImpl(),
                new LowerBoundComparatorFactory());
        CompleteUtilityCalculator calculator = new CompleteUtilityCalculator(data);
        List<ActionPureStrategy> strategies1 = getActionStrategies(rootState, expander);
        List<ActionPureStrategy> strategies2 = getActionStrategies(rootState.performAction(strategies1.get(0).getAction()), expander);
        Map<ActionPureStrategy, double[]> p1Utilities = new HashMap<>(strategies1.size());
        Map<ActionPureStrategy, double[]> p2Utilities = new HashMap<>(strategies2.size());

        for (ActionPureStrategy strategy : strategies1) {
            p1Utilities.put(strategy, new double[strategies2.size()]);
        }
        for (ActionPureStrategy strategy : strategies2) {
            p2Utilities.put(strategy, new double[strategies1.size()]);
        }
        int strategy1Idx = 0;
        int strategy2Idx = 0;

        for (ActionPureStrategy strategy1 : strategies1) {
            for (ActionPureStrategy strategy2 : strategies2) {
                double value = calculator.getUtility(rootState.performAction(strategy1.getAction()).performAction(strategy2.getAction()), strategy1, strategy2);
                p1Utilities.get(strategy1)[strategy2Idx++] = value;
                p2Utilities.get(strategy2)[strategy1Idx] = -value;
            }
            strategy1Idx++;
            strategy2Idx = 0;
        }
        StrategyDominance<ActionPureStrategy> dominance = new StrategyDominance<>();
        Map<Player, StrategyDominance.DominanceResult> result = new HashMap<>(2);

        result.put(gameInfo.getAllPlayers()[0], dominance.computeMixedDominance(p1Utilities));
        result.put(gameInfo.getAllPlayers()[1], dominance.computeMixedDominance(p2Utilities));
        return result;
    }

    private static List<ActionPureStrategy> getActionStrategies(GameState rootState, Expander<SimABInformationSet> expander) {
        List<ActionPureStrategy> strategies = new ArrayList<>();

        for (Action action : expander.getActions(rootState)) {
            strategies.add(new ActionPureStrategy(action));
        }
        return strategies;
    }
}
