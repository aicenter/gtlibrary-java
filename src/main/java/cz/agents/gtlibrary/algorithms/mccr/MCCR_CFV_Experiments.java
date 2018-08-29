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


package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.ISMCTSExploitability;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
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
import cz.agents.gtlibrary.domain.phantomTTT.TTTExpander;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpander;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
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
import cz.agents.gtlibrary.iinodes.ISKey;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

public class MCCR_CFV_Experiments {

    private final Long seed;
    protected GameInfo gameInfo;
    protected GameState rootState;
    protected SQFBestResponseAlgorithm brAlg0;
    protected SQFBestResponseAlgorithm brAlg1;
    protected Expander expander;

    private String trackCFVinInformationSet;
    private GamePlayingAlgorithm alg;
    private Double minExploitability = 0.01;
    private Integer numItersPerLoop = 10000;

    public MCCR_CFV_Experiments(Long seed) {
        this.seed = seed;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: MCCR_CFV_Experiments " +
                    "[trackingISName] [seed] {OOS|MCCFR|MCCR} {LD|GS|OZ|PE|RG|RPS|Tron} [domain parameters ... ].");
            System.exit(-1);
        }

        String trackCFVinInformationSet = args[0];
        long seed = new Long(args[1]);
        String alg = args[2];
        String domain = args[3];

        Random rnd = new Random(seed);

        MCCR_CFV_Experiments exp = new MCCR_CFV_Experiments(seed);
        exp.setTracking(trackCFVinInformationSet);
        exp.handleDomain(domain, Arrays.copyOfRange(args, 4, args.length));
        exp.loadGame(domain, rnd);
        exp.runAlgorithm(alg);
    }

    public static InformationSet buildCompleteTree(InnerNode r) {
        System.err.println("Building complete tree.");
        int nodes = 0, infosets = 0, publicStates = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);

        // todo: debug
        InformationSet retIS = null;
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
//            MCTSPublicState ps = n.getPublicState();
            if (!(n instanceof ChanceNode)) {
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                    if(is.toString().equals("IS:(Pl0):Pl0: []")) {
//                    if(is.toString().equals("IS:(Pl1):Pl1: []")) {
                        retIS = is;
                    }
                }
            }
//            if (ps.getAlgorithmData() == null) {
//                publicStates++;
//                ps.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
//            }
            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        System.err.println("Created nodes: " + nodes + "; infosets: " + infosets + "; public states: " + publicStates);

        return retIS;
    }

    public void handleDomain(String domain, String[] domainParams) {
        switch (domain) {
            case "IIGS": // Goofspiel
                if (domainParams.length != 4) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "4 parameters are required {SEED} {DEPTH} {BIN_UTIL} {FIXED_CARDS}");
                }
                GSGameInfo.seed = new Integer(domainParams[0]);
                GSGameInfo.depth = new Integer(domainParams[1]);
                GSGameInfo.BINARY_UTILITIES = new Boolean(domainParams[2]);
                GSGameInfo.useFixedNatureSequence = new Boolean(domainParams[3]);

                GSGameInfo.regenerateCards = true;

                break;
            case "LD":   // Liar's dice
                if (domainParams.length != 3) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "3 parameters are required {P1DICE} {P2DICE} {FACES}");
                }
                LDGameInfo.P1DICE = new Integer(domainParams[0]);
                LDGameInfo.P2DICE = new Integer(domainParams[1]);
                LDGameInfo.FACES = new Integer(domainParams[2]);
                LDGameInfo.CALLBID = (LDGameInfo.P1DICE + LDGameInfo.P2DICE) * LDGameInfo.FACES + 1;
                break;
                
            case "GP": // generic poker
                GPGameInfo.MAX_CARD_TYPES = new Integer(domainParams[0]);
                GPGameInfo.MAX_CARD_OF_EACH_TYPE = new Integer(domainParams[1]);
                GPGameInfo.MAX_RAISES_IN_ROW = new Integer(domainParams[2]);
                GPGameInfo.MAX_DIFFERENT_BETS = new Integer(domainParams[3]);
                GPGameInfo.MAX_DIFFERENT_RAISES = GPGameInfo.MAX_DIFFERENT_BETS;
                break;
                
            case "OZ":  // Oshi Zumo
                if (domainParams.length != 5) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "5 parameters are required {SEED} {COINS} {LOC_K} {MIN_BID} {BIN_UTIL}");
                }
                OZGameInfo.seed = new Integer(domainParams[0]);
                OZGameInfo.startingCoins = new Integer(domainParams[1]);
                OZGameInfo.locK = new Integer(domainParams[2]);
                OZGameInfo.minBid = new Integer(domainParams[3]);
                OZGameInfo.BINARY_UTILITIES = new Boolean(domainParams[4]);
                break;
            case "PE":  // Pursuit Evasion Game
                if (domainParams.length != 3) {
                    throw new IllegalArgumentException("Illegal PEG domain arguments count: " +
                            "3 parameters are required {SEED} {DEPTH} {GRAPH}");
                }
                PursuitGameInfo.seed = new Integer(domainParams[0]);
                PursuitGameInfo.depth = new Integer(domainParams[1]);
                PursuitGameInfo.graphFile = domainParams[2];
                break;
            case "RG":  // Random Games
                if (domainParams.length != 6) {
                    throw new IllegalArgumentException("Illegal random game domain arguments count. " +
                            "6 are required {SEED} {DEPTH} {BF} {CENTER_MODIFICATION} {BINARY_UTILITY} {FIXED BF}");
                }
                RandomGameInfo.seed = new Integer(domainParams[0]);
                RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
                RandomGameInfo.MAX_DEPTH = new Integer(domainParams[1]);
                RandomGameInfo.MAX_BF = new Integer(domainParams[2]);
                RandomGameInfo.MAX_CENTER_MODIFICATION = new Integer(domainParams[3]);
                RandomGameInfo.BINARY_UTILITY = new Boolean(domainParams[4]);
                RandomGameInfo.FIXED_SIZE_BF = new Boolean(domainParams[5]);
                break;
            case "Tron":  // Tron
                if (domainParams.length != 4) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "4 parameters are required {SEED} {BOARDTYPE} {ROWS} {COLUMNS}");
                }
                TronGameInfo.seed = new Integer(domainParams[0]);
                TronGameInfo.BOARDTYPE = domainParams[1].charAt(0);
                TronGameInfo.ROWS = new Integer(domainParams[2]);
                TronGameInfo.COLS = new Integer(domainParams[3]);
                break;
            case "PTTT":  // Phantom Tic tac toe
                break;
            case "RPS":  // Tron
                if (domainParams.length != 1) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "1 parameter is required {SEED}");
                }
                RPSGameInfo.seed = new Integer(domainParams[0]);
                break;
            default:
                throw new IllegalArgumentException("Illegal domain: " + domainParams[1]);
        }
    }

    public void loadGame(String domain, Random rnd) {
        MCTSConfig mctsConfig = new MCTSConfig(rnd);

        switch (domain) {
            case "IIGS":
                gameInfo = new GSGameInfo();
                rootState = new IIGoofSpielGameState();
                expander = new GoofSpielExpander<>(mctsConfig);
                break;
            case "LD":
                gameInfo = new LDGameInfo();
                rootState = new LiarsDiceGameState();
                expander = new LiarsDiceExpander<>(mctsConfig);
                break;
            case "GP":
                gameInfo = new GPGameInfo();
                rootState = new GenericPokerGameState();
                expander = new GenericPokerExpander<>(mctsConfig);
                break;
            case "PE":
                gameInfo = new PursuitGameInfo();
                rootState = new PursuitGameState();
                expander = new PursuitExpander<>(mctsConfig);
                break;
            case "OZ":
                gameInfo = new OZGameInfo();
                rootState = new OshiZumoGameState();
                expander = new OshiZumoExpander<>(mctsConfig);
                break;
            case "RG":
                gameInfo = new RandomGameInfo();
                rootState = new SimRandomGameState();
                expander = new RandomGameExpander<>(mctsConfig);
                break;
            case "Tron":
                gameInfo = new TronGameInfo();
                rootState = new TronGameState();
                expander = new TronExpander<>(mctsConfig);
                break;
            case "PTTT":
                gameInfo = new TTTInfo();
                rootState = new TTTState();
                expander = new TTTExpander(mctsConfig);
                break;
            case "RPS":
                gameInfo = new RPSGameInfo();
                rootState = new RPSGameState();
                expander = new RPSExpander<>(mctsConfig);
                break;
            default:
                throw new IllegalArgumentException("Incorrect game:" + domain);
        }

        expander.setGameInfo(gameInfo);
        System.err.println(gameInfo.getInfo());
    }

    public void runAlgorithm(String alg) {
        System.err.println("Using algorithm " + alg);
        OOSAlgorithmData.gatherActionCFV = true;
        if (alg.equals("OOS")) {
            runOOS_CFVA();
        }
        if (alg.equals("MCCFR")) {
            runMCCFR_CFVA();
        }
        if (alg.equals("MCCR")) {
            runMCCR();
        }
        if (alg.equals("MCCR-match")) {
            runMCCR_match();
        }
        if (alg.equals("CFR")) {
            runCFR();
        }
        if (alg.equals("uniform")) {
            runUniform();
        }
    }

    private double calcExploitability() {
        Strategy strategy0 = StrategyCollector.getStrategyFor(
                alg.getRootNode(), rootState.getAllPlayers()[0], new MeanStratDist());
        Strategy strategy1 = StrategyCollector.getStrategyFor(
                alg.getRootNode(), rootState.getAllPlayers()[1], new MeanStratDist());

        Double br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
        Double br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
        return br0Val + br1Val;
    }

    private void printHeader(OOSAlgorithmData data) {
        double[] incrementalCFVs = data.getActionCFV();
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

    private void printIterationStatistics(int iterCnt, long runningTime, OOSAlgorithmData data, double exploitability) {
        double[] incrementalCFVs = data.getActionCFV();
        double[] mp = data.getMp();

        // print iteration info
        System.out.print(iterCnt + "," + runningTime + "," + exploitability);
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

    private void runOOS_CFVA() {
        double epsilonExploration = new Double(getenv("epsExploration", "0.6"));
        int delta = new Integer(getenv("delta", "0.2"));

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
        System.err.println("Searching for IS " + trackCFVinInformationSet);
        MCTSInformationSet is;
        int searchingLoops = 0;
        do {
            alg.runIterations(2);
            is = identifyTargetInfoSet(alg.getRootNode(), trackCFVinInformationSet);
            searchingLoops++;
            if (searchingLoops % 1000 == 0) System.err.println(searchingLoops);
        } while (is == null);

        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        printHeader(data);

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        double loop = 10.;
        int total = 0;
        Double exploitability;
        long runningTime = 0;
        do {
            int iters = (int) Math.floor(Math.pow(10., 1 + loop/10.)) - total;

            long time = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(iters);
            runningTime += threadBean.getCurrentThreadCpuTime() - time;
            total += iters;

            exploitability = calcExploitability();
            printIterationStatistics(total, runningTime, data, exploitability);

            loop++;
        } while (exploitability > minExploitability);
    }

    private void runMCCFR_CFVA() {
        double epsilonExploration = new Double(getenv("epsExploration", "0.6"));


        expander.getAlgorithmConfig().createInformationSetFor(rootState);

        OOSAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], new OOSSimulator(expander), rootState,
                expander, 0., epsilonExploration);
        this.alg = alg;

        buildCompleteTree(alg.getRootNode());
//
//        System.err.println("Several first infosets:");
//        printSeveralFirstInfoSets(alg.getRootNode(), 10, 10, new HashSet<>());
//        Distribution dist = new MeanStratDist();

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);

        System.err.println("Searching for IS " + trackCFVinInformationSet);
        MCTSInformationSet is = identifyTargetInfoSet(alg.getRootNode(), trackCFVinInformationSet);
        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        printHeader(data);

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        double loop = 10.;
        int total = 0;
        Double exploitability;
        long runningTime = 0;
        do {
            int iters = (int) Math.floor(Math.pow(10., 1 + loop/10.)) - total;
            long time = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(iters);
            runningTime += threadBean.getCurrentThreadCpuTime() - time;
            total += iters;

            exploitability = calcExploitability();
            printIterationStatistics(total, runningTime, data, exploitability);

            loop++;
        } while (total <= 10e7);
//        } while (exploitability > minExploitability);
    }

    private void runMCCR() {
        double epsExploration = new Double(getenv("epsExploration", "0.6"));
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "100000"));
        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
        boolean resetData = new Boolean(getenv("resetData", "true"));

        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        MCCRAlgorithm alg = new MCCRAlgorithm(rootState, expander, epsExploration);
        alg.setResetData(resetData);
        this.alg = alg;

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);

        alg.solveEntireGame(iterationsInRoot, iterationsPerGadgetGame);
        Double exploitability = calcExploitability();
        System.out.println(seed+","+epsExploration+","+iterationsInRoot+","+iterationsPerGadgetGame+","+resetData+","+exploitability);
    }

    private void runUniform() {
        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        MCCRAlgorithm alg = new MCCRAlgorithm(rootState, expander, 0.6);
        this.alg = alg;
        buildCompleteTree(alg.getRootNode());

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);

        Double exploitability = calcExploitability();
        System.out.println(exploitability);
    }

    private void runMCCR_match() {
        double epsExploration = new Double(getenv("epsExploration", "0.6"));
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "100000"));
        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
        boolean resetData = new Boolean(getenv("resetData", "true"));

        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        MCCRAlgorithm alg = new MCCRAlgorithm(rootState, expander, epsExploration);
        alg.setResetData(resetData);
        this.alg = alg;

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig(), gameInfo);

        double[] utils = alg.runIterations(iterationsInRoot, iterationsPerGadgetGame);
        System.err.println(utils[0]);
    }

    private void runCFR_d() {
        CFRAlgorithm alg = new CFRAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);
        this.alg = alg;

        MCTSInformationSet is = (MCTSInformationSet) buildCompleteTree(alg.getRootNode());
        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        buildCompleteTree(alg.getRootNode());

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        int n = 100;
        for (int i = 1; i <= 1000; i++) {
            alg.runIterations(n);
            Double exploitability = calcExploitability();
            double cfv = alg.computeCFVofIS(is);
            OOSAlgorithmData data= (OOSAlgorithmData) is.getAlgorithmData();
            System.out.println("ITER:"+i*n+","+exploitability+","+cfv);
            Map<Action, Double> dist = new MeanStratDist().getDistributionFor(data);
            System.out.println(dist);

            double cum = 0.0;
            double[] cfvas = alg.computeCFVAofIS(is);
            for (int a = 0; a < is.getActions().size(); a++) {
                System.out.print(cfvas[a] +",");
                cum += cfvas[a] * dist.get(is.getActions().get(a));
            }

            System.err.println(cum - cfv);
            System.out.println();
        }
    }

    private void runCFR() {
        OOSAlgorithmData.useEpsilonRM = true;
        OOSAlgorithmData.epsilon = 0.00001f;

        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));

        rootState = rootState.performAction((Action) expander.getActions(rootState).get(0));

        CFRAlgorithm algCFR = new CFRAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);
        this.alg = algCFR;

//        MCTSInformationSet is = (MCTSInformationSet) buildCompleteTree(alg.getRootNode());
        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        buildCompleteTree(algCFR.getRootNode());


//        exportPublicTree(alg.getRootNode());

        brAlg0 = new SQFBestResponseAlgorithm(expander, 0, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1, new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]}, (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);

        algCFR.runIterations(100);

        Double exploitability = calcExploitability();
        System.err.println(">>> Exploit Before: " + exploitability);

        Set<MCTSPublicState> publicStates = ((MCTSConfig) expander.getAlgorithmConfig()).getAllPublicStates();
        MCTSPublicState targetPS = publicStates.stream().filter(ps -> ps.getPSKey().getHash() == 1).findFirst().get();
//        MCTSPublicState targetPS = publicStates.stream().filter(ps -> ps.getPSKey().getHash() == 7).findFirst().get();

        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.add(targetPS);
        while(!q.isEmpty()) {
            PublicState PS = q.removeFirst();
            for(MCTSInformationSet IS : PS.getAllInformationSets()) {
                ((OOSAlgorithmData) IS.getAlgorithmData()).setIsCFV(algCFR.computeCFVofIS(IS));

                for(InnerNode in : IS.getAllNodes()) {
                    double rp = algCFR.calcRpOfNode(in, IS.getPlayer());
                    ((InnerNodeImpl) in).setReachPr(rp);
                    double eu = algCFR.computeExpUtilityOfState(in, IS.getOpponent());
                    in.updateExpectedValue(eu * iterationsPerGadgetGame);
                }
            }
            q.addAll(PS.getNextPlayerPublicStates());
        }

        System.err.println("Collecting behav strategy");
        Map<ISKey, Map<Action,Double>> solvedBehavCFR = getBehavioralStrategy(algCFR.getRootNode());
        Map<ISKey, Map<Action,Double>> copyCFR = getBehavioralStrategy(algCFR.getRootNode());


        System.err.println("Running resolving");
        MCCRAlgorithm mccrAlg = new MCCRAlgorithm(algCFR.getRootNode(), expander, 0.6);
        mccrAlg.updateCRstatistics = false;
        mccrAlg.gadgetMCCFR = false;
        mccrAlg.setResetData(true);

        q.add(targetPS);
        int depth = -1;
        while (!q.isEmpty()) {
            PublicState s = q.removeFirst();
            if(s.getDepth() != depth) {
                System.err.println("=========================================== Depth "+s.getDepth());
                depth = s.getDepth();
            }
            InnerNode n = s.getAllNodes().iterator().next();

            mccrAlg.runStep(n, iterationsPerGadgetGame);

            // evaluate expl
            copyCFR = cloneBehavStrategy(solvedBehavCFR);
            Map<ISKey, Map<Action,Double>> behavMCCR = getBehavioralStrategy(mccrAlg.getRootNode());

            substituteStrategy(copyCFR, behavMCCR, targetPS);

            Strategy strategy0 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                    copyCFR, rootState, expander, rootState.getAllPlayers()[0]);
            Strategy strategy1 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                    copyCFR, rootState, expander, rootState.getAllPlayers()[1]);

            Double br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
            Double br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
            exploitability = br0Val + br1Val;
            System.err.println(">>> Exploit root: " + exploitability);
            System.err.println(">>> Exploit normal: " + calcExploitability());
            ;
            q.addAll(s.getNextPlayerPublicStates());
        }

        System.err.println("Calculating expl after");
        Map<ISKey, Map<Action,Double>> behavMCCR = getBehavioralStrategy(mccrAlg.getRootNode());

        substituteStrategy(solvedBehavCFR, behavMCCR, targetPS);

        Strategy strategy0 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                solvedBehavCFR, rootState, expander, rootState.getAllPlayers()[0]);
        Strategy strategy1 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                solvedBehavCFR, rootState, expander, rootState.getAllPlayers()[1]);

        Double br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
        Double br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
        exploitability = br0Val + br1Val;
        System.err.println(">>> Exploit After: " + exploitability);
        System.out.println(seed +","+iterationsPerGadgetGame +"," + exploitability);
    }

    private  Map<ISKey, Map<Action,Double>> cloneBehavStrategy(Map<ISKey, Map<Action,Double>> orig) {
        Map<ISKey, Map<Action,Double>> out = new HashMap<>();

        for (Map.Entry<ISKey, Map<Action,Double>> entry : orig.entrySet()) {
            Map<Action,Double> dist = new HashMap<Action,Double>();

            for(Map.Entry<Action,Double> act : entry.getValue().entrySet()) {
                dist.put(act.getKey(), new Double(act.getValue()));
            }
            out.put(entry.getKey(), dist);
        }
        return out;
    }

    private void exportPublicTree(InnerNode rootNode) {
        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.add(rootNode.getPublicState());
        Set<PublicState> traversed = new HashSet<>();
        System.out.println("graph {");
        while (!q.isEmpty()){
            PublicState ps = q.removeFirst();
            traversed.add(ps);
            Set<PublicState> nextStates = ps.getNextPlayerPublicStates();

            int id = ps.getAllNodes().iterator().next().getPlayerToMove().getId();
            if(id == 2) assert nextStates.size() == 1;
            if(id == 0) assert nextStates.size() == 1;
            if(id == 1) assert nextStates.size() == 3 ||  nextStates.size() == 0;

            for(PublicState n : ps.getNextPlayerPublicStates()){
                System.out.println("\t\"ps "+ps.getPSKey().getHash() + "\" -- \"ps "+n.getPSKey().getHash() + "\";");
                q.add(n);
            }
        }
        System.out.println("}");
    }

    private Map<ISKey, Map<Action, Double>> getBehavioralStrategy(InnerNode rootNode) {
        Map<ISKey, Map<Action, Double>> out = new HashMap<>();

        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.add(rootNode);

        while (!q.isEmpty()){
            InnerNode curNode = q.removeFirst();
            MCTSInformationSet curNodeIS = curNode.getInformationSet();
            if (curNodeIS == null) {
                assert (curNode.getGameState().isPlayerToMoveNature());
            } else {
                OOSAlgorithmData data = ((OOSAlgorithmData) curNodeIS.getAlgorithmData());
                out.put(curNodeIS.getISKey(), new MeanStratDist().getDistributionFor(data));
            }

            for(Node n : curNode.getChildren().values()){
                if ((n instanceof InnerNode)) q.addLast((InnerNode)n);
            }
        }
        return out;
    }

    private String getenv(String env, String def) {
        return System.getenv(env) == null ? def : System.getenv(env);
    }

    private void substituteStrategy(Map<ISKey, Map<Action,Double>> s1, Map<ISKey, Map<Action,Double>> s2,
                                    PublicState replaceS1byS2atPublicState) {
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.addAll(replaceS1byS2atPublicState.getAllNodes());

        while (!q.isEmpty()){
            InnerNode curNode = q.removeFirst();
            MCTSInformationSet curNodeIS = curNode.getInformationSet();
            if (curNodeIS == null) {
                assert (curNode.getGameState().isPlayerToMoveNature());
            } else {
                // substitute
                ISKey key = curNodeIS.getISKey();
                if(s1.containsKey(key) && s2.containsKey(key)){
                    Map<Action,Double> oldvalue = s1.get(key);
                    Map<Action,Double> newvalue = s2.get(key);
                    s1.put(key, newvalue);
//                    System.err.println(oldvalue + " : " + newvalue);
                }
            }

            for(Node n : curNode.getChildren().values()){
                if ((n instanceof InnerNode)) q.addLast((InnerNode)n);
            }
        }
    }
    private void printSeveralFirstInfoSets(InnerNode state, int maxDepth, int maxNames, Set<String> uniqueISNames) {
        if (maxDepth == 0) return;
        if (uniqueISNames.size() > maxNames) return;

        MCTSInformationSet is = state.getInformationSet();
        if (is != null && !uniqueISNames.contains(is.toString())) {
            System.err.println(is.toString());
            uniqueISNames.add(is.toString());
        }

        for (Node node : state.getChildren().values()) {
            if (node instanceof InnerNode) {
                printSeveralFirstInfoSets((InnerNode) node, maxDepth-1, maxNames, uniqueISNames);
            }
        }
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

    public void setTracking(String tracking) {
        this.trackCFVinInformationSet = tracking;
    }
}
