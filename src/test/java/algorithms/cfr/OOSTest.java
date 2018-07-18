package algorithms.cfr;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.runner.SMJournalExperiments;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceExpander;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.mp.MPAction;
import cz.agents.gtlibrary.domain.mp.MPExpander;
import cz.agents.gtlibrary.domain.mp.MPGameInfo;
import cz.agents.gtlibrary.domain.mp.MPGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OOSTest {

    @Test
    public void kuhnPokerTest() {
        GameState rootState = new KuhnPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        alg.runMiliseconds(100);
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(20000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(-0.055555555555, calculator.computeUtility(strategy0, strategy1), 1e-3);
    }

// todo: this test doesn't work
//    @Test
//    public void leducTest() {
//        GPGameInfo.MAX_RAISES_IN_ROW = 1;
//
//        GPGameInfo.MAX_DIFFERENT_BETS = 1;
//        GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;
//
//        GPGameInfo.BETS_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_BETS];
//        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_BETS; i++)
//            GPGameInfo.BETS_FIRST_ROUND[i] = (i + 1) * 2;
//
//
//        GPGameInfo.RAISES_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_RAISES];
//        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_RAISES; i++)
//            GPGameInfo.RAISES_FIRST_ROUND[i] = (i + 1) * 2;
//
//        GPGameInfo.MAX_CARD_TYPES = 3;
//        GPGameInfo.CARD_TYPES = new int[GPGameInfo.MAX_CARD_TYPES];
//        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
//            GPGameInfo.CARD_TYPES[i] = i;
//
//
//        GPGameInfo.MAX_CARD_OF_EACH_TYPE = 2;
//        GPGameInfo.DECK = new int[GPGameInfo.MAX_CARD_OF_EACH_TYPE * GPGameInfo.MAX_CARD_TYPES];
//        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
//            for (int j = 0; j < GPGameInfo.MAX_CARD_OF_EACH_TYPE; j++) {
//                GPGameInfo.DECK[i * GPGameInfo.MAX_CARD_OF_EACH_TYPE + j] = i;
//            }
//
//        GPGameInfo.BETS_SECOND_ROUND = new int[GPGameInfo.BETS_FIRST_ROUND.length];
//        for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
//            GPGameInfo.BETS_SECOND_ROUND[i] = 2 * GPGameInfo.BETS_FIRST_ROUND[i];
//        }
//
//        GPGameInfo.RAISES_SECOND_ROUND = new int[GPGameInfo.RAISES_FIRST_ROUND.length];
//        for (int i = 0; i < GPGameInfo.RAISES_FIRST_ROUND.length; i++) {
//            GPGameInfo.RAISES_SECOND_ROUND[i] = 2 * GPGameInfo.RAISES_FIRST_ROUND[i];
//        }
//        GameState rootState = new GenericPokerGameState();
//        MCTSConfig config = new MCTSConfig();
//        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
//        expander.getAlgorithmConfig().createInformationSetFor(rootState);
//
//        OOSAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState, expander);
//        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
//        Distribution dist = new MeanStratDist();
//        alg.runIterations(1000000);
//
//        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
//        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
//        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);
//
//        assertEquals(-0.08560642407800045, calculator.computeUtility(strategy0, strategy1), 1e-3);
//    }

    @Test
    public void cfvConvergenceLiarsDice() {
        GameInfo gameInfo = new LDGameInfo();
        GameState rootState = new LiarsDiceGameState();

        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new LiarsDiceExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        OOSAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        Distribution dist = new MeanStratDist();

        SQFBestResponseAlgorithm brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        SQFBestResponseAlgorithm brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        System.out.println("iteration,exploitability,cfvs");
        int nIters = 10000;
        int i = 0;
        Double exploitability;
        do {
            alg.runIterations(nIters);

            Strategy strategy0 = StrategyCollector.getStrategyFor(
                    alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            Strategy strategy1 = StrategyCollector.getStrategyFor(
                    alg.getRootNode(), rootState.getAllPlayers()[1], dist);
            UtilityCalculator calculator = new UtilityCalculator(rootState, expander);


            Double br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
            Double br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
            exploitability = br0Val + br1Val;
            System.out.print((i + 1) * nIters + "," + exploitability);

            InnerNode n = alg.getRootNode();
            // Quick and dirty way to bypass the first two chance states to get first infoset
            Map<Action, Node> children = n.getChildren();
            Map.Entry<Action, Node> entry = children.entrySet().iterator().next();
            Map<Action, Node> children2 = ((ChanceNode) entry.getValue()).getChildren();
            Map.Entry<Action, Node> entry2 = children2.entrySet().iterator().next();
            MCTSInformationSet is = ((InnerNode) entry2.getValue()).getInformationSet();
            OOSAlgorithmData data = ((OOSAlgorithmData) is.getAlgorithmData());
            for (double cfv : data.cfv) {
                System.out.print("," + cfv);
            }
            System.out.println();

            i++;
        } while(exploitability > 0.01);
    }




    @Test
    public void goofspielTest() {
        GSGameInfo.depth = 4;
        new GSGameInfo();
        GameState rootState = new GoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        alg.runMiliseconds(100);
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(2000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(0, calculator.computeUtility(strategy0, strategy1), 1e-1);
    }

    @Test
    public void oshiZumoTest() {
        OZGameInfo.seed = 1;
        OZGameInfo.startingCoins = 8;
        OZGameInfo.locK = 3;
        OZGameInfo.minBid = 1;
        OZGameInfo.BINARY_UTILITIES = false;
        new OZGameInfo();
        GameState rootState = new OshiZumoGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new OshiZumoExpander<>(config);

        GamePlayingAlgorithm alg =  new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(5000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(0, calculator.computeUtility(strategy0, strategy1), 1e-1);
    }
}
