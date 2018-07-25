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


package cz.agents.gtlibrary.algorithms.ocr;

import cz.agents.gtlibrary.algorithms.mcts.ISMCTSExploitability;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.goofspiel.GoofSpielExpander;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceExpander;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.oshizumo.OZGameInfo;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoExpander;
import cz.agents.gtlibrary.domain.oshizumo.OshiZumoGameState;
import cz.agents.gtlibrary.domain.pursuit.PursuitExpander;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameInfo;
import cz.agents.gtlibrary.domain.pursuit.PursuitGameState;
import cz.agents.gtlibrary.domain.randomgame.RandomGameExpander;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.randomgame.SimRandomGameState;
import cz.agents.gtlibrary.domain.rps.RPSExpander;
import cz.agents.gtlibrary.domain.rps.RPSGameInfo;
import cz.agents.gtlibrary.domain.rps.RPSGameState;
import cz.agents.gtlibrary.domain.tron.TronExpander;
import cz.agents.gtlibrary.domain.tron.TronGameInfo;
import cz.agents.gtlibrary.domain.tron.TronGameState;
import cz.agents.gtlibrary.iinodes.ConfigImpl;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.ArrayDeque;

public class OCR_CFV_Experiments {

    static GameInfo gameInfo;
    static GameState rootState;
    static SQFBestResponseAlgorithm brAlg0;
    static SQFBestResponseAlgorithm brAlg1;
    static Expander expander;

    static String trackCFVinInformationSet = "IS:(Pl0):Pl0: []";
    static GamePlayingAlgorithm alg;
    static Double minExploitability = 0.1;
    static Integer numItersPerLoop = 10000;

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Missing Arguments: OnlineContinualResolvingExperiments " +
                    "{OOS|MCCFR|OCR} {LD|GS|OZ|PE|RG|RPS|Tron} [domain parameters].");
            System.exit(-1);
        }

        String alg = args[0];
        String domain = args[1];

        OCR_CFV_Experiments exp = new OCR_CFV_Experiments();
        exp.handleDomain(args);
        exp.loadGame(domain);
        exp.runAlgorithm(alg);
    }

    private void handleDomain(String[] args) {
        switch (args[1]) {
            case "IIGS": // Goofspiel
                if (args.length != 6) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "4 parameters are required {SEED} {DEPTH} {BIN_UTIL} {FIXED_CARDS}");
                }
                GSGameInfo.seed = new Integer(args[2]);
                GSGameInfo.depth = new Integer(args[3]);
                GSGameInfo.BINARY_UTILITIES = new Boolean(args[4]);;
                GSGameInfo.useFixedNatureSequence = new Boolean(args[5]);;

                GSGameInfo.regenerateCards = true;

                break;
            case "LD":   // Liar's dice
                if (args.length != 5) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "3 parameters are required {P1DICE} {P2DICE} {FACES}");
                }
                LDGameInfo.P1DICE = new Integer(args[2]);
                LDGameInfo.P2DICE = new Integer(args[3]);
                LDGameInfo.FACES = new Integer(args[4]);
                break;
            case "OZ":  // Oshi Zumo
                if (args.length != 7) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "5 parameters are required {SEED} {COINS} {LOC_K} {MIN_BID} {BIN_UTIL}");
                }
                OZGameInfo.seed = new Integer(args[2]);
                OZGameInfo.startingCoins = new Integer(args[3]);
                OZGameInfo.locK = new Integer(args[4]);
                OZGameInfo.minBid = new Integer(args[5]);
                OZGameInfo.BINARY_UTILITIES = new Boolean(args[6]);
                break;
            case "PE":  // Pursuit Evasion Game
                if (args.length != 5) {
                    throw new IllegalArgumentException("Illegal PEG domain arguments count: " +
                            "3 parameters are required {SEED} {DEPTH} {GRAPH}");
                }
                PursuitGameInfo.seed = new Integer(args[2]);
                PursuitGameInfo.depth = new Integer(args[3]);
                PursuitGameInfo.graphFile = args[4];
                break;
            case "RG":  // Random Games
                if (args.length != 8) {
                    throw new IllegalArgumentException("Illegal random game domain arguments count. " +
                            "7 are required {SEED} {DEPTH} {BF} {CENTER_MODIFICATION} {BINARY_UTILITY} {FIXED BF}");
                }
                RandomGameInfo.seed = new Integer(args[2]);
                RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
                RandomGameInfo.MAX_DEPTH = new Integer(args[3]);
                RandomGameInfo.MAX_BF = new Integer(args[4]);
                RandomGameInfo.MAX_CENTER_MODIFICATION = new Integer(args[5]);
                RandomGameInfo.BINARY_UTILITY = new Boolean(args[6]);
                RandomGameInfo.FIXED_SIZE_BF = new Boolean(args[7]);
                break;
            case "Tron":  // Tron
                if (args.length != 6) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "4 parameters are required {SEED} {BOARDTYPE} {ROWS} {COLUMNS}");
                }
                TronGameInfo.seed = new Integer(args[2]);
                TronGameInfo.BOARDTYPE = args[3].charAt(0);
                TronGameInfo.ROWS = new Integer(args[4]);
                TronGameInfo.COLS = new Integer(args[5]);
                break;
            case "RPS":  // Tron
                if (args.length != 3) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "1 parameter is required {SEED}");
                }
                RPSGameInfo.seed = new Integer(args[2]);
                break;
            default:
                throw new IllegalArgumentException("Illegal domain: " + args[1]);
        }
    }

    private void loadGame(String domain) {
        switch (domain) {
            case "IIGS":
                gameInfo = new GSGameInfo();
                rootState = new IIGoofSpielGameState();
                expander = new GoofSpielExpander<>(new MCTSConfig());
                break;
            case "LD":
                gameInfo = new LDGameInfo();
                rootState = new LiarsDiceGameState();
                expander = new LiarsDiceExpander<>(new MCTSConfig());
                break;
            case "PE":
                gameInfo = new PursuitGameInfo();
                rootState = new PursuitGameState();
                expander = new PursuitExpander<>(new MCTSConfig());
                break;
            case "OZ":
                gameInfo = new OZGameInfo();
                rootState = new OshiZumoGameState();
                expander = new OshiZumoExpander<>(new MCTSConfig());
                break;
            case "RG":
                gameInfo = new RandomGameInfo();
                rootState = new SimRandomGameState();
                expander = new RandomGameExpander<>(new MCTSConfig());
                break;
            case "Tron":
                gameInfo = new TronGameInfo();
                rootState = new TronGameState();
                expander = new TronExpander<>(new MCTSConfig());
                break;
            case "RPS":
                gameInfo = new RPSGameInfo();
                rootState = new RPSGameState();
                expander = new RPSExpander<>(new MCTSConfig());
                break;
            default:
                throw new IllegalArgumentException("Incorrect game:" + domain);
        }
        System.err.println(gameInfo.getInfo());
    }

    private void runAlgorithm(String alg) {
        System.err.println("Using algorithm "+alg);
        OOSAlgorithmData.gatherCFV = true;
        if (alg.equals("OOS")) {
            runOOS(0.9,0.6);
        }
        if (alg.equals("MCCFR")) {
            runMCCFR(0.6);
        }
    }


    private double calcExploitability() {
        Strategy strategy0 = StrategyCollector.getStrategyFor(
                alg.getRootNode(), rootState.getAllPlayers()[0],  new MeanStratDist());
        Strategy strategy1 = StrategyCollector.getStrategyFor(
                alg.getRootNode(), rootState.getAllPlayers()[1],  new MeanStratDist());

        Double br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
        Double br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
        return br0Val + br1Val;
    }


    private void printHeader(OOSAlgorithmData data) {
        double[] incrementalCFVs = data.getIncrementalCfv();
        double[] mp = data.getMp();

        System.out.print("iteration,exploitability");
        for (int i = 0; i < incrementalCFVs.length; i++) {
            System.out.print(",cfv_" + i);
        }
        for (int i = 0; i < mp.length; i++) {
            System.out.print(",mean_strategy_" + i);
        }
        System.out.println();
    }

    private void printIterationStatistics(int iterCnt, OOSAlgorithmData data, double exploitability) {
        double[] incrementalCFVs = data.getIncrementalCfv();
        double[] mp = data.getMp();

        // print iteration info
        System.out.print(iterCnt + "," + exploitability);
        for (double anIncrementalCFV : incrementalCFVs) {
            System.out.print("," + anIncrementalCFV);
        }
        double mpSum = 0;
        for (double d : mp) mpSum += d;
        for (int j = 0; j < mp.length; j++) {
            System.out.print("," + (mpSum == 0 ? 1.0 / mp.length : mp[j] / mpSum));
        }
        System.out.println();
    }

    private void runOOS(double delta, double epsilonExploration) {
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        OOSAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState,
                expander, delta, epsilonExploration);
        this.alg = alg;

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);

        // run algorithm until we find the information set
        System.err.println("Searching for IS "+trackCFVinInformationSet);
        MCTSInformationSet is;
        int searchingLoops = 0;
        do {
            alg.runIterations(2);
            is = identifyTargetInfoSet(alg.getRootNode(), trackCFVinInformationSet);
            searchingLoops++;
            if (searchingLoops % 1000 == 0) System.err.println(searchingLoops);
        } while( is == null );

        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        printHeader(data);

        int loop = 1;
        Double exploitability;
        do {
            alg.runIterations(numItersPerLoop);

            exploitability = calcExploitability();
            printIterationStatistics(searchingLoops*2 + loop * numItersPerLoop, data, exploitability);

            loop++;
        } while (exploitability > minExploitability);
    }

    private void runMCCFR(double epsilonExploration) {
        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        OOSAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState,
                expander, 0., epsilonExploration);
        this.alg = alg;

        buildCompleteTree(alg.getRootNode());

        Distribution dist = new MeanStratDist();

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);

        System.err.println("Searching for IS "+trackCFVinInformationSet);
        MCTSInformationSet is = identifyTargetInfoSet(alg.getRootNode(), trackCFVinInformationSet);
        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        printHeader(data);

        int loop = 1;
        Double exploitability;
        do {
            alg.runIterations(numItersPerLoop);

            exploitability = calcExploitability();
            printIterationStatistics(loop * numItersPerLoop, data, exploitability);

            loop++;
        } while (exploitability > minExploitability);
    }

    private MCTSInformationSet identifyTargetInfoSet(InnerNode state, String trackCFVinInformationSet) {
        MCTSInformationSet is = state.getInformationSet();
        if (is != null && is.toString().equals(trackCFVinInformationSet)) {
            return state.getInformationSet();
        }

        for (Node node : state.getChildren().values()) {
            if (node instanceof InnerNode) {
                MCTSInformationSet childrenSearch = identifyTargetInfoSet((InnerNode) node, trackCFVinInformationSet);
                if (childrenSearch != null) {
                    return childrenSearch;
                }
            }
        }

        return null;
    }

    public static void buildCompleteTree(InnerNode r) {
        System.err.println("Building complete tree.");
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (!(n instanceof ChanceNode))
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                }
            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        System.err.println("Created nodes: " + nodes + "; infosets: " + infosets);
    }
}
