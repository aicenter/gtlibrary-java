/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.experiments;

import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
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
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl; 
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import cz.agents.gtlibrary.strategy.Strategy;
import java.io.FileOutputStream;
import java.io.PrintStream;
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

    public static void setupRnd(long seed) {
        RandomGameInfo.MAX_DEPTH = 5;
        RandomGameInfo.MAX_BF = 3;
        RandomGameInfo.BINARY_UTILITY = true;
        RandomGameInfo.seed = seed;
        gameInfo = new RandomGameInfo();
        rootState = new SimRandomGameState();
        expander = new RandomGameExpander<MCTSInformationSet> (new MCTSConfig());
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander, gameInfo, sfAlgConfig);
        optStrategies = efg.generate();
        GambitEFG.write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
    }
    
    public static void setupGoofSpiel(int d){
        assert GSGameInfo.depth==d;
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
//        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
//        sfExpander = new AntiMCTSExpander<SequenceInformationSet>(sfAlgConfig);
//        efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
//        efg.generate();
//        GambitEFG.write("AntiMCTS" + AntiMCTSInfo.gameDepth + ".efg", rootState, sfExpander);
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
            if (is.getAlgorithmData() == null) {
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
    public static void runMCTSExp3() throws Exception {
        assert algorithm.equals("Exp3");
        
        Distribution dist = new MeanStratDist();

        ISMCTSAlgorithm alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
                    new DefaultSimulator(expander),
                    new Exp3BackPropFactory(-1, 1, gamma, keepExploration),
                    //new RMBackPropFactory(-1, 1, gamma),
                    rootState, expander);
        alg.returnMeanValue=propagateMeans;

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
        System.out.println("P0BRs: " + outLine);
        //System.out.println("Strat: " + strategy0.fancyToString(rootState, expander, rootState.getAllPlayers()[0]));
        //System.out.println("Strat: " + strategy1.fancyToString(rootState, expander, rootState.getAllPlayers()[1]));
    }
    
    
    // game algorithm iterations_per_output
    //arguments: Anti[EL]D/GSX/RNDYYY OOS6/Exp3[MV][RK]2 100000
    
    private static int iterations = 100000;
    private static String algorithm = "Exp3";
    private static boolean keepExploration = false;
    private static boolean propagateMeans = false;
    public static void main(String[] args) throws Exception {
        //System.setOut(new PrintStream("experiments/SMMCTS/" + StringUtils.join(args)));
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
        }
        iterations=Integer.parseInt(args[2]);
        
        for (int j=0;j<100;j++) {
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

            }
            if (algorithm.equals("OOS")){
                runMCTSOOS();
            } else {
                runMCTSExp3();
            }
            
        }
    }
}
