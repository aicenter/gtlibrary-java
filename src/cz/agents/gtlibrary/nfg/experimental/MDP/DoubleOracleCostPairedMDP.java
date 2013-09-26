package cz.agents.gtlibrary.nfg.experimental.MDP;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle.MDPEpsilonFristBetterResponse;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.oracle.MDPFristBetterResponse;
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
public class DoubleOracleCostPairedMDP {

    public static boolean USE_ROBUST_BR = true;

    private MDPExpander expander;
    private MDPConfig config;
    private MDPIterativeStrategy firstPlayerStrategy;
    private MDPIterativeStrategy secondPlayerStrategy;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;

    private long BRTIME = 0;
    private long CPLEXTIME = 0;
    private long RGCONSTR = 0;

    private double gameValue = Double.NaN;

    public static void main(String[] args) {
//		runRG();
//        runBPG();
        runTG();
    }


    public DoubleOracleCostPairedMDP(MDPExpander expander, MDPConfig config) {
        this.expander = expander;
        this.config = config;
    }

    public static void runBPG() {
        DoubleOracleCostPairedMDP mdp = new DoubleOracleCostPairedMDP(new BPExpander(), new BPConfig());
        mdp.test();
    }

    public static void runRG() {
        DoubleOracleCostPairedMDP mdp = new DoubleOracleCostPairedMDP(new RGMDPExpander(), new RGMDPConfig());
        mdp.test();
    }

    public static void runTG() {
        DoubleOracleCostPairedMDP mdp = new DoubleOracleCostPairedMDP(new TGExpander(), new TGConfig());
        mdp.test();
    }


    private void test() {
        threadBean = ManagementFactory.getThreadMXBean();
        long startTime = threadBean.getCurrentThreadCpuTime();
        debugOutput.println("Testing DO CostPaired MDP.");
        firstPlayerStrategy = new MDPIterativeStrategy(config.getAllPlayers().get(0),config,expander);
        secondPlayerStrategy = new MDPIterativeStrategy(config.getAllPlayers().get(1),config,expander);

        firstPlayerStrategy.initIterativeStrategy(secondPlayerStrategy);
        secondPlayerStrategy.initIterativeStrategy(firstPlayerStrategy);

        Map<Player, MDPStrategy> playerStrategy = new HashMap<Player, MDPStrategy>();
        playerStrategy.put(config.getAllPlayers().get(0), firstPlayerStrategy);
        playerStrategy.put(config.getAllPlayers().get(1), secondPlayerStrategy);

        MDPOracleLP lp = new MDPOracleLP(config.getAllPlayers(), playerStrategy, config);

        double LB = Double.NEGATIVE_INFINITY;
        double UB = Double.POSITIVE_INFINITY;

        Set<MDPStateActionMarginal> newActions1 = new HashSet<MDPStateActionMarginal>();
        Set<MDPStateActionMarginal> newActions2 = new HashSet<MDPStateActionMarginal>();


        int iterations = 0;

        MDPFristBetterResponse br1 = null;
        MDPFristBetterResponse br2 = null;

//        MDPBestResponse br1 = new MDPBestResponse(config, config.getAllPlayers().get(0));
//        MDPBestResponse br2 = new MDPBestResponse(config, config.getAllPlayers().get(1));

        if (USE_ROBUST_BR) {
            br1 = new MDPEpsilonFristBetterResponse(config, config.getAllPlayers().get(0));
            br2 = new MDPEpsilonFristBetterResponse(config, config.getAllPlayers().get(1));
        } else {
            br1 = new MDPFristBetterResponse(config, config.getAllPlayers().get(0));
            br2 = new MDPFristBetterResponse(config, config.getAllPlayers().get(1));
        }

        while ( Math.abs(UB - LB) > MDPConfigImpl.getEpsilon() && UB > LB) {
//        for (int i=0; i<8; i++) {

            debugOutput.println("*********** Iteration = " + (++iterations) + " Bound Interval = " + Math.abs(UB - LB) + " [ " + LB + ";" + UB +  " ]      *************");

            long LpStart = threadBean.getCurrentThreadCpuTime();
            double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
            CPLEXTIME += threadBean.getCurrentThreadCpuTime() - LpStart;
            debugOutput.println("Result: " + r1);
            lp.extractStrategyForPlayer(config.getAllPlayers().get(0));
//            for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
//                debugOutput.println(m1 + " = " + firstPlayerStrategy.getStrategy().get(m1));
//            }
            LpStart = threadBean.getCurrentThreadCpuTime();
            double r2 = lp.solveForPlayer(config.getAllPlayers().get(1));
            CPLEXTIME += threadBean.getCurrentThreadCpuTime() - LpStart;
            debugOutput.println("Result: " + r2);
            lp.extractStrategyForPlayer(config.getAllPlayers().get(1));

//            debugOutput.println("strategy(MAX): " + firstPlayerStrategy.strategy);
//            debugOutput.println("strategy(MIN): " + secondPlayerStrategy.strategy);
//            for (MDPStateActionMarginal m : firstPlayerStrategy.getAllActionStates()) {
//                debugOutput.println("strategy(" + m + "): " + firstPlayerStrategy.getExpandedStrategy(m));
//            }
//            long ExpStart = System.nanoTime();
            firstPlayerStrategy.recalculateExpandedStrategy();
//            debugOutput.println("EXPST(MAX) TIME:" + ((System.nanoTime() - ExpStart)/1000000));
//            ExpStart = System.nanoTime();
            secondPlayerStrategy.recalculateExpandedStrategy();
//            debugOutput.println("EXPST(MIN) TIME:" + ((System.nanoTime() - ExpStart)/1000000));

//            debugOutput.println(firstPlayerStrategy.getExpandedNonZeroStrategy());
//            debugOutput.println(secondPlayerStrategy.getExpandedNonZeroStrategy());

            br1.setMDPUpperBound(UB);
            br1.setMDPLowerBound(LB);
            br1.setCurrentBest(r1);
            br2.setMDPUpperBound(LB);
            br2.setMDPLowerBound(UB);
            br2.setCurrentBest(r2);

//            firstPlayerStrategy.sanityCheck();
//            secondPlayerStrategy.sanityCheck();
//            firstPlayerStrategy.testUtility(secondPlayerStrategy, r1);
//            secondPlayerStrategy.testUtility(firstPlayerStrategy, r2);

            long brStart = threadBean.getCurrentThreadCpuTime();
            double currentBRValMax = br1.calculateBR(firstPlayerStrategy,  secondPlayerStrategy);
            BRTIME += threadBean.getCurrentThreadCpuTime() - brStart;
            debugOutput.println("BR(MAX) TIME:" + ((threadBean.getCurrentThreadCpuTime() - brStart)/1000000l));
            brStart = threadBean.getCurrentThreadCpuTime();
            double currentBRValMin = br2.calculateBR(secondPlayerStrategy, firstPlayerStrategy);
            BRTIME += threadBean.getCurrentThreadCpuTime() - brStart;
            debugOutput.println("BR(MIN) TIME:" + ((threadBean.getCurrentThreadCpuTime() - brStart)/1000000l));
            UB = Math.min(UB, currentBRValMax);
            LB = Math.max(LB, currentBRValMin);
            debugOutput.println("BR(MAX): " + currentBRValMax + " BR(MIN): " + currentBRValMin);


            Map<MDPState, Set<MDPStateActionMarginal>> bestResponseActions1 = br1.extractBestResponse(firstPlayerStrategy);
            Map<MDPState, Set<MDPStateActionMarginal>> bestResponseActions2 = br2.extractBestResponse(secondPlayerStrategy);

//            debugOutput.println(bestResponseActions1);
            long RGStart = threadBean.getCurrentThreadCpuTime();
            newActions1 = firstPlayerStrategy.addBRStrategy(firstPlayerStrategy.getRootState(), bestResponseActions1);
            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
            debugOutput.println("RG(MAX) TIME:" + ((threadBean.getCurrentThreadCpuTime() - RGStart)/1000000l));
//            debugOutput.println(bestResponseActions2);
//            debugOutput.println(MDPStrategy.getUtilityCache());
            RGStart = threadBean.getCurrentThreadCpuTime();
            newActions2 = secondPlayerStrategy.addBRStrategy(secondPlayerStrategy.getRootState(), bestResponseActions2);
            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
            debugOutput.println("RG(MIN) TIME:" + ((threadBean.getCurrentThreadCpuTime() - RGStart)/1000000l));
//            debugOutput.println(MDPStrategy.getUtilityCache());

//            debugOutput.println("New Actions MAX: " + newActions1);
//            debugOutput.println("New Actions MIN: " + newActions2);

            HashSet<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();
            newActions.addAll(newActions1);
            newActions.addAll(newActions2);
//            debugOutput.println("New Actions = " + newActions);
            lp.setNewActions(newActions);

            if (newActions1.isEmpty() && newActions2.isEmpty()) {
                if (Math.abs(UB - LB) > MDPConfigImpl.getEpsilon()) debugOutput.println("************* ERROR ****************");
                break;
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
