package cz.agents.gtlibrary.nfg.experimental.MDP.McMahan;

import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolver;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPExpander;

import java.io.PrintStream;
import java.lang.management.ThreadMXBean;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/9/13
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class McMahanDoubleOracle {

    private double lowerBound = Double.NEGATIVE_INFINITY;
    private double upperBound = Double.POSITIVE_INFINITY;

    private MDPExpander expander;
    private MDPConfig config;

    private MixedStrategy<McMahanMDPStrategy> maxPlayerMixedStrategy = new MixedStrategy<McMahanMDPStrategy>();
    private MixedStrategy<McMahanMDPStrategy> minPlayerMixedStrategy = new MixedStrategy<McMahanMDPStrategy>();

    private PlayerStrategySet<PureStrategy> maxPlayerStrategySet = new PlayerStrategySet<PureStrategy>();
    private PlayerStrategySet<PureStrategy> minPlayerStrategySet = new PlayerStrategySet<PureStrategy>();

    private ZeroSumGameNESolver coreSolver;
    private double resultValue;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;

    private double gameValue = Double.NaN;

    public static void main(String[] args) {
        runBPG();
    }

    public McMahanDoubleOracle(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    private static void runBPG() {
        MDPExpander expander = new BPExpander();
        MDPConfig config = new BPConfig();
        McMahanDoubleOracle mdp = new McMahanDoubleOracle(expander, config);
        mdp.test();
    }

    private void test() {
        MDPUtilityComputer utilityComputer = new MDPUtilityComputer(config);

        long startTime = System.nanoTime();
        debugOutput.println("Testing McMahan DO CostPaired MDP.");

        McMahanMDPStrategy p1Strategy = new McMahanMDPStrategy(config.getAllPlayers().get(0), config, expander);
        McMahanMDPStrategy p2Strategy = new McMahanMDPStrategy(config.getAllPlayers().get(1), config, expander);

        p1Strategy.generateCompleteStrategy();
        p2Strategy.generateCompleteStrategy();

//        debugOutput.println("MAX strategy" + p1Strategy.strategy);
//        debugOutput.println("MIN strategy" + p2Strategy.strategy);
//        debugOutput.println("Utility: " + utilityComputer.getUtility(p1Strategy, p2Strategy));



        coreSolver = new ZeroSumGameNESolverImpl(utilityComputer);
        maxPlayerStrategySet.add(p1Strategy);
        coreSolver.addPlayerOneStrategies(maxPlayerStrategySet);

        minPlayerStrategySet.add(p2Strategy);
        coreSolver.addPlayerTwoStrategies(minPlayerStrategySet);

        int iterations = 0;

        MDPBestResponse[] brAlgorithms = new MDPBestResponse[] {
                new MDPBestResponse(this.config, this.config.getAllPlayers().get(0)),
                new MDPBestResponse(this.config, this.config.getAllPlayers().get(1)),
        };



        while (Math.abs(upperBound - lowerBound) > MDPConfigImpl.getEpsilon()) {

            iterations++;

//            Pair<PureStrategy, Double> maxPlayerOracleResult;
//            Pair<PureStrategy, Double> minPlayerOracleResult;
            if (iterations > 1) {
                McMahanMDPStrategy p1CombinedStrategy = new McMahanMDPStrategy(config.getAllPlayers().get(0), config, expander, maxPlayerMixedStrategy);
                McMahanMDPStrategy p2CombinedStrategy = new McMahanMDPStrategy(config.getAllPlayers().get(1), config, expander, minPlayerMixedStrategy);

                p1CombinedStrategy.sanityCheck();
                p2CombinedStrategy.sanityCheck();

//                debugOutput.println("MAX: " + p1CombinedStrategy.strategy);
//                debugOutput.println("MIN: " + p2CombinedStrategy.strategy);

                double BRMax = brAlgorithms[0].calculateBR(p1CombinedStrategy, p2CombinedStrategy);
                double BRMin = brAlgorithms[1].calculateBR(p2CombinedStrategy, p1CombinedStrategy);

                debugOutput.println("Current BRMax : " + BRMax);
                debugOutput.println("Current BRMin : " + BRMin);

                upperBound = Math.min(upperBound, BRMax);
                McMahanMDPStrategy brs1 = new McMahanMDPStrategy(config.getAllPlayers().get(0), config, expander, brAlgorithms[0].extractBestResponse(p1CombinedStrategy));
//                debugOutput.println(brs1);
//                brs1.sanityCheck();
                if (maxPlayerStrategySet.add(brs1)) {
                    coreSolver.addPlayerOneStrategies(maxPlayerStrategySet);
                }

                lowerBound = Math.max(lowerBound, BRMin);
                McMahanMDPStrategy brs2 = new McMahanMDPStrategy(config.getAllPlayers().get(1), config, expander, brAlgorithms[1].extractBestResponse(p2CombinedStrategy));
//                debugOutput.println(brs2);
//                brs2.sanityCheck();
                if (minPlayerStrategySet.add(brs2)) {
                    coreSolver.addPlayerTwoStrategies(minPlayerStrategySet);
                }

                debugOutput.println("Current Max #PS : " + maxPlayerStrategySet.size());
                debugOutput.println("Current Min #PS : " + minPlayerStrategySet.size());
            }

            coreSolver.computeNashEquilibrium();
            maxPlayerMixedStrategy = coreSolver.getPlayerOneStrategy();
            minPlayerMixedStrategy = coreSolver.getPlayerTwoStrategy();

            resultValue = coreSolver.getGameValue();
            debugOutput.println("RGValue: " + resultValue);
            debugOutput.println("Iteration: " + iterations + " Bounds size: " + Math.abs(upperBound - lowerBound));
        }

        debugOutput.println("Finished.");
        debugOutput.println("final size: FirstPlayer Pure Strategies: " + maxPlayerStrategySet.size() + " \t SecondPlayer Pure Strategies: " + minPlayerStrategySet.size());
        debugOutput.println("final result:" + resultValue);

    }

}