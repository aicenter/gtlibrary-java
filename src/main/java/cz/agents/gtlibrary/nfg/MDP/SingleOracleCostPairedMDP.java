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


package cz.agents.gtlibrary.nfg.MDP;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.MDP.core.MDPCoreLP;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.implementations.oracle.MDPEpsilonFristBetterResponse;
import cz.agents.gtlibrary.nfg.MDP.implementations.oracle.MDPFristBetterResponse;
import cz.agents.gtlibrary.nfg.MDP.implementations.oracle.MDPIterativeStrategy;
import cz.agents.gtlibrary.nfg.MDP.implementations.oracle.MDPOracleLP;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGExpander;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleOracleCostPairedMDP {

    public static boolean USE_ROBUST_BR = false;
    public static boolean USE_NORMAL_BR = false;
    public static double END_EPSILON = MDPConfigImpl.getEpsilon();

    private MDPExpander expander;
    private MDPConfig config;
    private MDPStrategy firstPlayerStrategy;
    private MDPIterativeStrategy secondPlayerStrategy;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;

    private double gameValue = Double.NaN;

    private long BRTIME = 0;
    private long CPLEXTIME = 0;
    private long RGCONSTR = 0;

    public static void main(String[] args) {
//		runBPG();
//        runRG();
        runTG();
    }

    public SingleOracleCostPairedMDP(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    public static void runBPG() {
        MDPExpander expander = new BPExpander();
        MDPConfig config = new BPConfig();
        SingleOracleCostPairedMDP mdp = new SingleOracleCostPairedMDP(expander, config);
        mdp.test();
    }

    public static void runRG() {
        MDPExpander expander = new RGMDPExpander();
        MDPConfig config = new RGMDPConfig();
        SingleOracleCostPairedMDP mdp = new SingleOracleCostPairedMDP(expander, config);
        mdp.test();
    }

    public static void runTG() {
        MDPExpander expander = new TGExpander();
        MDPConfig config = new TGConfig();
        SingleOracleCostPairedMDP mdp = new SingleOracleCostPairedMDP(expander, config);
        mdp.test();
    }

    private void test() {
        threadBean = ManagementFactory.getThreadMXBean();
        long startTime = threadBean.getCurrentThreadCpuTime();
        debugOutput.println("Testing SO CostPaired MDP.");
        firstPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(0),config,expander);
        secondPlayerStrategy = new MDPIterativeStrategy(config.getAllPlayers().get(1),config,expander);
        firstPlayerStrategy.generateCompleteStrategy();
        secondPlayerStrategy.initIterativeStrategy(firstPlayerStrategy);

//        debugOutput.println(secondPlayerStrategy.getDefaultUtilityCache().get(secondPlayerStrategy.getRootState()));
//        debugOutput.println(secondPlayerStrategy.getDefaultUtilityCache().get(secondPlayerStrategy.getRootState()));

        Map<Player, MDPStrategy> playerStrategy = new HashMap<Player, MDPStrategy>();
        playerStrategy.put(config.getAllPlayers().get(0), firstPlayerStrategy);
        playerStrategy.put(config.getAllPlayers().get(1), secondPlayerStrategy);

        MDPOracleLP lp = new MDPOracleLP(config.getAllPlayers(), playerStrategy, config);

        double LB = Double.NEGATIVE_INFINITY;
        double UB = Double.POSITIVE_INFINITY;

        Set<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();

        int iterations = 0;

        MDPBestResponse br2;
        if (USE_NORMAL_BR) {
            br2 = new MDPBestResponse(config, config.getAllPlayers().get(1));
        } else {
            if (USE_ROBUST_BR) {
                br2 = new MDPEpsilonFristBetterResponse(config, config.getAllPlayers().get(1));
            } else {
                br2 = new MDPFristBetterResponse(config, config.getAllPlayers().get(1));
            }
        }

        double treshold = 0.01;

        while ( Math.abs(UB - LB) > MDPConfigImpl.getEpsilon() && UB > LB) {
//        for (int i=0; i<5; i++) {

            debugOutput.println("*********** Iteration = " + (++iterations) + " Bound Interval = " + Math.abs(UB - LB) + "     *************");

            long LpStart = threadBean.getCurrentThreadCpuTime();
            double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
            debugOutput.println("Result: " + r1);
            CPLEXTIME += threadBean.getCurrentThreadCpuTime() - LpStart;


            UB = Math.min(UB, r1);

            lp.extractStrategyForPlayer(config.getAllPlayers().get(0));
//            for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
//                debugOutput.println(m1 + " = " + firstPlayerStrategy.getStrategy().get(m1));
//            }
            firstPlayerStrategy.recalculateExpandedStrategy();

            long brStart = threadBean.getCurrentThreadCpuTime();
            double currentBRVal = br2.calculateBR(secondPlayerStrategy, firstPlayerStrategy);
            LB = Math.max(LB, currentBRVal);
            debugOutput.println("BR : " + currentBRVal);
            BRTIME += threadBean.getCurrentThreadCpuTime() - brStart;
            debugOutput.println("BR(MIN) TIME:" + ((threadBean.getCurrentThreadCpuTime() - brStart)/1000000l));

            if (USE_ROBUST_BR) {
//                debugOutput.println("BR(MIN) Improved Times: " + ((MDPEpsilonFristBetterResponse)br2).getImprovedBR());
            }
            debugOutput.println("BR(MIN) Pruned Times: " + ((MDPFristBetterResponse)br2).getPrunes());

            long RGStart = threadBean.getCurrentThreadCpuTime();
            Map<MDPState, Set<MDPStateActionMarginal>> br = br2.extractBestResponse(secondPlayerStrategy);

//            debugOutput.println("BR = " + br);
            newActions = secondPlayerStrategy.addBRStrategy(secondPlayerStrategy.getRootState(), br);
//            debugOutput.println("New Actions = " + newActions);
            lp.setNewActions(newActions);
//            debugOutput.println(secondPlayerStrategy.getStrategy());
            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
            if (newActions.isEmpty()) {
                treshold = treshold/10;
            }
        }

        long endTime = threadBean.getCurrentThreadCpuTime() - startTime;
        int p1SupportSize = 0;
        int p2SupportSize = 0;
        for (MDPStateActionMarginal m1 : firstPlayerStrategy.getAllMarginalsInStrategy()) {
            if (firstPlayerStrategy.getStrategyProbability(m1) > MDPConfigImpl.getEpsilon())
                p1SupportSize++;
        }
        for (MDPStateActionMarginal m2 : secondPlayerStrategy.getAllMarginalsInStrategy()) {
            if (secondPlayerStrategy.getStrategyProbability(m2) > MDPConfigImpl.getEpsilon())
                p2SupportSize++;
        }

        debugOutput.println("Overall Time: " + (endTime / 1000000l));
        debugOutput.println("BR Time: " + (BRTIME / 1000000l));
        debugOutput.println("CPLEX Time: " + (CPLEXTIME / 1000000l));
        debugOutput.println("RGConstr Time: " + (RGCONSTR / 1000000l));
        debugOutput.println("Building LP Time: " + (lp.getBUILDING_LP_TIME()/ 1000000l));
        debugOutput.println("Solving LP Time: " + (lp.getSOLVING_LP_TIME()/ 1000000l));
        debugOutput.println("final size: FirstPlayer Marginal Strategies: " + firstPlayerStrategy.getAllMarginalsInStrategy().size() + " \t SecondPlayer Marginal Strategies: " + secondPlayerStrategy.getAllMarginalsInStrategy().size());
        debugOutput.println("final size: FirstPlayer Support: " + p1SupportSize + " \t SecondPlayer Support: " + p2SupportSize);
        debugOutput.println("final result:" + UB);

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
    }
}
