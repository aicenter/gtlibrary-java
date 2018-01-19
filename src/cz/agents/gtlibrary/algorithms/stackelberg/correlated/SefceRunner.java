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


package cz.agents.gtlibrary.algorithms.stackelberg.correlated;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.algorithms.sequenceform.gensum.GenSumSequenceFormConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.StackelbergConfig;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.multiplayer.CompleteSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.CompleteTwoPlayerSefceLP;
import cz.agents.gtlibrary.algorithms.stackelberg.correlated.twoplayer.iterative.*;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.GenSumBPGGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenSumGPGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.randomgame.GenSumSimRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.GeneralSumRandomGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.testGame.TestGameExpander;
import cz.agents.gtlibrary.domain.testGame.TestGameState;
import cz.agents.gtlibrary.domain.testGame.TestGameInfo;
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class SefceRunner {

    final static int LEADER = 0;
    final static boolean EXPORT_LP = true;
    final static int depth = 3;

    private double restrictedGameRatio;

    protected final boolean PRINT_STRATEGY = true;

    public static void main(String[] args) {
//        runPoker();
//        runGenSumRandom();
//        runGenSumRandomImproved();
//        runBPG(depth);
//        runFlipIt(args);
//        runFlipIt(new String[]{"F", "3", "3", "AP", "C"});
        runTestGame();
    }

    public static void runTestGame(){

        GameInfo gameInfo = new TestGameInfo();
        GameState rootState = new TestGameState();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new TestGameExpander(algConfig);
        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

//        runner.generate(rootState.getAllPlayers()[LEADER],new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER],gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));

        new GambitEFG().write("testGame.gbt", rootState, expander);
    }

    public static void runPoker(){

    GameInfo gameInfo = new GPGameInfo();
    GameState rootState = new GenSumGPGameState();
    StackelbergConfig algConfig = new StackelbergConfig(rootState);
    Expander<SequenceInformationSet> expander = new GenericPokerExpander<>(algConfig);
    SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

//    runner.generate(rootState.getAllPlayers()[LEADER],new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER],gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
}

    protected static FlipItGameInfo initializeFlipIt(String[] args){
        int depth = Integer.parseInt(args[1]);
        int graphSize = Integer.parseInt(args[2]);
        String graphFile = "flipit_empty" + graphSize + ".txt";
//            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
        FlipItGameInfo gameInfo = new FlipItGameInfo(depth, 1, graphFile, 1);
        switch (args[3]){
            case "N" : gameInfo.gameVersion = FlipItGameInfo.FlipItInfo.NO; break;
            case "NP" : gameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_NODE_POINTS; break;
            case "AP" : gameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_ALL_POINTS; break;
            case "F" : gameInfo.gameVersion = FlipItGameInfo.FlipItInfo.FULL; break;
        }
        return gameInfo;

    }

    public static void runFlipIt(String[] args){
        FlipItGameInfo gameInfo;
        String alg = "";
        if (args.length > 0) {
            gameInfo = initializeFlipIt(args);
            alg = args[4];
        }
        else gameInfo = new FlipItGameInfo();
        gameInfo.ZERO_SUM_APPROX = false;
        GameState rootState = null;

        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;

        }
        FlipItExpander<SequenceInformationSet> expander = null;
        if (!alg.isEmpty()){
            StackelbergConfig algConfig = null;
            SefceRunner runner = null;
            switch(alg){
                case "C" :
                    algConfig = new StackelbergConfig(rootState);
                    expander = new FlipItExpander<>(algConfig);
                    runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                    runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                    break;
                case "O" :
                    algConfig = new StackelbergConfig(rootState);
                    expander = new FlipItExpander<>(algConfig);
                    runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                    runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                    break;
                case "CO" :
                    algConfig = new StackelbergConfig(rootState);
                    expander = new FlipItExpander<>(algConfig);
                    runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                    runner.generate(rootState.getAllPlayers()[LEADER], new CompleteGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                    break;
                case "LM" :
                    algConfig = new LeaderGenerationConfig(rootState);
                    expander = new FlipItExpander<>(algConfig);
                    runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                    Solver s1 = new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo);
                    runner.generate(rootState.getAllPlayers()[LEADER], s1);
                case "RW" :
                    algConfig = new StackelbergConfig(rootState);
                    expander = new FlipItExpander<>(algConfig);
                    runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                    runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGeneration2pRelevantWiseSefceLp(rootState.getAllPlayers()[LEADER], gameInfo));
                    break;
                case "TLS" :
                    algConfig = new StackelbergConfig(rootState);
                    expander = new FlipItExpander<>(algConfig);
                    runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                    runner.generate(rootState.getAllPlayers()[LEADER], new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                    break;
                case "TL" :
                    algConfig = new StackelbergConfig(rootState);
                    expander = new FlipItExpander<>(algConfig);
                    runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
                    runner.generate(rootState.getAllPlayers()[LEADER], new LeaderTLSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
                    break;
            }

            double max = Double.NEGATIVE_INFINITY;
            double min = Double.POSITIVE_INFINITY;
            for (Double[] u: algConfig.getUtilityForSequenceCombinationGenSum().values()){
                if (u!=null) {
                    for (int i = 0; i < u.length; i++) {
                        if (u[i] != null && u[i] > max) max = u[i];
                        if (u[i] != null && u[i] < min) min = u[i];
                    }
                }
            }

            System.out.println();
            runner.issOutsideSubGame = runner.calculateNumberOfSubgamesWithLeaderStateOnTop(LEADER);
            System.out.println();

            System.out.println("Min utility (tree): "+min);
            System.out.println("Max utility (tree): "+max);
        }
        else {
            StackelbergConfig algConfig = new StackelbergConfig(rootState);
            expander = new FlipItExpander<>(algConfig);

            SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
            runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGeneration2pLessMemSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));

        }


        System.out.println("Max utility (config): " + gameInfo.getMaxUtility());
        new GambitEFG().write("flipit.gbt", rootState, expander);
    }

    public static void runBPG(int depth) {
        GameState rootState = new GenSumBPGGameState();
        BPGGameInfo gameInfo = new BPGGameInfo();
        BPGGameInfo.DEPTH = depth;
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new BPGExpander<>(algConfig);
        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);

        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
    }

    public static void runGenSumRandom() {
        GameState rootState = new GenSumSimRandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new RandomGameExpander<>(algConfig);
        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
//        runner.generate(rootState.getAllPlayers()[0], new MultiplayerSefceLP(rootState.getAllPlayers()[0], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderTLSimulataneousSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[0], new CompleteDualGeneratingSEFCE(rootState.getAllPlayers()[0], gameInfo));
        new GambitEFG().write("randomGameSim.gbt", rootState, expander);
//        new DotEFG().writeII("", rootState,expander);
    }
    
    public static void runGenSumRandomImproved() {
    	GameInfo gameInfo = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameInfo();
        GameState rootState = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameState();
        StackelbergConfig algConfig = new StackelbergConfig(rootState);
        Expander<SequenceInformationSet> expander = new cz.agents.gtlibrary.domain.randomgameimproved.RandomGameExpander<>(algConfig);
//        new GambitEFG().write("randomGameImproved.gbt", rootState, expander);
        SefceRunner runner = new SefceRunner(rootState, expander, gameInfo, algConfig);
//        runner.generate(rootState.getAllPlayers()[0], new MultiplayerSefceLP(rootState.getAllPlayers()[0], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new CompleteTwoPlayerSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//        runner.generate(rootState.getAllPlayers()[LEADER], new LeaderGenerationSefceLP(rootState.getAllPlayers()[LEADER], gameInfo));
//      runner.generate(rootState.getAllPlayers()[0], new CompleteDualGeneratingSEFCE(rootState.getAllPlayers()[0], gameInfo));
//        new GambitEFG().write("newSefce_randomGameImproved.gbt", rootState, expander);
    }

 
    private GameState rootState;
    private Expander<SequenceInformationSet> expander;
    private GameInfo gameConfig;
    private GenSumSequenceFormConfig algConfig;

    private PrintStream debugOutput = System.out;
    //final private static boolean DEBUG = false;
    private ThreadMXBean threadBean;

    private double gameValue = Double.NaN;
    private long finalTime;

    protected HashMap<Integer, HashMap<Integer, Integer>> issOutsideSubGame;

    public SefceRunner(GameState rootState, Expander<SequenceInformationSet> expander, GameInfo gameInfo, GenSumSequenceFormConfig algConfig) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameConfig = gameInfo;
        this.algConfig = algConfig;
        this.issOutsideSubGame = new HashMap<>();

//        HashMap<Integer, Double> m = new HashMap<>();
//        ExtendedInteger ii = new ExtendedInteger(0);
//        for (int i = 0; i < 6; i++){
//            ii.transformInto(i);
//            m.put(ii.hashCode(), new Double(i));
//            System.out.println(m.toString());
//        }
////        System.out.println(m.toString());
////        System.out.println(m.containsKey(new ExtendedInteger(1)));
////        System.out.println(m.containsKey(new ExtendedInteger(5)));
//        System.exit(0);

    }


    public void generate(Player leader, Solver solver) {
        debugOutput.println(solver.getInfo());
        debugOutput.println(gameConfig.getInfo());
        threadBean = ManagementFactory.getThreadMXBean();

        long start = threadBean.getCurrentThreadCpuTime();
        long overallSequenceGeneration = 0;
        long overallCPLEX = 0;
        //Map<Player, Map<Sequence, Double>> realizationPlans = new HashMap<>();
        long startGeneration = threadBean.getCurrentThreadCpuTime();


        if (!(solver instanceof LeaderGeneration2pLessMemSefceLP)) {
            generateCompleteGame();
            System.out.println("Game tree built...");
            System.out.println("Information set count: " + algConfig.getAllInformationSets().size());
            overallSequenceGeneration = (threadBean.getCurrentThreadCpuTime() - startGeneration) / 1000000l;

            //Player[] actingPlayers = new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]};
            //System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
            System.out.println("final size [sequences] : ");
            for (Player p : rootState.getAllPlayers()) {
                if (p.getName() == "Nature") continue;
                System.out.printf("\t%d.player sequences: %d \n", p.getId() + 1, algConfig.getSequencesFor(p).size());
            }
        }
//        else algConfig.addStateToSequenceForm(rootState);

        long startCPLEX = threadBean.getCurrentThreadCpuTime();

        solver.calculateLeaderStrategies(algConfig, expander);

        long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX) / 1000000l;

        overallCPLEX += thisCPLEX;

        if (PRINT_STRATEGY && solver.getResultStrategiesForPlayer(leader)!=null){
            for (Sequence seq : solver.getResultStrategiesForPlayer(leader).keySet())
                if (solver.getResultStrategiesForPlayer(leader).get(seq) > 0.00001)
                    System.out.println(seq  + " : " + solver.getResultStrategiesForPlayer(leader).get(seq));
        }


//        for (Player player : rootState.getAllPlayers()) {
//            realizationPlans.put(player, solver.getResultStrategiesForPlayer(player));
//        }

//        System.out.println("done.");
        finalTime = (threadBean.getCurrentThreadCpuTime() - start) / 1000000l;

        //int[] support_size = new int[]{0, 0};

        try {
            Runtime.getRuntime().gc();
            Thread.sleep(500l);
        } catch (InterruptedException e) {
        }

        gameValue = solver.getResultForPlayer(leader);
//        System.out.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
//        System.out.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
        System.out.println("final result:" + gameValue);
        System.out.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        System.out.println("final time: " + finalTime);
        System.out.println("final CPLEX time: " + overallCPLEX);
        System.out.println("final CPLEX building time: " + solver.getOverallConstraintGenerationTime() / 1000000l);
        System.out.println("final CPLEX solving time: " + solver.getOverallConstraintLPSolvingTime() / 1000000l);
//        System.out.println("final BR time: " + 0);
//        System.out.println("final RGB time: " + 0);
        System.out.println("final StrategyGenerating time: " + overallSequenceGeneration);
        System.out.println("final IS count: " + algConfig.getAllInformationSets().size());
        if (solver instanceof LeaderGeneration2pLessMemSefceLP){
            System.out.println("final deviation finding time: " + ((LeaderGeneration2pLessMemSefceLP)solver).getDeviationIdentificationTime()/ 1000000l);
            System.out.println("final RG generation time: " + ((LeaderGeneration2pLessMemSefceLP)solver).getRestrictedGameGenerationTime()/ 1000000l);
        }

        if (solver instanceof CompleteTwoPlayerSefceLP)
            restrictedGameRatio = ((CompleteTwoPlayerSefceLP)solver).getRestrictedGameRatio();
        else {
            restrictedGameRatio = 1.0;
        }
        if (solver instanceof LeaderTLSimulataneousSefceLP)
            restrictedGameRatio = ((LeaderTLSimulataneousSefceLP) solver).getRestrictedGameRatio();

        System.out.println("final RG ratio: "+restrictedGameRatio);

//        if (DEBUG) {
//            // sanity check -> calculation of Full BR on the solution of SQF LP
//            SQFBestResponseAlgorithm brAlg = new SQFBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameConfig);
//            System.out.println("BR: " + brAlg.calculateBR(rootState, realizationPlans.get(actingPlayers[1])));
//
//            SQFBestResponseAlgorithm brAlg2 = new SQFBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameConfig);
//            System.out.println("BR: " + brAlg2.calculateBR(rootState, realizationPlans.get(actingPlayers[0])));
//
//            algConfig.validateGameStructure(rootState, expander);
//        }
        //return realizationPlans;
    }

    public double getRestrictedGameRatio(){
        return restrictedGameRatio;
    }


    public void generateCompleteGame() {
        LinkedList<GameState> queue = new LinkedList<>();

        queue.add(rootState);

        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
//            System.out.println(currentState.toString());

            algConfig.addStateToSequenceForm(currentState);
            if (currentState.isGameEnd()) {
                final double[] utilities = currentState.getUtilities();
                Double[] u = new Double[utilities.length];

                for (Player p : currentState.getAllPlayers()){
                	if(utilities.length > p.getId())
                		u[p.getId()] = utilities[p.getId()] * currentState.getNatureProbability()*gameConfig.getUtilityStabilizer();
                }
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


    protected HashMap<Integer, HashMap<Integer, Integer>> calculateNumberOfSubgamesWithLeaderStateOnTop(int LEADER){
        HashMap<GameState, HashSet<ISKey>> subGames = new HashMap<>();
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(rootState);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
//            System.out.println(currentState.toString());

            if (currentState.isGameEnd()) {
                continue;
            }

            if (currentState.getPlayerToMove().equals(gameConfig.getAllPlayers()[LEADER])) {
                subGames.put(currentState, getNumberOfISsOusideSubgameFor(currentState, LEADER));
            }

            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }

        System.out.println("SG size: " + subGames.size());

        HashMap<Integer, HashMap<Integer, Integer>> issOutsideSubgame = new HashMap<>();
        for (GameState state : subGames.keySet()){
            if (!issOutsideSubgame.containsKey(state.getSequenceForPlayerToMove().size()))
                issOutsideSubgame.put(state.getSequenceForPlayerToMove().size(), new HashMap<>());
            if(!issOutsideSubgame.get(state.getSequenceForPlayerToMove().size()).containsKey(subGames.get(state).size()))
                issOutsideSubgame.get(state.getSequenceForPlayerToMove().size()).put(subGames.get(state).size(),0);
            int currentValue = issOutsideSubgame.get(state.getSequenceForPlayerToMove().size()).get(subGames.get(state).size());
            issOutsideSubgame.get(state.getSequenceForPlayerToMove().size()).put(subGames.get(state).size(), currentValue+1);
        }
        for (Integer depth : issOutsideSubgame.keySet())
            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutsideSubgame.get(depth).toString());
        return issOutsideSubgame;
    }
    protected HashSet<ISKey> getNumberOfISsOusideSubgameFor(SequenceInformationSet set, int LEADER){
        HashSet<ISKey> followerISsOutsideSubgame = new HashSet<>();
        LinkedList<GameState> queue = new LinkedList<>();
        for (GameState state : set.getAllStates()) {
            queue.add(state);
            while (queue.size() > 0) {
                GameState currentState = queue.removeFirst();
//            System.out.println(currentState.toString());

                if (currentState.isGameEnd()) {
                    continue;
                }

                if (currentState.getPlayerToMove().equals(gameConfig.getOpponent(gameConfig.getAllPlayers()[LEADER]))) {
                    for (GameState isState : algConfig.getInformationSetFor(currentState).getAllStates())
                        if (!state.getSequenceForPlayerToMove().isPrefixOf(isState.getSequenceFor(gameConfig.getAllPlayers()[LEADER]))) {
                            followerISsOutsideSubgame.add(currentState.getISKeyForPlayerToMove());
                            break;
                        }
                }

                for (Action action : expander.getActions(currentState)) {
                    queue.add(currentState.performAction(action));
                }
            }
        }
        return followerISsOutsideSubgame;
    }

    public HashMap<Integer, HashMap<Integer, Integer>> getIssOutsideSubGame(int LEADER){
        return  calculateNumberOfSubgamesWithLeaderStateOnTop(LEADER);
    }

    protected HashSet<ISKey> getNumberOfISsOusideSubgameFor(GameState state, int LEADER){
        HashSet<ISKey> followerISsOutsideSubgame = new HashSet<>();
        LinkedList<GameState> queue = new LinkedList<>();
        queue.add(state);
        while (queue.size() > 0) {
            GameState currentState = queue.removeFirst();
//            System.out.println(currentState.toString());

            if (currentState.isGameEnd()) {
                continue;
            }

            if (currentState.getPlayerToMove().equals(gameConfig.getOpponent(gameConfig.getAllPlayers()[LEADER]))) {
                for (GameState isState : algConfig.getInformationSetFor(currentState).getAllStates())
                    if (!state.getSequenceForPlayerToMove().isPrefixOf(isState.getSequenceFor(gameConfig.getAllPlayers()[LEADER]))){
                        followerISsOutsideSubgame.add(currentState.getISKeyForPlayerToMove());
                        break;
                    }
            }

            for (Action action : expander.getActions(currentState)) {
                queue.add(currentState.performAction(action));
            }
        }
        return followerISsOutsideSubgame;
    }

    protected HashMap<Integer, HashMap<Integer, Integer>> calculateNumberOfSubgamesWithFollowerISOnTop(int LEADER){
        HashMap<SequenceInformationSet, HashSet<ISKey>> subGames = new HashMap<>();
        for (SequenceInformationSet set : algConfig.getAllInformationSets().values())
            if (set.getPlayer().equals(gameConfig.getOpponent(gameConfig.getAllPlayers()[LEADER])))


        System.out.println("SG size: " + subGames.size());

        HashMap<Integer, HashMap<Integer, Integer>> issOutsideSubgame = new HashMap<>();
        for (SequenceInformationSet state : subGames.keySet()){
            if (!issOutsideSubgame.containsKey(state.getPlayersHistory().size()))
                issOutsideSubgame.put(state.getPlayersHistory().size(), new HashMap<>());
            if(!issOutsideSubgame.get(state.getPlayersHistory().size()).containsKey(subGames.get(state).size()))
                issOutsideSubgame.get(state.getPlayersHistory().size()).put(subGames.get(state).size(),0);
            int currentValue = issOutsideSubgame.get(state.getPlayersHistory().size()).get(subGames.get(state).size());
            issOutsideSubgame.get(state.getPlayersHistory().size()).put(subGames.get(state).size(), currentValue+1);
        }
        for (Integer depth : issOutsideSubgame.keySet())
            System.out.println("Depth = " + depth + ", [#IS outside = #roots]: " + issOutsideSubgame.get(depth).toString());
        return issOutsideSubgame;
    }
}
