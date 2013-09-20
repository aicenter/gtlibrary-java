package cz.agents.gtlibrary.nfg.experimental.MDP;

import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.experimental.MDP.implementations.MDPStrategy;
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
        runBPG();
//        runTG();
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
        long startTime = System.nanoTime();
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

//        MDPBestResponse br1 = new MDPBestResponse(config, config.getAllPlayers().get(0));
//        MDPBestResponse br2 = new MDPBestResponse(config, config.getAllPlayers().get(1));
        MDPFristBetterResponse br1 = new MDPFristBetterResponse(config, config.getAllPlayers().get(0));
        MDPFristBetterResponse br2 = new MDPFristBetterResponse(config, config.getAllPlayers().get(1));

        while ( Math.abs(UB - LB) > MDPConfigImpl.getEpsilon() && UB > LB) {
//        for (int i=0; i<2; i++) {

            debugOutput.println("*********** Iteration = " + (++iterations) + " Bound Interval = " + Math.abs(UB - LB) + " [ " + LB + ";" + UB +  " ]      *************");

            long LpStart = System.nanoTime();
            double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
            CPLEXTIME += System.nanoTime() - LpStart;
            debugOutput.println("Result: " + r1);
            lp.extractStrategyForPlayer(config.getAllPlayers().get(0));
//            for (MDPStateActionMarginal m1 : firstPlayerStrategy.getStrategy().keySet()) {
//                debugOutput.println(m1 + " = " + firstPlayerStrategy.getStrategy().get(m1));
//            }
            LpStart = System.nanoTime();
            double r2 = lp.solveForPlayer(config.getAllPlayers().get(1));
            CPLEXTIME += System.nanoTime() - LpStart;
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
//            secondPlayerStrategy.testUtility(firstPlayerStrategy, r1);

            long brStart = System.nanoTime();
            double currentBRValMax = br1.calculateBR(firstPlayerStrategy,  secondPlayerStrategy);
            BRTIME += System.nanoTime() - brStart;
            debugOutput.println("BR(MAX) TIME:" + ((System.nanoTime() - brStart)/1000000));
            brStart = System.nanoTime();
            double currentBRValMin = br2.calculateBR(secondPlayerStrategy, firstPlayerStrategy);
            BRTIME += System.nanoTime() - brStart;
            debugOutput.println("BR(MIN) TIME:" + ((System.nanoTime() - brStart)/1000000));
            UB = Math.min(UB, currentBRValMax);
            LB = Math.max(LB, currentBRValMin);
            debugOutput.println("BR(MAX): " + currentBRValMax + " BR(MIN): " + currentBRValMin);


            Map<MDPState, Set<MDPStateActionMarginal>> bestResponseActions1 = br1.extractBestResponse(firstPlayerStrategy);
            Map<MDPState, Set<MDPStateActionMarginal>> bestResponseActions2 = br2.extractBestResponse(secondPlayerStrategy);

//            debugOutput.println(bestResponseActions1);
            long RGStart = System.nanoTime();
            newActions1 = firstPlayerStrategy.addBRStrategy(firstPlayerStrategy.getRootState(), bestResponseActions1);
            RGCONSTR += System.nanoTime() - RGStart;
            debugOutput.println("RG(MAX) TIME:" + ((System.nanoTime() - RGStart)/1000000));
//            debugOutput.println(bestResponseActions2);
            RGStart = System.nanoTime();
            newActions2 = secondPlayerStrategy.addBRStrategy(secondPlayerStrategy.getRootState(), bestResponseActions2);
            RGCONSTR += System.nanoTime() - RGStart;
            debugOutput.println("RG(MIN) TIME:" + ((System.nanoTime() - RGStart)/1000000));
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

        long endTime = System.nanoTime() - startTime;
        debugOutput.println("Overall Time: " + (endTime / 1000000));
        debugOutput.println("BR Time: " + (BRTIME / 1000000));
        debugOutput.println("CPLEX Time: " + (CPLEXTIME / 1000000));
        debugOutput.println("RGConstr Time: " + (RGCONSTR / 1000000));
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
