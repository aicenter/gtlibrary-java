/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.experiments;

import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.SMMCTSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.ConjectureFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.ABackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMConjectureFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMRMBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.antiMCTS.AntiMCTSExpander;
import cz.agents.gtlibrary.domain.antiMCTS.AntiMCTSInfo;
import cz.agents.gtlibrary.domain.antiMCTS.AntiMCTSState;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.SimSystematicGS;
import cz.agents.gtlibrary.iinodes.ConfigImpl; 
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import cz.agents.gtlibrary.strategy.Strategy;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import javax.print.attribute.standard.DateTimeAtCompleted;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author vilo
 */
public class SMConvergenceExperiment {

    static boolean buildCompleteTree = false;
    static GameInfo gameInfo;
    static GameState rootState;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static Expander<SequenceInformationSet> sfExpander;
    static FullSequenceEFG efg;
    static Map<Player, Map<Sequence, Double>> optStrategies;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;
    static Expander<MCTSInformationSet> expander;

    public static void setupRnd(int depth) {
        RandomGameInfo.MAX_DEPTH = depth;
        RandomGameInfo.MAX_BF = 3;
        String s = System.getProperty("RNDG_BF");
        if (s != null) RandomGameInfo.MAX_BF = Integer.parseInt(s);
        RandomGameInfo.MAX_CENTER_MODIFICATION=1;
        RandomGameInfo.UTILITY_CORRELATION = false;
        RandomGameInfo.MAX_UTILITY=1;
        RandomGameInfo.BINARY_UTILITY = false;
        RandomGameInfo.FIXED_SIZE_BF = false;
        if (s != null) RandomGameInfo.FIXED_SIZE_BF = true;
        RandomGameInfo.seed = 792;
        s = null;
        s = System.getProperty("RNDG_SEED");
        if (s != null) RandomGameInfo.seed = Integer.parseInt(s);
        gameInfo = new RandomGameInfo();
        rootState = new SimRandomGameState();
        expander = new RandomGameExpander<MCTSInformationSet> (new MCTSConfig());
        if (sfAlgConfig == null){
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
            efg = new FullSequenceEFG(rootState, sfExpander, gameInfo, sfAlgConfig);
            optStrategies = efg.generate();
            new GambitEFG().write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +RandomGameInfo.seed +".efg", rootState, sfExpander);
        }
    }
    
    public static void setupAntiExploration() {
        RandomGameInfo.MAX_DEPTH = 2;
        RandomGameInfo.MAX_BF = 2;
        SimSystematicGS.utilities = new double[]{-1,-1,-1,-1, 0,1,0,1, -1,-1,1,1, 0.5,1,0.5,1};
        gameInfo = new RandomGameInfo();
        rootState = new SimSystematicGS();
        expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig());
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander, gameInfo, sfAlgConfig);
        optStrategies = efg.generate();
        new GambitEFG().write("antiExploration.efg", rootState, sfExpander);
    }
    
    
    public static void setupGoofSpiel(int d){
        GSGameInfo.depth = d;
        GSGameInfo.regenerateCards = true;
        gameInfo = new GSGameInfo();
        rootState = new GoofSpielGameState();
        expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig());
//        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
//        sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
//        efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
//        efg.generateCompleteGame();
    }
    
    public static void setupAntiMCTS(int gameDepth, boolean expRewards){
        AntiMCTSInfo.gameDepth=gameDepth;
        AntiMCTSInfo.exponentialRewards=expRewards;
        gameInfo = new AntiMCTSInfo();
        rootState = new AntiMCTSState();
        expander = new AntiMCTSExpander<MCTSInformationSet>(new MCTSConfig());
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new AntiMCTSExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
        efg.generate();
        (new GambitEFG()).write("AntiMCTS" + AntiMCTSInfo.gameDepth + ".efg", rootState, sfExpander);
    }
    
    public static void setupOshiZumo(int coins, int locs){
        OZGameInfo.startingCoins = coins;
        OZGameInfo.locK = locs;
        gameInfo = new OZGameInfo();
        rootState = new OshiZumoGameState();
        expander = new OshiZumoExpander<>(new MCTSConfig());
    }
    
    public static void buildCompleteTree(InnerNode r){
        System.out.println("Building complete tree.");
        int nodes=0, infosets=0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()){
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (is!= null && is.getAlgorithmData() == null) {
                infosets++;
                is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
            }
            for (Action a : n.getActions()){
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode)ch);
                }
            }
        }
        System.out.println("Created nodes: " + nodes +"; infosets: " +infosets);
    }
    
    
    static double gamma = 0.6;
    public static void runMCTSOOS() throws Exception {
        assert algorithm.equals("OOS");
        OOSAlgorithm alg = new OOSAlgorithm(
                rootState.getAllPlayers()[0],
                new OOSSimulator(expander),
                rootState, expander, 0, gamma);
        Distribution dist = new MeanStratDist();

        if (buildCompleteTree) buildCompleteTree(alg.getRootNode());
        
        alg.runIterations(2);
        
        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        String outLine = "";
        System.out.print("P1BRs: ");

        for (int i = 0; i < 100; i++) {
            alg.runIterations(iterations);
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            System.out.print(brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0)) + " ");
            System.out.flush();
            outLine += brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1)) + " ";

            //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
            //System.out.println("BR: " + brAlg.getFullBRSequences());
        }
        System.out.println();
    }
    public static void runISMCTS() throws Exception {
        assert algorithm.equals("Exp3") || !keepExploration;
        
        Distribution dist = new MeanStratDist();

        ISMCTSAlgorithm alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
                    new DefaultSimulator(expander),
                    algorithm.equals("Exp3") ? (propagateMeans ? new ABackPropFactory(new Exp3BackPropFactory(-1, 1, gamma, keepExploration)) : new ConjectureFactory(new Exp3BackPropFactory(-1, 1, gamma, keepExploration)))
                            : (propagateMeans ? new ABackPropFactory(new RMBackPropFactory(-1, 1, gamma)) : new ConjectureFactory(new RMBackPropFactory(-1, 1, gamma))),
                    rootState, expander);
        //alg.returnMeanValue=propagateMeans;  //means should not be really propagated like this

        assert !buildCompleteTree;
        
        alg.runIterations(2);
        
        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        StringBuilder p0brs = new StringBuilder();
        StringBuilder p1brs = new StringBuilder();
        StringBuilder times = new StringBuilder();
        
        
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int its = 1000;
        int totalIts = 0;
        double totalTime=0;
        

        for (; totalIts < iterations;) {
            long start = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(its);
            totalTime += (threadBean.getCurrentThreadCpuTime() - start)/1e6;
            totalIts+=its;
            
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            p1brs.append(brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0)) + " ");
            p0brs.append(brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1)) + " ");
            times.append(totalTime + " ");
            if (!propagateMeans){
                if (alg.getRootNode().getGameState().isPlayerToMoveNature())
                    System.out.println(((InnerNode)alg.getRootNode().getChildren().values().iterator().next()).getInformationSet().getAlgorithmData().toString());
                else 
                    System.out.println(alg.getRootNode().getInformationSet().getAlgorithmData().toString());
                System.out.flush();
            }
            

            //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
            //System.out.println("BR: " + brAlg.getFullBRSequences());
            its *= 1.2;
        }
        System.out.println();
        System.out.println("P0BRs: " + p0brs.toString());
        System.out.println("P1BRs: " + p1brs.toString());
        System.out.println("Times: " + times.toString());
        //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
        //System.out.println("Strat: " + strategy1.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
    }
    
    public static void runSMMCTS_RM() throws Exception {        
        Distribution dist = new MeanStratDist();

        SMMCTSAlgorithm alg = new SMMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
                    new DefaultSimulator(expander),
                    new SMRMBackPropFactory(gamma),
                    rootState, expander);

        assert !buildCompleteTree;
        
        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        Strategy strategy0 = null;
        Strategy strategy1 = null;
        String outLine = "";
        System.out.print("P1BRs: ");

        for (int i = 0; i < 100; i++) {
            alg.runIterations(iterations);
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            System.out.print(brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0)) + " ");
            System.out.flush();
            outLine += brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1)) + " ";

            //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
            //System.out.println("BR: " + brAlg.getFullBRSequences());
        }
        System.out.println();
        System.out.println("P0BRs: " + outLine);
        //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
        //System.out.println("Strat: " + strategy1.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
    }
   
    public static void runSMMCTS_Decoupled() throws Exception {        
        Distribution dist = new MeanStratDist();

        assert algorithm.equals("Exp3") || !keepExploration;
        
        SMMCTSAlgorithm alg = new SMMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
                    new DefaultSimulator(expander),
                    //new SMConjectureFactory(gamma),
                    algorithm.equals("Exp3") ? new SMConjectureFactory(new Exp3BackPropFactory(-gameInfo.getMaxUtility(), gameInfo.getMaxUtility(), gamma, keepExploration), propagateMeans) 
                            : new SMConjectureFactory(new RMBackPropFactory(-gameInfo.getMaxUtility(), gameInfo.getMaxUtility(), gamma), propagateMeans),
                    rootState, expander);

        assert !buildCompleteTree;
        
        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        Strategy strategy0 = null;
        Strategy strategy1 = null;
        StringBuilder p0brs = new StringBuilder();
        StringBuilder p1brs = new StringBuilder();
        StringBuilder times = new StringBuilder();
        
        
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int its = 1000;
        int totalIts = 0;
        double totalTime=0;
        

        for (; totalIts < iterations;) {
            long start = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(its);
            totalTime += (threadBean.getCurrentThreadCpuTime() - start)/1e6;
            totalIts+=its;
            
            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            p1brs.append(brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0)) + " ");
            p0brs.append(brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1)) + " ");
            times.append(totalTime + " ");
            if (alg.getRootNode().getGameState().isPlayerToMoveNature())
                System.out.println(((InnerNode)alg.getRootNode().getChildren().values().iterator().next()).getInformationSet().getAlgorithmData().toString());
            else 
                System.out.println(alg.getRootNode().getInformationSet().getAlgorithmData().toString());
            System.out.flush();
            

            //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
            //System.out.println("BR: " + brAlg.getFullBRSequences());
            its *= 1.2;
        }
        System.out.println();
        System.out.println("P0BRs: " + p0brs.toString());
        System.out.println("P1BRs: " + p1brs.toString());
        System.out.println("Times: " + times.toString());
        //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
        //System.out.println("Strat: " + strategy1.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
    }
    
    
    // game algorithm iterations_per_output
    //arguments: Anti[EL]D/GSX/RNDYYY OOS6/Exp3[MV][RK]2 100000
    
    private static int runs=50;
    private static int iterations = (int) 1e6;
    private static String algorithm = "Exp3";
    private static boolean keepExploration = false;
    private static boolean propagateMeans = false;
    public static void batchMain(String[] args) throws Exception {
        //System.setOut(new PrintStream("experiments/SMMCTS/" + StringUtils.join(args)));
        
        String s = System.getProperty("RUNS");
        if (s != null) runs = new Integer(s);
        
        switch(args[1].substring(0, 3)){
            case "OOS":
                algorithm = "OOS";
                gamma = Double.parseDouble(args[1].substring(3))/10;
                if (args[1].charAt(3)=='0') gamma /= 10;
                break;
            case "Exp":
                algorithm = "Exp3";
                gamma = Double.parseDouble(args[1].substring(6))/10;
                if (args[1].charAt(6)=='0') gamma /= 10;
                propagateMeans = args[1].charAt(4) == 'M';
                keepExploration = args[1].charAt(5) == 'K';
                break;
            default:
                assert args[1].startsWith("RM");
                algorithm = "RM";
                gamma = Double.parseDouble(args[1].substring(4))/10;
                if (args[1].charAt(4)=='0') gamma /= 10;
                propagateMeans = args[1].charAt(2) == 'M';
                keepExploration = args[1].charAt(3) == 'K';
        }
        iterations=Integer.parseInt(args[2]);
        
        for (int j=0;j<runs;j++) {
            switch(args[0].substring(0, 2)){
                case "An":
                    boolean expRewards = args[0].charAt(4)=='E';
                    setupAntiMCTS(Integer.parseInt(args[0].substring(5)), expRewards);
                    break;
                case "GS":
                    setupGoofSpiel(Integer.parseInt(args[0].substring(2)));
                    break;
                case "RN":
                    setupRnd(Integer.parseInt(args[0].substring(3)));
                    break;
                case "OZ":
                    setupOshiZumo(Integer.parseInt(args[0].substring(2)), 2);
                    break;

            }
            if (algorithm.equals("OOS")){
                runMCTSOOS();
            } else {
                if (args[0].substring(0, 2).equals("An")){
                  runISMCTS();
                } else runSMMCTS_Decoupled();
            }
            
        }
    }
    
    public static void main(String[] args) throws Exception {
        batchMain(args);
        //setupGoofSpiel(3);
        //setupOshiZumo(8, 2);
        //setupRnd(6);
        //setupAntiExploration();
//        setupAntiMCTS(5, true);
//        gamma=0.01;
//        runSMMCTS_Exp3();
        //runMCTSExp3();
    }
}
