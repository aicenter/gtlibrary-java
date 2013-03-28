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
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.io.GambitEFG;
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
    
        
        static GameInfo gameInfo;
        static GameState rootState;
        static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
        static Expander<SequenceInformationSet> sfExpander;
        static FullSequenceEFG efg;
        static Map<Player, Map<Sequence, Double>> optStrategies;
        static SQFBestResponseAlgorithm brAlg;
        
        
        public static void setupGS(){
            gameInfo = new GSGameInfo();
            rootState = new GoofSpielGameState();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            optStrategies = efg.generate();
            brAlg = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
        }
        
        public static void setupRnd(long seed){
            gameInfo = new RandomGameInfo();
            RandomGameInfo.seed = seed;
            rootState = new SimRandomGameState();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            optStrategies = efg.generate();
            brAlg = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            GambitEFG.write("RND222_"+seed+".efg", rootState, sfExpander);
            System.out.println(optStrategies);
        }
        
        public static void runSMMCTS(){
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(5));
            double gamma = 0.05;
            double maxCFValue = gameInfo.getMaxUtility() / Math.pow(gamma/5.0, gameInfo.getMaxDepth());
            //double maxCFValue = 1e10;
            //double maxCFValue = (new GSGameInfo()).getMaxUtility();
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3BackPropFactory(-maxCFValue, maxCFValue), new UniformStrategyForMissingSequences.Factory(), new Exp3Selector(gamma));
//		MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new KuhnPokerGameState(), new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig));
//                Map<Sequence, Double> pureStrategy = runner.runMCTS(KPGameInfo.FIRST_PLAYER, new FrequenceDistribution());


            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig));
            
            
            
            
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
                    System.out.println(strategy);
                    System.out.println("Value: " + runner.getEV()[0] + " BR: " + brAlg.calculateBR(rootState, strategy) + " Same: " + counter);
                    if (counter == SAME_STRATEGY_CHECK_COUNT) {
                            break;
                    }
                    lastPureStrategy = strategy;
            }
            
            //Map<Sequence, Double> pureStrategy = runner.runMCTS(GSGameInfo.FIRST_PLAYER, new FrequenceDistribution());

            //System.out.println(pureStrategy);
        }
        
        
    
        public static void main(String[] args) {
            for (int i=0; i<5; i++) setupRnd(i);
  //          runSMMCTS();
	}
    
}
