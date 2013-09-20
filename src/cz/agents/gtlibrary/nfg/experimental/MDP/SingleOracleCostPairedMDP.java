package cz.agents.gtlibrary.nfg.experimental.MDP;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPBestResponse;
import cz.agents.gtlibrary.nfg.experimental.MDP.core.MDPCoreLP;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle.MDPIterativeStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle.MDPOracleLP;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.experimental.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.experimental.domain.bpg.BPExpander;
import cz.agents.gtlibrary.nfg.experimental.domain.randomgame.RGMDPConfig;
import cz.agents.gtlibrary.nfg.experimental.domain.randomgame.RGMDPExpander;
import cz.agents.gtlibrary.nfg.experimental.domain.transitgame.TGConfig;
import cz.agents.gtlibrary.nfg.experimental.domain.transitgame.TGExpander;

import java.io.PrintStream;
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

    private MDPExpander expander;
    private MDPConfig config;
    private MDPStrategy firstPlayerStrategy;
    private MDPIterativeStrategy secondPlayerStrategy;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;

    private double gameValue = Double.NaN;

    public static void main(String[] args) {
		runBPG();
//        runRG();
//        runTG();
    }

    public SingleOracleCostPairedMDP(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    private static void runBPG() {
        MDPExpander expander = new BPExpander();
        MDPConfig config = new BPConfig();
        SingleOracleCostPairedMDP mdp = new SingleOracleCostPairedMDP(expander, config);
        mdp.test();
    }

    private static void runRG() {
        MDPExpander expander = new RGMDPExpander();
        MDPConfig config = new RGMDPConfig();
        SingleOracleCostPairedMDP mdp = new SingleOracleCostPairedMDP(expander, config);
        mdp.test();
    }

    private static void runTG() {
        MDPExpander expander = new TGExpander();
        MDPConfig config = new TGConfig();
        SingleOracleCostPairedMDP mdp = new SingleOracleCostPairedMDP(expander, config);
        mdp.test();
    }

    private void test() {
        long startTime = System.nanoTime();
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


        while ( Math.abs(UB - LB) > MDPConfigImpl.getEpsilon() && UB > LB) {
//        for (int i=0; i<5; i++) {

            debugOutput.println("*********** Iteration = " + (++iterations) + " Bound Interval = " + Math.abs(UB - LB) + "     *************");

            double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
            debugOutput.println("Result: " + r1);

            UB = Math.min(UB, r1);

            lp.extractStrategyForPlayer(config.getAllPlayers().get(0));
//            for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
//                debugOutput.println(m1 + " = " + firstPlayerStrategy.getStrategy().get(m1));
//            }

            MDPBestResponse br2 = new MDPBestResponse(config, config.getAllPlayers().get(1));
            double currentBRVal = br2.calculateBR(secondPlayerStrategy, firstPlayerStrategy);
            LB = Math.max(LB, currentBRVal);
            debugOutput.println("BR : " + currentBRVal);

            Map<MDPState, Set<MDPStateActionMarginal>> br = br2.extractBestResponse(secondPlayerStrategy);

//            debugOutput.println("BR = " + br);
            newActions = secondPlayerStrategy.addBRStrategy(secondPlayerStrategy.getRootState(), br);
//            debugOutput.println("New Actions = " + newActions);
            lp.setNewActions(newActions);
//            debugOutput.println(secondPlayerStrategy.getStrategy());

        }

        long endTime = System.nanoTime() - startTime;
        debugOutput.println("Overall Time: " + (endTime / 1000000));
        debugOutput.println("final size: FirstPlayer Marginal Strategies: " + firstPlayerStrategy.getAllMarginalsInStrategy().size() + " \t SecondPlayer Marginal Strategies: " + secondPlayerStrategy.getAllMarginalsInStrategy().size());
        debugOutput.println("final result:" + UB);

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
    }
}
