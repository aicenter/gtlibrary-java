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


package cz.agents.gtlibrary.nfg.MDP.implementations.pureStrategyOracle;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MixedStrategy;
import cz.agents.gtlibrary.nfg.PlayerStrategySet;
import cz.agents.gtlibrary.nfg.PureStrategy;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolver;
import cz.agents.gtlibrary.nfg.core.ZeroSumGameNESolverImpl;
import cz.agents.gtlibrary.nfg.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGExpander;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 9/9/13
 * Time: 9:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class McMahanDoubleOracle {

    public static boolean REMOVE_STRATEGIES = false;

    private double lowerBound = Double.NEGATIVE_INFINITY;
    private double upperBound = Double.POSITIVE_INFINITY;

    private int strategyCountThreshold = 60;

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

    private long BRTIME = 0;
    private long CPLEXTIME = 0;
    private long RGCONSTR = 0;

    private Map<McMahanMDPStrategy, Double> maxStrategyWeights = new HashMap<McMahanMDPStrategy, Double>();
    private Map<McMahanMDPStrategy, Double> minStrategyWeights = new HashMap<McMahanMDPStrategy, Double>();

    private double gameValue = Double.NaN;

    public static void main(String[] args) {
//        runBPG();
        runTG();
//        runRG();
    }

    public McMahanDoubleOracle(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    public static void runBPG() {
        MDPExpander expander = new BPExpander();
        MDPConfig config = new BPConfig();
        McMahanDoubleOracle mdp = new McMahanDoubleOracle(expander, config);
        mdp.test();
    }

    public static void runTG() {
        MDPExpander expander = new TGExpander();
        MDPConfig config = new TGConfig();
        McMahanDoubleOracle mdp = new McMahanDoubleOracle(expander, config);
        mdp.test();
    }

    public static void runRG() {
        MDPExpander expander = new RGMDPExpander();
        MDPConfig config = new RGMDPConfig();
        McMahanDoubleOracle mdp = new McMahanDoubleOracle(expander, config);
        mdp.test();
    }

    private void test() {
        MDPUtilityComputer utilityComputer = new MDPUtilityComputer(config);
        threadBean = ManagementFactory.getThreadMXBean();

        long startTime = threadBean.getCurrentThreadCpuTime();
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

        McMahanMDPStrategy maxCenterStrategy = p1Strategy;
        McMahanMDPStrategy minCenterStrategy = p2Strategy;

        int oldSize1 = -1;
        int oldSize2 = -1;

        while (Math.abs(upperBound - lowerBound) > 1e-3) {
//        for (int iii=0; iii<3; iii++) {
            iterations++;

//            Pair<PureStrategy, Double> maxPlayerOracleResult;
//            Pair<PureStrategy, Double> minPlayerOracleResult;
            if (iterations > 1) {
                oldSize1 = maxPlayerStrategySet.size();
                oldSize2 = minPlayerStrategySet.size();

                McMahanMDPStrategy p1CombinedStrategy = new McMahanMDPStrategy(config.getAllPlayers().get(0), config, expander, maxPlayerMixedStrategy);
                McMahanMDPStrategy p2CombinedStrategy = new McMahanMDPStrategy(config.getAllPlayers().get(1), config, expander, minPlayerMixedStrategy);

                if (REMOVE_STRATEGIES) {
                    Iterator<Map.Entry<McMahanMDPStrategy, Double>> i = maxPlayerMixedStrategy.iterator();
                    while (i.hasNext()) {
                        Map.Entry<McMahanMDPStrategy, Double> item = i.next();
                        Double weight = maxStrategyWeights.get(item.getKey());
                        if (weight == null) weight = 0d;
                        weight += item.getValue();
                        maxStrategyWeights.put(item.getKey(), weight);
                    }

                    i = minPlayerMixedStrategy.iterator();
                    while (i.hasNext()) {
                        Map.Entry<McMahanMDPStrategy, Double> item = i.next();
                        Double weight = minStrategyWeights.get(item.getKey());
                        if (weight == null) weight = 0d;
                        weight += item.getValue();
                        minStrategyWeights.put(item.getKey(), weight);
                    }
                }
//                p1CombinedStrategy.sanityCheck();
//                p2CombinedStrategy.sanityCheck();

//                debugOutput.println("MAX: " + p1CombinedStrategy.strategy);
//                debugOutput.println("MIN: " + p2CombinedStrategy.strategy);

                long brStart = threadBean.getCurrentThreadCpuTime();
                double BRMax = brAlgorithms[0].calculateBR(p1CombinedStrategy, p2CombinedStrategy);
                double BRMin = brAlgorithms[1].calculateBR(p2CombinedStrategy, p1CombinedStrategy);
                McMahanMDPStrategy brs1 = new McMahanMDPStrategy(config.getAllPlayers().get(0), config, expander, brAlgorithms[0].extractBestResponse(p1CombinedStrategy));
                McMahanMDPStrategy brs2 = new McMahanMDPStrategy(config.getAllPlayers().get(1), config, expander, brAlgorithms[1].extractBestResponse(p2CombinedStrategy));

                double BRMax2 = Double.POSITIVE_INFINITY;
                double BRMin2 = Double.NEGATIVE_INFINITY;
                McMahanMDPStrategy brCenter1 = null;
                McMahanMDPStrategy brCenter2 = null;
                if (REMOVE_STRATEGIES) {
                    BRMax2 = brAlgorithms[0].calculateBR(maxCenterStrategy, minCenterStrategy);
                    BRMin2 =brAlgorithms[1].calculateBR(minCenterStrategy, maxCenterStrategy);
                    brCenter1 = new McMahanMDPStrategy(config.getAllPlayers().get(0), config, expander, brAlgorithms[0].extractBestResponse(p1CombinedStrategy));
                    brCenter2 = new McMahanMDPStrategy(config.getAllPlayers().get(1), config, expander, brAlgorithms[1].extractBestResponse(p2CombinedStrategy));
                }
                BRTIME += threadBean.getCurrentThreadCpuTime() - brStart;
                debugOutput.println("This BR TIME:" + (threadBean.getCurrentThreadCpuTime() - brStart)/ 1000000l);

                debugOutput.println("Current BRMax : " + BRMax);
                debugOutput.println("Current BRMin : " + BRMin);

                upperBound = Math.min(upperBound, Math.min(BRMax, BRMax2));

                long RGStart = threadBean.getCurrentThreadCpuTime();
//                debugOutput.println(brs1);
//                brs1.sanityCheck();
                boolean changed1 = false;
                changed1 |= maxPlayerStrategySet.add(brs1);

                if (REMOVE_STRATEGIES) {
                    Map<McMahanMDPStrategy, Double> maxCenterWeights = new HashMap<McMahanMDPStrategy, Double>();
                    maxCenterWeights.put(maxCenterStrategy, (double)iterations);
                    if (maxCenterWeights.put(brs1, 1d) == null) {
                        maxCenterStrategy = new McMahanMDPStrategy(config.getAllPlayers().get(0), config, expander, maxCenterWeights, iterations+1);
                        changed1 |= maxPlayerStrategySet.add(maxCenterStrategy);
                    }

                    changed1 |= maxPlayerStrategySet.add(p1CombinedStrategy);
                    changed1 |= maxPlayerStrategySet.add(brCenter1);
                }

                if (changed1) {
                    coreSolver.addPlayerOneStrategies(maxPlayerStrategySet);
                }

                lowerBound = Math.max(lowerBound, Math.max(BRMin, BRMin2));
                boolean changed2 = false;
//                debugOutput.println(brs2);
//                brs2.sanityCheck();
                changed2 |= minPlayerStrategySet.add(brs2);
                if (REMOVE_STRATEGIES) {
                    Map<McMahanMDPStrategy, Double> minCenterWeights = new HashMap<McMahanMDPStrategy, Double>();
                    minCenterWeights.put(minCenterStrategy, (double)iterations);
                    if (minCenterWeights.put(brs2, 1d) == null) {
                        minCenterStrategy = new McMahanMDPStrategy(config.getAllPlayers().get(1), config, expander, minCenterWeights, iterations+1);
                        changed2 |= minPlayerStrategySet.add(minCenterStrategy);
                    }

                    changed2 |= minPlayerStrategySet.add(p2CombinedStrategy);
                    changed2 |= minPlayerStrategySet.add(brCenter2);
                }
                if (changed2) {
                    coreSolver.addPlayerTwoStrategies(minPlayerStrategySet);
                }
                RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
//                if (minPlayerStrategySet.size() > strategyCountThreshold || maxPlayerStrategySet.size() > strategyCountThreshold) {
                if (REMOVE_STRATEGIES && iterations % 5 == 0) {
                    coreSolver.clearModel();

//                    if (maxPlayerStrategySet.size() > strategyCountThreshold) {
                        maxPlayerStrategySet.clear();
                        maxPlayerStrategySet.addAll(removeStrategies(config.getAllPlayers().get(0), maxStrategyWeights));
//                    }
//                    if (minPlayerStrategySet.size() > strategyCountThreshold) {
                        minPlayerStrategySet.clear();
                        minPlayerStrategySet.addAll(removeStrategies(config.getAllPlayers().get(1), minStrategyWeights));
//                    }
                    coreSolver.addPlayerOneStrategies(maxPlayerStrategySet);
                    coreSolver.addPlayerTwoStrategies(minPlayerStrategySet);
                }
//                RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
                debugOutput.println("This RG TIME:" + (threadBean.getCurrentThreadCpuTime() - RGStart)/1000000l);

                debugOutput.println("Current Max #PS : " + maxPlayerStrategySet.size());
                debugOutput.println("Current Min #PS : " + minPlayerStrategySet.size());

                if (!REMOVE_STRATEGIES && oldSize1 == maxPlayerStrategySet.size() && oldSize2 == minPlayerStrategySet.size())
                    break;
            }
            long LpStart = threadBean.getCurrentThreadCpuTime();
            coreSolver.computeNashEquilibrium();
            CPLEXTIME += threadBean.getCurrentThreadCpuTime() - LpStart;
            debugOutput.println("This CPLEX TIME:" + (threadBean.getCurrentThreadCpuTime() - LpStart)/ 1000000l);
            maxPlayerMixedStrategy = coreSolver.getPlayerOneStrategy();
            minPlayerMixedStrategy = coreSolver.getPlayerTwoStrategy();

            resultValue = coreSolver.getGameValue();
            debugOutput.println("RGValue: " + resultValue);
            debugOutput.println("*********** Iteration = " + (iterations) + " Bound Interval = " + Math.abs(upperBound - lowerBound) + " [ " + lowerBound + ";" + upperBound +  " ]      *************");
//            debugOutput.println("************** Iteration = " + iterations + " Bounds size = " + Math.abs(upperBound - lowerBound) + " ***************");
        }

        debugOutput.println("Finished.");
        long endTime = threadBean.getCurrentThreadCpuTime() - startTime;
        debugOutput.println("Overall Time: " + (endTime / 1000000l));
        debugOutput.println("BR Time: " + (BRTIME / 1000000l));
        debugOutput.println("CPLEX Time: " + (CPLEXTIME / 1000000l));
        debugOutput.println("RGConstr Time: " + (RGCONSTR / 1000000l));
        debugOutput.println("final size: FirstPlayer Pure Strategies: " + maxPlayerStrategySet.size() + " \t SecondPlayer Pure Strategies: " + minPlayerStrategySet.size());
        debugOutput.println("final size: FirstPlayer Support: " + maxPlayerMixedStrategy.getSupportSize() + " \t SecondPlayer Support: " + minPlayerMixedStrategy.getSupportSize());
        debugOutput.println("final result:" + resultValue);

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
    }

    private List<PureStrategy> removeStrategies(Player player, final Map<McMahanMDPStrategy, Double> strategyWeights) {
        List<PureStrategy> strategies = new LinkedList<PureStrategy>(strategyWeights.keySet());
        Collections.sort(strategies, new Comparator<PureStrategy>() {
            @Override
            public int compare(PureStrategy strategy1, PureStrategy strategy2) {
                double w1 = strategyWeights.get(strategy1);
                double w2 = strategyWeights.get(strategy2);
                if (w1 < w2) {
                    return -1;
                } else if (w1 > w2) {
                    return 1;
                } else
                    return 0;
            }
        });
        Map<McMahanMDPStrategy, Double> removedStrategies = new HashMap<McMahanMDPStrategy, Double>();
        double sumWeight = 0;
        for (int i=Math.max(5, strategyWeights.size()-strategyCountThreshold); i>=0; i--) {
            PureStrategy s = strategies.remove(i);
            double w = strategyWeights.get(s);
            if (removedStrategies.containsKey(s)) {
                w += removedStrategies.get(s);
            }
            sumWeight += w;
            removedStrategies.put((McMahanMDPStrategy)s, w);
        }
        strategyWeights.clear();
        if (sumWeight > 0) {
            McMahanMDPStrategy newAggregation = new McMahanMDPStrategy(player, config, expander, removedStrategies, sumWeight);
//            newAggregation.sanityCheck();
            strategies.add(newAggregation);
        }
        return strategies;
    }

}