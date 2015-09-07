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
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGExpander;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPState;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGExpander;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class FullCostPairedMDP {

    private MDPExpander expander;
    private MDPConfig config;
    private MDPStrategy firstPlayerStrategy;
    private MDPStrategy secondPlayerStrategy;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;
    private MemoryMXBean memoryBean ;

    private double gameValue = Double.NaN;

    public static void main(String[] args) {
//		runRG();
//      runBPG();
//        runTG();
        runTIG();
    }

    public FullCostPairedMDP(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    public static void runBPG() {
        MDPExpander expander = new BPExpander();
        MDPConfig config = new BPConfig();
        FullCostPairedMDP mdp = new FullCostPairedMDP(expander, config);
        mdp.test();
    }

    public static void runRG() {
        MDPExpander expander = new RGMDPExpander();
        MDPConfig config = new RGMDPConfig();
        FullCostPairedMDP mdp = new FullCostPairedMDP(expander, config);
        mdp.test();
    }

    public static void runTG() {
        MDPExpander expander = new TGExpander();
        MDPConfig config = new TGConfig();
        FullCostPairedMDP mdp = new FullCostPairedMDP(expander, config);
        mdp.test();
    }

    public static void runTIG() {
        FullCostPairedMDP mdp = new FullCostPairedMDP(new TIGExpander(), new TIGConfig());
        mdp.test();
    }
        
     public static void testGame(MDPExpander expander, MDPConfig config) {
        FullCostPairedMDP mdp = new FullCostPairedMDP(expander, config);
        mdp.test();
    }


    private void test() {
        threadBean = ManagementFactory.getThreadMXBean();
        long startTime = threadBean.getCurrentThreadCpuTime();
        threadBean = ManagementFactory.getThreadMXBean();
        memoryBean = ManagementFactory.getMemoryMXBean();
        debugOutput.println("Testing Full CostPaired MDP.");
        firstPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(0),config,expander);
        secondPlayerStrategy = new MDPStrategy(config.getAllPlayers().get(1),config,expander);
        long p1StrategyGeneration = threadBean.getCurrentThreadCpuTime();
        firstPlayerStrategy.generateCompleteStrategy();
        p1StrategyGeneration = (threadBean.getCurrentThreadCpuTime() - p1StrategyGeneration)/1000000;
        debugOutput.println("P1 Strategy generation : " + p1StrategyGeneration);

//        for (MDPStateActionMarginal m1 : firstPlayerStrategy.getAllMarginalsInStrategy()) {
//            debugOutput.println(m1 + " sucessors:" + firstPlayerStrategy.getSuccessors(m1));
//            if (!m1.getState().isRoot()) debugOutput.println("Predecessors:" + firstPlayerStrategy.getPredecessors(m1.getState()));
//
//        }
        long p2StrategyGeneration = threadBean.getCurrentThreadCpuTime();
        secondPlayerStrategy.generateCompleteStrategy();
        p2StrategyGeneration = (threadBean.getCurrentThreadCpuTime() - p2StrategyGeneration)/1000000;
        debugOutput.println("P2 Strategy generation : " + p2StrategyGeneration);

//        debugOutput.println("final size: FirstPlayer Marginal Strategies: " + firstPlayerStrategy.getAllMarginalsInStrategy().size() + " \t SecondPlayer Marginal Strategies: " + secondPlayerStrategy.getAllMarginalsInStrategy().size());
//
//        if (2 + 2 < 5) return;

//        for (MDPStateActionMarginal m2 : secondPlayerStrategy.getAllMarginalsInStrategy()) {
//            debugOutput.println(m2 + " sucessors:" + secondPlayerStrategy.getSuccessors(m2));
//            if (!m2.getState().isRoot()) debugOutput.println("Predecessors:" + secondPlayerStrategy.getPredecessors(m2.getState()));
//        }

/*        for (MDPStateActionMarginal m1 : firstPlayerStrategy.getAllMarginalsInStrategy()) {
            for (MDPStateActionMarginal m2 : secondPlayerStrategy.getAllMarginalsInStrategy()) {
                double utility = config.getUtility(m1, m2);
                if (utility != 0)
                   debugOutput.println("["+m1+","+m2+"] = " + utility);
                assert (config.getUtility(m1, m2) == config.getUtility(m2, m1));
            }
        } //*/

//        for (MDPState s : firstPlayerStrategy.getStates()) {
//            debugOutput.println(s + ": " + s.horizon());
//        }
//
//        for (MDPState s : secondPlayerStrategy.getStates()) {
//            debugOutput.println(s + ": " + s.horizon());
//        }
//
//        if (1 == 1) return;

//        debugOutput.println(secondPlayerStrategy.getSuccessors(new MDPStateActionMarginal(secondPlayerStrategy.getRootState(), secondPlayerStrategy.getActions(secondPlayerStrategy.getRootState()).get(0))));

        long utilityStrategyGeneration = threadBean.getCurrentThreadCpuTime();
//        firstPlayerStrategy.storeAllUtilityToCache(firstPlayerStrategy.getAllActionStates(), secondPlayerStrategy.getAllActionStates());
        utilityStrategyGeneration = (threadBean.getCurrentThreadCpuTime() - utilityStrategyGeneration)/1000000;
        debugOutput.println("Utility generation : " + utilityStrategyGeneration);

        Map<Player, MDPStrategy> playerStrategy = new HashMap<Player, MDPStrategy>();
        playerStrategy.put(config.getAllPlayers().get(0), firstPlayerStrategy);
        playerStrategy.put(config.getAllPlayers().get(1), secondPlayerStrategy);

        debugOutput.println("Starting LP Construction.");
        MDPCoreLP lp = new MDPCoreLP(config.getAllPlayers(), playerStrategy, config);
        double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
        debugOutput.println("Result: " + r1);

        long halfTime = threadBean.getCurrentThreadCpuTime() - startTime;
        debugOutput.println("Half Time: " + (halfTime / 1000000));

        double r2 = lp.solveForPlayer(config.getAllPlayers().get(1));
        debugOutput.println("Result: " + r2);

        lp.extractStrategyForPlayer(config.getAllPlayers().get(0));
//        for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
//            debugOutput.println(m1 + " = " + firstPlayerStrategy.getStrategy().get(m1));
//        }

        lp.extractStrategyForPlayer(config.getAllPlayers().get(1));
//        for (MDPStateActionMarginal m2 : secondPlayerStrategy.getStrategy().keySet()) {
//            debugOutput.println(m2 + " = " + secondPlayerStrategy.getStrategy().get(m2));
//        }

        firstPlayerStrategy.sanityCheck();
        secondPlayerStrategy.sanityCheck();
//
//        MDPBestResponse br1 = new MDPBestResponse(config, config.getAllPlayers().get(0));
//        debugOutput.println("BR : " + br1.calculateBR(firstPlayerStrategy,  secondPlayerStrategy));
//        debugOutput.println(br1.extractBestResponse(firstPlayerStrategy));

//        MDPBestResponse br2 = new MDPBestResponse(config, config.getAllPlayers().get(1));
//        debugOutput.println("BR : " + br2.calculateBR(secondPlayerStrategy, firstPlayerStrategy));
//        debugOutput.println(br2.extractBestResponse(secondPlayerStrategy));

//*/
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


        debugOutput.println("Overall Time: " + (endTime / 1000000));
        debugOutput.println("Building LP Time: " + (lp.getBUILDING_LP_TIME()/ 1000000));
        debugOutput.println("Solving LP Time: " + (lp.getSOLVING_LP_TIME()/ 1000000));
        debugOutput.println("final size: FirstPlayer Marginal Strategies: " + firstPlayerStrategy.getAllMarginalsInStrategy().size() + " \t SecondPlayer Marginal Strategies: " + secondPlayerStrategy.getAllMarginalsInStrategy().size());
        debugOutput.println("final size: FirstPlayer Support: " + p1SupportSize + " \t SecondPlayer Support: " + p2SupportSize);
        debugOutput.println("final result:" + r1);

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }
        System.out.println("final memory:" + ((memoryBean.getHeapMemoryUsage().getCommitted() + memoryBean.getNonHeapMemoryUsage().getCommitted()) / 1024 / 1024));
        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
    }
}
