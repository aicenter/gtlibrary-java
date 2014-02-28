/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.HighestValueAction;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MostFrequentAction;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.randomgame.FIRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameAction;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class FIMCTSExperiment {
        private static int REPETITIONS = 1000;
    
    
        static GameInfo gameInfo;
        static GameState rootState;
        static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
        static Expander<SequenceInformationSet> sfExpander;
        static FullSequenceEFG efg;
        static Map<Player, Map<Sequence, Double>> optStrategies;
        static SQFBestResponseAlgorithm brAlg0;
        static SQFBestResponseAlgorithm brAlg1;
        static Map<Action, Double> rootActionValues;
        static double gameValue = -Double.MAX_VALUE;
        
        public static void setupRnd(long seed){
            if (seed == RandomGameInfo.seed && rootState != null) return;
            RandomGameInfo.seed = seed;
            rootActionValues = new HashMap<Action, Double>();
            gameValue = -Double.MAX_VALUE;
            gameInfo = new RandomGameInfo();
            rootState = new FIRandomGameState();
            sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
            sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
            efg = new FullSequenceEFG(rootState, sfExpander , gameInfo, sfAlgConfig);
            efg.generateCompleteGame();
            for (Action a : sfExpander.getActions(rootState)){
                GameState s = rootState.performAction(a);
                double value = getMinimaxValue(s);
                rootActionValues.put(a, value);
                if (gameValue < value) gameValue = value;
            }
            System.out.println(rootActionValues);
//            GambitEFG.write("FIRND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
        }
        
        private static double[] means;
        public static void runFIMCTS() throws Exception {
            //compute the probability that the selected action is sub-optional over time/iterations
            MCTSConfig firstMCTSConfig = new MCTSConfig(new DefaultSimulator(), new UCTBackPropFactory(gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
            Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (firstMCTSConfig);
            MCTSRunner runner = new MCTSRunner(firstMCTSConfig, rootState, expander);
            Distribution dist =  new MostFrequentAction();
            //Distribution dist = new HighestValueAction();
            
            for (int i=0; i<100; i++){
                runner.runMCTS(10, gameInfo.getAllPlayers()[0]);
                //Strategy strategy = runner.runMCTS(10, gameInfo.getAllPlayers()[0],  );
                Action selAct = null;
                for (Map.Entry<Action, Double> en : dist.getDistributionFor(runner.getRootNode().getInformationSet()).entrySet()) {
                    if (en.getValue() == 1d) {
                        selAct = en.getKey();
                        break;
                    }
                }
                means[i] += (gameValue - rootActionValues.get(selAct)) / REPETITIONS;
                //System.out.print((gameValue - rootActionValues.get(selAct)) + " ");
            }
            //System.out.println();
            
        }
        
        private static Action getRootAction(Map<Sequence, Double> strategy){
            for (Map.Entry<Sequence, Double> en : strategy.entrySet()){
                if (en.getKey().size() == 1) return en.getKey().getFirst();
            }
            return null;
        }
        
        public static void main(String[] args) throws Exception {
            for (int game=0; game < 1000; game++){
                means = new double[100];
                for (int r=0; r<REPETITIONS; r++){
                    setupRnd(game);
                    runFIMCTS();
                }
                System.out.println(game + " " + Arrays.toString(means));
            }
        }
        
        private static double getMinimaxValue(GameState s){
            if (s.isGameEnd()) return s.getUtilities()[0];
            int modifier = (s.getPlayerToMove().getId() == 0 ? 1 : -1);
            double max = -Double.MAX_VALUE;
            for (Action a : sfExpander.getActions(s)){
                double value = getMinimaxValue(s.performAction(a)) * modifier;
                if (value > max) max = value;
            }
            return modifier*max;
        }
        
        
}
