/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.backprop.exp3.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3Selector;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class SMMCTSExperiment {
        private static final int MCTS_ITERATIONS_PER_CALL = 1000;
	private static final int SAME_STRATEGY_CHECK_COUNT = 20;
    
    
        public static void main(String[] args) {
            GSGameInfo gameInfo = new GSGameInfo();
            GameState rootState = new GoofSpielGameState();
            SequenceFormConfig<SequenceInformationSet> sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            Expander<SequenceInformationSet> sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
            FullSequenceEFG efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            Map<Player, Map<Sequence, Double>> optStrategies = efg.generate();
            SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
        
            
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(5));
            double gamma = 0.01;
            double maxCFValue = gameInfo.getMaxUtility() / Math.pow(gamma/5.0, gameInfo.getMaxDepth());
            //double maxCFValue = 1e10;
            //double maxCFValue = (new GSGameInfo()).getMaxUtility();
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3BackPropFactory(-maxCFValue, maxCFValue), new UniformStrategyForMissingSequences.Factory(), new Exp3Selector(gamma));
//		MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new KuhnPokerGameState(), new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig));
//                Map<Sequence, Double> pureStrategy = runner.runMCTS(KPGameInfo.FIRST_PLAYER, new FrequenceDistribution());


            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new GoofSpielGameState(), new GoofSpielExpander<MCTSInformationSet>(firstMCTSConfig));
            
            
            
            
            FrequenceDistribution distribution = new FrequenceDistribution();
            Strategy lastPureStrategy = null;
            Strategy strategy = null;
            int counter = 0;

            while (true) {
                    strategy = runner.runMCTS(MCTS_ITERATIONS_PER_CALL, gameInfo.getAllPlayers()[0], distribution);
                    if (lastPureStrategy != null && strategy.maxDifferenceFrom(lastPureStrategy) < 0.001) {
                            counter++;
                    } else {
                            counter = 0;
                    }
                    //System.out.println(strategy);
                    System.out.println("Value: " + runner.getEV()[0] + " BR: " + brAlg.calculateBR(rootState, strategy) + " Same: " + counter);
                    if (counter == SAME_STRATEGY_CHECK_COUNT) {
                            break;
                    }
                    lastPureStrategy = strategy;
            }
            
            Map<Sequence, Double> pureStrategy = runner.runMCTS(GSGameInfo.FIRST_PLAYER, new FrequenceDistribution());

            System.out.println(pureStrategy);
	}
    
}
