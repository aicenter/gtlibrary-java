/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.OOSLeafNode;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.*;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.io.GambitEFG;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;

import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author vilo
 */
public class IIGMCTSExperiment {
        private static final int MCTS_ITERATIONS_PER_CALL = (int)1000000;
        private static final int MCTS_CALLS = 100;
	private static PrintStream out = System.out;
    
        
        static GameInfo gameInfo;
        static GameState rootState;
        static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
        static Expander<SequenceInformationSet> sfExpander;
        static Expander<MCTSInformationSet> expander;
        static FullSequenceEFG efg;
        static Map<Player, Map<Sequence, Double>> optStrategies;
        static SQFBestResponseAlgorithm brAlg0;
        static SQFBestResponseAlgorithm brAlg1;

        static MCTSRunner mctsRunner;
        
        public static void setupPTTT(){
            gameInfo = new TTTInfo();
            rootState = new TTTState();
//            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
//            sfExpander = new TTTExpander<SequenceInformationSet>(sfAlgConfig);
            expander = new TTTExpander<MCTSInformationSet> (firstMCTSConfig);
//            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
//            efg.generateCompleteGame();
//            SMMCTSExperiment.printRootDistribution(optStrategies.get(rootState.getAllPlayers()[1]));
//            System.out.println();
//            brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
//            brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
        }

         public static void setupRnd(long seed){
            if (seed == RandomGameInfo.seed && rootState != null) return;
            RandomGameInfo.seed = seed;
            gameInfo = new RandomGameInfo();
            rootState = new RandomGameState();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
            expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            //optStrategies = efg.generate();
            efg.generateCompleteGame();
            brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, sfAlgConfig, gameInfo);
            GambitEFG.write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
        }
        
        static final double gamma = 0.6;
        static final OOSBackPropFactory fact = new OOSBackPropFactory(gamma);
        static {OOSLeafNode.fact = fact;}
        static MCTSConfig firstMCTSConfig = new MCTSConfig(new OOSSimulator(fact), fact, new UniformStrategyForMissingSequences.Factory(), null);
        //MCTSConfig firstMCTSConfig = new MCTSConfig(new Simulator(), new UCTBackPropFactory(gamma*gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
        
        public static void runMCTS() throws Exception {
            OOSMCTSRunner runner = new OOSMCTSRunner(firstMCTSConfig, rootState, expander);

            Strategy strategy0 = null;
            Strategy strategy1 = null;
            String outLine = "";
            out .print("P1BRs: ");
            
            for (int i=0; i<MCTS_CALLS; i++){
                strategy0 = runner.runMCTS(MCTS_ITERATIONS_PER_CALL, gameInfo.getAllPlayers()[0], new MeanStratDist());
                strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1], new MeanStratDist());
                
                out.println("Strategy 0 size = " + strategy0.size());
                out.println("Strategy 1 size = " + strategy1.size());
                filterLow(strategy0);
                filterLow(strategy1);
                out.println("Strategy 0 size = " + strategy0.size());
                out.println("Strategy 1 size = " + strategy1.size());
                
                SQFBestResponseAlgorithm mctsBR0 = new SQFBestResponseAlgorithm(expander, 0, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
                SQFBestResponseAlgorithm mctsBR1 = new SQFBestResponseAlgorithm(expander, 1, new Player[] { rootState.getAllPlayers()[0], rootState.getAllPlayers()[1] }, firstMCTSConfig, gameInfo);
               
                //out.print((brAlg1.calculateBR(rootState, strategy0) /*+ efg.getGameValue()*/) + " ");
                out.print((mctsBR0.calculateBR(rootState, strategy1) /*+ efg.getGameValue()*/) + " ");
                outLine += (mctsBR1.calculateBR(rootState, strategy0) /* - efg.getGameValue()*/) + " ";
            }
            out.println("\n");
            out.println("P0BRs: " + outLine + "\n");
            out.println();
            //out.println(strategy0.toString() + "\n");
            //out.println(strategy1.toString());
            
            
            
        }
        
        public static Strategy filterLow(Strategy s){
            for (Iterator<Map.Entry<Sequence, Double>> it = s.entrySet().iterator(); it.hasNext();){
                if (it.next().getValue() < 1e-5) it.remove();
            }
            return s;
        }
        
        public static void main(String[] args) throws Exception{
            //setupRnd(325432);
            setupPTTT();
            runMCTS();
        }
 }
