package algorithms.simalphabeta;

import cz.agents.gtlibrary.algorithms.mcts.DefaultSimulator;
import cz.agents.gtlibrary.algorithms.mcts.ISMCTSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GamePlayingAlgorithm;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABConfig;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.SimAlphaBeta;
import cz.agents.gtlibrary.strategy.Strategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SimAlphaBetaTest {

    @Test
    public void goofspielTest() {
        GSGameInfo.depth = 4;
        GameState rootState = new GoofSpielGameState();
        SimABConfig config = new SimABConfig();
        Expander<SimABInformationSet> expander = new GoofSpielExpander<>(config);

        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        double value = simAlphaBeta.runSimAlphabeta(rootState, expander, true, true, false, false, new GSGameInfo());

        assertEquals(0, value, 1e-3);
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
        SimABConfig config = new SimABConfig();
        Expander<SimABInformationSet> expander = new OshiZumoExpander<>(config);

        SimAlphaBeta simAlphaBeta = new SimAlphaBeta();
        double value = simAlphaBeta.runSimAlphabeta(rootState, expander, true, true, false, false, new GSGameInfo());

        assertEquals(0, value, 1e-3);
    }
}
