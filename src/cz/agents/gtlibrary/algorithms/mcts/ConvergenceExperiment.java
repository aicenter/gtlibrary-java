/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

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
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl; 
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class ConvergenceExperiment {

    static boolean buildCompleteTree = true;
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
        RandomGameInfo.MAX_DEPTH = 1;
        RandomGameInfo.MAX_BF = 2;
        RandomGameInfo.BINARY_UTILITY = true;
        RandomGameInfo.seed = seed;
        gameInfo = new RandomGameInfo();
        rootState = new RandomGameState();
        expander = new RandomGameExpander<MCTSInformationSet> (new MCTSConfig());
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander, gameInfo, sfAlgConfig);
        optStrategies = efg.generate();
        GambitEFG.write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
    }
    
    public static void setupIIGoofSpielExpl(){
        gameInfo = new GSGameInfo();
        rootState = new GoofSpielGameState();
        expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig());
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new GoofSpielExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
        efg.generateCompleteGame();
    }
    
    public static void setupPoker(){
        GPGameInfo.MAX_RAISES_IN_ROW = 2;
        GPGameInfo.MAX_DIFFERENT_BETS = 3;
        GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;
        GPGameInfo.MAX_CARD_TYPES = 4;
        GPGameInfo.MAX_CARD_OF_EACH_TYPE = 3;
        gameInfo = new GPGameInfo();
        rootState = new GenericPokerGameState();
        expander = new GenericPokerExpander<MCTSInformationSet>(new MCTSConfig());
        //sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        //sfExpander = new GenericPokerExpander<SequenceInformationSet>(sfAlgConfig);
        //efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
        //efg.generateCompleteGame();
    }
    
    public static void setupPTTT(){
        gameInfo = new TTTInfo();
        rootState = new TTTState();
        expander = new TTTExpander<MCTSInformationSet>(new MCTSConfig());
        //sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        //sfExpander = new GenericPokerExpander<SequenceInformationSet>(sfAlgConfig);
        //efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
        //efg.generateCompleteGame();
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
    public static void runMCTS() throws Exception {
        
        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        
        CFRAlgorithm alg = new CFRAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);
        
//        OOSAlgorithm alg = new OOSAlgorithm(
//                rootState.getAllPlayers()[0],
//                new OOSSimulator(expander),
//                rootState, expander, 0, gamma);
        Distribution dist = new MeanStratDist();

//        ISMCTSAlgorithm alg = new ISMCTSAlgorithm(
//                    rootState.getAllPlayers()[0],
//                    new DefaultSimulator(expander),
//                    //new UCTBackPropFactory(2),
//                    new Exp3BackPropFactory(-1, 1, 0.05),
//                    //new RMBackPropFactory(-1,1,0.4),
//                    rootState, expander);
        //alg.returnMeanValue=true;
        //Distribution dist = new FrequenceDistribution();

        if (buildCompleteTree) buildCompleteTree(alg.getRootNode());
        
        alg.runMiliseconds(100);
        
        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl)expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        String outLine = "";
        System.out.print("P1BRs: ");

        for (int i = 0; i < 500; i++) {
            alg.runMiliseconds(1000);
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
    
    public static void main(String[] args) throws Exception {
        //setupIIGoofSpielExpl();
        //setupPoker();
        setupRnd(2);
        for (;;) runMCTS();
    }
}
