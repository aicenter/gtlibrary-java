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


package cz.agents.gtlibrary.algorithms.runner;

import cz.agents.gtlibrary.algorithms.mcts.oos.SMOOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.SMMCTSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Exp3BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMRMBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.domain.tron.TronExpander;
import cz.agents.gtlibrary.domain.tron.TronGameInfo;
import cz.agents.gtlibrary.domain.tron.TronGameState;
import cz.agents.gtlibrary.iinodes.RandomAlgorithm;
import cz.agents.gtlibrary.iinodes.SimultaneousGameState;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.nfg.simalphabeta.ComparatorAlgorithm;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABConfig;
import cz.agents.gtlibrary.nfg.simalphabeta.SimABInformationSet;
import cz.agents.gtlibrary.nfg.simalphabeta.SimAlphaBetaAlgorithm;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.ArrayDeque;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class SMJournalOnlineExperiments {

    static GameInfo gameInfo;
    static GameState rootState;
    static SequenceFormConfig<SequenceInformationSet> sfAlgConfig;
    static Expander expander;
    static Random rnd = new HighQualityRandom();
    static int compTime = 500;
    static boolean printDebugInfo = true;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: SMJournalOnlineExperiments {BI|BIAB|DO|DOAB|DOSAB|OOS|MCTS-UCT|MCTS-EXP3|MCTS-RM|RAND|COMP} {BI|BIAB|DO|DOAB|DOSAB|OOS|MCTS-UCT|MCTS-EXP3|MCTS-RM|RAND|COMP} {GS|PE|RG|Tron} [domain parameters].");
            System.exit(-1);
        }
        SMJournalOnlineExperiments exp = new SMJournalOnlineExperiments();
        exp.handleDomain(args);

        String newCompTimeString = System.getProperty("COMPTIME");
        if (newCompTimeString != null)
            compTime = new Integer(newCompTimeString);


        double sum = 0;
        int iterationCount = 1000;

        String newMatchesString = System.getProperty("MATCHES");
        if (newMatchesString != null)
            iterationCount = new Integer(newMatchesString);

        for (int i = 0; i < iterationCount; i++) {
            sum += exp.runMatch(args);
            System.out.println("avg: " + sum / (i + 1));
        }
        System.out.println("Overall avg: " + sum / iterationCount);
    }


    public void handleDomain(String[] args) {
        if (args[2].equalsIgnoreCase("GS")) {  // Goofspiel
            if (args.length != 7) {
                throw new IllegalArgumentException("Illegal domain arguments count: 4 parameters are required {SEED} {DEPTH} {BIN_UTIL} {FIXED_CARDS}");
            }
            GSGameInfo.seed = new Integer(args[3]);
            int depth = new Integer(args[4]);
            GSGameInfo.depth = depth;

            boolean binUtil = new Boolean(args[5]);
            GSGameInfo.BINARY_UTILITIES = binUtil;

            boolean fixedCards = new Boolean(args[6]);
            GSGameInfo.useFixedNatureSequence = fixedCards;

            GSGameInfo.regenerateCards = true;

            //GSGameInfo.useFixedNatureSequence = false;
        } else if (args[2].equalsIgnoreCase("OZ")) { // Oshi Zumo
            if (args.length != 8) {
                throw new IllegalArgumentException("Illegal domain arguments count: 5 parameters are required {SEED} {COINS} {LOC_K} {MIN_BID} {BIN_UTIL}");
            }
            OZGameInfo.seed = new Integer(args[3]);
            OZGameInfo.startingCoins = new Integer(args[4]);
            OZGameInfo.locK = new Integer(args[5]);
            OZGameInfo.minBid = new Integer(args[6]);
            
            boolean binUtil = new Boolean(args[7]);
            OZGameInfo.BINARY_UTILITIES = binUtil;
        } else if (args[2].equalsIgnoreCase("PE")) { // Pursuit evasion
            if (args.length != 6) {
                throw new IllegalArgumentException("Illegal pursuit evasion domain arguments count: 3 parameters are required {SEED} {DEPTH} {GRAPH}");
            }
            PursuitGameInfo.seed = new Integer(args[3]);
            PursuitGameInfo.depth = new Integer(args[4]);
            PursuitGameInfo.graphFile = args[5];
        } else if (args[2].equalsIgnoreCase("RG")) { // Random Games
            if (args.length != 9) {
                throw new IllegalArgumentException("Illegal random game domain arguments count. 7 are required {SEED} {DEPTH} {BF} {CENTER_MODIFICATION} {BINARY_UTILITY} {FIXED BF}");
            }
            RandomGameInfo.seed = new Integer(args[3]);
            RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
            RandomGameInfo.MAX_DEPTH = new Integer(args[4]);
            RandomGameInfo.MAX_BF = new Integer(args[5]);
            RandomGameInfo.MAX_CENTER_MODIFICATION = new Integer(args[6]);
            RandomGameInfo.BINARY_UTILITY = new Boolean(args[7]);
            RandomGameInfo.FIXED_SIZE_BF = new Boolean(args[8]);
        } else if (args[2].equalsIgnoreCase("Tron")) { // Tron
            if (args.length != 7) {
                throw new IllegalArgumentException("Illegal domain arguments count: 4 parameters are required {SEED} {BOARDTYPE} {ROWS} {COLUMNS}");
            }
            TronGameInfo.seed = new Integer(args[3]);
            TronGameInfo.BOARDTYPE = args[4].charAt(0);
            TronGameInfo.ROWS = new Integer(args[5]);
            TronGameInfo.COLS = new Integer(args[6]);
        } else throw new IllegalArgumentException("Illegal domain: " + args[2]);
    }

    public void loadGame(String domain) {
        Random random = new HighQualityRandom();

        if (domain.equals("GS")) {
            gameInfo = new GSGameInfo();
            rootState = new GoofSpielGameState();
            expander = new GoofSpielExpander<MCTSInformationSet>(new MCTSConfig(random));
        } else if (domain.equals("PE")) {
            gameInfo = new PursuitGameInfo();
            rootState = new PursuitGameState();
            expander = new PursuitExpander<MCTSInformationSet>(new MCTSConfig(random));
        } else if (domain.equals("OZ")) {
            gameInfo = new OZGameInfo();
            rootState = new OshiZumoGameState();
            expander = new OshiZumoExpander<MCTSInformationSet>(new MCTSConfig(random));
        } else if (domain.equals("RG")) {
            gameInfo = new RandomGameInfo();
            rootState = new SimRandomGameState();
            expander = new RandomGameExpander<MCTSInformationSet>(new MCTSConfig(random));
        } else if (domain.equals("Tron")) {
            gameInfo = new TronGameInfo();
            rootState = new TronGameState();
            expander = new TronExpander<MCTSInformationSet>(new MCTSConfig(random));
        }
    }

    public void loadSMGame(String domain) {
        loadGame(domain);
        if (domain.equals("GS")) {
            expander = new GoofSpielExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("PE")) {
            expander = new PursuitExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("OZ")) {
            expander = new OshiZumoExpander<SimABInformationSet>(new SimABConfig());
        } else if (domain.equals("RG")) {
            expander = new RandomGameExpander<SimABInformationSet>(new SimABConfig());
        }
    }

    public GamePlayingAlgorithm getPlayer(int posIndex, String alg, String domain) {
        int sampSimDepth = Integer.MAX_VALUE;

        String SSDString = System.getProperty("SIMDEPTH");
        if (SSDString != null)
            sampSimDepth = new Integer(SSDString);

        if (alg.startsWith("MCTS")) {

            loadGame(domain);
            expander.getAlgorithmConfig().createInformationSetFor(rootState);

            if (!alg.equals("MCTS-RM")) {
                BackPropFactory fact = null;
                Random random = ((MCTSConfig)expander.getAlgorithmConfig()).getRandom();

                switch (alg) {
                    case "MCTS-UCT":
                        String explorationString = System.getProperty("EXPL"+(posIndex+1));
                        Double exploration = 2*gameInfo.getMaxUtility();
                        if (explorationString != null) exploration = new Double(explorationString);
                        fact = new UCTBackPropFactory(exploration, random);
                        break;
                    case "MCTS-EXP3":
                        explorationString = System.getProperty("EXPL"+(posIndex+1));
                        exploration = 0.2d;
                        if (explorationString != null) exploration = new Double(explorationString);
                        fact = new Exp3BackPropFactory(-gameInfo.getMaxUtility(), gameInfo.getMaxUtility(), exploration, random);
                        break;
                    case "MCTS-DRM":
                        explorationString = System.getProperty("EXPL"+(posIndex+1));
                        exploration = 0.1d;
                        if (explorationString != null) exploration = new Double(explorationString);
                        fact = new RMBackPropFactory(-1, 1, exploration, random);
                        break;
                }
                ISMCTSAlgorithm player = new ISMCTSAlgorithm(
                        rootState.getAllPlayers()[posIndex],
                        new DefaultSimulator(sampSimDepth, expander, random),
                        fact,
                        rootState, expander);
                player.returnMeanValue = false;
                player.runIterations(2);
                return player;
            } else {
                String explorationString = System.getProperty("EXPL"+(posIndex+1));
                Double exploration = 0.1;
                if (explorationString != null) exploration = new Double(explorationString);
                Random random = ((MCTSConfig)expander.getAlgorithmConfig()).getRandom();
                SMMCTSAlgorithm player = new SMMCTSAlgorithm(
                        rootState.getAllPlayers()[posIndex],
                        new DefaultSimulator(sampSimDepth,expander, random),
                        new SMRMBackPropFactory(exploration, random),
                        rootState, expander);

                player.runIterations(2);
                return player;
            }
        } else if (alg.equals("OOS")) {
            String explorationString = System.getProperty("EXPL"+(posIndex+1));
            Double exploration = 0.6;
            if (explorationString != null) exploration = new Double(explorationString);
            loadGame(domain);
            expander.getAlgorithmConfig().createInformationSetFor(rootState);
            Random random = ((MCTSConfig)expander.getAlgorithmConfig()).getRandom();
            GamePlayingAlgorithm player = new SMOOSAlgorithm(rootState.getAllPlayers()[posIndex], new OOSSimulator(sampSimDepth, expander, random), rootState, expander, exploration, random);

            player.runMiliseconds(20);
            return player;
        } else if (alg.contains("BI") || alg.contains("DO")) { // backward induction algorithms
            loadSMGame(domain);
            boolean AB = alg.endsWith("AB");
            boolean DO = alg.contains("DO");
            boolean SORT = alg.contains("DOS");
            boolean CACHE = alg.startsWith("CDO");
            if (!DO && (SORT || CACHE)) {
                throw new IllegalArgumentException("Illegal Argument Combination for Algorithm");
            }
            SimAlphaBetaAlgorithm player = new SimAlphaBetaAlgorithm(rootState.getAllPlayers()[posIndex], expander, gameInfo, AB, DO, SORT, CACHE);
            return player;
        } else if (alg.equals("COMP")) {
            loadSMGame(domain);
            return new ComparatorAlgorithm(rootState.getAllPlayers()[posIndex], expander, gameInfo, true, true, false, false);
        } else if (alg.equals("RAND")) {
            loadSMGame(domain);
            return new RandomAlgorithm(rootState.getAllPlayers()[posIndex], expander);
        } else {
            throw new UnsupportedOperationException("Unknown algorithms.");
        }
    }

    public double runMatch(String[] args) {
        StringBuilder moves = new StringBuilder();
        GamePlayingAlgorithm p1 = getPlayer(0, args[0], args[2]);
        GamePlayingAlgorithm p2 = getPlayer(1, args[1], args[2]);

        SimultaneousGameState curState = (SimultaneousGameState)rootState.copy();
        while (!curState.isActualGameEnd()) {
            if (printDebugInfo) {
                System.out.println("");
                System.out.println(curState);
            }

            if (curState.isPlayerToMoveNature()) {
                double r = rnd.nextDouble();
                for (Action ca : (List<Action>) expander.getActions(curState)) {
                    final double ap = curState.getProbabilityOfNatureFor(ca);
                    if (r <= ap) {
                        moves.append(ca + " ");
                        curState = (SimultaneousGameState)curState.performAction(ca);

                        if (printDebugInfo)
                            System.out.println("Nature chose: " + ca);

                        break;
                    }
                    r -= ap;
                }
            } else {
                if (printDebugInfo)
                    System.out.println("Current State Eval Value: " + curState.evaluate()[0]);

                if (printDebugInfo)
                    System.out.println("Searching player 1...");
                Action a1 = p1.runMiliseconds(compTime, curState.copy());
                if (printDebugInfo)
                    System.out.println("P1 chose: " + a1);

                if (printDebugInfo)
                    System.out.println("Searching player 2...");
                Action a2 = p2.runMiliseconds(compTime, curState.copy());
                if (printDebugInfo)
                    System.out.println("P2 chose: " + a2);

                moves.append(a1 + " ");
                moves.append(a2 + " ");
                curState = (SimultaneousGameState)curState.performAction(a1);
                curState = (SimultaneousGameState)curState.performAction(a2);
            }
        }
        System.out.println("MATCH: " + moves.toString() + curState.getUtilities()[0]);
        return curState.getUtilities()[0];
    }
}
