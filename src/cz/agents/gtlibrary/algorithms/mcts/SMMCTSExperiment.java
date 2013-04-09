/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.backprop.DefaultBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.backprop.exp3.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.ValueDistribution;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3Selector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
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
import java.io.File;
import java.util.Arrays;
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
        static SQFBestResponseAlgorithm brAlg0;
        static SQFBestResponseAlgorithm brAlg1;

        static MCTSRunner mctsRunner;
        
        public static void setupGS(){
            gameInfo = new GSGameInfo();
            rootState = new GoofSpielGameState();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            optStrategies = efg.generate();
            brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
        }
        
        public static void setupRnd(long seed){
            RandomGameInfo.seed = seed;
            gameInfo = new RandomGameInfo();
            rootState = new SimRandomGameState();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            optStrategies = efg.generate();
            brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            GambitEFG.write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
            //System.out.println(optStrategies);
            
//            MCTSConfig mctsConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(gameInfo.getMaxUtility()));
//            mctsRunner = new BestResponseMCTSRunner(mctsConfig,                   
//                    rootState, new RandomGameExpander<MCTSInformationSet> (mctsConfig),  gameInfo.getAllPlayers()[0]);

            
        }
        
        static double gamma = 0.05;
        public static void runSMMCTS(){
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(gameInfo.getMaxUtility()));
            
            //double maxCFValue = gameInfo.getMaxUtility() / Math.pow(gamma/2.0, gameInfo.getMaxDepth());
            //double maxCFValue = 1e2;
            double maxCFValue = gameInfo.getMaxUtility();
            Exp3BackPropFactory bpFactory = new Exp3BackPropFactory(-maxCFValue, maxCFValue);
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), bpFactory, new UniformStrategyForMissingSequences.Factory(), new Exp3Selector(gamma));
//		MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new KuhnPokerGameState(), new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig));
//                Map<Sequence, Double> pureStrategy = runner.runMCTS(KPGameInfo.FIRST_PLAYER, new FrequenceDistribution());


            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig));
            //MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, new GoofSpielExpander<MCTSInformationSet> (firstMCTSConfig));
            
            
            Strategy strategy0 = null;
            Strategy strategy1 = null;
            int counter = 0;
            String outLine = "";
            System.out.print("P1BRs: ");
            
            for (int i=0; i<100; i++){
                //strategy = runner.runMCTS(MCTS_ITERATIONS_PER_CALL, gameInfo.getAllPlayers()[0], new FrequenceDistribution((int)(MCTS_ITERATIONS_PER_CALL*i*gamma)));
                strategy0 = runner.runMCTS(MCTS_ITERATIONS_PER_CALL, gameInfo.getAllPlayers()[0], new FrequenceDistribution());
                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1], new FrequenceDistribution());
                
                System.out.print(brAlg1.calculateBR(rootState, strategy0) + " ");
                outLine += brAlg0.calculateBR(rootState, strategy1) + " ";
                
                //System.out.println("Value: " + runner.getEV()[0] + " BR: " + brAlg.calculateBR(rootState, strategy) + " Same: " + counter);
                //System.out.println("Strat: " + strategy);
                //System.out.println("BR: " + brAlg.getFullBRSequences());
            }
            System.out.println();
            System.out.println("P0BRs: " + outLine);
            System.out.println("Normalized root value: " + Arrays.toString(runner.getEV()));
        }
        
        
        //args: seed BF
        public static void main(String[] args) {           
//             setupGS();
//             runSMMCTS();
            
            
            RandomGameInfo.MAX_BF = Integer.parseInt(args[1]);
            setupRnd(Long.parseLong(args[0]));
//            
            for (double g : new double[]{/*0.1,*/0.05, 0.025}){
              gamma = g;
              System.out.println("Gamma: " + g);
              for (int trial=0; trial < 100; trial++) runSMMCTS();
            }
	}
    
}
