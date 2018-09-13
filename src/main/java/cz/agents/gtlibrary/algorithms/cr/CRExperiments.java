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


package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInfoSet;
import cz.agents.gtlibrary.algorithms.mcts.ISMCTSExploitability;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.MCTSPublicState;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.domain.goofspiel.*;
import cz.agents.gtlibrary.domain.liarsdice.*;
import cz.agents.gtlibrary.domain.oshizumo.*;
import cz.agents.gtlibrary.domain.phantomTTT.*;
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

import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.updateCFRResolvingData;
import static cz.agents.gtlibrary.algorithms.cr.ResolvingMethod.RESOLVE_CFR;


public class CRExperiments {

    private final Long seed;
    protected GameInfo gameInfo;
    protected GameState rootState;
    protected SQFBestResponseAlgorithm brAlg0;
    protected SQFBestResponseAlgorithm brAlg1;
    protected Expander<MCTSInformationSet> expander;
    protected MCTSConfig config;

    private String trackCFVinInformationSet;
    private GamePlayingAlgorithm alg;
    private Double minExploitability = 0.01;
    private Integer numItersPerLoop = 10000;

    public CRExperiments(Long seed) {
        this.seed = seed;
        System.err.println("Using seed "+seed);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: MCCR_CFV_Experiments " +
                    "[trackingISName] [seed] {OOS|RESOLVE_MCCFR|MCCR} {LD|GS|OZ|PE|RG|RPS|Tron} [domain parameters ... ].");
            System.exit(-1);
        }

        String trackCFVinInformationSet = args[0];
        long seed = new Long(args[1]);
        String alg = args[2];
        String domain = args[3];

        Random rnd = new Random(seed);

        CRExperiments exp = new CRExperiments(seed);
        exp.setTracking(trackCFVinInformationSet);
        exp.prepareDomain(domain, Arrays.copyOfRange(args, 4, args.length));
        exp.createGame(domain, rnd);
        exp.runAlgorithm(alg);
    }

    public static void buildCompleteTree(InnerNode r) {
        buildCompleteTree(r, Integer.MAX_VALUE);
    }
    public static void buildCompleteTree(InnerNode r, Integer maxDepth) {
        System.err.println("Building complete tree.");
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);

        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (!(n instanceof ChanceNode)) {
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                }
            }

            if(n.getDepth() < maxDepth) {
                for (Action a : n.getActions()) {
                    Node ch = n.getChildFor(a);
                    if (ch instanceof InnerNode) {
                        q.add((InnerNode) ch);
                    }
                }
            }
        }
        System.err.println("Created nodes: " + nodes + "; infosets: " + infosets);
    }

    public static boolean checkDomainPublicTree(InnerNode r) {
        ArrayDeque<InnerNode> q = new ArrayDeque<InnerNode>();
        q.add(r);

        while (!q.isEmpty()) {
            InnerNode n = q.removeFirst();

            assert !(n.getGameState() instanceof IIGoofSpielGameState) || (
                    ((n.getPublicState().getPlayer().getId() == 2 && n.getPublicState().getNextPublicStates().size() == 1)
                 || (n.getPublicState().getPlayer().getId() == 0 && n.getPublicState().getNextPublicStates().size() == 1)
                 || (n.getPublicState().getPlayer().getId() == 1 && (n.getPublicState().getNextPublicStates().size() == 3
                            || n.getPublicState().getNextPublicStates().size() == 0))
            ));

            for (Action a : n.getActions()) {
                Node ch = n.getChildFor(a);
                if (ch instanceof InnerNode) {
                    q.add((InnerNode) ch);
                }
            }
        }
        return true;
    }

    public void prepareDomain(String domain, String[] domainParams) {
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

                if (GSGameInfo.seed == 0 && GSGameInfo.depth == 4) {
                    GSGameInfo.seed = 0;
                    GSGameInfo.depth = 4;
                    GSGameInfo.CARDS_FOR_PLAYER = new int[]{4, 3, 2, 1};
                    GSGameInfo.BINARY_UTILITIES = true;
                    GSGameInfo.useFixedNatureSequence = true;
                    GSGameInfo.regenerateCards = false;
                }

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
                RandomGameInfo.rnd = new HighQualityRandom(RandomGameInfo.seed);
                RandomGameInfo.seed = new Integer(domainParams[0]);
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

    public void createGame(String domain, Random rnd) {
        config = new MCTSConfig(rnd);

        switch (domain) {
            case "IIGS":
                gameInfo = new GSGameInfo();
                rootState = new IIGoofSpielGameState();
                expander = new GoofSpielExpander<>(config);
                break;
            case "LD":
                gameInfo = new LDGameInfo();
                rootState = new LiarsDiceGameState();
                expander = new LiarsDiceExpander<>(config);
                break;
            case "GP":
                gameInfo = new GPGameInfo();
                rootState = new GenericPokerGameState();
                expander = new GenericPokerExpander<>(config);
                break;
            case "PE":
                gameInfo = new PursuitGameInfo();
                rootState = new PursuitGameState();
                expander = new PursuitExpander<>(config);
                break;
            case "OZ":
                gameInfo = new OZGameInfo();
                rootState = new OshiZumoGameState();
                expander = new OshiZumoExpander<>(config);
                break;
            case "RG":
                gameInfo = new RandomGameInfo();
                rootState = new SimRandomGameState();
                expander = new RandomGameExpander<>(config);
                break;
            case "Tron":
                gameInfo = new TronGameInfo();
                rootState = new TronGameState();
                expander = new TronExpander<>(config);
                break;
            case "PTTT":
                gameInfo = new TTTInfo();
                rootState = new TTTState();
                expander = new TTTExpander(config);
                break;
            case "RPS":
                gameInfo = new RPSGameInfo();
                rootState = new RPSGameState();
                expander = new RPSExpander<>(config);
                break;
            default:
                throw new IllegalArgumentException("Incorrect game:" + domain);
        }

        expander.setGameInfo(gameInfo);
        System.err.println(gameInfo.getInfo());

        // prepare BR
        brAlg0 = new SQFBestResponseAlgorithm(expander, 0,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(expander, 1,
                new Player[]{rootState.getAllPlayers()[0], rootState.getAllPlayers()[1]},
                (ConfigImpl) expander.getAlgorithmConfig()/*sfAlgConfig*/, gameInfo);
    }

    public void runAlgorithm(String alg) {
        System.err.println("Using algorithm " + alg);
        OOSAlgorithmData.gatherActionCFV = true;

        if (alg.equals("OOS")) {
            runOOS_CFVA();
            return;
        }
        if (alg.equals("MCCFR_gadget_CFV")) {
            runMCCFR_gadget_CFV();
            return;
        }
        if (alg.equals("MCCR")) {
            runMCCR();
            return;
        }
//        if (alg.equals("MCCR-match")) {
//            runMCCR_match();
//            return;
//        }
        if (alg.equals("CFR")) {
            runCFR();
            return;
        }
        if (alg.equals("CR-mix")) {
            runCR_mix();
            return;
        }
        if (alg.equals("uniform")) {
            runUniform();
            return;
        }
        if (alg.equals("stats")) {
            runStats();
            return;
        }

        System.err.println("No such algorithm found!");
        System.exit(1);
    }

    private void runStats() {
        CRAlgorithm alg = new CRAlgorithm(rootState, expander);
        buildCompleteTree(alg.getRootNode());
        alg.printDomainStatistics();
    }

    private double calcExploitability(InnerNode rootNode) {
        Strategy strategy0 = StrategyCollector.getStrategyFor(
                rootNode, rootState.getAllPlayers()[0], new MeanStratDist());
        Strategy strategy1 = StrategyCollector.getStrategyFor(
                rootNode, rootState.getAllPlayers()[1], new MeanStratDist());

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
            int iters = (int) Math.floor(Math.pow(10., 1 + loop / 10.)) - total;

            long time = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(iters);
            runningTime += threadBean.getCurrentThreadCpuTime() - time;
            total += iters;

            exploitability = calcExploitability(alg.getRootNode());
            printIterationStatistics(total, runningTime, data, exploitability);

            loop++;
        } while (exploitability > minExploitability);
    }

    private void runMCCFR_gadget_CFV() {
        double epsilonExploration = new Double(getenv("epsExploration", "0.6"));
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "1000"));
        int resolvingPlayerIdx = new Integer(getenv("resolvingPlayer", "0"));

        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        Player resolvingPlayer = rootState.getAllPlayers()[resolvingPlayerIdx];

        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, new OOSSimulator(expander), rootState,
                expander, 0., epsilonExploration);
        this.alg = alg;

        InnerNode rootNode = alg.getRootNode();
        buildCompleteTree(rootNode);
//        assert checkDomainPublicTree(rootNode);

        Map<PublicState, List<GadgetInfoSet>> targetPsGadgetIs = new LinkedHashMap<>();
        rootNode.getPublicState()
                .getNextPlayerPublicStates(resolvingPlayer)
                .iterator().next()
                .getNextPlayerPublicStates(resolvingPlayer)
                .forEach(ps -> {
                    Set<GadgetInfoSet> gadgetIs= ps.getSubgame().getGadgetInformationSets();
                    ArrayList<GadgetInfoSet> gadgetIsAL = new ArrayList<>(gadgetIs);
                    gadgetIsAL.sort((g1, g2) -> {
                        int c = g1.getAllNodes().size() - g2.getAllNodes().size();
                        if(c == 0) {
                            String g1s = g1.getAllNodes().stream().map(InnerNode::toString).reduce("", String::concat);
                            String g2s = g2.getAllNodes().stream().map(InnerNode::toString).reduce("", String::concat);
                            c = g1s.compareTo(g2s);
                        }
                        return c;
                    });
                    System.err.println(ps + ": "+
                            gadgetIsAL.stream().map(gis -> gis.getAllNodes().stream().map(in -> in+", ").reduce("", String::concat) + " - ").reduce("", String::concat)
                      );
                    targetPsGadgetIs.put(ps, gadgetIsAL);
                });


        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        double loop = 10.;
        int total = 0;
        Double exploitability;
        long runningTime = 0;

//        System.out.print("seed;iterations;expl;runningTime");
//        targetPsGadgetIs.forEach((ps, gadgetIs) -> {
//            System.out.print(";ps_"+ps.hashCode());
//            int i = 0;
//            for (GadgetInfoSet gadgetI : gadgetIs) {
//                System.out.print(
//                        ";is_reach_"+ps.hashCode()+"_"+i+
//                        ";is_cfv_"+ps.hashCode()+"_"+i+
//                        ";is_visits_"+ps.hashCode()+"_"+i);
//
////                System.out.print(ps.hashCode()+"_"+i+": ");
////                gadgetI.getAllNodes().forEach(n ->
////                        System.out.print(((GadgetInnerNode) n).getOriginalNode().toString() + "; "));
////                System.out.println();
//                i++;
//            }
//        });
//        System.out.println();

        do {
            int iters = (int) Math.floor(Math.pow(10., 1 + loop / 10.)) - total;
            iters += (iters % 2 == 0) ? 0 : 1;
            long time = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(iters);
            runningTime += threadBean.getCurrentThreadCpuTime() - time;
            total += iters;

            exploitability = calcExploitability(rootNode);
            int finalTotal = total - alg.samplesSkipped;

//            System.out.print(seed + ";" + total + ";"+exploitability+";"+runningTime);
//            targetPsGadgetIs.forEach((ps, gadgetIs) -> {
//                System.out.print(";"+ps.hashCode());
//                for (GadgetInfoSet gadgetI : gadgetIs) {
//                    gadgetI.getAllNodes().forEach(in -> {
//                        GadgetInnerNode gin = (GadgetInnerNode) in;
//                        InnerNode o = gin.getOriginalNode();
//                        o.setReachPrByPlayer(o.getPlayerToMove(), CFRAlgorithm.calcRpPlayerOfNode(o, o.getPlayerToMove()));
//                        o.setReachPrByPlayer(o.getOpponentPlayerToMove(), CFRAlgorithm.calcRpPlayerOfNode(o, o.getOpponentPlayerToMove()));
//                    });
//                    System.out.print(";" + gadgetI.getIsReach());
//                    System.out.print(";" + gadgetI.getIsCFV(finalTotal));
//
//                    int visits = gadgetI.getAllNodes().stream()
//                            .map(in -> ((GadgetInnerNode) in).getOriginalNode())
//                            .map(InnerNode::getInformationSet)
//                            .map(MCTSInformationSet::getVisitsCnt)
//                            .reduce(0, Integer::sum);
//                    System.out.print(";" + visits);
//                }
//            });
//            System.out.println();

//            if(total > 1000) {
//                System.err.println("wait");
//                PublicState ps13 = config.getAllPublicStates().stream().filter(ps -> ps.hashCode() == 13).findFirst().get();
//                int finalTotal1 = total;
//                ps13.getSubgame().getGadgetInformationSets().forEach(gis -> {
//                    for(InnerNode in : gis.getAllNodes()) {
//                        InnerNode par = ((GadgetInnerNode) in).getOriginalNode().getParent();
//                        OOSAlgorithmData data1 = (OOSAlgorithmData) par.getParent().getInformationSet().getAlgorithmData();
//                        double pi_1 = data1.getMeanStrategy()[par.getParent().getActions().indexOf(
//                                par.getLastAction())];
//
//                        OOSAlgorithmData data0 = (OOSAlgorithmData) par.getParent().getParent().getInformationSet().getAlgorithmData();
//                        double pi_0 = data0.getMeanStrategy()[par.getParent().getParent().getActions().indexOf(
//                                par.getParent().getLastAction())];
//                        System.out.println(
//                                finalTotal1 + ";" +gis+" "+ in + " pi_0=" + pi_0 + " pi_1=" + pi_1 + " rp=" + (pi_0 * pi_1));
//                    }
//                });
//            }

            loop++;
        } while (total <= iterationsInRoot);

//
        // re-create expander and config
//        prepareDomain("IIGS", new String[]{"0", "5", "true", "true"});
//        createGame("IIGS", new Random(0));
//        prepareDomain("GP", new String[]{"3", "3", "2", "2"});
//        createGame("GP", new Random(0));
//        prepareDomain("LD", new String[]{"1", "1", "4"});
//        createGame("LD", new Random(0));

//        CFRAlgorithm cfrAlg = new CFRAlgorithm(resolvingPlayer, rootState, expander);
//        buildCompleteTree(cfrAlg.getRootNode());
//        assert checkDomainPublicTree(rootNode);
//
//        Map<PublicState, List<GadgetInfoSet>> targetPsGadgetIsCfr = new LinkedHashMap<>();
//        cfrAlg.getRootNode().getPublicState()
//                .getNextPlayerPublicStates(resolvingPlayer)
//                .iterator().next()
//                .getNextPlayerPublicStates(resolvingPlayer)
//                .forEach(ps -> {
//                    Set<GadgetInfoSet> gadgetIs= ps.getSubgame().getGadgetInformationSets();
//                    targetPsGadgetIsCfr.put(ps, new ArrayList<>(gadgetIs));
//                });
//
//        cfrAlg.runIterations(10000);
//
//        CFRData rootCfrData = collectCFRResolvingData(cfrAlg.getRootNode().getPublicState().getNextPlayerPublicStates(resolvingPlayer));
//        cfrAlg.getRootNode().getPublicState()
//                .getNextPlayerPublicStates(resolvingPlayer)
//                .iterator().next()
//                .getNextPlayerPublicStates(resolvingPlayer)
//                .forEach(targetPS -> updateCFRResolvingData(targetPS, rootCfrData.reachProbs, rootCfrData.historyExpValues));
//
//        exploitability = calcExploitability(cfrAlg.getRootNode());
//
//        System.out.print(seed + ";CFR;"+exploitability+";"+runningTime);
//        targetPsGadgetIsCfr.forEach((ps, gadgetIs) -> {
//            System.out.print(";"+ps.hashCode());
//            for (GadgetInfoSet gadgetI : gadgetIs) {
//                System.out.print(";" + gadgetI.getIsReach());
//                System.out.print(";" + gadgetI.getIsCFV(2));
//
//                int visits = gadgetI.getAllNodes().stream()
//                        .map(in -> ((GadgetInnerNode) in).getOriginalNode())
//                        .map(InnerNode::getInformationSet)
//                        .map(MCTSInformationSet::getVisitsCnt)
//                        .reduce(0, Integer::sum);
//                System.out.print(";" + visits);
//            }
//        });
    }

    private void runMCCR() {
        double epsExploration = new Double(getenv("epsExploration", "0.6"));
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "100000"));
        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
        boolean resetData = new Boolean(getenv("resetData", "true"));
        int player = new Integer(getenv("player", "0"));


        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        CRAlgorithm alg = new CRAlgorithm(rootState, expander, epsExploration);
        alg.setDoResetData(resetData);
        this.alg = alg;


        InnerNode rootNode = alg.solveEntireGame(rootState.getAllPlayers()[player], iterationsInRoot,
                iterationsPerGadgetGame);

        Strategy strategy0 = StrategyCollector.getStrategyFor(
                rootNode, rootState.getAllPlayers()[0], new MeanStratDist());
        Strategy strategy1 = StrategyCollector.getStrategyFor(
                rootNode, rootState.getAllPlayers()[1], new MeanStratDist());

        Double br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
        Double br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));

        double gameValue = 0.; // for player 0
        if (this.rootState instanceof RPSGameState) {
            gameValue = 0.3235;
        }

        double exploitability = br0Val + br1Val;
        double expl0 = gameValue + br1Val;
        double expl1 = -gameValue + br0Val;

        MCTSConfig config = rootNode.getAlgConfig();
        double timePerResolve = CRAlgorithm.totalTimeResolving / config.getAllPublicStates().size();

        System.err.println("seed,epsExploration,iterationsInRoot,iterationsPerGadgetGame,resetData,player," +
                "expl0,expl1,br1Val,br0Val,exploitability,timePerResolve");
        System.out.println(seed + "," +
                epsExploration + "," +
                iterationsInRoot + "," +
                iterationsPerGadgetGame + ","
                + resetData + ","
                + player + ","
                + expl0 + ","
                + expl1 + ","
                + br1Val + ","
                + br0Val + ","
                + exploitability + ","
                + timePerResolve);
    }

    private void runUniform() {
        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        CRAlgorithm alg = new CRAlgorithm(rootState, expander, 0.6);
        this.alg = alg;
        InnerNode rootNode = alg.getRootNode();
        buildCompleteTree(rootNode);

        Double exploitability = calcExploitability(rootNode);
        System.out.println(exploitability);
    }

//    private void runMCCR_match() {
//        double epsExploration = new Double(getenv("epsExploration", "0.6"));
//        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "100000"));
//        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
//        boolean resetData = new Boolean(getenv("resetData", "true"));
//
//        expander.getAlgorithmConfig().createInformationSetFor(rootState);
//        CRAlgorithm alg = new CRAlgorithm(rootState, expander, epsExploration);
//        alg.setDoResetData(resetData);
//        this.alg = alg;
//
//
//        double[] utils = alg.runIterations(rootState.getAllPlayers()[0], iterationsInRoot, iterationsPerGadgetGame);
//        System.err.println(utils[0]);
//    }

    private void runCFR() {
        CFRAlgorithm alg = new CFRAlgorithm(
                rootState.getAllPlayers()[0],
                rootState, expander);
        this.alg = alg;

        MCTSInformationSet is = null; // todo
        buildCompleteTree(alg.getRootNode());
        expander.getAlgorithmConfig().createInformationSetFor(rootState);
        buildCompleteTree(alg.getRootNode());


        int n = 100;
        for (int i = 1; i <= 1000; i++) {
            alg.runIterations(n);
            Double exploitability = calcExploitability(alg.getRootNode());
            double cfv = alg.computeCFVofIS(is);
            OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();
            System.out.println("ITER:" + i * n + "," + exploitability + "," + cfv);
            Map<Action, Double> dist = new MeanStratDist().getDistributionFor(data);
            System.out.println(dist);

            double cum = 0.0;
            double[] cfvas = alg.computeCFVAofIS(is);
            for (int a = 0; a < is.getActions().size(); a++) {
                System.out.print(cfvas[a] + ",");
                cum += cfvas[a] * dist.get(is.getActions().get(a));
            }

            System.err.println(cum - cfv);
            System.out.println();
        }
    }

    private void runCR_mix() {
        OOSAlgorithmData.useEpsilonRM = true;
        OOSAlgorithmData.epsilon = 0.00001f;

        // CFR-specific settings
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "1000"));
        int iterationsLevel2 = new Integer(getenv("iterationsLevel2", "100000"));
        int iterationsLevel3 = new Integer(getenv("iterationsLevel3", "100000"));
        int targetPSId = new Integer(getenv("targetPSId", "5"));
        boolean subtreeResolving = new Boolean(getenv("subtreeResolving", "true"));
        int resolvingPlayerIdx = new Integer(getenv("resolvingPlayer", "0"));
        ResolvingMethod resolvingMethod2 = ResolvingMethod.fromString(getenv("resolvingMethod2", "RESOLVE_MCCFR"));
        ResolvingMethod resolvingMethod3 = ResolvingMethod.fromString(getenv("resolvingMethod3", "RESOLVE_MCCFR"));
        ResolvingMethod rootMethod = ResolvingMethod.fromString(getenv("rootMethod", "RESOLVE_CFR"));

        Player resolvingPlayer = rootState.getAllPlayers()[resolvingPlayerIdx];
        MCTSConfig config = ((MCTSConfig) expander.getAlgorithmConfig());
        Random rnd = config.getRandom();

        // Alg init
        CRAlgorithm mccrAlg = new CRAlgorithm(rootState, expander, 0.6);
        mccrAlg.defaultResolvingMethod = RESOLVE_CFR;
        mccrAlg.setDoResetData(false);
        InnerNode rootNode = mccrAlg.getRootNode();

        // Target ps
        buildCompleteTree(rootNode);
        Set<MCTSPublicState> publicStates = config.getAllPublicStates();
        MCTSPublicState targetPS = publicStates.stream().filter(
                ps -> ps.getPSKey().getHash() == targetPSId).findFirst().get();

        // Run root
        mccrAlg.runRoot(rootMethod, resolvingPlayer, rootNode, iterationsInRoot);
        Map<ISKey, Map<Action, Double>> solvedBehavCFR = getBehavioralStrategy(rootNode);
        Exploitability exp = calcExploitability(solvedBehavCFR);
        System.err.println("Root CFR exploitability: expl,expl0,expl1");
        System.err.println(exp.total() + "," + exp.expl0 + "," + exp.expl1);
        System.err.println("Game value for resolving player: " + mccrAlg.rootCfrData.historyExpValues.get(rootNode));

        // Prepare resolving
        System.err.println("Running resolving");
        long seed = 0;
        for (long i = 0; i < 100; i++) {
            seed = i;
            rnd.setSeed(seed);

            // prepare node reach pr / exp values
            targetPS.resetData(true);
            targetPS.setResolvingIterations(iterationsInRoot);
            targetPS.setResolvingMethod(rootMethod);
            updateCFRResolvingData(targetPS, mccrAlg.rootCfrData.reachProbs, mccrAlg.rootCfrData.historyExpValues);

            repeatedEvaluation(seed, targetPS, iterationsInRoot,
                    resolvingMethod2, resolvingMethod3,
                    iterationsLevel2, iterationsLevel3,
                    resolvingPlayer, mccrAlg,
                    solvedBehavCFR, rootNode, subtreeResolving);
        }
    }

    private void repeatedEvaluation(long seed,
                                    PublicState targetPS,
                                    int iterationsInRoot,
                                    ResolvingMethod resolvingMethod2,
                                    ResolvingMethod resolvingMethod3,
                                    int iterationsLevel2,
                                    int iterationsLevel3,
                                    Player resolvingPlayer,
                                    CRAlgorithm mccrAlg,
                                    Map<ISKey, Map<Action, Double>> solvedBehavCFR,
                                    InnerNode rootNode,
                                    boolean subtreeResolving) {
        System.err.println("########################################################################");
        System.err.println("using seed " + seed);

        // run resolving
        ResolvingMethod resolvingMethod;
        int iterationsPerGadgetGame = 0;
        Exploitability exp;
        Map<ISKey, Map<Action, Double>> copyCFR;
        Map<ISKey, Map<Action, Double>> behavMCCR;
        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.add(targetPS);
        while (!q.isEmpty()) {
            PublicState ps = q.removeFirst();
            InnerNode node = ps.getAllNodes().iterator().next();

            if (ps.equals(targetPS)) {
                resolvingMethod = resolvingMethod2;
                iterationsPerGadgetGame = iterationsLevel2;
            } else {
                resolvingMethod = resolvingMethod3;
                iterationsPerGadgetGame = iterationsLevel3;
            }
            mccrAlg.runStep(resolvingPlayer, node, resolvingMethod,
                    iterationsPerGadgetGame, iterationsInRoot);

            copyCFR = cloneBehavStrategy(solvedBehavCFR);
            behavMCCR = getBehavioralStrategy(rootNode);
            substituteStrategy(copyCFR, behavMCCR, targetPS);
            exp = calcExploitability(copyCFR);
            System.err.println("seed;iterationsPerGadgetGame;subtreeResolving;currentPS;expl0;expl1;total");
            System.err.println(seed + ";" + iterationsPerGadgetGame + ";" + subtreeResolving + ";" + ps + ";" + exp.expl0 + ";" + exp.expl1 + ";" + exp.total());

            if (subtreeResolving) {
                q.addAll(ps.getNextPlayerPublicStates());
            }
        }

        // evaluate expl
        copyCFR = cloneBehavStrategy(solvedBehavCFR);
        behavMCCR = getBehavioralStrategy(rootNode);
        substituteStrategy(copyCFR, behavMCCR, targetPS);
        exp = calcExploitability(copyCFR);
        System.err.println("seed;iterationsPerGadgetGame;subtreeResolving;targetPSId;expl0;expl1;total");
        System.out.println(seed + ";" + iterationsPerGadgetGame + ";" + subtreeResolving + ";" + targetPS.hashCode() + ";" + exp.expl0 + ";" + exp.expl1 + ";" + exp.total());
    }

    protected Exploitability calcExploitability(Map<ISKey, Map<Action, Double>> solvedBehavCFR) {
        Strategy strategy0 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                solvedBehavCFR, rootState, expander, rootState.getAllPlayers()[0]);
        Strategy strategy1 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                solvedBehavCFR, rootState, expander, rootState.getAllPlayers()[1]);

        double br1Val = brAlg1.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy0));
        double br0Val = brAlg0.calculateBR(rootState, ISMCTSExploitability.filterLow(strategy1));
        return new Exploitability(br1Val, br0Val);
    }

    protected Map<ISKey, Map<Action, Double>> cloneBehavStrategy(Map<ISKey, Map<Action, Double>> orig) {
        Map<ISKey, Map<Action, Double>> out = new HashMap<>();

        for (Map.Entry<ISKey, Map<Action, Double>> entry : orig.entrySet()) {
            Map<Action, Double> dist = new HashMap<Action, Double>();

            for (Map.Entry<Action, Double> act : entry.getValue().entrySet()) {
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
        while (!q.isEmpty()) {
            PublicState ps = q.removeFirst();
            traversed.add(ps);
            Set<PublicState> nextStates = ps.getNextPublicStates();

            int id = ps.getAllNodes().iterator().next().getPlayerToMove().getId();
            if (id == 2) assert nextStates.size() == 1;
            if (id == 0) assert nextStates.size() == 1;
            if (id == 1) assert nextStates.size() == 3 || nextStates.size() == 0;

            for (PublicState n : ps.getNextPublicStates()) {
                System.out.println(
                        "\t\"ps " + ps.getPSKey().getHash() + "\" -- \"ps " + n.getPSKey().getHash() + "\";");
                q.add(n);
            }
        }
        System.out.println("}");
    }

    protected Map<ISKey, Map<Action, Double>> getBehavioralStrategy(InnerNode rootNode) {
        Map<ISKey, Map<Action, Double>> out = new HashMap<>();

        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.add(rootNode);

        while (!q.isEmpty()) {
            InnerNode curNode = q.removeFirst();
            MCTSInformationSet curNodeIS = curNode.getInformationSet();
            if (curNodeIS == null) {
                assert (curNode.getGameState().isPlayerToMoveNature());
            } else {
                OOSAlgorithmData data = ((OOSAlgorithmData) curNodeIS.getAlgorithmData());
                Map<Action, Double> dist = new MeanStratDist().getDistributionFor(data);
                out.put(curNodeIS.getISKey(), dist);
            }

            for (Node n : curNode.getChildren().values()) {
                if ((n instanceof InnerNode)) q.addLast((InnerNode) n);
            }
        }
        return out;
    }

    private String getenv(String env, String def) {
        return System.getenv(env) == null ? def : System.getenv(env);
    }

    protected void substituteStrategy(Map<ISKey, Map<Action, Double>> target, Map<ISKey, Map<Action, Double>> replacement,
                                    PublicState replaceStartingAtPublicPlace) {
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.addAll(replaceStartingAtPublicPlace.getAllNodes());

        while (!q.isEmpty()) {
            InnerNode curNode = q.removeFirst();
            MCTSInformationSet curNodeIS = curNode.getInformationSet();
            if (curNodeIS == null) {
                assert (curNode.getGameState().isPlayerToMoveNature());
            } else {
                // substitute
                ISKey key = curNodeIS.getISKey();
                if (target.containsKey(key) && replacement.containsKey(key)) {
                    Map<Action, Double> oldvalue = target.get(key);
                    Map<Action, Double> newvalue = replacement.get(key);
                    target.put(key, newvalue);
                }
            }

            for (Node n : curNode.getChildren().values()) {
                if ((n instanceof InnerNode)) q.addLast((InnerNode) n);
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
                printSeveralFirstInfoSets((InnerNode) node, maxDepth - 1, maxNames, uniqueISNames);
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

    protected class Exploitability {
        public double expl0;
        public double expl1;

        public Exploitability(double expl0, double expl1) {
            this.expl0 = expl0;
            this.expl1 = expl1;
        }

        public double total() {
            return expl0 + expl1;
        }

        @Override
        public String toString() {
            return "Exploitability{" +
                    "expl0=" + expl0 +
                    ", expl1=" + expl1 +
                    '}';
        }
    }
}
