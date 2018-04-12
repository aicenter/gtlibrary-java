package algorithms.mcts;

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
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GamePlayingAlgorithm;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.strategy.Strategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UCTTest {

    @Test
    public void goofspielTest() {
        GSGameInfo.depth = 4;
        new GSGameInfo();
        GameState rootState = new GoofSpielGameState();
        MCTSConfig config = new MCTSConfig();
        Expander<MCTSInformationSet> expander = new GoofSpielExpander<>(config);
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg =  new ISMCTSAlgorithm(
                rootState.getAllPlayers()[0],
                new DefaultSimulator(expander),
                new UCTBackPropFactory(2),
                rootState, expander);
        ((ISMCTSAlgorithm) alg).returnMeanValue = false;
        ((ISMCTSAlgorithm) alg).runIterations(2);
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
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        GamePlayingAlgorithm alg =  new ISMCTSAlgorithm(
                rootState.getAllPlayers()[0],
                new DefaultSimulator(expander),
                new UCTBackPropFactory(2),
                rootState, expander);
        ((ISMCTSAlgorithm) alg).returnMeanValue = false;
        Distribution dist = new MeanStratDist();

        alg.runMiliseconds(15000);

        Strategy strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);
        UtilityCalculator calculator = new UtilityCalculator(rootState, expander);

        assertEquals(0, calculator.computeUtility(strategy0, strategy1), 1e-2);
    }

}
