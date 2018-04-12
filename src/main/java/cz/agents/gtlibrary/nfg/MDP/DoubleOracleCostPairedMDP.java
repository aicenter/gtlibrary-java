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
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPConfigImpl;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.implementations.oracle.*;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPAction;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPExpander;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPState;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.bpg.BPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.randomgame.RGMDPExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.tig.TIGExpander;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGConfig;
import cz.agents.gtlibrary.nfg.MDP.domain.transitgame.TGExpander;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/25/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class DoubleOracleCostPairedMDP {

    public static boolean USE_ROBUST_BR = false;
    public static boolean CONTRACTING = false;
    public static boolean USE_REORDER_ACTIONS = false;
    public static double END_EPSILON = MDPConfigImpl.getEpsilon();

    private MDPExpander expander;
    private MDPConfig config;
    private MDPContractingStrategy firstPlayerStrategy;
    private MDPContractingStrategy secondPlayerStrategy;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean ;

    private long BRTIME = 0;
    private long CPLEXTIME = 0;
    private long RGCONSTR = 0;

    private int CONTRACTED_STATES_P1 = 0;
    private int CONTRACTED_STATES_P2 = 0;
    private int EXPANDED_STATES_P1 = 0;
    private int EXPANDED_STATES_P2 = 0;

    private double gameValue = Double.NaN;

    public static Map<MDPState, Map<ArrayList<Integer>, Integer>> behavioralStrategies = new HashMap<MDPState, Map<ArrayList<Integer>, Integer>>();

//    private Map<MDPStateActionMarginal, Integer> debugStrategyMap = new HashMap<MDPStateActionMarginal, Integer>();
//    private ArrayList<Integer> actionsAddedInIteration = new ArrayList<Integer>();

    public static void main(String[] args) {
//		runRG();
//        runBPG();
//        runTG();
        runTIG();
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
    
    public static void runTIG() {
        DoubleOracleCostPairedMDP mdp = new DoubleOracleCostPairedMDP(new TIGExpander(), new TIGConfig());
        mdp.test();
    }
    
     public static void testGame(MDPExpander expander, MDPConfig config) {
        DoubleOracleCostPairedMDP mdp = new DoubleOracleCostPairedMDP(expander, config);
        mdp.test();
    }


    private void test() {
//        try {
//            System.in.read();
//        } catch ( Exception e ) {
//            e.printStackTrace();
//        }

        threadBean = ManagementFactory.getThreadMXBean();
        long startTime = threadBean.getCurrentThreadCpuTime();
        debugOutput.println("Testing DO CostPaired MDP.");
        firstPlayerStrategy = new MDPContractingStrategy(config.getAllPlayers().get(0),config,expander);
        secondPlayerStrategy = new MDPContractingStrategy(config.getAllPlayers().get(1),config,expander);

//        firstPlayerStrategy.generateAllStateActions();
//        secondPlayerStrategy.generateAllStateActions();

        debugOutput.println("Strategies generated.");

        firstPlayerStrategy.initIterativeStrategy(secondPlayerStrategy);
        secondPlayerStrategy.initIterativeStrategy(firstPlayerStrategy);

        MDPIterativeStrategy.updateDefaultUtilityValues(MDPIterativeStrategy.getLastActions(), firstPlayerStrategy,secondPlayerStrategy);

        Map<Player, MDPStrategy> playerStrategy = new HashMap<Player, MDPStrategy>();
        playerStrategy.put(config.getAllPlayers().get(0), firstPlayerStrategy);
        playerStrategy.put(config.getAllPlayers().get(1), secondPlayerStrategy);

        MDPContractingLP lp = new MDPContractingLP(config.getAllPlayers(), playerStrategy, config);

        double LB = Double.NEGATIVE_INFINITY;
        double UB = Double.POSITIVE_INFINITY;

        Set<MDPStateActionMarginal> newActions1 = new HashSet<MDPStateActionMarginal>();
        Set<MDPStateActionMarginal> newActions2 = new HashSet<MDPStateActionMarginal>();


        int iterations = 0;

//        MDPContractingBR br1 = null;
//        MDPContractingBR br2 = null;

        MDPBestResponse br1 = new MDPBestResponse(config, config.getAllPlayers().get(0));
        MDPBestResponse br2 = new MDPBestResponse(config, config.getAllPlayers().get(1));

//        if (USE_ROBUST_BR) {
//        MDPEpsilonFristBetterResponse br1 = new MDPEpsilonFristBetterResponse(config, config.getAllPlayers().get(0));
//        MDPEpsilonFristBetterResponse br2 = new MDPEpsilonFristBetterResponse(config, config.getAllPlayers().get(1));
//        } else if (USE_REORDER_ACTIONS) {
//            br1 = new MDPFBRActionOrdering(config, config.getAllPlayers().get(0));
//            br2 = new MDPFBRActionOrdering(config, config.getAllPlayers().get(1));
//        } else {
//            br1 = new MDPFristBetterResponse(config, config.getAllPlayers().get(0));
//            br2 = new MDPFristBetterResponse(config, config.getAllPlayers().get(1));
//            br1 = new MDPContractingBR(config, config.getAllPlayers().get(0));
//            br2 = new MDPContractingBR(config, config.getAllPlayers().get(1));
//        }

//        double treshold = 0.1;
//        debugStrategyMap.put(firstPlayerStrategy.getAllMarginalsInStrategy().iterator().next(), 0);
//        debugStrategyMap.put(secondPlayerStrategy.getAllMarginalsInStrategy().iterator().next(), 0);
//        actionsAddedInIteration.add(2);

        Set<MDPState> statesToContract1 = new HashSet<MDPState>();
        Set<MDPState> statesToContract2 = new HashSet<MDPState>();
        Set<MDPState> statesToExpand1 = new HashSet<MDPState>();
        Set<MDPState> statesToExpand2 = new HashSet<MDPState>();


        while (true) {
//        while ( Math.abs(UB - LB) > END_EPSILON && UB > LB) {
//        while ( ((Math.abs(UB-LB)/Math.abs(LB)) > 0.001 || LB/UB < 0 || LB == Double.NEGATIVE_INFINITY || UB == Double.POSITIVE_INFINITY) && UB > LB) {
//        for (int i=0; i<8; i++) {

            iterations++;
            debugOutput.println("*********** Iteration = " + (iterations) + " Bound Interval = " + Math.abs(UB - LB) + " [ " + LB + ";" + UB +  " ]      *************");
            debugOutput.println("I = " + (iterations) + " Time = " + ((threadBean.getCurrentThreadCpuTime() - startTime)/1000000l));

            long LpStart = threadBean.getCurrentThreadCpuTime();
            double r1 = lp.solveForPlayer(config.getAllPlayers().get(0));
            CPLEXTIME += threadBean.getCurrentThreadCpuTime() - LpStart;
            debugOutput.println("Result: " + r1);
            lp.extractStrategyForPlayer(config.getAllPlayers().get(0));

            LpStart = threadBean.getCurrentThreadCpuTime();
            double r2 = lp.solveForPlayer(config.getAllPlayers().get(1));
            CPLEXTIME += threadBean.getCurrentThreadCpuTime() - LpStart;
            debugOutput.println("Result: " + r2);
            lp.extractStrategyForPlayer(config.getAllPlayers().get(1));

            firstPlayerStrategy.recalculateExpandedStrategy();
            secondPlayerStrategy.recalculateExpandedStrategy();

//            if (CONTRACTING) {
//                br1.setStatesProbs(lp.getfValues());
//                br2.setStatesProbs(lp.getfValues());
//            }

//            rememberBehavioralStrategies(firstPlayerStrategy, iterations);
//            rememberBehavioralStrategies(secondPlayerStrategy, iterations);

//            br1.setMDPUpperBound(UB);
//            br1.setMDPLowerBound(LB);
//            ((MDPEpsilonFristBetterResponse)br1).setCurrentBest(r1);
//            br2.setMDPUpperBound(LB);
//            br2.setMDPLowerBound(UB);
//            ((MDPEpsilonFristBetterResponse)br2).setCurrentBest(r2);

//            firstPlayerStrategy.sanityCheck();
//            secondPlayerStrategy.sanityCheck();
//            firstPlayerStrategy.lastActionsTest();
//            firstPlayerStrategy.testUtility(secondPlayerStrategy, r1);
//            secondPlayerStrategy.testUtility(firstPlayerStrategy, r2);

            MDPIterativeStrategy.clearRemovedLastActions();
            firstPlayerStrategy.clearActionMarginalsToRemove();
            secondPlayerStrategy.clearActionMarginalsToRemove();

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

//            if (USE_ROBUST_BR) {
//                debugOutput.println("BR(MAX) Improved Times: " + ((MDPEpsilonFristBetterResponse)br1).getImprovedBR());
//                debugOutput.println("BR(MIN) Improved Times: " + ((MDPEpsilonFristBetterResponse)br2).getImprovedBR());
//            }

            Map<MDPState, Set<MDPStateActionMarginal>> bestResponseActions1 = br1.extractBestResponse(firstPlayerStrategy);
            Map<MDPState, Set<MDPStateActionMarginal>> bestResponseActions2 = br2.extractBestResponse(secondPlayerStrategy);

//            statesToContract1.clear();
//            statesToContract2.clear();
//            statesToContract1.addAll(br1.getStatesToContract());
//            statesToContract2.addAll(br2.getStatesToContract());
//
//            statesToExpand1.clear();
//            statesToExpand2.clear();
//            statesToExpand1.addAll(br1.getStatesToExpand());
//            statesToExpand2.addAll(br2.getStatesToExpand());

            HashSet<MDPStateActionMarginal> newActions = new HashSet<MDPStateActionMarginal>();
            HashSet<MDPStateActionMarginal> actionsToRemove = new HashSet<MDPStateActionMarginal>();
            newActions1.clear();
            newActions2.clear();

            long RGStart = threadBean.getCurrentThreadCpuTime();
            if (CONTRACTING) {
                if (statesToExpand1.size() > 0) {
//                    debugOutput.println("Expanding States MAX: " + statesToExpand1);
                    EXPANDED_STATES_P1 += statesToExpand1.size();
                    newActions1.addAll(firstPlayerStrategy.expandStates(statesToExpand1));
                }
                if (statesToExpand2.size() > 0) {
//                    debugOutput.println("Expanding States MIN: " + statesToExpand2);
                    EXPANDED_STATES_P2 += statesToExpand2.size();
                    newActions2.addAll(secondPlayerStrategy.expandStates(statesToExpand2));
                }
            }
            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;

            RGStart = threadBean.getCurrentThreadCpuTime();
            newActions1.addAll(firstPlayerStrategy.addBRStrategy(firstPlayerStrategy.getRootState(), bestResponseActions1));
            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
            debugOutput.println("RG(MAX) TIME:" + ((threadBean.getCurrentThreadCpuTime() - RGStart)/1000000l));

            RGStart = threadBean.getCurrentThreadCpuTime();
            newActions2.addAll(secondPlayerStrategy.addBRStrategy(secondPlayerStrategy.getRootState(), bestResponseActions2));

            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
            debugOutput.println("RG(MIN) TIME:" + ((threadBean.getCurrentThreadCpuTime() - RGStart)/1000000l));

            RGStart = threadBean.getCurrentThreadCpuTime();
            if (CONTRACTING) {


                if (statesToContract1.size() > 0) {
//                    debugOutput.println("Contracting States MAX: " + statesToContract1);
                    CONTRACTED_STATES_P1 += statesToContract1.size();
                    newActions1.addAll(firstPlayerStrategy.concractStates(statesToContract1, lp.getfValues()));
                }
                if (statesToContract2.size() > 0) {
//                    debugOutput.println("Contracting States MIN: " + statesToContract2);
                    CONTRACTED_STATES_P2 += statesToContract2.size();
                    newActions2.addAll(secondPlayerStrategy.concractStates(statesToContract2, lp.getfValues()));
                }

                actionsToRemove.addAll(firstPlayerStrategy.getActionMarginalsToRemove());
                actionsToRemove.addAll(secondPlayerStrategy.getActionMarginalsToRemove());

//                debugOutput.println("Removing Actions: " + actionsToRemove);

                firstPlayerStrategy.removeMaringalsFromStrategy();
                secondPlayerStrategy.removeMaringalsFromStrategy();

//                firstPlayerStrategy.lastActionsSanity();
//                secondPlayerStrategy.lastActionsSanity();
            }
            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;

            newActions.addAll(newActions1);
            newActions.addAll(newActions2);
            newActions.removeAll(actionsToRemove);
            lp.setNewActions(newActions);
            lp.setActionsToRemove(actionsToRemove);

//            debugOutput.println("New Actions MAX: " + newActions1);
//            debugOutput.println("New Actions MIN: " + newActions2);

            RGStart = threadBean.getCurrentThreadCpuTime();
            firstPlayerStrategy.completeStrategy();
            secondPlayerStrategy.completeStrategy();
            MDPIterativeStrategy.updateDefaultUtilityValues(newActions, firstPlayerStrategy,secondPlayerStrategy);
            RGCONSTR += threadBean.getCurrentThreadCpuTime() - RGStart;
            debugOutput.println("RG DEFUPDATE TIME:" + ((threadBean.getCurrentThreadCpuTime() - RGStart)/1000000l));

//            actionsAddedInIteration.add(newActions.size());
//            for (MDPStateActionMarginal a : newActions) {
//                debugStrategyMap.put(a, iterations);
//            }
//            if (Math.abs(UB - LB) < 1e-2) CONTRACTING = true;
            if (newActions1.isEmpty() && newActions2.isEmpty()) {
//                treshold = treshold / 10;
//                if (treshold < MDPConfigImpl.getEpsilon()/100) {
                    if (Math.abs(UB - LB) > END_EPSILON) debugOutput.println("************* ERROR ****************");
                    break;
//                }
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

        if (CONTRACTING) {
            debugOutput.println("Overall Contracted States MAX: " + CONTRACTED_STATES_P1);
            debugOutput.println("Overall Contracted States MIN: " + CONTRACTED_STATES_P2);
            debugOutput.println("Overall Expanded States MAX: " + EXPANDED_STATES_P1);
            debugOutput.println("Overall Expanded States MIN: " + EXPANDED_STATES_P2);

            debugOutput.println("Current Contracted States MAX: " + firstPlayerStrategy.getFixedBehavioralStrategiesSize() + " out of " + firstPlayerStrategy.getStates().size());
            debugOutput.println("Current Contracted States MIN: " + secondPlayerStrategy.getFixedBehavioralStrategiesSize()+ " out of " + secondPlayerStrategy.getStates().size());
        }

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
//        printIterations(iterations);
    }

    private void rememberBehavioralStrategies(MDPStrategy strategy, int currentIteration) {
        for (MDPState s : strategy.getStates()) {
            ArrayList<Integer> currentStrategy = new ArrayList<Integer>();
            double probOfState = 0;
            if (s.isRoot()) {
                probOfState = 1;
            } else {
                Map<MDPStateActionMarginal, Double> preds = strategy.getPredecessors(s);
                for (MDPStateActionMarginal m : preds.keySet()) {
                    probOfState += strategy.getExpandedStrategy(m) * preds.get(m);
                }
            }
            for (MDPAction a : strategy.getAllActions(s)) {
                double v = strategy.getExpandedStrategy(new MDPStateActionMarginal(s, a))/probOfState;
                int vv = new Double(v*1e6).intValue();
                currentStrategy.add(vv);
            }
            Map<ArrayList<Integer>, Integer> storedBS = behavioralStrategies.get(s);
            if (storedBS == null || !storedBS.containsKey(currentStrategy)) {
                if (storedBS != null) {
                    if (currentIteration - storedBS.values().iterator().next() > 10)
                        System.out.println("Removing stored strategy fixed for " + (currentIteration - storedBS.values().iterator().next()) + " iterations.");
                }
                Map<ArrayList<Integer>, Integer> tmp = new HashMap<ArrayList<Integer>, Integer>();
                tmp.put(currentStrategy, currentIteration);
                behavioralStrategies.put(s, tmp);
            }
        }
    }

    private void printIterations(int finalIterations) {
        System.out.println("Number of Behavioral Strategies Determined in Iterations");
        int[] amounts = new int[finalIterations];
        for (MDPState s : behavioralStrategies.keySet())
            for (ArrayList<Integer> actions : behavioralStrategies.get(s).keySet()) {
//                System.out.println(behavioralStrategies.get(s).get(actions) + " in state " + s);
                amounts[behavioralStrategies.get(s).get(actions)-1]++;
            }

        for (int i=0; i<finalIterations; i++) {
            if (amounts[i] == 0) continue;
            System.out.println((i+1) + ". iteration -> " + amounts[i] + " strategies");
        }

    }
}
