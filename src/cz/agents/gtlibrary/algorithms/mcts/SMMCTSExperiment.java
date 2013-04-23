/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.*;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielAction;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.io.GambitEFG;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.FileManager;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.UtilityCalculator;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.SecureRandom;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            System.out.println();
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
            printRootDistribution(optStrategies.get(rootState.getAllPlayers()[0]));
            brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            GambitEFG.write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
            //System.out.println(optStrategies);
            
//            MCTSConfig mctsConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(gameInfo.getMaxUtility()));
//            mctsRunner = new BestResponseMCTSRunner(mctsConfig,                   
//                    rootState, new RandomGameExpander<MCTSInformationSet> (mctsConfig),  gameInfo.getAllPlayers()[0]);

            
        }
        
        static double gamma = 0.05;
        public static void runSMMCTS() throws Exception {
            
            //double maxCFValue = gameInfo.getMaxUtility() / Math.pow(gamma/2.0, gameInfo.getMaxDepth());
            //double maxCFValue = 1e2;
            double maxCFValue = gameInfo.getMaxUtility();
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new OOSBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(gamma*gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3BackPropFactory(-maxCFValue, maxCFValue, gamma), new UniformStrategyForMissingSequences.Factory(), null);
//		MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new KuhnPokerGameState(), new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig));
//                Map<Sequence, Double> pureStrategy = runner.runMCTS(KPGameInfo.FIRST_PLAYER, new FrequenceDistribution());


            Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (firstMCTSConfig);
            //Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
            //MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig));
            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);

            brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
            
            
            Strategy strategy0 = null;
            Strategy strategy1 = null;
            int counter = 0;
            String outLine = "";
            System.out.print("P1BRs: ");
            
            for (int i=0; i<100; i++){
                strategy0 = runner.runMCTS(MCTS_ITERATIONS_PER_CALL, gameInfo.getAllPlayers()[0], /*new MeanStratDist() /*/ new FrequenceDistribution(gamma));
                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1],/* new MeanStratDist() /*/ new FrequenceDistribution(gamma));
                
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
        
        public static void runSMMCTS_VK() throws Exception {
            
            double maxCFValue = gameInfo.getMaxUtility();
            
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new OOSBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(gamma*gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3BackPropFactory(-maxCFValue, maxCFValue, gamma), new UniformStrategyForMissingSequences.Factory(), null);

            //Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (firstMCTSConfig);
            Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);

            brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
            
            
            Strategy strategy0 = null;
            Strategy strategy1 = null;
            
            System.out.print("P1BRs: ");
            
            for (int i=0; i<1000; i++){
                strategy0 = runner.runMCTS(MCTS_ITERATIONS_PER_CALL, gameInfo.getAllPlayers()[0], /*new MeanStratDist() /*/ new FrequenceDistribution());
                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1],/* new MeanStratDist() /*/ new FrequenceDistribution());
                
                Strategy strategy_nogamma = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[0],/* new MeanStratDist() /*/ new FrequenceDistribution(gamma));
              
                printRootDistribution(strategy0);
                //printRootDistribution(strategy_nogamma);
                double[] p = ((Exp3SelectionStrategy)(((InnerNode)(runner.getRootNode().selectChild())).getInformationSet().selectionStrategy)).p;
                for (double d : p) System.out.print(d+",");
                System.out.print(" ");
                System.out.print(runner.getEV()[0] + " ");
                double brVal = brAlg1.calculateBR(rootState, strategy0);
                System.out.print(brVal + " ");
                System.out.println(brAlg1.calculateBR(rootState, strategy_nogamma) + " ");
            }
            System.out.println();            
        }
        
        public static void printRootDistribution(Map<Sequence, Double> s){
            List<Map.Entry<Sequence, Double>> l = new LinkedList<Map.Entry<Sequence, Double>>();
            for (Map.Entry<Sequence, Double> en : s.entrySet()){
                if (en.getKey().size() == 1) l.add(en);
            }
            Collections.sort(l, new Comparator<Map.Entry<Sequence, Double>>() {
                @Override
                public int compare(Entry<Sequence, Double> t, Entry<Sequence, Double> t1) {
//                    GoofSpielAction a1 = (GoofSpielAction) t.getKey().getFirst();
//                    GoofSpielAction a2 = (GoofSpielAction) t1.getKey().getFirst();
                    return t.getKey().getFirst().hashCode() - t1.getKey().getFirst().hashCode();
                }
            });
            for (Map.Entry<Sequence, Double> en : l){
                System.out.print(en.getValue() + ",");
            }
            System.out.print(" ");
        }

        public static void runSMMCTSExploitability(int milisPerMove){
            //double maxCFValue = gameInfo.getMaxUtility() / Math.pow(gamma/2.0, gameInfo.getMaxDepth());
            //double maxCFValue = 1e2;
            double maxCFValue = gameInfo.getMaxUtility();
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new OOSBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3BackPropFactory(-maxCFValue, maxCFValue, gamma), new UniformStrategyForMissingSequences.Factory(), null);
//		MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new KuhnPokerGameState(), new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig));
//                Map<Sequence, Double> pureStrategy = runner.runMCTS(KPGameInfo.FIRST_PLAYER, new FrequenceDistribution());


            Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (firstMCTSConfig);
            //MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig));
            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);

            brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);


            Strategy strategy0 = null;
            Strategy strategy1 = null;

            List<Node> curLevel = new ArrayList<Node>();
            //init run
            runner.runMCTS(1000, gameInfo.getAllPlayers()[0], null);
            InnerNode realRoot = runner.getRootNode();
            assert gameInfo instanceof GSGameInfo;
            curLevel.addAll(realRoot.getChildren().values());

             ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

             //int[] levelNums = new int[]{10000,25000,60000};

            for (int l=0; l<3;l++){
                System.out.println("Curlevel size: " +  curLevel.size());
                for (Node curRoot : curLevel){
                    runner.setRootNode((InnerNode)curRoot);
                    long start = threadBean.getCurrentThreadCpuTime();
                    int i=0;
                    for (;(threadBean.getCurrentThreadCpuTime()-start)/1000000 < milisPerMove;){
                       runner.runMCTS(100, gameInfo.getAllPlayers()[0], null);
                       i++;
                    }
                    System.out.print(i*100 + " ");
                      //runner.runMCTS(levelNums[l], gameInfo.getAllPlayers()[0], null);
                }
                System.out.println();
                List<Node> newLevel = new ArrayList<Node>();
                for (Node n : curLevel){
                    if (n instanceof InnerNode && ((InnerNode)n).getChildren() != null){
                        //opp move
                        for (Node nn : ((InnerNode)n).getChildren().values()){
                            if (nn instanceof InnerNode && ((InnerNode)nn).getChildren() != null){
                                //chance move
                                for (Node nnn : ((InnerNode)nn).getChildren().values()){
                                    if (nnn instanceof InnerNode && ((InnerNode)nnn).getChildren() != null){
                                        //opp move
                                        for (Node nnnn : ((InnerNode)nnn).getChildren().values()){
                                            newLevel.add(nnnn);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                curLevel = newLevel;
            }
            runner.setRootNode(realRoot);

            strategy0 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[0], /*new OOSMeanStratDist()*/ new FrequenceDistribution());
            strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1], /*new OOSMeanStratDist()*/ new FrequenceDistribution());
            System.out.println("P1BR: " + brAlg1.calculateBR(rootState, strategy0));
            System.out.println("P0BR: " + brAlg0.calculateBR(rootState, strategy1));

            FileManager<Strategy> fm = new FileManager<Strategy>();
            fm.saveObject(strategy0, "P0Exp3Mg"+gamma+"FixR" + run + ".strat");
            fm.saveObject(strategy1, "P1Exp3Mg"+gamma+"FixR" + run + ".strat");
        }
        
        static int run = 0;
        //args: seed BF
        public static void main(String[] args) throws Exception {
//            setupRnd(0);
//            for (int i = 0; i < 100; i++) {
//                run = i;
                setupGS();
//                //runSMMCTS();
//                runSMMCTSExploitability(Integer.parseInt(args[0]));
//            }
            //             runSMMCTS();
            //            RandomGameInfo.MAX_BF = Integer.parseInt(args[1]);
            //            setupRnd(Long.parseLong(args[0]));
            //            for (;;){
            //            int r = (new Random()).nextInt();
            //            System.out.println(r);
            //            setupRnd(r);
            //            runSMMCTS();
            //            }
            //
            //            for (Map.Entry<Sequence, Double> en : optStrategies.get(gameInfo.getAllPlayers()[0]).entrySet()){
            //                if (en.getKey().size() < 3 && en.getValue() > 1e-5) System.out.println(en);
            //            }
            //            for (double g : new double[]{0.1,0.05, 0.025}){
            //            //for (double g : new double[]{0.2,0.3,0.35}){
            //              gamma = g;
            //              System.out.println("Gamma: " + g);
            //              for (int trial=0; trial < 100; trial++) runSMMCTS();
            //            }
            //             runSMMCTS();
            //            RandomGameInfo.MAX_BF = Integer.parseInt(args[1]);
            //            setupRnd(Long.parseLong(args[0]));
            //            }
            //            int r = (new Random()).nextInt();
            //            System.out.println(r);
            //            setupRnd(r);
            //            runSMMCTS();
            //            }
            //
            //            for (Map.Entry<Sequence, Double> en : optStrategies.get(gameInfo.getAllPlayers()[0]).entrySet()){
            //                if (en.getKey().size() < 3 && en.getValue() > 1e-5) System.out.println(en);
            //            }
            
            
                        for (double g : new double[]{0.1, 0.15}){
                        //for (double g : new double[]{0.2,0.3,0.35}){
                          gamma = g;
                          System.out.println("Gamma: " + g);
                          for (int trial=0; trial < 100; trial++) runSMMCTS();
                        }
	}
    
}
