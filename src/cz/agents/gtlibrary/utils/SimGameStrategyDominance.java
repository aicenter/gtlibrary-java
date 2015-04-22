package cz.agents.gtlibrary.utils;

import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.ActionPureStrategy;
import cz.agents.gtlibrary.nfg.simalphabeta.Data;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABConfig;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.AlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.alphabeta.factory.NullAlphaBetaFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.DOCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.cache.NatureCacheImpl;
import cz.agents.gtlibrary.nfg.simalphabeta.comparators.factory.LowerBoundComparatorFactory;
import cz.agents.gtlibrary.nfg.simalphabeta.doubleoracle.factory.DoubleOracleFactory;
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
//        Map<Player, StrategyDominance.DominanceResult> result = runOshiZumo();
        Map<Player, StrategyDominance.DominanceResult> result = runSimRnd();
//        Map<Player, StrategyDominance.DominanceResult> result = runGoofspiel();

        System.out.println(result);
    }

    private static Map<Player, StrategyDominance.DominanceResult> runOshiZumo() {
        return getDominatedStrategies(new OshiZumoGameState(), new OshiZumoExpander<>(new SimABConfig()), new OZGameInfo());
    }

    private static Map<Player, StrategyDominance.DominanceResult> runSimRnd() {
        return getDominatedStrategies(new SimRandomGameState(), new RandomGameExpander<>(new SimABConfig()), new RandomGameInfo());
    }

    private static Map<Player, StrategyDominance.DominanceResult> runGoofspiel() {
        GoofSpielGameState root = new GoofSpielGameState();
        Expander<SimABInformationSet> expander = new GoofSpielExpander<>(new SimABConfig());
        GameInfo gameInfo = new GSGameInfo();

        root.performActionModifyingThisState(expander.getActions(root).get(0));
        return getDominatedStrategies(root, expander, gameInfo);
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
        List<Action> strategies1 = expander.getActions(rootState);
        List<Action> strategies2 = expander.getActions(rootState.performAction(strategies1.get(0)));
        Map<Action, double[]> p1Utilities = new HashMap<>(strategies1.size());
        Map<Action, double[]> p2Utilities = new HashMap<>(strategies2.size());

        for (Action action : strategies1) {
            p1Utilities.put(action, new double[strategies2.size()]);
        }
        for (Action action : strategies2) {
            p2Utilities.put(action, new double[strategies1.size()]);
        }
        int strategy1Idx = 0;
        int strategy2Idx = 0;

        for (Action action1 : strategies1) {
            for (Action action2 : strategies2) {
                double value = calculator.getUtility(rootState.performAction(action1).performAction(action2), new ActionPureStrategy(action1), new ActionPureStrategy(action2));
                p1Utilities.get(action1)[strategy2Idx++] = value;
                p2Utilities.get(action2)[strategy1Idx] = -value;
            }
            strategy1Idx++;
            strategy2Idx = 0;
        }
        StrategyDominance<Action> dominance = new StrategyDominance<>();
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
