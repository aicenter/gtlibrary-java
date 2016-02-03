package algorithms.cfr;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.DefaultSimulator;
import cz.agents.gtlibrary.algorithms.mcts.ISMCTSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.runner.SMJournalExperiments;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GamePlayingAlgorithm;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.strategy.Strategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
public class CFRTest {

    @Test
    public void kuhnPokerTest() {
        GameState rootState = new KuhnPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new KuhnPokerExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg = new CFRAlgorithm(rootState.getAllPlayers()[0], rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        alg.runMiliseconds(100);
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(2000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(-0.055555555555, calculator.computeUtility(strategy0, strategy1), 1e-3);
    }

    @Test
    public void leducTest() {
        GPGameInfo.MAX_RAISES_IN_ROW = 1;

        GPGameInfo.MAX_DIFFERENT_BETS = 1;
        GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;

        GPGameInfo.BETS_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_BETS];
        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_BETS; i++)
            GPGameInfo.BETS_FIRST_ROUND[i] = (i + 1) * 2;


        GPGameInfo.RAISES_FIRST_ROUND = new int[GPGameInfo.MAX_DIFFERENT_RAISES];
        for (int i = 0; i < GPGameInfo.MAX_DIFFERENT_RAISES; i++)
            GPGameInfo.RAISES_FIRST_ROUND[i] = (i + 1) * 2;

        GPGameInfo.MAX_CARD_TYPES = 3;
        GPGameInfo.CARD_TYPES = new int[GPGameInfo.MAX_CARD_TYPES];
        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
            GPGameInfo.CARD_TYPES[i] = i;


        GPGameInfo.MAX_CARD_OF_EACH_TYPE = 2;
        GPGameInfo.DECK = new int[GPGameInfo.MAX_CARD_OF_EACH_TYPE * GPGameInfo.MAX_CARD_TYPES];
        for (int i = 0; i < GPGameInfo.MAX_CARD_TYPES; i++)
            for (int j = 0; j < GPGameInfo.MAX_CARD_OF_EACH_TYPE; j++) {
                GPGameInfo.DECK[i * GPGameInfo.MAX_CARD_OF_EACH_TYPE + j] = i;
            }

        GPGameInfo.BETS_SECOND_ROUND = new int[GPGameInfo.BETS_FIRST_ROUND.length];
        for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
            GPGameInfo.BETS_SECOND_ROUND[i] = 2 * GPGameInfo.BETS_FIRST_ROUND[i];
        }

        GPGameInfo.RAISES_SECOND_ROUND = new int[GPGameInfo.RAISES_FIRST_ROUND.length];
        for (int i = 0; i < GPGameInfo.RAISES_FIRST_ROUND.length; i++) {
            GPGameInfo.RAISES_SECOND_ROUND[i] = 2 * GPGameInfo.RAISES_FIRST_ROUND[i];
        }
        GameState rootState = new GenericPokerGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GenericPokerExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg = new CFRAlgorithm(rootState.getAllPlayers()[0], rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        alg.runMiliseconds(100);
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(5000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(-0.08560642407800045, calculator.computeUtility(strategy0, strategy1), 1e-3);
    }

    @Test
    public void goofspielTest() {
        GSGameInfo.depth = 4;
        new GSGameInfo();
        GameState rootState = new GoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg = new CFRAlgorithm(rootState.getAllPlayers()[0], rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        alg.runMiliseconds(100);
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(2000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(0, calculator.computeUtility(strategy0, strategy1), 1e-3);
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
        GamePlayingAlgorithm alg = new CFRAlgorithm(rootState.getAllPlayers()[0], rootState, expander);
        SMJournalExperiments.buildCompleteTree(alg.getRootNode());
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(5000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(0, calculator.computeUtility(strategy0, strategy1), 1e-3);
    }
}
