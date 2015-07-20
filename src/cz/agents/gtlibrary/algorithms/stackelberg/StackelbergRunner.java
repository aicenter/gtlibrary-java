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


package cz.agents.gtlibrary.algorithms.stackelberg;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.CplexBBStackelbergSequenceFormIterativeLP;
import cz.agents.gtlibrary.algorithms.stackelberg.iterativelp.StackelbergSequenceFormIterativeLP;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.DOBSS;
import cz.agents.gtlibrary.algorithms.stackelberg.milp.StackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergMultipleLPs;
import cz.agents.gtlibrary.algorithms.stackelberg.multiplelps.StackelbergSequenceFormMultipleLPs;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.pursuit.GenSumPursuitGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.simpleGeneralSum.SimpleGSExpander;
import cz.agents.gtlibrary.domain.simpleGeneralSum.SimpleGSInfo;
import cz.agents.gtlibrary.domain.simpleGeneralSum.SimpleGSState;
import cz.agents.gtlibrary.domain.stacktest.StackTestExpander;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameInfo;
import cz.agents.gtlibrary.domain.stacktest.StackTestGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 12/2/13
 * Time: 2:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class StackelbergRunner {

    public static void main(String[] args) {
        runGenSumRandom();
//        runBPG();
//        runSGSG();
//        runPEG();
//        runStackTest();
    }

    public static void runGenSumRandom() {
        GameState rootState = new GeneralSumRandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

//        runner.generate(rootState.getAllPlayers()[1], new StackelbergSequenceFormMILP(rootState.getAllPlayers(), rootState.getAllPlayers()[1], rootState.getAllPlayers()[0], gameInfo, expander));
//        runner.generate(rootState.getAllPlayers()[0], new StackelbergSequenceFormMultipleLPs(rootState.getAllPlayers(), rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], gameInfo, expander));
//        runner.generate(rootState.getAllPlayers()[0], new StackelbergSequenceFormIterativeLP(rootState.getAllPlayers()[0], gameInfo));
        runner.generate(rootState.getAllPlayers()[0], new CplexBBStackelbergSequenceFormIterativeLP(rootState.getAllPlayers()[0], gameInfo));
        new GambitEFG().write("randomGame.gbt", rootState, expander);
    }

    public static void runBPG() {
        GameState rootState = new GenSumBPGGameState();
        BPGGameInfo gameInfo = new BPGGameInfo();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);
        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);

//        runner.generate(rootState.getAllPlayers()[0], new StackelbergSequenceFormMILP(rootState.getAllPlayers(), rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], gameInfo, expander));
        runner.generate(rootState.getAllPlayers()[0], new StackelbergSequenceFormIterativeLP(rootState.getAllPlayers()[0], gameInfo));

    }

    public static void runSGSG() {
        SimpleGSInfo gameInfo = new SimpleGSInfo();
        GameState rootState = new SimpleGSState(gameInfo.getAllPlayers());
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new SimpleGSExpander(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        runner.generate(rootState.getAllPlayers()[0],
                new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], gameInfo, expander));
//        new GambitEFG().write("simpleGSG.gbt", rootState, expander);
    }

    public static void runPEG() {
        GameInfo gameInfo = new PursuitGameInfo();
        GameState rootState = new GenSumPursuitGameState();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new PursuitExpander(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
        runner.generate(rootState.getAllPlayers()[1],
                new StackelbergSequenceFormMultipleLPs(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], gameInfo, expander));
    }

    public static void runStackTest() {
        GameInfo gameInfo = new StackTestGameInfo();
        GameState rootState = new StackTestGameState();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new StackTestExpander(algConfig);

        StackelbergRunner runner = new StackelbergRunner(rootState, expander, gameInfo, algConfig);
//        runner.generate(rootState.getAllPlayers()[0], new StackelbergSequenceFormMILP(rootState.getAllPlayers(), rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], gameInfo, expander));
        runner.generate(rootState.getAllPlayers()[0], new StackelbergSequenceFormIterativeLP(rootState.getAllPlayers()[0], gameInfo));
        new GambitEFG().write("stackTest.gbt", rootState, expander);
    }

    private GameState rootState;
    private Expander<SequenceInformationSet> expander;
    private GameInfo gameConfig;
    private StackelbergConfig algConfig;

    private PrintStream debugOutput = System.out;
    final private static boolean DEBUG = false;
    private ThreadMXBean threadBean;

    private double gameValue = Double.NaN;
    private long finalTime;

    public StackelbergRunner(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo gameInfo, StackelbergConfig algConfig) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameConfig = gameInfo;
        this.algConfig = algConfig;
    }

    public Map<Player, Map<Sequence, Double>> generate(Player leader, StackelbergSequenceFormLP solver) {
        debugOutput.println("Full Sequence Multiple LP Stackelberg");
        debugOutput.println(gameConfig.getInfo());
        threadBean = ManagementFactory.getThreadMXBean();

        long start = threadBean.getCurrentThreadCpuTime();
        long overallSequenceGeneration = 0;
        long overallCPLEX = 0;
        Map<Player, Map<Sequence, Double>> realizationPlans = new HashMap<>();
        long startGeneration = threadBean.getCurrentThreadCpuTime();

        generateCompleteGame();
        System.out.println("Game tree built...");
        System.out.println("Information set count: " + algConfig.getAllInformationSets().size());
        overallSequenceGeneration = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;

        Player[] actingPlayers = new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]};
        System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());

        long startCPLEX = threadBean.getCurrentThreadCpuTime();

        solver.calculateLeaderStrategies(algConfig, expander);

        long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX) / 1000000l;

        overallCPLEX += thisCPLEX;

        for (Player player : rootState.getAllPlayers()) {
            realizationPlans.put(player, solver.getResultStrategiesForPlayer(player));
        }

        System.out.println("done.");
        finalTime = (threadBean.getCurrentThreadCpuTime() - start) / 1000000l;

        int[] support_size = new int[]{0, 0};
        for (Player player : actingPlayers) {
            for (Sequence sequence : realizationPlans.get(player).keySet()) {
                if (realizationPlans.get(player).get(sequence) > 0) {
                    support_size[player.getId()]++;
                    if (DEBUG)
                        System.out.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
                }
            }
        }

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

        gameValue = solver.getResultForPlayer(leader);
        System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
        System.out.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
        System.out.println("final result:" + gameValue);
        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        System.out.println("final time: " + finalTime);
        System.out.println("final CPLEX time: " + overallCPLEX);
        System.out.println("final CPLEX building time: " + solver.getOverallConstraintGenerationTime() / 1000000l);
        System.out.println("final CPLEX solving time: " + solver.getOverallConstraintLPSolvingTime() / 1000000l);
        System.out.println("final BR time: " + 0);
        System.out.println("final RGB time: " + 0);
        System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);
        System.out.println("final IS count: " + algConfig.getAllInformationSets().size());
        if (solver instanceof StackelbergSequenceFormMultipleLPs) {
            System.out.println("Computing rp count");
            System.out.println("pruned rp count: " + ((StackelbergSequenceFormMultipleLPs) solver).prunnedRPCountWhileBuilding(algConfig));
        }

        if (DEBUG) {
            // sanity check -> calculation of Full BR on the solution of SQF LP
            SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameConfig);
            System.out.println("BR: " + brAlg.calculateBR(rootState, realizationPlans.get(actingPlayers[1])));

            SQFBestResponseAlgorithm brAlg2 = new SQFBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameConfig);
            System.out.println("BR: " + brAlg2.calculateBR(rootState, realizationPlans.get(actingPlayers[0])));

            algConfig.validateGameStructure(rootState, expander);
        }
        return realizationPlans;
    }

    public void generateCompleteGame() {
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers())
                    u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*gameConfig.getUtilityStabilizer();
                algConfig.setUtility(currentState, u);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
    }

    public double getGameValue() {
        return gameValue;
    }

    public long getFinalTime() {
        return finalTime;
    }
}
