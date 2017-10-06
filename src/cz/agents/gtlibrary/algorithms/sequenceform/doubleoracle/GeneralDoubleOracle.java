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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import cz.agents.gtlibrary.algorithms.flipit.bestresponse.FlipItBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.flipit.iskeys.FlipItPerfectRecallISKey;
import cz.agents.gtlibrary.domain.aceofspades.AoSExpander;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameInfo;
import cz.agents.gtlibrary.domain.aceofspades.AoSGameState;
import cz.agents.gtlibrary.domain.artificialchance.ACExpander;
import cz.agents.gtlibrary.domain.artificialchance.ACGameInfo;
import cz.agents.gtlibrary.domain.artificialchance.ACGameState;
import cz.agents.gtlibrary.domain.bpg.BPGExpander;
import cz.agents.gtlibrary.domain.bpg.BPGGameInfo;
import cz.agents.gtlibrary.domain.bpg.BPGGameState;
import cz.agents.gtlibrary.domain.flipit.*;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotExpander;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotGameInfo;
import cz.agents.gtlibrary.domain.honeypotGame.HoneypotGameState;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceExpander;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.oshizumo.IIOshiZumoGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.domain.poker.kuhn.KPGameInfo;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerExpander;
import cz.agents.gtlibrary.domain.poker.kuhn.KuhnPokerGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameState;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.iinodes.ArrayListSequenceImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.FixedSizeMap;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class GeneralDoubleOracle {
    private GameState rootState;
    private Expander<DoubleOracleInformationSet> expander;
    private GameInfo gameInfo;
    private DoubleOracleConfig<DoubleOracleInformationSet> algConfig;

    private PrintStream debugOutput = System.out;
    private long finishTime;

    private int iterations;

    final private double EPS = 0.00000001;
    final public static boolean DEBUG = false;
    final private static boolean MY_RP_BR_ORDERING = false;
    private ThreadMXBean threadBean;

    public double gameValue;

    public enum PlayerSelection {
        BOTH, SINGLE_ALTERNATING, SINGLE_IMPROVED
    }

    public static PlayerSelection playerSelection = PlayerSelection.BOTH;

    public static void main(String[] args) {
//		runAC();
//        runBP();
//        runGenericPoker();
//        runKuhnPoker();
//        runGoofSpiel();
//        runIIOshiZumo();
//        runRandomGame();
//		runSimRandomGame();
//                runLiarsDice();
//		runPursuit();
//        runPhantomTTT();
//		runAoS();
        runFlipIt(args);
//        runHoneyPot(args);
    }

    private static void runHoneyPot(String[] args) {
        HoneypotGameInfo gameInfo;
        if (args.length == 0) {
            gameInfo = new HoneypotGameInfo();
        }
        else {
            gameInfo = new HoneypotGameInfo(args[0]);
        }

        HoneypotGameState rootState = new HoneypotGameState(gameInfo.allNodes);

        DoubleOracleConfig<DoubleOracleInformationSet> algConfig;
        GeneralDoubleOracle doefg;
        Map<Player, Map<Sequence, Double>> init = null;
        int depth = gameInfo.attacksAllowed;

        if (HoneypotGameInfo.ENABLE_ITERATIVE_SOLVING) {
            depth = Math.max(depth / 2, 1);
        }

        for (int i = depth; i <= gameInfo.attacksAllowed; i++) {
            System.out.println("CURRENT DEPTH: " + i);
            rootState.setRemainingAttacks(i);
            algConfig = new DoubleOracleConfig<>(rootState, gameInfo);
            doefg = new GeneralDoubleOracle(rootState, new HoneypotExpander<>(algConfig), gameInfo, algConfig);
            init = doefg.generate(init);
            System.out.println();
        }

        ArrayList<Sequence> seqs = new ArrayList<>();//init.get(HoneypotGameInfo.DEFENDER).keySet());
        for (Sequence seq : init.get(HoneypotGameInfo.DEFENDER).keySet()) {
            boolean isprefix = false;
            for (Sequence seq2 : init.get(HoneypotGameInfo.DEFENDER).keySet()) {
                if (!seq.equals(seq2) && seq.isPrefixOf(seq2)){
                    isprefix = true;
                    break;
                }
            }
            if (!isprefix) seqs.add(seq);
        }

        Collections.sort(seqs, new Comparator<Sequence>() {
            @Override
            public int compare(Sequence o1, Sequence o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        double sum = 0.0;
        for (Sequence seq : seqs) {
            sum += init.get(HoneypotGameInfo.DEFENDER).get(seq);
            if (init.get(HoneypotGameInfo.DEFENDER).get(seq) > 0.0001)
                System.out.println(seq + " : " + init.get(HoneypotGameInfo.DEFENDER).get(seq));
        }
        if (sum < 0.9999) System.out.println("Sum does NOT correspond to distribution.");

    }


    private static void runFlipIt(String[] args){
        // args for flipit : depth::int graphSize::int
        FlipItGameInfo gameInfo;
        if (args.length == 0)
            gameInfo = new FlipItGameInfo();
        else {
            int depth = Integer.parseInt(args[0]);
            int graphSize = Integer.parseInt(args[1]);
            String graphFile = (graphSize == 3 ) ? "flipit_empty3.txt" : (graphSize == 4 ? "flipit_empty4.txt" : (graphSize == 5 ? "flipit_empty5.txt" : ""));
            gameInfo = new FlipItGameInfo(depth, 1, graphFile, 1);
            FlipItGameInfo.OUTPUT_STRATEGY = true;
            if (args.length > 2) {
                String version = args[2];
                switch (version) {
                    case "F":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.FULL;
                        break;
                    case "N":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.NO;
                        break;
                    case "NP":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_NODE_POINTS;
                        break;
                    case "AP":
                        FlipItGameInfo.gameVersion = FlipItGameInfo.FlipItInfo.REVEALED_ALL_POINTS;
                        break;//
                }
            }
        }
        int depth = FlipItGameInfo.depth;
        if (FlipItGameInfo.ENABLE_ITERATIVE_SOLVING) {
            FlipItGameInfo.depth = FlipItGameInfo.depth / 2;
        }
        gameInfo.ZERO_SUM_APPROX = true;

        GameState rootState = null;
        if (FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) gameInfo.calculateMinMaxBounds();

        switch (FlipItGameInfo.gameVersion){
            case NO:                    rootState = new NoInfoFlipItGameState(); break;
            case FULL:                  rootState = new FullInfoFlipItGameState(); break;
            case REVEALED_ALL_POINTS:   rootState = new AllPointsFlipItGameState(); break;
            case REVEALED_NODE_POINTS:  rootState = new NodePointsFlipItGameState(); break;

        }

        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new FlipItExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        Map<Player, Map<Sequence, Double>> rps = doefg.generate(null);

        if (FlipItGameInfo.ENABLE_ITERATIVE_SOLVING) {
            for (int i = FlipItGameInfo.depth + 1; i <= depth; i++) {
                FlipItGameInfo.depth = i;
                if (FlipItGameInfo.CALCULATE_UTILITY_BOUNDS) gameInfo.calculateMinMaxBounds();
                algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
                expander = new FlipItExpander<DoubleOracleInformationSet>(algConfig);
                doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
                rps = doefg.generate(rps);
            }
        }

        Map<InformationSet,Map<Action,Double>> behavioral = new HashMap<>();
        for (Sequence sequence : rps.get(FlipItGameInfo.DEFENDER).keySet()){
            if(sequence.isEmpty()) continue;
            if (!behavioral.containsKey(sequence.getLastInformationSet()))
                behavioral.put(sequence.getLastInformationSet(),new HashMap<Action,Double>());
            behavioral.get(sequence.getLastInformationSet())
                    .put(sequence.getLast(),rps.get(FlipItGameInfo.DEFENDER).get(sequence));
        }

        ArrayList<InformationSet> sets = new ArrayList<>();
        for(InformationSet set : behavioral.keySet()){
            double realization = 0.0;
            for(Action a : behavioral.get(set).keySet())
                realization += behavioral.get(set).get(a);
            if(realization > 0.000001) sets.add(set);
            else continue;
            for(Action a : behavioral.get(set).keySet())
                behavioral.get(set).replace(a, behavioral.get(set).get(a)/realization);
        }

        Collections.sort(sets, new Comparator<InformationSet>() {
            @Override
            public int compare(InformationSet o1, InformationSet o2) {
                Integer i1 = o1.getAllStates().iterator().next().getSequenceForPlayerToMove().size();
                Integer i2 = o2.getAllStates().iterator().next().getSequenceForPlayerToMove().size();
                return i1.compareTo(i2);
            }
        });

        if (FlipItGameInfo.OUTPUT_STRATEGY) {
            for (InformationSet set : sets) {
                GameState state = set.getAllStates().iterator().next();
                System.out.println(state.getSequenceFor(FlipItGameInfo.DEFENDER));
                if (set.getISKey() instanceof FlipItPerfectRecallISKey)
                    System.out.println(((FlipItPerfectRecallISKey)set.getISKey()).getObservation());
                System.out.println(state.getSequenceFor(FlipItGameInfo.ATTACKER));
                if (!state.getSequenceFor(FlipItGameInfo.ATTACKER).isEmpty() && state.getSequenceFor(FlipItGameInfo.ATTACKER).getLastInformationSet().getISKey()
                        instanceof FlipItPerfectRecallISKey)
                    System.out.println(((FlipItPerfectRecallISKey)state.getSequenceFor(FlipItGameInfo.ATTACKER)
                            .getLastInformationSet().getISKey()).getObservation());
                for (Action a : behavioral.get(set).keySet())
                    if (behavioral.get(set).get(a) > 0.00001)
                        System.out.printf("\t %s : %f\n", a, behavioral.get(set).get(a));
            }
        }



//        GambitEFG ggg = new GambitEFG();
//        ggg.write("flipit.gbt", rootState, (Expander) expander);


//        for (Map.Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[0]).entrySet()) {
//            if(entry.getValue() > doefg.EPS)
//                System.out.println(entry);
//        }
//        System.out.println("**********");
//        for (Map.Entry<Sequence, Double> entry : rps.get(rootState.getAllPlayers()[1]).entrySet()) {
//            if(entry.getValue() > doefg.EPS)
//                System.out.println(entry);
//        }
    }

    public static void runAC() {
        GameState rootState = new ACGameState();
        GameInfo gameInfo = new ACGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new ACExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runPhantomTTT() {
        GameState rootState = new TTTState();
        GameInfo gameInfo = new TTTInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new TTTExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
//        GeneralDoubleOracle.traverseCompleteGameTree(rootState, expander);
    }

    public static void runAoS() {
        GameState rootState = new AoSGameState();
        GameInfo gameInfo = new AoSGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new AoSExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }


    public static void runPursuit() {
        GameState rootState = new PursuitGameState();
        GameInfo gameInfo = new PursuitGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new PursuitExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runKuhnPoker() {
        GameState rootState = new KuhnPokerGameState();
        GameInfo gameInfo = new KPGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new KuhnPokerExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runRandomGame() {
        GameState rootState = new RandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);
//        Expander<DoubleOracleInformationSet> expander = new RandomGameExpanderWithMoveOrdering<DoubleOracleInformationSet>(algConfig, new int[] {1, 2, 0});
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
//        GambitEFG.write("randomgame.gbt", rootState, (Expander) expander);
    }

    public static void runSimRandomGame() {
        GameState rootState = new SimRandomGameState();
        GameInfo gameInfo = new RandomGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new RandomGameExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
//        GambitEFG ggg = new GambitEFG();
//        ggg.write("randomgame.gbt", rootState, (Expander) expander);
    }

    public static void runGenericPoker() {
        GameState rootState = new GenericPokerGameState();
        GameInfo gameInfo = new GPGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new GenericPokerExpander<DoubleOracleInformationSet>(algConfig);
//        Expander<DoubleOracleInformationSet> expander = new GenericPokerExpanderDomain<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
        System.out.println("number of ISs: "+algConfig.getAllInformationSets().size());
    }

    public static void runBP() {
        GameState rootState = new BPGGameState();
        GameInfo gameInfo = new BPGGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new BPGExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runGoofSpiel() {
        GSGameInfo.useFixedNatureSequence = true;
        GameState rootState = new IIGoofSpielGameState();
        GSGameInfo gameInfo = new GSGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new GoofSpielExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runIIOshiZumo() {
        GameState rootState = new IIOshiZumoGameState();
        GameInfo gameInfo = new OZGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander<DoubleOracleInformationSet> expander = new OshiZumoExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public static void runLiarsDice() {
        GameState rootState = new LiarsDiceGameState();
        LDGameInfo gameInfo = new LDGameInfo();
        DoubleOracleConfig<DoubleOracleInformationSet> algConfig = new DoubleOracleConfig<DoubleOracleInformationSet>(rootState, gameInfo);
        Expander expander = new LiarsDiceExpander<DoubleOracleInformationSet>(algConfig);
        GeneralDoubleOracle doefg = new GeneralDoubleOracle(rootState, expander, gameInfo, algConfig);
        doefg.generate(null);
    }

    public GeneralDoubleOracle(GameState rootState, Expander<DoubleOracleInformationSet> expander, GameInfo config, DoubleOracleConfig<DoubleOracleInformationSet> algConfig) {
        this.rootState = rootState;
        this.expander = expander;
        this.gameInfo = config;
        this.algConfig = algConfig;
    }

    public int getIterations(){ return  iterations;}

    public Map<Player, Map<Sequence, Double>> generate(Map<Player, Map<Sequence, Double>> initializationRG) {
        debugOutput.println("Double Oracle");
        debugOutput.println(gameInfo.getInfo());
        threadBean = ManagementFactory.getThreadMXBean();

        long start = threadBean.getCurrentThreadCpuTime();
        long systemStart = System.currentTimeMillis();
        long overallSequenceGeneration = 0;
        long overallBRCalculation = 0;
        long overallCPLEX = 0;
        long overallRGBuilding = 0;

        iterations = 0;

        Player[] actingPlayers = new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]};
        DoubleOracleBestResponse[] brAlgorithms;

        if (gameInfo instanceof FlipItGameInfo){
            brAlgorithms = new DoubleOracleBestResponse[]{
                    new FlipItBestResponseAlgorithm(expander, 0, actingPlayers, algConfig, gameInfo),
                    new FlipItBestResponseAlgorithm(expander, 1, actingPlayers, algConfig, gameInfo)};
        }
        else{
            brAlgorithms = new DoubleOracleBestResponse[]{
                    new DoubleOracleBestResponse(expander, 0, actingPlayers, algConfig, gameInfo),
                    new DoubleOracleBestResponse(expander, 1, actingPlayers, algConfig, gameInfo)};
        }
        Map<Player, Map<Sequence, Double>> realizationPlans = new FixedSizeMap<Player, Map<Sequence, Double>>(2);


        if (initializationRG == null || initializationRG.isEmpty()) {
            GameState firstState = findFirstNonNatureState(rootState, expander);

            algConfig.addStateToSequenceForm(firstState);

            // init realization plans -> for each player, an empty sequence has probability equal to 1
            realizationPlans.put(actingPlayers[0], new HashMap<Sequence, Double>());
            realizationPlans.put(actingPlayers[1], new HashMap<Sequence, Double>());
            realizationPlans.get(actingPlayers[0]).put(firstState.getSequenceFor(actingPlayers[0]), 1d);
            realizationPlans.get(actingPlayers[1]).put(firstState.getSequenceFor(actingPlayers[1]), 1d);

            algConfig.addFullBRSequences(actingPlayers[0], realizationPlans.get(actingPlayers[0]).keySet());
            algConfig.addFullBRSequences(actingPlayers[1], realizationPlans.get(actingPlayers[1]).keySet());
        } else {
            realizationPlans = initializationRG;
            Map<Player, Set<Sequence>> tmpMap = new HashMap<Player, Set<Sequence>>();
            tmpMap.put(actingPlayers[0], initializationRG.get(actingPlayers[0]).keySet());
            tmpMap.put(actingPlayers[1], initializationRG.get(actingPlayers[1]).keySet());
            algConfig.initializeRG(tmpMap, brAlgorithms, expander);
            for (Player p : actingPlayers) {
                Set<Sequence> shorter = new HashSet<Sequence>();
                for (Sequence s : initializationRG.get(p).keySet()) {
                    if (s.size() == 0) continue;
                    Sequence ss = s.getSubSequence(s.size() - 1);
                    shorter.add(ss);
                }
                for (Sequence s : initializationRG.get(p).keySet()) {
                    if (!shorter.contains(s))
                        algConfig.addFullBRSequence(p, s);
                }
            }
        }
        int currentPlayerIndex = 0;
        DoubleOracleLPSolver doRestrictedGameSolver = new DoubleOracleSequenceFormLP(actingPlayers);
//        DoubleOracleLPSolver doRestrictedGameSolver = new UndominatedSolver(actingPlayers);
//        DoubleOracleLPSolver doRestrictedGameSolver = new NFPSolver(actingPlayers, gameInfo);

        doRestrictedGameSolver.setDebugOutput(debugOutput);

        double p1BoundUtility = gameInfo.getMaxUtility();
        double p2BoundUtility = gameInfo.getMaxUtility();

        int[] oldSize = new int[]{-1, -1};
//        int[] diffSize = new int[] {-1, -1};
        double[] lastBRValue = new double[]{-1.0, -1.0};

//        boolean[] newSeqs = new boolean[] {true, true};

        mainloop:
        while ((Math.abs(p1BoundUtility + p2BoundUtility) > EPS) ||
                Math.abs(doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]) + doRestrictedGameSolver.getResultForPlayer(actingPlayers[1])) > EPS) {

            iterations++;
            debugOutput.println("Iteration " + iterations + ": Cumulative Time from Beginning:" + ((threadBean.getCurrentThreadCpuTime() - start) / 1000000l));
            debugOutput.println("Iteration " + iterations + ": System Cumulative Time:" + ((System.currentTimeMillis()) - systemStart));

//            diffSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex];

            debugOutput.println("Last difference: " + (algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]) - oldSize[currentPlayerIndex]));
            debugOutput.println("Current Size: " + algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]));
            oldSize[currentPlayerIndex] = algConfig.getSizeForPlayer(actingPlayers[currentPlayerIndex]);

//            if (diffSize[0] == 0 && diffSize[1] == 0) {
//                System.out.println("ERROR : NOT CONVERGED");
//                break;
//            }


            int opponentPlayerIndex = (currentPlayerIndex + 1) % 2;

            long startFullBR = threadBean.getCurrentThreadCpuTime();
            double currentBRVal;
            if (MY_RP_BR_ORDERING)
                currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]), realizationPlans.get(actingPlayers[currentPlayerIndex]));
            else
                currentBRVal = brAlgorithms[currentPlayerIndex].calculateBR(rootState, realizationPlans.get(actingPlayers[opponentPlayerIndex]));
            long thisBR = (threadBean.getCurrentThreadCpuTime() - startFullBR) / 1000000l;

            debugOutput.println("BR Value " + actingPlayers[currentPlayerIndex] + " : " + currentBRVal);
            debugOutput.println("Iteration " + iterations + " : full BR time : " + thisBR);
            overallBRCalculation += thisBR;

            lastBRValue[currentPlayerIndex] = currentBRVal;

            HashSet<Sequence> currentFullBRSequences = brAlgorithms[currentPlayerIndex].getFullBRSequences();
            HashSet<Sequence> newFullBRSequences = new HashSet<Sequence>();
            for (Sequence s : currentFullBRSequences) {
                if (!algConfig.getSequencesFor(actingPlayers[currentPlayerIndex]).contains(s)) {
                    newFullBRSequences.add(s);
                }
            }
            if (DEBUG) debugOutput.println("All BR Sequences: " + currentFullBRSequences);
            long startRGB = threadBean.getCurrentThreadCpuTime();
            if (newFullBRSequences.size() > 0) {
                if (DEBUG) debugOutput.println("New Full BR Sequences: " + newFullBRSequences);
                algConfig.createValidRestrictedGame(actingPlayers[currentPlayerIndex], newFullBRSequences, brAlgorithms, expander);
                algConfig.addFullBRSequences(actingPlayers[currentPlayerIndex], newFullBRSequences);
//                newSeqs[0] = true;
//                newSeqs[1] = true;
//            } else {
//                newSeqs[currentPlayerIndex] = false;
            }
            long thisRGB = (threadBean.getCurrentThreadCpuTime() - startRGB) / 1000000l;
            overallRGBuilding += thisRGB;

            if (currentPlayerIndex == 0) {
                p1BoundUtility = Math.min(p1BoundUtility, currentBRVal);
            } else {
                p2BoundUtility = Math.min(p2BoundUtility, currentBRVal);
            }

            debugOutput.println("Iteration " + iterations + ": Bounds Interval Size :" + (p1BoundUtility + p2BoundUtility));

            if (DEBUG) debugOutput.println(algConfig.getNewSequences());

            if (algConfig.getNewSequences().isEmpty()
                    && (Math.abs(p1BoundUtility + p2BoundUtility) > EPS)
                    && doRestrictedGameSolver.getNewSequencesSinceLastLPCalc(actingPlayers[0]).isEmpty()
                    && doRestrictedGameSolver.getNewSequencesSinceLastLPCalc(actingPlayers[1]).isEmpty()) {
                debugOutput.println("ERROR : NOT CONVERGED");
                System.exit(0);
                break;
            }

            switch (playerSelection) {
                case BOTH:
                    if (currentPlayerIndex != 0) {

                        long startCPLEX = threadBean.getCurrentThreadCpuTime();
                        doRestrictedGameSolver.calculateStrategyForPlayer(currentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                        doRestrictedGameSolver.calculateStrategyForPlayer(opponentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                        long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX) / 1000000l;

                        debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                        overallCPLEX += thisCPLEX;
                        debugOutput.println("LP Value " + actingPlayers[currentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]));
                        debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));
                        algConfig.clearNewSequences();
                    }
                    currentPlayerIndex = opponentPlayerIndex;
                    break;
                case SINGLE_ALTERNATING:
                    long startCPLEX = threadBean.getCurrentThreadCpuTime();
                    doRestrictedGameSolver.calculateStrategyForPlayer(currentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                    long thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX) / 1000000l;

                    debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                    overallCPLEX += thisCPLEX;
                    debugOutput.println("LP Value " + actingPlayers[currentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]));
                    debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));

                    currentPlayerIndex = opponentPlayerIndex;

                    algConfig.clearNewSequences();
                    break;

                case SINGLE_IMPROVED:
                    if (doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]) == null ||
                            doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]) == null) { // we have not calculated the reward for the current player in RG yet
                        currentPlayerIndex = opponentPlayerIndex;
                    } else {
                        double oldLPResult = doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]);
//                    double oldLPResult1 = doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]);
//                    double oldLPResult2 = doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]);
                        if (currentPlayerIndex == 0) {
                            if (newFullBRSequences.size() > 0 && Math.abs(p1BoundUtility - (oldLPResult)) > Math.abs(p2BoundUtility - (-oldLPResult))) {
//                        if (Math.abs(currentBRVal - (oldLPResult)) > EPS) {
//                        if (Math.abs(lastBRValue[0] - (oldLPResult2)) - EPS > Math.abs(lastBRValue[1] - (oldLPResult1))) {
                                currentPlayerIndex = 0;
                            } else {
                                currentPlayerIndex = 1;
                            }
                        } else {
                            if (newFullBRSequences.size() == 0 || Math.abs(p1BoundUtility - (-oldLPResult)) >= Math.abs(p2BoundUtility - (oldLPResult))) {
//                        if (Math.abs(currentBRVal - (oldLPResult)) > EPS) {
//                        if (Math.abs(lastBRValue[0] - (-oldLPResult2)) >= Math.abs(lastBRValue[1] - (-oldLPResult1)) - EPS ) {
                                currentPlayerIndex = 0;
                            } else {
                                currentPlayerIndex = 1;
                            }
                        }

                    }

                    opponentPlayerIndex = (1 + currentPlayerIndex) % 2;

                    startCPLEX = threadBean.getCurrentThreadCpuTime();
                    doRestrictedGameSolver.calculateStrategyForPlayer(opponentPlayerIndex, rootState, algConfig, (p1BoundUtility + p2BoundUtility));
                    thisCPLEX = (threadBean.getCurrentThreadCpuTime() - startCPLEX) / 1000000l;

                    debugOutput.println("Iteration " + iterations + " : CPLEX time : " + thisCPLEX);
                    overallCPLEX += thisCPLEX;
                    debugOutput.println("LP Value " + actingPlayers[currentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[currentPlayerIndex]));
                    debugOutput.println("LP Value " + actingPlayers[opponentPlayerIndex] + " : " + doRestrictedGameSolver.getResultForPlayer(actingPlayers[opponentPlayerIndex]));

                    algConfig.clearNewSequences();

                    break;
                default:
                    assert false;
                    break;
            }
            opponentPlayerIndex = (1 + currentPlayerIndex) % 2;

            Map<Sequence, Double> tmp = doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[currentPlayerIndex]);
            realizationPlans.put(actingPlayers[currentPlayerIndex], tmp);
            Map<Sequence, Double> tmp2 = doRestrictedGameSolver.getResultStrategiesForPlayer(actingPlayers[opponentPlayerIndex]);
            realizationPlans.put(actingPlayers[opponentPlayerIndex], tmp2);

            if (DEBUG)
                for (Player player : actingPlayers) {
                    for (Sequence sequence : realizationPlans.get(player).keySet()) {
                        if (realizationPlans.get(player).get(sequence) > 0) {
                            if (DEBUG)
                                debugOutput.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
                        }
                    }
                }

            if (DEBUG)
                algConfig.validateRestrictedGameStructure(expander, brAlgorithms);

//            if (!playerSelection.equals(PlayerSelection.BOTH) && !newSeqs[0] && !newSeqs[1]) {
//                System.out.println("ERROR : NOT CONVERGED");
//                break;
//            }
//            break;
        }


        debugOutput.println("done.");
        finishTime = (threadBean.getCurrentThreadCpuTime() - start) / 1000000l;

        doRestrictedGameSolver.calculateStrategyForPlayer(1, rootState, algConfig, (p1BoundUtility + p2BoundUtility));

        int[] support_size = new int[]{0, 0};
//        int[] maxIt = new int[] { 0, 0 };
        for (Player player : actingPlayers) {
            for (Sequence sequence : realizationPlans.get(player).keySet()) {
                if (realizationPlans.get(player).get(sequence) > 0) {
                    support_size[player.getId()]++;
//                    maxIt[player.getId()] = Math.max(maxIt[player.getId()], algConfig.getIterationForSequence(sequence));
//                    if (DEBUG)
//                        System.out.println(sequence + "\t:\t" + realizationPlans.get(player).get(sequence));
                }
            }
        }

//        try {
//            Runtime.getRuntime().gc();
//            Thread.currentThread().sleep(500l);
//        } catch (InterruptedException e) {
//        }

        debugOutput.println("final size: FirstPlayer Sequences: " + algConfig.getSequencesFor(actingPlayers[0]).size() + " \t SecondPlayer Sequences : " + algConfig.getSequencesFor(actingPlayers[1]).size());
        debugOutput.println("final support_size: FirstPlayer: " + support_size[0] + " \t SecondPlayer: " + support_size[1]);
        debugOutput.println("final support_percent: FirstPlayer: " + 100*((double)support_size[0])/algConfig.getSequencesFor(actingPlayers[0]).size() + "% \t SecondPlayer: " + 100*((double)support_size[1])/algConfig.getSequencesFor(actingPlayers[1]).size()+"%");
        debugOutput.println("final result:" + doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]));
        debugOutput.println("final memory:" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
        debugOutput.println("final time: " + finishTime);
        debugOutput.println("final number of iterations: " + iterations);
        debugOutput.println("final CPLEX time: " + overallCPLEX);
        debugOutput.println("final BR time: " + overallBRCalculation);
        debugOutput.println("final RGB time: " + overallRGBuilding);
        debugOutput.println("final StrategyGenerating time: " + overallSequenceGeneration);
//        debugOutput.println("last support sequence iteration: PL1: " + maxIt[0] + " \t PL2: " + maxIt[1]);
        debugOutput.println("LP GenerationTime:" + doRestrictedGameSolver.getOverallGenerationTime());
        debugOutput.println("LP Constraint GenerationTime:" + doRestrictedGameSolver.getOverallConstraintGenerationTime());
        debugOutput.println("LP ComputationTime:" + doRestrictedGameSolver.getOverallConstraintLPSolvingTime());

        gameValue = doRestrictedGameSolver.getResultForPlayer(actingPlayers[0]);
        return realizationPlans;
    }

    public double getGameValue() {
        return gameValue;
    }

    public GameState findFirstNonNatureState(GameState rootState, Expander<DoubleOracleInformationSet> expander) {
        GameState tmpState = rootState.copy();

        while (tmpState.isPlayerToMoveNature()) {
            Action action = expander.getActions(tmpState).get(0);
            tmpState = tmpState.performAction(action);
        }

        return tmpState;
    }

    public static void traverseCompleteGameTree(GameState rootState, Expander<DoubleOracleInformationSet> expander) {
        System.out.println("Calculating the size of the game.");
        LinkedList<GameState> queue = new LinkedList<GameState>();
        long nodes = 0;
        queue.add(rootState);

        while (queue.size() > 0) {
            nodes++;
            GameState currentState = queue.removeFirst();

            if (currentState.isGameEnd()) {
                continue;
            }
            for (Action action : expander.getActions(currentState)) {
                GameState newState = currentState.performAction(action);

                queue.addFirst(newState);
                currentState.performAction(action);
            }
        }

        System.out.println("Nodes: " + nodes);
    }

    public void setDebugOutput(PrintStream debugOutput) {
        this.debugOutput = debugOutput;
    }

    public long getFinishTime() {
        return finishTime;
    }
}
