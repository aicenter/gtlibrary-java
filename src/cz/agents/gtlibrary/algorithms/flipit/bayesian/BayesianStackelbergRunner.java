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


package cz.agents.gtlibrary.algorithms.flipit.bayesian;

import cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.LeaderGenerationBayesianStackelbergLP;
import cz.agents.gtlibrary.algorithms.flipit.bayesian.milp.BayesianStackelbergSequenceFormMILP;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergSequenceFormLP;
import cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.ShallowestBrokenCplexBayesianStackelbergLP;
import cz.agents.gtlibrary.algorithms.flipit.bayesian.iterative.SumForbiddingBayesianStackelbergLP;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.interfaces.*;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Jakub Cerny
 * Date: 14/3/17
 * Time: 2:43 PM
 *
 * This implementation of algorithms computing BSSE assumes that the utilities are in format
 * [ u(leader) ; u(follower :: type 1) ; u(follower : type 2) ; ... ; u(follower :: type n) ; u(nature) ]
 *
 *
 */

public class BayesianStackelbergRunner {

    final static int LEADER = 0;
    static boolean OUTPUT = false;
    static String outputFile;
    static String output;

//    static String alg = "AI-LP";
//    static String alg = "AI-CG";
    static String alg = "MILP";

    public static void main(String[] args) {
//        runFlipIt(new String[]{"3", "5", "flipit_simple3.txt", "10", "test_bsgsse.txt", alg , "F"});
    }

    public static void runFlipIt(String[] args){
        FlipItGameInfo gameInfo;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else{
            int depth = Integer.parseInt(args[0]);
            int numTypes = Integer.parseInt(args[1]);
            String graphFile = args[2];
            long seed  = Integer.parseInt(args[3]);
            gameInfo = new FlipItGameInfo(depth,numTypes,graphFile, seed);
//            gameInfo.setInfo(depth,numTypes,graphFile);
            outputFile = args[4];
            output = depth + " " + numTypes + " " + graphFile + " " + seed + " ";

            alg = args[5];

            OUTPUT = false;

            String version = args[6];
            switch(version){
                case "N" : FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.NO; break;
                case "AP" : FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_ALL_POINTS; break;
                case "NP" : FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_NODE_POINTS; break;
                case "F" : FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.FULL; break;
            }

        }
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState = null;

        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;

        }
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        FlipItExpander<SequenceInformationSet> expander = new FlipItExpander<>(algConfig);
        BayesianStackelbergRunner runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);

        switch(alg){
            case "AI-LP" : runner.generate(rootState.getAllPlayers()[0], new SumForbiddingBayesianStackelbergLP( gameInfo, expander));
                break;
            case "AI-MILP" : runner.generate(rootState.getAllPlayers()[0], new ShallowestBrokenCplexBayesianStackelbergLP( gameInfo, expander));
                break;
            case "MILP" : runner.generate(rootState.getAllPlayers()[0], new BayesianStackelbergSequenceFormMILP(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], gameInfo, expander));
                break;
            case "AI-CG" : runner.generate(rootState.getAllPlayers()[0], new LeaderGenerationBayesianStackelbergLP(FlipItGameInfo.DEFENDER, gameInfo, false, true));
                break;
        }

//        double LP = runner.generate(rootState.getAllPlayers()[0], new SumForbiddingBayesianStackelbergLP( gameInfo, expander));
//        double LP = runner.generate(rootState.getAllPlayers()[0], new ShallowestBrokenCplexBayesianStackelbergLP( gameInfo, expander));

//        runner = new BayesianStackelbergRunner(rootState, expander, gameInfo, algConfig);
//        double MILP = runner.generate(rootState.getAllPlayers()[0], new BayesianStackelbergSequenceFormMILP(new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, rootState.getAllPlayers()[0], rootState.getAllPlayers()[1], gameInfo, expander));
//        new GambitEFG().write("flipit.gbt", rootState, expander);

//        System.out.println("LP : " + LP + "; MILP : " + MILP);
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

    public BayesianStackelbergRunner(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo gameInfo, StackelbergConfig algConfig) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameConfig = gameInfo;
        this.algConfig = algConfig;
    }

    public double generate(Player leader, StackelbergSequenceFormLP solver) {
        if (solver instanceof BayesianStackelbergSequenceFormMILP) debugOutput.println("Bayesian Stackelberg MILP");
        if (solver instanceof SumForbiddingBayesianStackelbergLP) debugOutput.println("Bayesian Stackelberg Iterative LP");
        if (solver instanceof ShallowestBrokenCplexBayesianStackelbergLP) debugOutput.println("Bayesian Stackelberg Iterative MILP");
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
//        for (Player player : actingPlayers) {
//            for (Sequence sequence : realizationPlans.get(player).keySet()) {
//                if (realizationPlans.get(player).get(sequence) > 0) {
//                    support_size[player.getId()]++;
//                    if (DEBUG)
//                        System.out.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
//                }
//            }
//        }

//        for (Sequence sequence : realizationPlans.get(FlipItGameInfo.DEFENDER).keySet()) {
//            if (realizationPlans.get(FlipItGameInfo.DEFENDER).get(sequence) > 0) {
//                System.out.println(sequence + " : " + realizationPlans.get(FlipItGameInfo.DEFENDER).get(sequence));
//            }
//        }

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

        if (OUTPUT){
            output += alg + " ";
            output += gameValue + " ";
            output += (algConfig.getSequencesFor(actingPlayers[0]).size() + " ");
            output += (algConfig.getSequencesFor(actingPlayers[1]).size() + " ");
            output += (algConfig.getSequencesFor(actingPlayers[0]).size() + FlipItGameInfo.numTypes*algConfig.getSequencesFor(actingPlayers[1]).size() + " ");
            output += (algConfig.getAllInformationSets().size() + " ");
            output += (overallSequenceGeneration + " ");
            output += (((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
                    .freeMemory()) / 1024 / 1024) + " ");
            output += (finalTime + " ");
            output += (overallCPLEX + " ");
            output += (solver.getOverallConstraintGenerationTime() / 1000000l + " ");
            output += (solver.getOverallConstraintLPSolvingTime() / 1000000l + " ");

            try {
                Files.write(Paths.get(outputFile), (output+"\n").getBytes(),
                        StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("Appending failed");
            }
        }

        return gameValue;//realizationPlans;
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

                for (int i = 0; i < utilities.length; i++)
                    u[i] = utilities[i] * currentState.getNatureProbability()*gameConfig.getUtilityStabilizer();
                algConfig.setUtility(currentState, u);
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                if (action == null) System.out.println("null action");
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
