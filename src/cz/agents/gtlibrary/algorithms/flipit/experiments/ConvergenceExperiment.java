package cz.agents.gtlibrary.algorithms.flipit.experiments;

import cz.agents.gtlibrary.algorithms.cfr.CFRISAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.DefaultSimulator;
import cz.agents.gtlibrary.algorithms.mcts.ISMCTSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.*;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.GeneralDoubleOracle;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jakub Cerny on 07/05/17.
 *
 *
 * Experiments with using fast equilibrium approximate by MCTS or CFR as a starting strategy for DO.
 * The approximate strategy has large support, which has to be cut off in order to begin with a small enough
 * restricted game.
 */
public class ConvergenceExperiment {

    private static int RUN_MS = 280000;
    private static boolean RUN_CFR = false; // use MCTS if false
    private static int SUPPORT_SIZE = 2;
    private static double SUPPORT_MIN = 0.2;

    private static double EPS = 0.001;

    static boolean OUTPUT = false;
    static String outputFile;
    static String output;

    public static void main(String[] args) {
        FlipItGameInfo gameInfo;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[0]);
            int graphSize = Integer.parseInt(args[1]);
            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, 1, graphFile, 1);
            if (args.length > 2) {
                if (Integer.parseInt(args[2]) == 0) RUN_CFR = true;
                else RUN_CFR = false;
                RUN_MS = Integer.parseInt(args[3]);
                SUPPORT_SIZE = Integer.parseInt(args[4]);

                outputFile = args[5];
                output = graphSize + " " + depth + " " + RUN_MS + " " + SUPPORT_SIZE + " ";

                OUTPUT = true;

            }
        }
        gameInfo.ZERO_SUM_APPROX = true;
        GameState rootState = null;

        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;

        }
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander expander = new FlipItExpander<MCTSInformationSet>(new MCTSConfig());

        CFRISAlgorithm algIS = new CFRISAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);

        ISMCTSAlgorithm alg = new ISMCTSAlgorithm(
                    rootState.getAllPlayers()[0],
                    new DefaultSimulator(expander),
//                    new UCTBackPropFactory(2),
                    new Exp3BackPropFactory(-1, 1, 0.05),
                    //new RMBackPropFactory(-1,1,0.4),
                    rootState, expander);
//        alg.returnMeanValue=true;

        Strategy strategy0 = null;
        Strategy strategy1 = null;

//        Distribution dist = new MostFrequentAction();//MeanStratDist();
        Distribution dist = new BoundedMeanStratDist(SUPPORT_SIZE);
//        Distribution dist = new BoundedMeanStratDist(SUPPORT_MIN);

        if (!RUN_CFR) {

            System.out.println("Running MCTS ... ");
            alg.runMiliseconds(RUN_MS);

            strategy0 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[0], dist);
            strategy1 = StrategyCollector.getStrategyFor(alg.getRootNode(), rootState.getAllPlayers()[1], dist);

            System.out.println(strategy0);
        }
        else {

        System.out.println("Running CFR ... ");
        algIS.runMiliseconds(RUN_MS);


        strategy0 = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[0], dist, algIS.getInformationSets(), expander);
        strategy1 = StrategyCollector.getStrategyFor(rootState, rootState.getAllPlayers()[1], dist, algIS.getInformationSets(), expander);
        }

        algIS = null;
        alg = null;

//        if (FlipItGameInfo.NO_INFO) rootState = new NoInfoFlipItGameState();
//        else rootState = new NodePointsFlipItGameState();
        expander = new FlipItExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        Map<Player, Map<Sequence, Double>> rp = new HashMap<>();
        rp.put(FlipItGameInfo.DEFENDER, strategy0);
        rp.put(FlipItGameInfo.ATTACKER, strategy1);
        Map<Player, Map<Sequence, Double>> rps = doefg.generate(rp);

        if (OUTPUT) {
            output += doefg.getGameValue() + " ";
            output += doefg.getIterations() + " ";
            output += doefg.getFinishTime() + " ";
            output += (algConfig.getSequencesFor(FlipItGameInfo.DEFENDER).size() + " ");
            output += (algConfig.getSequencesFor(FlipItGameInfo.ATTACKER).size() + " ");
        }


        analyzeStrategicDifference(strategy0, strategy1, rps);

        if (OUTPUT){
            try {
                Files.write(Paths.get(outputFile), (output+"\n").getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Appending failed");
            }
        }


    }

    private static void analyzeStrategicDifference(Strategy strategy0, Strategy strategy1, Map<Player, Map<Sequence, Double>> rps) {
        int defNotContaining = 0;
        double defDiff = 0.0;
        for (Sequence sequence : strategy0.keySet()) {
            if (!rps.get(FlipItGameInfo.DEFENDER).keySet().contains(sequence)) {
                defNotContaining++;
            } else {
                if (rps.get(FlipItGameInfo.DEFENDER).get(sequence) < EPS && strategy0.get(sequence) > EPS) defNotContaining ++;
                defDiff += Math.abs(strategy0.get(sequence) - rps.get(FlipItGameInfo.DEFENDER).get(sequence));
            }
        }
        defDiff = defDiff / (strategy0.keySet().size());// - defNotContaining);

        int attNotContaining = 0;
        double attDiff = 0.0;
        for (Sequence sequence : strategy1.keySet()) {
            if (!rps.get(FlipItGameInfo.ATTACKER).keySet().contains(sequence)) attNotContaining++;
            else {
                if (rps.get(FlipItGameInfo.ATTACKER).get(sequence) < EPS && strategy1.get(sequence) > EPS) attNotContaining ++;
                attDiff += Math.abs(strategy1.get(sequence) - rps.get(FlipItGameInfo.ATTACKER).get(sequence));
            }
        }
        attDiff = attDiff / (strategy1.keySet().size());// - attNotContaining);
        System.out.println();
        System.out.println("Unused sequences of DEF = " + defNotContaining + " / " + strategy0.size() + "; unused sequences of ATT = " + attNotContaining+ " / " + strategy1.size());
        System.out.println("Mean diff DEF = " + defDiff + "; mean diff ATT = " + attDiff);

        if (OUTPUT){
            output += defNotContaining + " " + strategy0.size() + " ";
            output += attNotContaining+ " " + strategy1.size() + " ";
            output += defDiff + " " + attDiff;
        }
    }
}
