/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.FrequenceDistribution;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.FullSequenceEFG;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author vilo
 */
public class ConvergenceExperiment {

    static GameInfo gameInfo;
    static GameState rootState;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static Expander<SequenceInformationSet> sfExpander;
    static FullSequenceEFG efg;
    static Map<Player, Map<Sequence, Double>> optStrategies;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;

    public static void setupRnd(long seed) {
        RandomGameInfo.seed = seed;
        gameInfo = new RandomGameInfo();
        rootState = new RandomGameState();
        sfAlgConfig = new SequenceFormConfig<SequenceInformationSet>();
        sfExpander = new RandomGameExpander<SequenceInformationSet>(sfAlgConfig);
        efg = new FullSequenceEFG(rootState, sfExpander, gameInfo, sfAlgConfig);
        optStrategies = efg.generate();
        brAlg0 = new SQFBestResponseAlgorithm(sfExpander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, sfAlgConfig, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(sfExpander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, sfAlgConfig, gameInfo);
        GambitEFG.write("RND" + RandomGameInfo.MAX_BF + RandomGameInfo.MAX_DEPTH + "_" +seed+".efg", rootState, sfExpander);
    }
    
    static double gamma = 0.1;
    public static void runMCTS() throws Exception {
        //MCTSConfig mctsConfig = new MCTSConfig(new Simulator(1), new UCTBackPropFactory(gameInfo.getMaxUtility()), new UniformStrategyForMissingSequences.Factory(), null);
        MCTSConfig mctsConfig = new MCTSConfig(new DefaultSimulator(1), new Exp3BackPropFactory(-gameInfo.getMaxUtility(), gameInfo.getMaxUtility(), gamma), new UniformStrategyForMissingSequences.Factory(), null);
        Expander<MCTSInformationSet> expander = new RandomGameExpander<MCTSInformationSet> (mctsConfig);
        MCTSRunner runner = new MCTSRunner(mctsConfig, rootState, expander);

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, sfAlgConfig, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, sfAlgConfig, gameInfo);


        Strategy strategy0 = null;
        Strategy strategy1 = null;
        String outLine = "";
        System.out.print("P1BRs: ");

        for (int i = 0; i < 100; i++) {
            strategy0 = runner.runMCTS(100000, gameInfo.getAllPlayers()[0], new FrequenceDistribution());
            strategy1 = runner.getCurrentStrategyFor(gameInfo.getAllPlayers()[1], new FrequenceDistribution());

            System.out.print(brAlg1.calculateBR(rootState, strategy0) + " ");
            System.out.flush();
            outLine += brAlg0.calculateBR(rootState, strategy1) + " ";

            //System.out.println("Value: " + runner.getEV()[0] + " BR: " + brAlg.calculateBR(rootState, strategy) + " Same: " + counter);
            //System.out.println("Strat: " + strategy);
            //System.out.println("BR: " + brAlg.getFullBRSequences());
        }
        System.out.println();
        System.out.println("P0BRs: " + outLine);
        System.out.println("Normalized root value: " + Arrays.toString(runner.getEV()));
    }
    
    public static void main(String[] args) throws Exception {
        setupRnd(0);
        runMCTS();
    }
}
