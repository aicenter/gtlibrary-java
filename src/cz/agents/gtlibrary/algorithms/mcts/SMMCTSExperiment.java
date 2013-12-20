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
import cz.agents.gtlibrary.domain.randomgame.SimSystematicGS;
import cz.agents.gtlibrary.experimental.utils.UtilityCalculator;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.io.GambitEFG;
import cz.agents.gtlibrary.nfg.UtilityMatrix;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolver;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.FileManager;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.security.SecureRandom;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author vilo
 */
public class SMMCTSExperiment {
        private static final int MCTS_ITERATIONS_PER_CALL = (int)1000;
        private static final int MCTS_CALLS = 100;
	private static PrintStream out = System.out;
    
        
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
            printRootDistribution(optStrategies.get(rootState.getAllPlayers()[1]));
            System.out.println();
            brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
        }
        
        public static void setupRnd(long seed){
            if (seed == RandomGameInfo.seed && rootState != null) return;
            RandomGameInfo.seed = seed;
            gameInfo = new RandomGameInfo();
            rootState = new SimRandomGameState();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            optStrategies = efg.generate();
            printRootDistribution(optStrategies.get(rootState.getAllPlayers()[1]));
            brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            //GambitEFG.write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
            //out .println(optStrategies);
            
//            MCTSConfig mctsConfig = new MCTSConfig(new Simulator(1), new DefaultBackPropFactory(), new UniformStrategyForMissingSequences.Factory(), new UCTSelector(gameInfo.getMaxUtility()));
//            mctsRunner = new BestResponseMCTSRunner(mctsConfig,                   
//                    rootState, new RandomGameExpander<MCTSInformationSet> (mctsConfig),  gameInfo.getAllPlayers()[0]);

            
        }
        
        static double gamma = 0.05;
        static MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
        //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new OOSBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
        //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(gamma*gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
        //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3BackPropFactory(-maxCFValue, maxCFValue, gamma), new UniformStrategyForMissingSequences.Factory(), null);
        //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3MRemBackPropFactory(-maxCFValue, maxCFValue, gamma), new UniformStrategyForMissingSequences.Factory(), null);
        static Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
        
        public static void runSMMCTS() throws Exception {
            
            //double maxCFValue = gameInfo.getMaxUtility() / Math.pow(gamma/2.0, gameInfo.getMaxDepth());
            //double maxCFValue = 1e2;
            double maxCFValue = gameInfo.getMaxUtility();
            
//		MCTSRunner runner = new MCTSRunner(firstMCTSConfig, new KuhnPokerGameState(), new KuhnPokerExpander<MCTSInformationSet>(firstMCTSConfig));
//                Map<Sequence, Double> pureStrategy = runner.runMCTS(KPGameInfo.FIRST_PLAYER, new FrequenceDistribution());


            //Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (firstMCTSConfig);
            Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
            //MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig));
            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);

            brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            
            
            Strategy strategy0 = null;
            Strategy strategy1 = null;
            int counter = 0;
            String outLine = "";
            out .print("P1BRs: ");
            
            for (int i=0; i<MCTS_CALLS; i++){
                strategy0 = runner.runMCTS(MCTS_ITERATIONS_PER_CALL, gameInfo.getAllPlayers()[0], /*new MeanStratDist() /*/ new FrequenceDistribution());
                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1], /*new MeanStratDist() /*/ new FrequenceDistribution());
                
                out.print((brAlg1.calculateBR(rootState, strategy0) + efg.getGameValue()) + " ");
                outLine += (brAlg0.calculateBR(rootState, strategy1) - efg.getGameValue()) + " ";
//                out.println(brAlg0.calculateBR(rootState, strategy1) - efg.getGameValue());
//                out.println((brAlg1.calculateBR(rootState, strategy0) + efg.getGameValue()));
//                ((RMMSelector)runner.getRootNode().getInformationSet().selectionStrategy).printMatrix();
//                out.println(strategy1);
//                out.println(strategy0);
                
                
                //out.println("Value: " + runner.getEV()[0] + " BR: " + brAlg.calculateBR(rootState, strategy) + " Same: " + counter);
                //out.println("Strat: " + strategy);
                //out.println("BR: " + brAlg.getFullBRSequences());
            }
            out.println();
            out.println("P0BRs: " + outLine);
            out.println("Normalized root value: " + Arrays.toString(runner.getEV()));
        }
        
        
        static int start = 10;
        static int multiplier = 2;
        static int end = (int)1e6;
        public static void runSMMCTS_Exponential() throws Exception {
            //firstMCTSConfig = new MCTSConfig(new Simulator(), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            firstMCTSConfig = new MCTSConfig(new Simulator(), new Exp3MBackPropFactory(0,1,gamma), new UniformStrategyForMissingSequences.Factory(), null);
            Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);

            brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            
            
            Strategy strategy0 = null;
            Strategy strategy1 = null;
            String outLine = "";
            out.println("start= " + start + "; multiplier= " + multiplier + "; end= " + end);
            out.print("P1BRs: ");
            
            int last = 0;
            int next = start;
            for (;next < end;){
                strategy0 = runner.runMCTS(next-last, gameInfo.getAllPlayers()[0], /*new MeanStratDist() /*/ new FrequenceDistribution());
                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1], /*new MeanStratDist() /*/ new FrequenceDistribution());
                
                //out.print(next + ": " + (brAlg1.calculateBR(rootState, strategy0) + efg.getGameValue()) + " ");
                out.print((brAlg1.calculateBR(rootState, strategy0) + efg.getGameValue()) + " ");
                outLine += (brAlg0.calculateBR(rootState, strategy1) - efg.getGameValue()) + " ";
                last = next;
                next = last*multiplier;
            }
            out.println();
            out.println("P0BRs: " + outLine);
            out.println("Normalized root value: " + Arrays.toString(runner.getEV()));
        }
        
        public static void runSMMCTS_VK() throws Exception {
            
            double maxCFValue = gameInfo.getMaxUtility();
            
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new OOSBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(gamma*gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new Exp3BackPropFactory(-maxCFValue, maxCFValue, gamma), new UniformStrategyForMissingSequences.Factory(), null);

            Expander<MCTSInformationSet> expander = new GoofSpielExpander<MCTSInformationSet> (firstMCTSConfig);
            //Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);

            brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
            
            
            Strategy strategy0 = null;
            Strategy strategy1 = null;
            
            out.print("P1BRs: ");
            
            for (int i=0; i<5000; i++){
                strategy0 = runner.runMCTS(100, gameInfo.getAllPlayers()[0], /*new MeanStratDist() /*/ new FrequenceDistribution());
                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1],/* new MeanStratDist() /*/ new FrequenceDistribution());
                
                Strategy strategy_nogamma = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1],/* new MeanStratDist() /*/ new FrequenceDistribution(gamma));
              
                printRootDistribution(strategy1);
                //printRootDistribution(strategy_nogamma);
                double[] p = ((Exp3SelectionStrategy)(((InnerNode)(runner.getRootNode().selectChild())).getInformationSet().selectionStrategy)).p;
                for (double d : p) out.print(d+",");
                out.print(" ");
                out.print(runner.getEV()[1] + " ");
                double brVal = brAlg0.calculateBR(rootState, strategy1);
                out.print(brVal + " ");
                out.println(brAlg0.calculateBR(rootState, strategy_nogamma) + " ");
            }
            out.println();            
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
                out.print(en.getValue() + ",");
            }
            out.println();
        }

        public static void runSMMCTSExploitability(int milisPerMove){
            //double maxCFValue = gameInfo.getMaxUtility() / Math.pow(gamma/2.0, gameInfo.getMaxDepth());
            //double maxCFValue = 1e2;
            double maxCFValue = gameInfo.getMaxUtility();
            //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(1), new OOSBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
            MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(), new UCTBackPropFactory(gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
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
                out.println("Curlevel size: " +  curLevel.size());
                for (Node curRoot : curLevel){
                    runner.setRootNode((InnerNode)curRoot);
                    long start = threadBean.getCurrentThreadCpuTime();
                    int i=0;
                    for (;(threadBean.getCurrentThreadCpuTime()-start)/1000000 < milisPerMove;){
                       runner.runMCTS(100, gameInfo.getAllPlayers()[0], null);
                       i++;
                    }
                    out.print(i*100 + " ");
                      //runner.runMCTS(levelNums[l], gameInfo.getAllPlayers()[0], null);
                }
                out.println();
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
            out.println("P1BR: " + brAlg1.calculateBR(rootState, strategy0));
            out.println("P0BR: " + brAlg0.calculateBR(rootState, strategy1));

            FileManager<Strategy> fm = new FileManager<Strategy>();
            fm.saveObject(strategy0, "P0Exp3Mg"+gamma+"FixR" + run + ".strat");
            fm.saveObject(strategy1, "P1Exp3Mg"+gamma+"FixR" + run + ".strat");
        }
        
        public static void nipsManyGames() throws Exception {
            for (int bf : new int[]{2,3,5}){
                RandomGameInfo.MAX_BF = bf;
                for (double g : new double[]{0.05d, 0.1d, 0.2d}){
                    gamma = g;
                    out = new PrintStream("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_g" + gamma + ".out");
                    System.setOut(out);
                    out.println("Gamma=" + gamma + "; " + SMMCTSExperiment.firstMCTSConfig.getBackPropagationStrategyFactory().getClass());
                    for (int game = 0; game < 100; game++){
                        out.println("GAME: " + game);
                        setupRnd(game);
                        for (int i = 0; i < 100; i++) {
                            runSMMCTS_Exponential();
                        }
                    }
                }
            }
        }
        
        public static void nipsByParams(String[] args) throws Exception {
            gamma = Double.parseDouble(args[0]);
            //out = new PrintStream("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_g" + gamma + "_" + args[1] + "-" + args[2]  +".out");
            System.setOut(out);
            out.println("Gamma=" + gamma + "; " + SMMCTSExperiment.firstMCTSConfig.getBackPropagationStrategyFactory().getClass());
            for (int game = Integer.parseInt(args[1]); game <= Integer.parseInt(args[2]); game++){
                firstMCTSConfig = new MCTSConfig(new Simulator(), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
                out.println("GAME: " + game);
                setupRnd(game);
                for (int i = 0; i < 50; i++) {
                    runSMMCTS_Exponential();
                }
            }
        }
        
        public static void nipsRootExp() throws Exception {
            for (int bf : new int[]{2,3,5}){
                RandomGameInfo.MAX_BF = bf;
                for (double g : new double[]{0.05d, 0.1d, 0.2d}){
                    gamma = g;
                    out = new PrintStream("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_g" + gamma + ".R.out");
                    System.setOut(out);
                    out.println("Gamma=" + gamma + "; " + SMMCTSExperiment.firstMCTSConfig.getBackPropagationStrategyFactory().getClass());
                    for (int game = 0; game < 100; game++){
                        firstMCTSConfig = new MCTSConfig(new Simulator(), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
                        expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
                        out.println("GAME: " + game);
                        RandomGameInfo.seed = game;
                        gameInfo = new RandomGameInfo();
                        rootState = new SimRandomGameState();
                        Map<Action,Map<Action, Double>> rootMatrix = getRootMatrix(rootState);
                        for (int run = 0; run < 100; run++) {
                            firstMCTSConfig = new MCTSConfig(new Simulator(), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
                            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);
                            Strategy strategy1 = null;
                            out.print("P0BRs: ");
                            for (int i=0; i<1000; i++){
                                runner.runMCTS(5000, gameInfo.getAllPlayers()[0]);
                                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[0], new FrequenceDistribution(), 2);
                                out.print((computeP0RootBR(rootState, rootMatrix, strategy1) - gameValue) + " ");
                            }
                        }
                    }
                }
            }
        }

        public static void nipsSearchForWC(int start) throws Exception {
            gameInfo = new RandomGameInfo();
            
            PrintStream file = new PrintStream("WCsearch_" + start + ".out");
            //PrintStream file = System.out;
            System.setOut(new PrintStream("/dev/null"));

            for (int game = start; game < start + 10000; game++){
                file.print(game + " ");
                SimSystematicGS.gameCode = game;
                SimSystematicGS.utilities = SimSystematicGS.computeUtilities(game);
                //SimSystematicGS.utilities = new double[]{0,0,1,1, 0,0,1,1, 1,0.5,1,0.5, 0.5,1,0.5,1};
                rootState = new SimSystematicGS();
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
                sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
                efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
                optStrategies = efg.generate();
                GambitEFG.write("SYS22"+game+".efg", rootState, sfExpander);
                brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);

                int numRuns = 100;
                double[] runResults = new double[10];
                for (int run = 0; run < numRuns; run++){
                    firstMCTSConfig = new MCTSConfig(new Simulator(), new RMBackPropFactory(gamma), new UniformStrategyForMissingSequences.Factory(), null);
                    Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
                    MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);
                    Strategy strategy0 = null;
                    for (int i=0; i<10; i++){
                        strategy0 = runner.runMCTS(100, gameInfo.getAllPlayers()[0], /*new MeanStratDist() /*/ new FrequenceDistribution());
                        runResults[i] += ((double)(brAlg1.calculateBR(rootState, strategy0) + efg.getGameValue()))/numRuns;
                    }
                }
                file.println(Arrays.toString(runResults));
            }
        }
        
        public static void nipsWC_Trends() throws Exception {
            gameInfo = new RandomGameInfo();
            out = new PrintStream("WCtrendsExp3M.out");
            System.setOut(out);

                                    //WM_RMM, WC_RM, RMM << RM
            for (int game : new int[]{678921, 386943}){
                out.print(game + " ");
                SimSystematicGS.gameCode = game;
                SimSystematicGS.utilities = SimSystematicGS.computeUtilities(game);
                //SimSystematicGS.utilities = new double[]{0,0,1,1, 0,0,1,1, 1,0.5,1,0.5, 0.5,1,0.5,1};
                rootState = new SimSystematicGS();
                sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
                sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
                efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
                optStrategies = efg.generate();
                GambitEFG.write("SYS22"+game+".efg", rootState, sfExpander);
                brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);

                for (int run = 0; run < 1000; run++){
                    runSMMCTS_Exponential();
                }
            }
        }
        
        static int run = 0;
        //args: seed BF
        public static void main(String[] args) throws Exception {
            nipsWC_Trends();
            //nipsSearchForWC(386943);
//            nipsSearchForWC(Integer.parseInt(args[0]));
            
//            setupRnd(182);
//            runSMMCTS_Exponential();
            
//            nipsManyGames();
            
//            nipsByParams(args);
            
            
            //             runSMMCTS();
            //            RandomGameInfo.MAX_BF = Integer.parseInt(args[1]);
            //            setupRnd(Long.parseLong(args[0]));
            //            for (;;){
            //            int r = (new Random()).nextInt();
            //            out.println(r);
            //            setupRnd(r);
            //            runSMMCTS();
            //            }
            //
            //            for (Map.Entry<Sequence, Double> en : optStrategies.get(gameInfo.getAllPlayers()[0]).entrySet()){
            //                if (en.getKey().size() < 3 && en.getValue() > 1e-5) out.println(en);
            //            }
            //            for (double g : new double[]{0.1,0.05, 0.025}){
            //            //for (double g : new double[]{0.2,0.3,0.35}){
            //              gamma = g;
            //              out.println("Gamma: " + g);
            //              for (int trial=0; trial < 100; trial++) runSMMCTS();
            //            }
            //             runSMMCTS();
            //            RandomGameInfo.MAX_BF = Integer.parseInt(args[1]);
            //            setupRnd(Long.parseLong(args[0]));
            //            }
            //            int r = (new Random()).nextInt();
            //            out.println(r);
            //            setupRnd(r);
            //            runSMMCTS();
            //            }
            //
            //            for (Map.Entry<Sequence, Double> en : optStrategies.get(gameInfo.getAllPlayers()[0]).entrySet()){
            //                if (en.getKey().size() < 3 && en.getValue() > 1e-5) out.println(en);
            //            }
            
            
//                        for (double g : new double[]{0.1, 0.15}){
//                        //for (double g : new double[]{0.2,0.3,0.35}){
//                          gamma = g;
//                          out.println("Gamma: " + g);
//                          for (int trial=0; trial < 100; trial++) runSMMCTS();
//                        }
	}
 
        private static double computeP0RootBR(GameState rootState, Map<Action,Map<Action, Double>>  rootMatrix, Strategy p1Strategy){
            double max = -Double.MAX_VALUE;
            for (Action a0 : expander.getActions(rootState)){
                Map<Action, Double> line = rootMatrix.get(a0);
                double val=0;
                for (Map.Entry<Sequence, Double> en : p1Strategy.entrySet()){
                    assert en.getKey().size() == 1;
                    Action a1 = en.getKey().getFirst();
                    val += en.getValue() * line.get(a1);
                }
                if (val > max) max = val;
            }            
            return max;
        }
        
        
        private static double gameValue = Double.NaN;
        private static Map<Action,Map<Action, Double>>  getRootMatrix(GameState s){
            HashMap<Action, Map<Action, Double>> out = new HashMap<Action, Map<Action, Double>>();
            ArrayList<Action> p1Actions = new ArrayList(expander.getActions(s));
            ArrayList<Action> p2Actions = new ArrayList(expander.getActions(s.performAction(p1Actions.get(0))));
            
            for (Action a1 : p1Actions){
                HashMap<Action, Double> line = new HashMap<Action, Double>();
                out.put(a1, line);
                GameState halfMove = s.performAction(a1);
                for (Action a2 : p2Actions){
                    double val = getNodeValue(halfMove.performAction(a2));
                    line.put(a2, val);
                }
            }
            
            return out;
        }
        
        
        private static double  getNodeValue(GameState s){
            if (s.isGameEnd()) return s.getUtilities()[0];
            
            
            ArrayList<Action> p1Actions = new ArrayList(expander.getActions(s));
            ArrayList<Action> p2Actions = new ArrayList(expander.getActions(s.performAction(p1Actions.get(0))));
            
            UtilityMatrix matrix = new UtilityMatrix(p1Actions.size(), p2Actions.size());
            
            int i=0,j=0;
            for (Action a1 : p1Actions){
                j = 0;
                GameState halfMove = s.performAction(a1);
                for (Action a2 : p2Actions){
                    double val = getNodeValue(halfMove.performAction(a2));
                    matrix.set(i,j++,val, -val);
                }
                i++;
            }

            ZeroSumGameNESolver solver = new ZeroSumGameNESolverImpl<UtilityMatrix.IndexOfPlayer, UtilityMatrix.IndexOfPlayer>(matrix);
            solver.addPlayerOneStrategies(matrix.getStrategies(0));
            solver.addPlayerTwoStrategies(matrix.getStrategies(1));
            solver.computeNashEquilibrium();
            return solver.getGameValue();
        }
}
