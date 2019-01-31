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
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInfoSet;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerNode;
import cz.agents.gtlibrary.algorithms.mcts.*;
import cz.agents.gtlibrary.algorithms.mcts.distribution.CurrentStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSSimulator;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.RMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTBackPropFactory;
import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.domain.goofspiel.GSGameInfo;
import cz.agents.gtlibrary.domain.liarsdice.LDGameInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTInfo;
import cz.agents.gtlibrary.domain.phantomTTT.TTTState;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.randomgame.RandomGameInfo;
import cz.agents.gtlibrary.domain.rps.RPSGameInfo;
import cz.agents.gtlibrary.domain.rps.RPSGameState;
import cz.agents.gtlibrary.iinodes.*;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.strategy.StrategyImpl;
import cz.agents.gtlibrary.strategy.UniformStrategyForMissingSequences;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.MTRandom;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;

import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.updateCFRResolvingData;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_NUM_SAMPLES;
import static cz.agents.gtlibrary.algorithms.cr.CRAlgorithm.Budget.BUDGET_TIME;
import static cz.agents.gtlibrary.algorithms.cr.ResolvingMethod.RESOLVE_CFR;
import static cz.agents.gtlibrary.algorithms.cr.ResolvingMethod.RESOLVE_MCCFR;
import static cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerNode.*;
import static cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl.BASELINE_NONE;
import static cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl.BASELINE_ORACLE;
import static cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl.BASELINE_UTILITY_WEIGHTED_ALL;

import static cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl.BASELINE_UTILITY_WEIGHTED_PL;
import static cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl.BASELINE_UTILITY_TIME;
import static cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl.BASELINE_UTILITY_CONST;
import static cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm.*;


public class CRExperiments {

    private final Long seed;
    protected SQFBestResponseAlgorithm brAlg0;
    protected SQFBestResponseAlgorithm brAlg1;

    private GamePlayingAlgorithm alg;
    private String domain;
    private String[] domainParams;

    public static boolean safeResolving = true;


    public CRExperiments(Long seed) {
        this.seed = seed;
        System.err.println("Using seed " + seed);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Missing Arguments: CRExperiments " +
                    "[seed] {experiment_name} {LD|IIGS|GP|BRPS|PTTT} [domain parameters ... ].");
            System.exit(-1);
        }

        long seed = new Long(args[0]);
        String alg = args[1];
        String domain = args[2];

        Random rnd = new MTRandom(seed);

        CRExperiments exp = new CRExperiments(seed);
        exp.domain = domain;
        exp.domainParams = Arrays.copyOfRange(args, 3, args.length);
        exp.prepareDomain(domain, Arrays.copyOfRange(args, 3, args.length));
        Game game = exp.createGame(domain, rnd);
        exp.runAlgorithm(alg, game);
    }

    public static void buildCompleteTree(InnerNode r) {
        buildCompleteTree(r, Integer.MAX_VALUE);
    }

    public static void buildCompleteTree(InnerNode r, Integer maxDepth) {
        System.err.println("Building complete tree to max depth " + maxDepth);
        MCTSConfig config = ((MCTSConfig) r.getExpander().getAlgorithmConfig());
        boolean useEpsilonRM = config.useEpsilonRM;
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.add(r);

        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            MCTSInformationSet is = n.getInformationSet();
            if (!(n instanceof ChanceNode)) {
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions(), useEpsilonRM));
                }
            }

            if (n.getDepth() < maxDepth) {
                for (Action a : n.getActions()) {
                    Node ch = n.getChildFor(a);
                    if (ch instanceof InnerNode) {
                        q.add((InnerNode) ch);
                    }
                }
            }
        }
        System.err.println("Created nodes: " + nodes + "; infosets: " + infosets +"; public states: "+config.getAllPublicStates().size());
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
                    GSGameInfo.CARDS_FOR_PLAYER = new int[]{1, 2, 3, 4};
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

            case "RG":  // Random Games
                if (domainParams.length != 7) {
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
                RandomGameInfo.UTILITY_CORRELATION = new Boolean(domainParams[6]);
                RandomGameInfo.MAX_OBSERVATION = 3;
                RandomGameInfo.MAX_UTILITY = 10;
                break;
            case "PTTT":  // Phantom Tic tac toe
                TTTInfo.useDomainDependentExpander = true;
                TTTInfo.restrictWinningMoves = true;
                TTTState.skewed = false;
                TTTState.forceFirstMoves = false;
                break;
            case "RPS":
                if (domainParams.length != 1) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "1 parameter is required {SEED}");
                }
                RPSGameInfo.seed = new Integer(domainParams[0]);
                RPSGameInfo.biasing = 1.;
                break;
            case "BRPS":
                if (domainParams.length != 2) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "2 parameters are required {SEED} {BIASING}");
                }
                RPSGameInfo.seed = new Integer(domainParams[0]);
                RPSGameInfo.biasing = new Double(domainParams[1]);
                break;
            case "ML":
                ;
                if (domainParams.length != 0) {
                    throw new IllegalArgumentException("Illegal domain arguments count: " +
                            "0 parameters are required");
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal domain: " + domainParams[1]);
        }
    }

    public Game createGame(String domain, Random rnd) {
        Game g = new Game(domain, rnd);
        System.err.println(g.gameInfo.getInfo());

        // prepare BR
        brAlg0 = new SQFBestResponseAlgorithm(g.expander, 0,
                new Player[]{g.rootState.getAllPlayers()[0], g.rootState.getAllPlayers()[1]},
                (ConfigImpl) g.expander.getAlgorithmConfig()/*sfAlgConfig*/, g.gameInfo);
        brAlg1 = new SQFBestResponseAlgorithm(g.expander, 1,
                new Player[]{g.rootState.getAllPlayers()[0], g.rootState.getAllPlayers()[1]},
                (ConfigImpl) g.expander.getAlgorithmConfig()/*sfAlgConfig*/, g.gameInfo);

        if (domain.equals("RG")) {
            InnerNode rootNode = g.getRootNode();
            buildCompleteTree(rootNode);
            PublicTreeGenerator.constructPublicTree(rootNode);
        }

        return g;
    }

    public void runAlgorithm(String alg, Game game) {
        System.err.println("Using algorithm " + alg);

        if (alg.equals("CFVgadget")) {
            runCFVgadget(game);
            return;
        }
        if (alg.equals("MCCR")) {
            runMCCR(game);
            return;
        }
        if (alg.equals("MCCRavg")) {
            runMCCRavg(game);
            return;
        }
        if (alg.equals("OOSavg")) {
            runOOSavg(game);
            return;
        }
        if (alg.equals("CFR")) {
            runCFR(game);
            return;
        }
        if (alg.equals("CR-mix")) {
            runCR_mix(game);
            return;
        }
        if (alg.equals("uniform")) {
            runUniform(game);
            return;
        }
        if (alg.equals("stats")) {
            runStats(game);
            return;
        }
        if (alg.equals("match")) {
            runMatch(game);
            return;
        }
        if (alg.equals("firstMoves")) {
            runFirstMoves(game);
            return;
        }
        if (alg.equals("gambit")) {
            runGambit(game);
            return;
        }
        if (alg.equals("CRrootCFRresolvingCFR")) {
            runCRrootCFRresolvingCFR(game);
            return;
        }
        if (alg.equals("sampleRoot")) {
            runSampleRoot(game);
            return;
        }
        if (alg.equals("baselines")) {
            runBaselines(game);
            return;
        }

        System.err.println("No such algorithm found!");
        System.exit(1);
    }

    private void runSampleRoot(Game g) {
        double epsilonExploration = new Double(getenv("epsExploration", "0.6"));
        int iters = new Integer(getenv("iters", "1000"));

        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);
        Player resolvingPlayer = g.rootState.getAllPlayers()[0];

        InnerNodeImpl.baselineMethod = InnerNodeImpl.BASELINE_UTILITY_WEIGHTED_ALL;
        InnerNodeImpl.shouldCacheOracleValue = true;

        OOSFixedStrategy alg = new OOSFixedStrategy(resolvingPlayer, new OOSSimulator(g.expander), g.rootState,
                g.expander, 0., epsilonExploration);
        alg.saveEVTime = true;
        alg.saveEVWeightedPl = true;
        alg.saveEVWeightedAll = true;

        this.alg = alg;

        double expValue = CFRAlgorithm.computeExpUtilityOfState(alg.getRootNode(), resolvingPlayer);
        alg.expValue = expValue;
//        System.err.println("exp value: "+expValue);
        System.out.println("n;orig_x;orig_mean;orig_var;orig_diff;vr_x;vr_mean;vr_var;vr_diff");
//        System.out.println("n;orig_diff;vr_diff");
        alg.runIterations(iters);
    }

    private void runGambit(Game g) {
        InnerNode rootNode = g.getRootNode();
        String name = g.expander.getClass().getSimpleName() + "_" + domain;
        name += "_" + Arrays.stream(domainParams).reduce("", String::concat);

        buildCompleteTree(rootNode);

        GambitEFG gambit = new GambitEFG();
        gambit.wISKeys = false;
        gambit.write(name + " _PS.gbt", rootNode);
        gambit.wISKeys = true;
        gambit.write(name + "_IS.gbt", rootNode);
        gambit.wAugISKeys = true;
        gambit.write(name + "_AugIS.gbt", rootNode);
    }

    private void runStats(Game g) {
        CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander);
        int maxDepth = 100;
        buildCompleteTree(alg.getRootNode(), maxDepth);

        MCTSConfig config = alg.getRootNode().getAlgConfig();

        ArrayList<InnerNode> innerNodes = new ArrayList<>();
        config.getAllInformationSets().values().stream()
                .map(MCTSInformationSet::getAllNodes)
                .forEach(innerNodes::addAll);
        long innerCnt = innerNodes.size();

        Long leafsCnt = config.getAllInformationSets().values().stream()
                .map(MCTSInformationSet::getAllNodes)
                .map(setIN -> setIN.stream()
                        .map(InnerNode::getChildren)
                        .map(Map::values)
                        .map(mapCh -> mapCh.stream().filter(Node::isGameEnd).count())
                        .reduce(0L, Long::sum))
                .reduce(0L, Long::sum);

        ArrayList<GadgetInfoSet> augIS = new ArrayList<>();
        config.getAllPublicStates().stream()
                .filter(ps -> ps.getPlayer().getId() <= 1) // exclude chance
                .map(ps -> ps.getSubgame().getGadgetInformationSets())
                .forEach(augIS::addAll);


        System.err.println("Game has: \n" +
                        "public states & info sets " +
//                "& aug info sets " +
                        "& inner nodes & leaf nodes " +
//                "& max PT depth"
                        "& omega" +
                        "& strategySize"
                          );

        double psCnt = config.getAllPublicStates().size();
        double isCnt = config.getAllInformationSets().size();
        int augIsSize = augIS.size();
        int omega = getTargetPsGadgetIs(getTargetPs(g, alg.getRootNode(), g.rootState.getAllPlayers()[0]))
                .values().stream().map(List::size).reduce(0, Integer::sum);
        int strategySize = config.getAllInformationSets().values().stream()
                .map(is -> is.getActions().size()-1).reduce(0, Integer::sum);

        // total counts
        System.out.println(
            psCnt + " & " +
            isCnt + " & " +
            innerCnt + " & " +
            leafsCnt + " & " +
            omega + " & " +
            strategySize
        );

        // depth-limited counts
        for (int depth = 0; depth < maxDepth; depth++) {
            int finalDepth = depth;

            psCnt = config.getAllPublicStates().stream()
                    .filter(ps -> ps.getDepth() == finalDepth)
                    .count();
            isCnt = config.getAllInformationSets().values().stream()
                    .map(is->is.getAllNodes().iterator().next())
                    .filter(n -> n.getDepth() == finalDepth)
                    .count();
            innerCnt = innerNodes.stream()
                    .filter(n -> n.getDepth() == finalDepth)
                    .count();
            strategySize = config.getAllInformationSets().values().stream()
                    .filter(is->is.getAllNodes().iterator().next().getDepth() == finalDepth)
                    .map(is -> is.getActions().size()-1).reduce(0, Integer::sum);
            System.out.println(
                    finalDepth + " & " +
                psCnt + " & " +
                isCnt + " & " +
                innerCnt + " & " +
                strategySize
            );
        }

    }

    private double calcExploitability(Game g, InnerNode rootNode) {
        return calcExploitability(g, rootNode, new MeanStratDist());
    }

    private double calcExploitability(Game g, InnerNode rootNode, Distribution dist) {
        Strategy strategy0 = StrategyCollector.getStrategyFor(
                rootNode, g.rootState.getAllPlayers()[0], dist);
        Strategy strategy1 = StrategyCollector.getStrategyFor(
                rootNode, g.rootState.getAllPlayers()[1], dist);

        Double br1Val = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy0));
        Double br0Val = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy1));
        return br0Val + br1Val;
    }


    private void runCFVgadget(Game g) {
        double epsilonExploration = new Double(getenv("epsExploration", "0.6"));
        double memoryLimit = new Double(getenv("memoryLimit", "3.8")) * 1e+9; // in GB
        boolean printHeader = new Boolean(getenv("printHeader", "false"));
        boolean calcExploitability = new Boolean(getenv("calcExploitability", "false"));
        boolean calcTarget = new Boolean(getenv("calcTarget", "false"));
        int runMinutes = new Integer(getenv("runMinutes", "15"));
        int maxIters = new Integer(getenv("maxIters", "1000000000"));
        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);
        Player resolvingPlayer = g.rootState.getAllPlayers()[0];

        InnerNodeImpl.baselineMethod = BASELINE_ORACLE;
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, new OOSSimulator(g.expander), g.rootState,
                g.expander, 0., epsilonExploration);
        alg.useRegretMatchingPlus = false;
        alg.saveEVTime = true;
        alg.saveEVWeightedPl = true;
        this.alg = alg;

        InnerNode rootNode = alg.getRootNode();
        if (calcTarget || calcExploitability) buildCompleteTree(rootNode);
        else buildCompleteTree(rootNode, 6);

        Set<PublicState> targetPs = getTargetPs(g, rootNode, resolvingPlayer);
        Set<InnerNode> targetPsNodes = new HashSet<>();
        targetPs.forEach(ps -> targetPsNodes.addAll(ps.getAllNodes()));
        Map<PublicState, List<GadgetInfoSet>> targetPsGadgetIs = getTargetPsGadgetIs(targetPs);

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        double loop = 0.;
        int total = 0;
        Double exploitability = 0.;
        long runningTime = 0;
        long allocatedMemory = 0;

        if (printHeader) {
            System.out.print("seed;iterations;runningTime");
            if(calcExploitability) System.out.print(";expl");
            targetPsGadgetIs.forEach((ps, gadgetIs) -> {
                System.out.print(";ps_" + ps.hashCode());
                int i = 0;
                for (GadgetInfoSet gadgetI : gadgetIs) {
                    System.out.print(
                            ";is_reach_" + ps.hashCode() + "_" + i +
                            ";is_cfv_" + ps.hashCode() + "_" + i +
                            ";is_cfv2_" + ps.hashCode() + "_" + i);
                    if (calcTarget) System.out.print(";is_cfv3_" + ps.hashCode() + "_" + i);
                    i++;
                }
            });
            System.out.println();
        }

        do {
            int iters = (int) Math.floor(Math.pow(10., 1 + loop / 10.)) - total;
            iters += (iters % 2 == 0) ? 0 : 1;
            long time = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(iters);
            runningTime += threadBean.getCurrentThreadCpuTime() - time;
            total += iters;

            if (calcExploitability) exploitability = calcExploitability(g, rootNode);
            int finalTotal = total;

            System.out.print(seed + ";" + total + ";" + runningTime);
            if (calcExploitability) System.out.print(";" + exploitability);

            CRAlgorithm.updatePlayerRp(rootNode.getPublicState(), resolvingPlayer, targetPsNodes);

            targetPsGadgetIs.forEach((ps, gadgetIs) -> {
                System.out.print(";" + ps.hashCode());
                for (GadgetInfoSet gadgetI : gadgetIs) {
                    System.out.print(";" + gadgetI.getIsReach());
                    System.out.print(";" + gadgetI.getCFVWeightedPl());
                    System.out.print(";" + gadgetI.getCFVTime(finalTotal));
                    if (calcTarget) System.out.print(";" + gadgetI.getCFVExact(finalTotal));
                }
            });
            System.out.println();
            loop++;


            Runtime runtime = Runtime.getRuntime();
            allocatedMemory = runtime.totalMemory();
        } while (total < maxIters && allocatedMemory < memoryLimit && runningTime < runMinutes * 60 * 1e9);

        if (allocatedMemory >= memoryLimit) System.err.println("exited due to memoryout");
        if (runningTime >= runMinutes * 60 * 1e9) System.err.println("exited due to timeout");
        if (total >= maxIters) System.err.println("exited due to max iters");
    }


    private void runBaselines(Game g) {
        int maxIters = new Integer(getenv("maxIters", "1000000"));
        String baselineMethod = getenv("baselineMethod", "BASELINE_NONE");
        String avgStrategy = getenv("avgStrategy", "AVG_STRATEGY_UNIFORM");
        Boolean rmPlus = new Boolean(getenv("rmPlus", "true"));
        System.err.println("Using baselineMethod = "+baselineMethod +" avgStrategy = "+avgStrategy +" rmPlus = "+rmPlus);

        switch(baselineMethod) {
            case "BASELINE_NONE": InnerNodeImpl.baselineMethod = BASELINE_NONE; break;
            case "BASELINE_UTILITY_WEIGHTED_PL": InnerNodeImpl.baselineMethod = BASELINE_UTILITY_WEIGHTED_PL; break;
            case "BASELINE_UTILITY_WEIGHTED_ALL": InnerNodeImpl.baselineMethod = BASELINE_UTILITY_WEIGHTED_ALL; break;
            case "BASELINE_UTILITY_TIME": InnerNodeImpl.baselineMethod = BASELINE_UTILITY_TIME; break;
            case "BASELINE_UTILITY_CONST": InnerNodeImpl.baselineMethod = BASELINE_UTILITY_CONST; break;
            case "BASELINE_ORACLE": InnerNodeImpl.baselineMethod = BASELINE_ORACLE; break;
        }

        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);
        Player resolvingPlayer = g.rootState.getAllPlayers()[0];

        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, new OOSSimulator(g.expander),
                g.rootState, g.expander, 0., 0.6);
        alg.useRegretMatchingPlus = rmPlus;

        switch(avgStrategy) {
            case "AVG_STRATEGY_UNIFORM": alg.avgStrategyComputation = AVG_STRATEGY_UNIFORM; break;
            case "AVG_STRATEGY_LINEAR": alg.avgStrategyComputation = AVG_STRATEGY_LINEAR; break;
            case "AVG_STRATEGY_SQUARE": alg.avgStrategyComputation = AVG_STRATEGY_SQUARE; break;
            case "AVG_STRATEGY_XLOGX": alg.avgStrategyComputation = AVG_STRATEGY_XLOGX; break;
        }

        alg.saveEVTime = true;
        alg.saveEVWeightedPl = true;
        this.alg = alg;

        InnerNode rootNode = alg.getRootNode();
        buildCompleteTree(rootNode);

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        double loop = 0.;
        int total = 0;
        long runningTime = 0;
        Double exploitabilityAvg, exploitabilityCur;
//        System.out.println("seed;iterations;runningTime;expl_avg;expl_cur");

        do {
            int iters = (int) Math.floor(Math.pow(10., 1 + loop / 20.)) - total;
            iters += (iters % 2 == 0) ? 0 : 1;
            long time = threadBean.getCurrentThreadCpuTime();
            alg.runIterations(iters);
            runningTime += threadBean.getCurrentThreadCpuTime() - time;
            total += iters;

            exploitabilityAvg = calcExploitability(g, rootNode, new MeanStratDist());
            exploitabilityCur = calcExploitability(g, rootNode, new CurrentStratDist());
            System.out.println(seed + ";" + total + ";" + runningTime + ";" + exploitabilityAvg +";"+exploitabilityCur);
            loop++;
        } while (total < maxIters);
    }

    private void runMCCR(Game g) {
        double epsExploration = new Double(getenv("epsExploration", "0.6"));
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "100000"));
        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
        boolean resetData = new Boolean(getenv("resetData", "true"));
        int player = new Integer(getenv("player", "0"));
        safeResolving = new Boolean(getenv("safeResolving", "true"));


        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);
        CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander, epsExploration);
//        alg.budgetRoot = BUDGET_NUM_SAMPLES;
//        alg.budgetGadget = BUDGET_NUM_SAMPLES;
        alg.budgetRoot = BUDGET_TIME;
        alg.budgetGadget = BUDGET_TIME;
        alg.setDoResetData(resetData);
        this.alg = alg;

        GadgetChanceNode.useRootResolving = true;
        GadgetChanceNode.rootResolvingEpsilon = 0.1;

        InnerNode rootNode = alg.solveEntireGame(g.rootState.getAllPlayers()[player], iterationsInRoot,
                iterationsPerGadgetGame);


        Strategy strategy0 = StrategyCollector.getStrategyFor(
                rootNode, g.rootState.getAllPlayers()[0], new MeanStratDist());
        Strategy strategy1 = StrategyCollector.getStrategyFor(
                rootNode, g.rootState.getAllPlayers()[1], new MeanStratDist());

        Double br1Val = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy0));
        Double br0Val = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy1));

        double gameValue = 0.; // for player 0
        if (g.rootState instanceof RPSGameState) {
            gameValue = 1 / 3. - 1 / (RPSGameInfo.biasing + 2);
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

    private void runMCCRavg(Game game) {
        double epsExploration = new Double(getenv("epsExploration", "0.6"));
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "100000"));
        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
        safeResolving = new Boolean(getenv("safeResolving", "true"));
        String resolvingCFVOption = getenv("resolvingCFV", "weighted");
        int numSeeds = new Integer(getenv("numSeeds", "30"));
        boolean resetData = new Boolean(getenv("resetData", "true"));
        boolean resolveTime = new Boolean(getenv("resolveTime", "false"));
        int player = new Integer(getenv("player", "0"));
        boolean printHeader = new Boolean(getenv("printHeader", "false"));

        if(resolvingCFVOption.equals("time")) GadgetInnerNode.resolvingCFV = RESOLVE_TIME;
        if(resolvingCFVOption.equals("weighted")) GadgetInnerNode.resolvingCFV = RESOLVE_WEIGHTED_PL;
        if(resolvingCFVOption.equals("exact")) GadgetInnerNode.resolvingCFV = RESOLVE_EXACT;
        if(resolvingCFVOption.equals("fixed")) GadgetInnerNode.resolvingCFV = RESOLVE_FIXED;

        GadgetChanceNode.useRootResolving = true;
        GadgetChanceNode.rootResolvingEpsilon = 0.1;

        UniformStrategyForMissingSequences strategy0;
        UniformStrategyForMissingSequences strategy1;
        UniformStrategyForMissingSequences cumulativeStrategy0 = null;
        UniformStrategyForMissingSequences cumulativeStrategy1 = null;
        InnerNode rootNode;
        double avg_expl0_cur = 0.;
        double avg_expl1_cur = 0.;
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        for (int seed = 1; seed <= numSeeds; seed++) {
            Game g = game.clone(new MTRandom(seed));
            g.config.createInformationSetFor(g.rootState);
            CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander, epsExploration);
            alg.seed = seed;
            alg.setDoResetData(resetData);
            if(resolveTime) {
                alg.budgetRoot = BUDGET_TIME;
                alg.budgetGadget = BUDGET_TIME;
            } else {
                alg.budgetRoot = BUDGET_NUM_SAMPLES;
                alg.budgetGadget = BUDGET_NUM_SAMPLES;
            }
            Player player0 = g.rootState.getAllPlayers()[0];
            Player player1 = g.rootState.getAllPlayers()[1];

            long time = threadBean.getCurrentThreadCpuTime();
            rootNode = alg.solveEntireGame(g.rootState.getAllPlayers()[player], iterationsInRoot,
                    iterationsPerGadgetGame);
            double resolvingTime = (threadBean.getCurrentThreadCpuTime() - time) / 1e6; // in ms

            strategy0 = StrategyCollector.getStrategyFor(rootNode, g.rootState.getAllPlayers()[0], new MeanStratDist());
            strategy1 = StrategyCollector.getStrategyFor(rootNode, g.rootState.getAllPlayers()[1], new MeanStratDist());
            if (cumulativeStrategy0 == null) {
                cumulativeStrategy0 = strategy0;
                cumulativeStrategy1 = strategy1;
            } else {
                accumulateStrategy(cumulativeStrategy0, strategy0);
                accumulateStrategy(cumulativeStrategy1, strategy1);
            }

            Strategy avgStrategy0 = normalizeStrategy(player0, rootNode, cumulativeStrategy0);
            Strategy avgStrategy1 = normalizeStrategy(player1, rootNode, cumulativeStrategy1);

            double gameValue = 0.; // for player 0
            if (g.rootState instanceof RPSGameState) {
                gameValue = 1 / 3. - 1 / (RPSGameInfo.biasing + 2);
            }

            double br1Val_cur = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy0));
            double br0Val_cur = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy1));
            double exploitability_cur = br0Val_cur + br1Val_cur;
            double expl0_cur = gameValue + br1Val_cur;
            double expl1_cur = -gameValue + br0Val_cur;

            double br1Val_avg = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(avgStrategy0));
            double br0Val_avg = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(avgStrategy1));
            double exploitability_avg = br0Val_avg + br1Val_avg;
            double expl0_avg = gameValue + br1Val_avg;
            double expl1_avg = -gameValue + br0Val_avg;

            avg_expl0_cur = avg_expl0_cur + (expl0_cur - avg_expl0_cur) / seed;
            avg_expl1_cur = avg_expl1_cur + (expl1_cur - avg_expl1_cur) / seed;

            if(printHeader) {
                System.err.println("seed,epsExploration,iterationsInRoot,iterationsPerGadgetGame," +
                        "resetData,player,resolvingTime," +
                        "expl0_avg,expl1_avg,exploitability_avg," +
                        "expl0_cur,expl1_cur,exploitability_cur,avg_expl0_cur,avg_expl1_cur");
            }
            System.out.println(seed + ","
                    + epsExploration + ","
                    + iterationsInRoot + ","
                    + iterationsPerGadgetGame + ","
                    + resetData + ","
                    + player + ","
                    + resolvingTime + ","
                    + expl0_avg + ","
                    + expl1_avg + ","
                    + exploitability_avg + ","
                    + expl0_cur + ","
                    + expl1_cur + ","
                    + exploitability_cur + ","
                    + avg_expl0_cur + ","
                    + avg_expl1_cur);

            assert (player == 0 && expl0_avg < avg_expl0_cur + 1e-9)
                    || (player == 1 && expl1_avg < avg_expl1_cur + 1e-9);
        }
    }

    private void runOOSavg(Game game) {
        double epsExploration = new Double(getenv("epsExploration", "0.6"));
        double deltaTargeting = new Double(getenv("deltaTargeting", "0.9"));
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "100000"));
        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
        int numSeeds = new Integer(getenv("numSeeds", "30"));
        int player = new Integer(getenv("player", "0"));
        boolean budgetTime = new Boolean(getenv("budgetTime", "true"));
        boolean printHeader = new Boolean(getenv("printHeader", "false"));

        UniformStrategyForMissingSequences strategy0;
        UniformStrategyForMissingSequences strategy1;
        UniformStrategyForMissingSequences cumulativeStrategy0 = null;
        UniformStrategyForMissingSequences cumulativeStrategy1 = null;
        InnerNode rootNode;
        double avg_expl0_cur = 0.;
        double avg_expl1_cur = 0.;
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        for (int seed = 1; seed <= numSeeds; seed++) {
            Random rnd = new MTRandom(seed);
            Game g = game.clone(rnd);
            g.config.createInformationSetFor(g.rootState);
            OOSAlgorithm alg = new OOSAlgorithm(g.rootState.getAllPlayers()[player], g.getRootNode(), epsExploration, deltaTargeting);
            alg.seed = seed;
            if(budgetTime) {
                alg.budgetRoot = BUDGET_TIME;
                alg.budgetGadget = BUDGET_TIME;
            } else {
                alg.budgetRoot = BUDGET_NUM_SAMPLES;
                alg.budgetGadget = BUDGET_NUM_SAMPLES;
            }
            g.config.useEpsilonRM = true;
            Player player0 = g.rootState.getAllPlayers()[0];
            Player player1 = g.rootState.getAllPlayers()[1];

            long time = threadBean.getCurrentThreadCpuTime();
            rootNode = alg.solveEntireGame(g, g.rootState.getAllPlayers()[player], iterationsInRoot, iterationsPerGadgetGame);
            double resolvingTime = (threadBean.getCurrentThreadCpuTime() - time) / 1e6; // in ms

            strategy0 = StrategyCollector.getStrategyFor(rootNode, g.rootState.getAllPlayers()[0], new MeanStratDist());
            strategy1 = StrategyCollector.getStrategyFor(rootNode, g.rootState.getAllPlayers()[1], new MeanStratDist());
            if (cumulativeStrategy0 == null) {
                cumulativeStrategy0 = strategy0;
                cumulativeStrategy1 = strategy1;
            } else {
                accumulateStrategy(cumulativeStrategy0, strategy0);
                accumulateStrategy(cumulativeStrategy1, strategy1);
            }

            Strategy avgStrategy0 = normalizeStrategy(player0, rootNode, cumulativeStrategy0);
            Strategy avgStrategy1 = normalizeStrategy(player1, rootNode, cumulativeStrategy1);

            double gameValue = 0.; // for player 0
            if (g.rootState instanceof RPSGameState) {
                gameValue = 1 / 3. - 1 / (RPSGameInfo.biasing + 2);
            }

            double br1Val_cur = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy0));
            double br0Val_cur = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy1));
            double exploitability_cur = br0Val_cur + br1Val_cur;
            double expl0_cur = gameValue + br1Val_cur;
            double expl1_cur = -gameValue + br0Val_cur;

            double br1Val_avg = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(avgStrategy0));
            double br0Val_avg = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(avgStrategy1));
            double exploitability_avg = br0Val_avg + br1Val_avg;
            double expl0_avg = gameValue + br1Val_avg;
            double expl1_avg = -gameValue + br0Val_avg;

            avg_expl0_cur = avg_expl0_cur + (expl0_cur - avg_expl0_cur) / seed;
            avg_expl1_cur = avg_expl1_cur + (expl1_cur - avg_expl1_cur) / seed;

            if(printHeader) {
                System.out.println("seed,epsExploration,iterationsInRoot,iterationsPerGadgetGame," +
                        "resetData,player,resolvingTime," +
                        "expl0_avg,expl1_avg,exploitability_avg," +
                        "expl0_cur,expl1_cur,exploitability_cur,avg_expl0_cur,avg_expl1_cur");
            }
            System.out.println(seed + ","
                    + epsExploration + ","
                    + iterationsInRoot + ","
                    + iterationsPerGadgetGame + ","
                    + "false,"
                    + player + ","
                    + resolvingTime + ","
                    + expl0_avg + ","
                    + expl1_avg + ","
                    + exploitability_avg + ","
                    + expl0_cur + ","
                    + expl1_cur + ","
                    + exploitability_cur + ","
                    + avg_expl0_cur + ","
                    + avg_expl1_cur);

            assert (player == 0 && expl0_avg < avg_expl0_cur + 1e-9)
                    || (player == 1 && expl1_avg < avg_expl1_cur + 1e-9);
        }
    }


    private void runMatch(Game g) {
        safeResolving = new Boolean(getenv("safeResolving", "true"));
        Integer preplayTime = new Integer(getenv("preplayTime", "0")); // in ms
        Integer preplayTime1 = new Integer(getenv("preplayTime1", "0")); // in ms
        Integer preplayTime2 = new Integer(getenv("preplayTime2", "0")); // in ms
        Integer roundTime = new Integer(getenv("roundTime", "0")); // in ms
        Integer roundTime1 = new Integer(getenv("roundTime1", "0")); // in ms
        Integer roundTime2 = new Integer(getenv("roundTime2", "0")); // in ms
        Integer rnd1 = new Integer(getenv("rnd1", "0")); // random seed for alg
        Integer rnd2 = new Integer(getenv("rnd2", "0")); // random seed for alg
        String alg1 = getenv("alg1", "MCCR"); // alg name
        String alg2 = getenv("alg2", "MCCR"); // alg name
        Boolean binaryUtils = new Boolean(getenv("binaryUtils", "false"));
        Boolean prettyPrint = new Boolean(getenv("prettyPrint", "false"));
        Boolean debug = new Boolean(getenv("debug", "false"));
        Boolean runTime = new Boolean(getenv("runTime", "true"));

        preplayTime1 = preplayTime1 == 0 ? preplayTime : preplayTime1;
        preplayTime2 = preplayTime2 == 0 ? preplayTime : preplayTime2;
        roundTime1  = roundTime1 == 0 ? roundTime : roundTime1;
        roundTime2  = roundTime2 == 0 ? roundTime : roundTime2;

        System.err.println("params:"+
            " safeResolving="+safeResolving + " " +
            " preplayTime1="+preplayTime1 + " " +
            " preplayTime2="+preplayTime2 + " " +
            " roundTime1="+roundTime1 + " " +
            " roundTime2="+roundTime2 + " " +
            " rnd1="+rnd1 + " " +
            " rnd2="+rnd2 + " " +
            " alg1="+alg1 + " " +
            " alg2="+alg2 + " " +
            " binaryUtils="+binaryUtils + " " +
            " prettyPrint="+prettyPrint);

        Game gs[] = new Game[2];
        gs[0] = g.clone(new MTRandom(rnd1));
        gs[1] = g.clone(new MTRandom(rnd2));

//        buildCompleteTree(gs[0].getRootNode(), 4);
////        buildCompleteTree(gs[1].getRootNode(), 3);
//        GambitEFG gambit = new GambitEFG();
//        gambit.wISKeys = true;
//        gambit.write(
//                gs[0].expander.getClass().getSimpleName() + ".gbt",
//                gs[0].getRootNode(),
//                false);
//
//        gambit.wISKeys = true;
//        gambit.write(
//                gs[0].expander.getClass().getSimpleName() + "_PS.gbt",
//                gs[0].config.getAllPublicStates().iterator().next().getSubgame().getGadgetRoot(),
//                false);


        GamePlayingAlgorithm p1 = initMatchAlg(gs[0], alg1, 0);
        GamePlayingAlgorithm p2 = initMatchAlg(gs[1], alg2, 1);

        if(runTime) {
            p1.runMiliseconds(preplayTime1);
            p2.runMiliseconds(preplayTime2);
        } else {
            p1.runIterations(preplayTime1);
            p2.runIterations(preplayTime2);
        }

        GameState curState = g.rootState.copy();
        int i = 0;
        int p1giveUpAtMove = -1;
        int p2giveUpAtMove = -1;
        int p1breaksAtMove = -1;
        int p2breaksAtMove = -1;
        double pa = 1.;
        double[] p_dist = new double[0];
        int numSamplesDuringRun = 0;
        int numSamplesInCurrentIS = 0;
        int numSamplesInCurrentPS = 0;
        int numNodesTouchedDuringRun = 0;
        int numInfoSets = 0;;
        int numNodes = 0;
        String info = "";
        ArrayList<Move> moves = new ArrayList<>();
        while (!curState.isGameEnd()) {
            Action a = null;
            info = "";

            if (curState.isPlayerToMoveNature()) {
                double r = g.config.getRandom().nextDouble();
                List<Action> actions = g.expander.getActions(curState);
                for (Action ca : actions) {
                    final double ap = curState.getProbabilityOfNatureFor(ca);
                    if (r <= ap) {
                        pa = ap;
                        a = ca;
                        break;
                    }
                    r -= ap;
                }
                p_dist = new double[actions.size()];
                for (int j = 0; j < actions.size(); j++) {
                    p_dist[j] = curState.getProbabilityOfNatureFor(actions.get(j));
                }
                numSamplesDuringRun = 0;
                numSamplesInCurrentIS = 0;
                numSamplesInCurrentPS = 0;
                numNodesTouchedDuringRun = 0;
                numInfoSets = 0;
                numNodes = 0;
            } else if (curState.getPlayerToMove().getId() == 0) {
                if (p1breaksAtMove == -1) {
                    if (p1.getRootNode() != null) { //mainly for the random player
                        MCTSInformationSet curIS = p1.getRootNode().getExpander()
                                .getAlgorithmConfig().getInformationSetFor(curState);
                        p1.setCurrentIS(curIS);
                    }

                    try {
                        if(runTime) {
                            a = p1.runMiliseconds(roundTime1);
                        } else {
                            a = p1.runIterations(roundTime1);
                        }
                        pa = p1.actionChosenWithProb();
                        p_dist = p1.currentISprobDist();
                        info = p1.extraInfo();
                        numSamplesDuringRun = p1.numSamplesDuringRun();
                        numSamplesInCurrentIS = p1.numSamplesInCurrentIS();
                        numSamplesInCurrentPS = p1.numSamplesInCurrentPS();
                        numNodesTouchedDuringRun = p1.numNodesTouchedDuringRun();
                        numInfoSets = gs[0].config.getAllInformationSets().size();
                        numNodes = gs[0].config.getAllInformationSets().values().stream().map(is -> is.getAllNodes().size()).reduce(0, Integer::sum);
                    } catch (Exception e) {
                        a = null;
                        p1breaksAtMove = i;
                        e.printStackTrace();
                        if(debug) throw e;
                    }
                } else { // if it's broken, play randomly
                    a = null;
                }
            } else {
                if (p2breaksAtMove == -1) {
                    if (p2.getRootNode() != null) { //mainly for the random player
                        MCTSInformationSet curIS = p2.getRootNode().getExpander()
                                .getAlgorithmConfig().getInformationSetFor(curState);
                        p2.setCurrentIS(curIS);
                    }

                    try {
                        if(runTime) {
                        a = p2.runMiliseconds(roundTime2);
                        } else {
                            a = p2.runIterations(roundTime2);
                        }
                        pa = p2.actionChosenWithProb();
                        p_dist = p2.currentISprobDist();
                        info = p2.extraInfo();
                        numSamplesDuringRun = p2.numSamplesDuringRun();
                        numSamplesInCurrentIS = p2.numSamplesInCurrentIS();
                        numSamplesInCurrentPS = p2.numSamplesInCurrentPS();
                        numNodesTouchedDuringRun = p2.numNodesTouchedDuringRun();
                        numInfoSets = gs[1].config.getAllInformationSets().size();
                        numNodes = gs[1].config.getAllInformationSets().values().stream().map(is -> is.getAllNodes().size()).reduce(0, Integer::sum);
                    } catch (Exception e) {
                        a = null;
                        p2breaksAtMove = i;
                        e.printStackTrace();
                        if(debug) throw e;
                    }
                } else { // if it's broken, play randomly
                    a = null;
                }
            }

            List<Action> actions = g.expander.getActions(curState);
            if (a == null) {
                a = actions.get(gs[curState.getPlayerToMove().getId()].config.getRandom().nextInt(actions.size()));
                pa = 1. / actions.size();
                p_dist = new double[actions.size()];
                for (int j = 0; j < actions.size(); j++) {
                    p_dist[j] = pa;
                }
                numSamplesDuringRun = 0;
                numSamplesInCurrentIS = 0;
                numSamplesInCurrentPS = 0;
                numNodesTouchedDuringRun = 0;
            } else {
                a = actions.get(actions.indexOf(a));//just to prevent memory leaks
            }

            System.err.println("P" + curState.getPlayerToMove().getId() + " chose: " + a + " with prob="+pa);
            moves.add(new Move(
                    curState.getPlayerToMove().getId(),
                    a.toString(),
                    pa,
                    p_dist,
                    numSamplesDuringRun,
                    numSamplesInCurrentIS,
                    numSamplesInCurrentPS,
                    numNodesTouchedDuringRun,
                    numInfoSets,
                    numNodes,
                    info));

            curState = curState.performAction(a);

            if (p1giveUpAtMove == -1 && p1.hasGivenUp()) p1giveUpAtMove = i;
            if (p2giveUpAtMove == -1 && p2.hasGivenUp()) p2giveUpAtMove = i;
            i++;
        }

        double utils0 = curState.getUtilities()[0];
        double utils1 = curState.getUtilities()[1];
        if (binaryUtils) {
            utils0 = utils0 > 0 ? 1 : utils0 < 1 ? -1 : 0;
            utils1 = utils1 > 0 ? 1 : utils1 < 1 ? -1 : 0;
        }

        if(prettyPrint && (alg1.equals("FIX") || alg2.equals("FIX"))) {
            System.out.println(
                    alg1 + ";" +
                    alg2 + ";" +
                    rnd1 + ";" +
                    rnd2 + ";" +
                    utils0 + ";" +
                    utils1
            );
            System.out.println(moves.stream()
                    .filter(m -> m.player == (alg1.equals("FIX") ? 1 : 0))
                    .map(m ->
                            m.action +"\n"+
                            stratToString(m.p_dist) + "\n"+
                            m.info.replace(" ~ ", "\n")
                                    .replace("p_dist_before: ", "")+"\n"
                        ).reduce("\n", String::concat)

            );
        } else {
            if (debug) {
                System.out.println(
                        alg1 + ";" +
                                alg2 + ";" +
                                rnd1 + ";" +
                                rnd2 + ";" +
                                utils0 + ";" +
                                utils1 + ";" +
                                preplayTime1 + ";" +
                                preplayTime2 + ";" +
                                roundTime1 + ";" +
                                roundTime2 + ";" +
                                p1giveUpAtMove + ";" +
                                p2giveUpAtMove + ";" +
                                p1breaksAtMove + ";" +
                                p2breaksAtMove + ";" +
                                moves.stream().filter(m -> m.player == 0).map(m -> m.prob).reduce(1.,
                                        (a, b) -> a * b) + ";" +
                                moves.stream().filter(m -> m.player == 1).map(m -> m.prob).reduce(1.,
                                        (a, b) -> a * b) + ";" +
                                moves.stream().filter(m -> m.player == 2).map(m -> m.prob).reduce(1.,
                                        (a, b) -> a * b) + ";" +
                                (prettyPrint ? moves.stream().map(
                                        m -> (m.player == 2 ? "\n\n" : "") + m.pretty() + "\n").reduce("\n",
                                        String::concat)
                                        : moves.stream().map(m -> m + ", ").reduce("", String::concat))
                                  );
            } else {
                System.out.println( alg1 + ";" + alg2 + ";" + rnd1 + ";" + rnd2 + ";" + utils0 + ";" + utils1);
            }
        }
    }
    public static String stratToString(double[] arr) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        int idx = 0;
        int len = arr.length-1;

        while(true) {
            stringBuilder.append(String.format("% 2.5f", arr[idx]));
            if (idx == len) {
                return stringBuilder.append(']').toString();
            }

            stringBuilder.append(", ");
            ++idx;
        }
    }


    private void runFirstMoves(Game g) {
        safeResolving = new Boolean(getenv("safeResolving", "true"));
        Integer preplayTime = new Integer(getenv("preplayTime", "0")); // in ms
        Integer preplayTime1 = new Integer(getenv("preplayTime1", "0")); // in ms
        Integer preplayTime2 = new Integer(getenv("preplayTime2", "0")); // in ms
        Integer roundTime = new Integer(getenv("roundTime", "0")); // in ms
        Integer roundTime1 = new Integer(getenv("roundTime1", "0")); // in ms
        Integer roundTime2 = new Integer(getenv("roundTime2", "0")); // in ms
        Integer rnd1 = new Integer(getenv("rnd1", "0")); // random seed for alg
        Integer rnd2 = new Integer(getenv("rnd2", "0")); // random seed for alg
        String alg1 = getenv("alg1", "MCCR"); // alg name
        String alg2 = getenv("alg2", "MCCR"); // alg name
        Boolean binaryUtils = new Boolean(getenv("binaryUtils", "false"));
        Boolean prettyPrint = new Boolean(getenv("prettyPrint", "false"));
        Boolean debug = new Boolean(getenv("debug", "false"));
        Boolean runTime = new Boolean(getenv("runTime", "true"));


        System.err.println("params:"+
            " safeResolving="+safeResolving + " " +
            " preplayTime="+preplayTime + " " +
            " preplayTime1="+preplayTime1 + " " +
            " preplayTime2="+preplayTime2 + " " +
            " roundTime="+roundTime + " " +
            " roundTime1="+roundTime1 + " " +
            " roundTime2="+roundTime2 + " " +
            " rnd1="+rnd1 + " " +
            " rnd2="+rnd2 + " " +
            " alg1="+alg1 + " " +
            " alg2="+alg2 + " " +
            " binaryUtils="+binaryUtils + " " +
            " prettyPrint="+prettyPrint);

        Game gs[] = new Game[2];
        gs[0] = g.clone(new Random(rnd1));
        gs[1] = g.clone(new Random(rnd2));

        GamePlayingAlgorithm p1 = initMatchAlg(gs[0], alg1, 0);
        GamePlayingAlgorithm p2 = initMatchAlg(gs[1], alg2, 1);

        if(runTime) {
            p1.runMiliseconds(preplayTime1 == 0 ? preplayTime : preplayTime1);
            p2.runMiliseconds(preplayTime2 == 0 ? preplayTime : preplayTime2);
        } else {
            p1.runIterations(preplayTime1 == 0 ? preplayTime : preplayTime1);
            p2.runIterations(preplayTime2 == 0 ? preplayTime : preplayTime2);
        }

        GameState curState = g.rootState.copy();
        int i = 0;
        int p1giveUpAtMove = -1;
        int p2giveUpAtMove = -1;
        int p1breaksAtMove = -1;
        int p2breaksAtMove = -1;
        int moves1 = 0;
        int moves2 = 0;
        double pa = 1.;
        double[] p_dist = new double[0];
        int numSamplesDuringRun = 0;
        int numSamplesInCurrentIS = 0;
        int numNodesTouchedDuringRun = 0;
        ArrayList<Move> moves = new ArrayList<>();

        System.out.print(
                "XXX;"+
                alg1 +";"+
                alg2 +";"+
                rnd1 +";"+
                rnd2 +";"+
                preplayTime +";"+
                roundTime +";"+
                preplayTime1 +";"+
                roundTime1 +";"+
                preplayTime2 +";"+
                roundTime2 +";"
       );
        while (!curState.isGameEnd()) {
            Action a = null;

            if (curState.isPlayerToMoveNature()) {
                double r = g.config.getRandom().nextDouble();
                List<Action> actions = g.expander.getActions(curState);
                for (Action ca : actions) {
                    final double ap = curState.getProbabilityOfNatureFor(ca);
                    if (r <= ap) {
                        pa = ap;
                        a = ca;
                        break;
                    }
                    r -= ap;
                }
                p_dist = new double[actions.size()];
                for (int j = 0; j < actions.size(); j++) {
                    p_dist[j] = curState.getProbabilityOfNatureFor(actions.get(j));
                }
                numSamplesDuringRun = 0;
                numSamplesInCurrentIS = 0;
                numNodesTouchedDuringRun = 0;
            } else if (curState.getPlayerToMove().getId() == 0) {
                if (p1breaksAtMove == -1) {
                    if (p1.getRootNode() != null) { //mainly for the random player
                        MCTSInformationSet curIS = p1.getRootNode().getExpander()
                                .getAlgorithmConfig().getInformationSetFor(curState);
                        p1.setCurrentIS(curIS);
                    }

                    try {
                        moves1++;
                        if(runTime) {
                            a = p1.runMiliseconds(roundTime1 == 0 ? roundTime : roundTime1);
                        } else {
                            a = p1.runIterations(roundTime1 == 0 ? roundTime : roundTime1);
                        }
                        pa = p1.actionChosenWithProb();
                        p_dist = p1.currentISprobDist();
                        numSamplesDuringRun = p1.numSamplesDuringRun();
                        numSamplesInCurrentIS = p1.numSamplesInCurrentIS();
                        numNodesTouchedDuringRun = p1.numNodesTouchedDuringRun();
                    } catch (Exception e) {
                        a = null;
                        p1breaksAtMove = i;
                        e.printStackTrace();
                        if(debug) throw e;
                    }
                } else { // if it's broken, play randomly
                    a = null;
                }
            } else {
                if (p2breaksAtMove == -1) {
                    if (p2.getRootNode() != null) { //mainly for the random player
                        MCTSInformationSet curIS = p2.getRootNode().getExpander()
                                .getAlgorithmConfig().getInformationSetFor(curState);
                        p2.setCurrentIS(curIS);
                    }

                    try {
                        moves2++;
                        if(runTime) {
                        a = p2.runMiliseconds(roundTime2 == 0 ? roundTime : roundTime2);
                        } else {
                            a = p2.runIterations(roundTime2 == 0 ? roundTime : roundTime2);
                        }
                        pa = p2.actionChosenWithProb();
                        p_dist = p2.currentISprobDist();
                        numSamplesDuringRun = p2.numSamplesDuringRun();
                        numSamplesInCurrentIS = p2.numSamplesInCurrentIS();
                        numNodesTouchedDuringRun = p2.numNodesTouchedDuringRun();
                    } catch (Exception e) {
                        a = null;
                        p2breaksAtMove = i;
                        e.printStackTrace();
                        if(debug) throw e;
                    }
                } else { // if it's broken, play randomly
                    a = null;
                }
            }

            System.out.print(Arrays.toString(p_dist)+";");
            System.out.print(numSamplesDuringRun+";"+numSamplesInCurrentIS+";"+numNodesTouchedDuringRun+";");
            curState = curState.performAction(a);

            if (p1giveUpAtMove == -1 && p1.hasGivenUp()) p1giveUpAtMove = i;
            if (p2giveUpAtMove == -1 && p2.hasGivenUp()) p2giveUpAtMove = i;
            i++;
            if(moves2 == 1) break;
        }

        System.out.println("YYY");
    }

    private GamePlayingAlgorithm initMatchAlg(Game g, String algName, int playerId) {
        switch (algName) {
            case "RND":
                return new RandomAlgorithm(g.rootState.getAllPlayers()[playerId], g.expander);

            case "FIX":
                return new FixedPlayer(g.rootState.getAllPlayers()[playerId], g.expander, g.rootState);

            case "OOS":
            case "OOS_IST":
                ((MCTSConfig) g.expander.getAlgorithmConfig()).useEpsilonRM = true;
                OOSAlgorithm oosAlgIst = new OOSAlgorithm(
                        g.rootState.getAllPlayers()[playerId],
                        new OOSSimulator(g.expander),
                        g.rootState,
                        g.expander, 0.9, 0.4);
                oosAlgIst.setTargeting("IST");
                return oosAlgIst;

            case "OOS_PST":
                ((MCTSConfig) g.expander.getAlgorithmConfig()).useEpsilonRM = true;
                OOSAlgorithm oosAlgPst = new OOSAlgorithm(
                        g.rootState.getAllPlayers()[playerId],
                        new OOSSimulator(g.expander),
                        g.rootState,
                        g.expander, 0.9, 0.4);
                oosAlgPst.setTargeting("PST");
                return oosAlgPst;

            case "MCCFR":
                ((MCTSConfig) g.expander.getAlgorithmConfig()).useEpsilonRM = true;
                return new OOSAlgorithm(
                        g.rootState.getAllPlayers()[playerId],
                        new OOSSimulator(g.expander),
                        g.rootState,
                        g.expander, 0., 0.4);

            case "UCT":
                Double c = 2d * g.gameInfo.getMaxUtility();
                BackPropFactory bpFactory = new UCTBackPropFactory(c);

                ISMCTSAlgorithm algUCT = new ISMCTSAlgorithm(
                        g.rootState.getAllPlayers()[playerId],
                        new DefaultSimulator(g.expander),
                        bpFactory, g.rootState, g.expander);
                algUCT.returnMeanValue = false;
                return algUCT;

            case "RM":
//                double gamma = 0.1d; // old value
                double gamma = 0.2d;
                bpFactory = new RMBackPropFactory(-g.gameInfo.getMaxUtility(), g.gameInfo.getMaxUtility(), gamma);

                ISMCTSAlgorithm algRM = new ISMCTSAlgorithm(
                        g.rootState.getAllPlayers()[playerId],
                        new DefaultSimulator(g.expander),
                        bpFactory, g.rootState, g.expander);
                algRM.returnMeanValue = false;
                return algRM;

            default:
                if(!algName.startsWith("MCCR")) {
                    throw new RuntimeException("alg not recognized");
                }

                ((MCTSConfig) g.expander.getAlgorithmConfig()).useEpsilonRM = false;
                CRAlgorithm algMCCR = new CRAlgorithm(
                        g.rootState.getAllPlayers()[playerId], g.rootState, g.expander,0.6);
                algMCCR.defaultRootMethod = RESOLVE_MCCFR;
                algMCCR.defaultResolvingMethod = RESOLVE_MCCFR;
                algMCCR.deallocate = true;

                if(algName.contains("reset")) {
                    algMCCR.setDoResetData(true);
                } else { // keep
                    algMCCR.setDoResetData(false);
                }

                if(algName.contains("cfvExact")) {
                    GadgetInnerNode.resolvingCFV = RESOLVE_EXACT;
                } else if(algName.contains("cfvTime")) {
                    GadgetInnerNode.resolvingCFV = RESOLVE_TIME;
                } else if(algName.contains("cfvFixed")) {
                    GadgetInnerNode.resolvingCFV = RESOLVE_FIXED;
                } else { // cfvWeighted
                    GadgetInnerNode.resolvingCFV = RESOLVE_WEIGHTED_PL;
                }

                if(algName.contains("rootNothing")) {
                    GadgetChanceNode.useRootResolving = false;
                } else if(algName.contains("rootOpponentOnly")) {
                    GadgetChanceNode.useRootResolving = true;
                    GadgetChanceNode.rootResolvingEpsilon = 0.;
                } else { // rootOpponentEps
                    GadgetChanceNode.useRootResolving = true;
                    GadgetChanceNode.rootResolvingEpsilon = 0.1;
                }

                if(algName.contains("noIST")) {
                    OOSAlgorithm.gadgetDelta = 0.;
                    OOSAlgorithm.gadgetEpsilon = 0.;
                } else { // useIST
                    OOSAlgorithm.gadgetDelta = 0.5;
                    OOSAlgorithm.gadgetEpsilon = 0.1;
                }

                if(algName.contains("unsafe")) {
                    CRExperiments.safeResolving = false;
                } else { // safe
                    CRExperiments.safeResolving = true;
                }

                if(algName.contains("buildGadget")) {
                    CRAlgorithm.buildResolvingGadget = 1500;
                } else { // buildSampled
                    CRAlgorithm.buildResolvingGadget = 0;
                }
                return algMCCR;


        }
    }


    private Strategy normalizeStrategy(Player pl, InnerNode rootNode, Strategy sumStrategy) {
        StrategyImpl out = new StrategyImpl();
        out.put(rootNode.getGameState().getSequenceFor(pl), 1.0);
        Deque<InnerNode> q = new ArrayDeque<>();
        q.add(rootNode);
        while (!q.isEmpty()) {//DFS
            InnerNode curNode = q.removeFirst();

            List<Action> actions = curNode.getActions();
            if (curNode.getPlayerToMove().equals(pl)) {
                // tmp |=| h.a
                Sequence currentSeq = curNode.getGameState().getSequenceFor(pl);
                Double currentSum = sumStrategy.get(currentSeq);
                Double reachP = out.get(currentSeq);
                assert reachP != null;
                assert currentSum != null;

                for (Action a : actions) {
                    Sequence nextSeq = new ArrayListSequenceImpl(curNode.getGameState().getSequenceFor(pl));
                    nextSeq.addLast(a);
                    double nextSum = sumStrategy.get(nextSeq);
                    double nextP = reachP * 1. / actions.size();
                    if (currentSum > 0) {
                        assert nextSum <= currentSum;

                        // prevent numerical instability
                        double w = Math.abs(nextSum - currentSum) < 1e-9 ? 1. : nextSum / currentSum;
                        nextP = reachP * w; // w = h.a / h
                    }
                    assert !out.containsKey(nextSeq) || nextP == out.get(nextSeq);
                    assert nextP <= reachP;
                    out.put(nextSeq, nextP);
                }
            }
            for (Node nextNode : curNode.getChildren().values()) {
                if (nextNode instanceof InnerNode) q.addFirst((InnerNode) nextNode);
            }
        }

        for (Sequence a : out.keySet()) {
            Set<Sequence> children = new HashSet<>();
            for (Sequence b : out.keySet()) {
                if (a.isPrefixOf(b) && a.size() == b.size() + 1) {
                    children.add(b);
                }
            }
            if (children.size() > 0) {
                assert out.get(a) == children.stream().map(s -> out.get(s)).reduce(0., Double::sum);
            }
        }
        return out;
    }

    private void accumulateStrategy(Strategy cumulativeStrategy, Strategy incrementStrategy) {
        cumulativeStrategy.replaceAll((seq, prob) -> {
            Double incr = incrementStrategy.get(seq);
            if (incr == null) throw new NullPointerException();
            return prob + incr;
        });
        incrementStrategy.forEach((seq, prob) -> {
            if (!cumulativeStrategy.containsKey(seq)) {
                cumulativeStrategy.put(seq, prob);
            }
        });
    }

    private double getReachPr(Sequence onePlayerSeq) {
        return onePlayerSeq.getAsList().stream().map(a -> {
            MCTSInformationSet is = (MCTSInformationSet) a.getInformationSet();
            int ai = is.getActions().indexOf(a);
            return ((OOSAlgorithmData) is.getAlgorithmData()).getMeanStrategy()[ai];
        }).reduce(1., (p1, p2) -> p1 * p2);
    }

    private void runUniform(Game g) {
        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);
        CRAlgorithm alg = new CRAlgorithm(g.rootState, g.expander, 0.6);
        this.alg = alg;
        InnerNode rootNode = alg.getRootNode();
        buildCompleteTree(rootNode);

        Strategy strategy0 = StrategyCollector.getStrategyFor(
                rootNode, g.rootState.getAllPlayers()[0], new MeanStratDist());
        Strategy strategy1 = StrategyCollector.getStrategyFor(
                rootNode, g.rootState.getAllPlayers()[1], new MeanStratDist());

        Double br1Val = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy0));
        Double br0Val = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy1));
        Double exploitability = br0Val + br1Val;

        System.out.println(exploitability +";"+br1Val+";"+br0Val);
    }

    private void runCFR(Game g) {
        CFRAlgorithm alg = new CFRAlgorithm(
                g.rootState.getAllPlayers()[0],
                g.rootState, g.expander);
        this.alg = alg;

        MCTSInformationSet is = null; // todo
        buildCompleteTree(alg.getRootNode());
        g.expander.getAlgorithmConfig().createInformationSetFor(g.rootState);


        int n = 100;
        for (int i = 1; i <= 10000; i++) {
//            System.out.println(i);
            alg.runIterations(1);

            OOSAlgorithmData dataPl0 = (OOSAlgorithmData) alg.getRootNode().getInformationSet().getAlgorithmData();
            OOSAlgorithmData dataPl1 = (OOSAlgorithmData) ((InnerNode) alg.getRootNode().getChildren().values().iterator().next())
                    .getInformationSet().getAlgorithmData();

            double[] rmPl0 = dataPl0.getRMStrategy();
            double[] avgPl0 = dataPl0.getMeanStrategy();
            double[] rmPl1 = dataPl1.getRMStrategy();
            double[] avgPl1 = dataPl1.getMeanStrategy();

            System.out.println(i +","+
                              rmPl0[0]+","+
                              rmPl0[1]+","+
                              rmPl0[2]+","+
                              avgPl0[0]+","+
                              avgPl0[1]+","+
                              avgPl0[2]+","+
                              rmPl1[0]+","+
                              rmPl1[1]+","+
                              rmPl1[2]+","+
                              avgPl1[0]+","+
                              avgPl1[1]+","+
                              avgPl1[2]);
//            Double exploitability = calcExploitability(g, alg.getRootNode());
//            double cfv = alg.computeCFVofIS(is);
//            OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();
//            System.out.println("ITER:" + i * n + "," + exploitability + "," + cfv);
//            Map<Action, Double> dist = new MeanStratDist().getDistributionFor(data);
//            System.out.println(dist);

//            double cum = 0.0;
//            double[] cfvas = alg.computeCFVAofIS(is);
//            for (int a = 0; a < is.getActions().size(); a++) {
//                System.out.print(cfvas[a] + ",");
//                cum += cfvas[a] * dist.get(is.getActions().get(a));
//            }

//            System.err.println(cum - cfv);
//            System.out.println();
        }

//        System.out.println(alg.computeExpUtilityOfState(alg.getRootNode(), g.rootState.getAllPlayers()[0]));

    }

    private void runCRrootCFRresolvingCFR(Game g) {

        // CFR-specific settings
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "1000"));
        int iterationsPerGadgetGame = new Integer(getenv("iterationsPerGadgetGame", "100000"));
        int targetPSId = new Integer(getenv("targetPSId", "5"));
        int resolvingPlayerIdx = new Integer(getenv("resolvingPlayer", "0"));
        ResolvingMethod rootMethod = ResolvingMethod.fromString(getenv("rootMethod", "RESOLVE_CFR"));

        Player resolvingPlayer = g.rootState.getAllPlayers()[resolvingPlayerIdx];
        MCTSConfig config = ((MCTSConfig) g.expander.getAlgorithmConfig());
        Random rnd = config.getRandom();

        // Alg init
        CRAlgorithm mccrAlg = new CRAlgorithm(g.rootState, g.expander, 0.6);
        mccrAlg.defaultResolvingMethod = RESOLVE_CFR;
        mccrAlg.setDoResetData(false);
        InnerNode rootNode = mccrAlg.getRootNode();
        g.config.useEpsilonRM = true;
        OOSAlgorithmData.epsilon = 0.00001f;

        // Target ps
        buildCompleteTree(rootNode);
        Set<MCTSPublicState> publicStates = config.getAllPublicStates();
        MCTSPublicState targetPS = publicStates.stream().filter(
                ps -> ps.getPSKey().getId() == targetPSId).findFirst().get();

        // Run root
        mccrAlg.runRoot(rootMethod, resolvingPlayer, rootNode, iterationsInRoot);
        Map<ISKey, Map<Action, Double>> solvedBehavCFR = getBehavioralStrategy(rootNode);
        Exploitability exp = calcExploitability(g, solvedBehavCFR);
        System.out.println(iterationsInRoot + ";0;" + exp.expl0 + ";" + exp.expl1 + ";" + exp.total());


        // Prepare resolving
//        mccrAlg.runStep(resolvingPlayer, targetPS.getAllNodes().iterator().next(), RESOLVE_CFR, 1000);
//        exp = calcExploitability(getBehavioralStrategy(mccrAlg.getRootNode()));
//        System.out.println(iterationsInRoot+";1000;"+ exp.expl0+";"+exp.expl1+";"+exp.total());


        System.err.println("Running resolving");
        int loop = 0;
        int total = 0;
        do {
            targetPS.resetData(true);
            targetPS.setResolvingIterations(iterationsInRoot);
            targetPS.setResolvingMethod(rootMethod);
            updateCFRResolvingData(targetPS, mccrAlg.rootCfrData.reachProbs, mccrAlg.rootCfrData.historyExpValues);

            int iters = (int) Math.floor(Math.pow(10., 1 + loop / 10.)) - total;
            mccrAlg.runStep(resolvingPlayer, targetPS.getAllInformationSets().iterator().next(), RESOLVE_CFR, total);
            exp = calcExploitability(g, getBehavioralStrategy(mccrAlg.getRootNode()));
            System.out.println(iterationsInRoot + ";" + total + ";" + exp.expl0 + ";" + exp.expl1 + ";" + exp.total());

            total += iters;
            loop++;
        } while (total <= iterationsPerGadgetGame);
    }

    private void runCR_mix(Game g) {
//        OOSAlgorithmData.useEpsilonRM = true;
//        OOSAlgorithmData.epsilon = 0.00001f;

        // CFR-specific settings
        int iterationsInRoot = new Integer(getenv("iterationsInRoot", "1000"));
        int iterationsLevel2 = new Integer(getenv("iterationsLevel2", "100000"));
        int iterationsLevel3 = new Integer(getenv("iterationsLevel3", "100000"));
        long seed2 = new Long(getenv("seed2", "0"));
        long seed3 = new Long(getenv("seed3", "0"));
        int targetId2 = new Integer(getenv("target2", "5"));
        int targetId3 = new Integer(getenv("target3", "5"));
        boolean subtreeResolving = new Boolean(getenv("subtreeResolving", "true"));
        int resolvingPlayerIdx = new Integer(getenv("resolvingPlayer", "0"));
        ResolvingMethod resolvingMethod2 = ResolvingMethod.fromString(getenv("resolvingMethod2", "RESOLVE_MCCFR"));
        ResolvingMethod resolvingMethod3 = ResolvingMethod.fromString(getenv("resolvingMethod3", "RESOLVE_MCCFR"));
        ResolvingMethod rootMethod = ResolvingMethod.fromString(getenv("rootMethod", "RESOLVE_CFR"));

        Player resolvingPlayer = g.rootState.getAllPlayers()[resolvingPlayerIdx];
        MCTSConfig config = ((MCTSConfig) g.expander.getAlgorithmConfig());
        Random rnd = config.getRandom();

        // Alg init
        CRAlgorithm mccrAlg = new CRAlgorithm(g.rootState, g.expander, 0.6);
        mccrAlg.defaultResolvingMethod = RESOLVE_CFR;
        mccrAlg.setDoResetData(true);
        InnerNode rootNode = mccrAlg.getRootNode();

        // Target ps
        buildCompleteTree(rootNode);
        Set<MCTSPublicState> publicStates = config.getAllPublicStates();
//        config.getAllPublicStates().stream()
//                .filter(ps->ps.getPlayer().getId() == resolvingPlayerIdx)
//                .forEach(ps ->
//                        System.out.println(ps + " - "+ ps.getDepth() + " - "+ ps.getParentPublicState()));

        MCTSPublicState target2 = publicStates.stream().filter(
                ps -> ps.getPSKey().getId() == targetId2).findFirst().get();
        MCTSPublicState target3 = publicStates.stream().filter(
                ps -> ps.getPSKey().getId() == targetId3).findFirst().get();

        // Run root
        mccrAlg.runRoot(rootMethod, resolvingPlayer, rootNode, iterationsInRoot);
        Map<ISKey, Map<Action, Double>> solvedBehavCFR = getBehavioralStrategy(rootNode);
        Exploitability exp = calcExploitability(g, solvedBehavCFR);
        System.err.println("Root CFR exploitability: expl,expl0,expl1");
        System.err.println(exp.total() + "," + exp.expl0 + "," + exp.expl1);
        System.err.println("Game value for resolving player: " + mccrAlg.rootCfrData.historyExpValues.get(rootNode));

        // Prepare resolving
        System.err.println("Running resolving");
//        long seed = 0;
//        for (long i = 0; i < 100; i++) {
//            seed = i;
//            rnd.setSeed(seed);

        // prepare node reach pr / exp values
        target2.resetData(true);
        target2.setResolvingIterations(iterationsInRoot);
        target2.setResolvingMethod(rootMethod);
        updateCFRResolvingData(target2, mccrAlg.rootCfrData.reachProbs, mccrAlg.rootCfrData.historyExpValues);

        repeatedEvaluation(
                g, seed2, seed3,
                target2, target3,
                resolvingMethod2, resolvingMethod3,
                iterationsInRoot, iterationsLevel2, iterationsLevel3,
                resolvingPlayer, mccrAlg,
                solvedBehavCFR, rootNode, subtreeResolving);
//        }
    }

    private void repeatedEvaluation(Game g,
                                    long seed2, long seed3,
                                    PublicState target2, PublicState target3,
                                    ResolvingMethod resolvingMethod2,
                                    ResolvingMethod resolvingMethod3,
                                    int iterationsInRoot,
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
        q.add(target2);
        if (!subtreeResolving) {
            q.add(target3);
        }
        Random rnd = mccrAlg.getConfig().getRandom();

        copyCFR = cloneBehavStrategy(solvedBehavCFR);

        while (!q.isEmpty()) {
            PublicState ps = q.removeFirst();
            MCTSInformationSet is = ps.getAllInformationSets().iterator().next();

            if (ps.equals(target2)) {
                resolvingMethod = resolvingMethod2;
                iterationsPerGadgetGame = iterationsLevel2;
                rnd.setSeed(seed2);
            } else {
                resolvingMethod = resolvingMethod3;
                iterationsPerGadgetGame = iterationsLevel3;
                rnd.setSeed(seed3);
            }
            mccrAlg.runStep(resolvingPlayer, is, resolvingMethod, iterationsPerGadgetGame);

            behavMCCR = getBehavioralStrategy(rootNode);
            substituteStrategy(copyCFR, behavMCCR, ps);
//            exp = calcExploitability(copyCFR);
//            System.err.println("seed;iterationsPerGadgetGame;subtreeResolving;currentPS;expl0;expl1;total");
//            System.err.println(seed + ";" + iterationsPerGadgetGame + ";" + subtreeResolving + ";" + ps + ";" + exp.expl0 + ";" + exp.expl1 + ";" + exp.total());

            if (subtreeResolving) {
                q.addAll(ps.getNextPlayerPublicStates());
            }
        }

        // evaluate expl
        exp = calcExploitability(g, copyCFR);

        System.err.println(
                "seed2;seed3;target2;target3;resolvingMethod2;resolvingMethod3;iterationsInRoot;iterationsLevel2;iterationsLevel3;subtreeResolving;expl0;expl1;total");
        System.out.println(
                seed2 + ";" + seed3 + ";" + target2.hashCode() + ";" + target3.hashCode() + ";" + resolvingMethod2 + ";" + resolvingMethod3 + ";" + iterationsInRoot + ";" + iterationsLevel2 + ";" +
                        iterationsLevel3 + ";" + subtreeResolving + ";" + exp.expl0 + ";" + exp.expl1 + ";" + exp.total()
                          );
    }

    protected Exploitability calcExploitability(Game g, Map<ISKey, Map<Action, Double>> solvedBehavCFR) {
        Strategy strategy0 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                solvedBehavCFR, g.rootState, g.expander, g.rootState.getAllPlayers()[0]);
        Strategy strategy1 = UniformStrategyForMissingSequences.fromBehavioralStrategy(
                solvedBehavCFR, g.rootState, g.expander, g.rootState.getAllPlayers()[1]);

        double br1Val = brAlg1.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy0));
        double br0Val = brAlg0.calculateBR(g.rootState, ISMCTSExploitability.filterLow(strategy1));
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
                        "\t\"ps " + ps.getPSKey().getId() + "\" -- \"ps " + n.getPSKey().getId() + "\";");
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
                assert data != null;
                Map<Action, Double> dist = new MeanStratDist().getDistributionFor(data);
                assert dist != null;
                out.put(curNodeIS.getISKey(), dist);
            }

            for (Node n : curNode.getChildren().values()) {
                if (n instanceof InnerNode) {
                    q.add((InnerNode) n);
                }
            }
        }
        return out;
    }

    public static String getenv(String env, String def) {
        return System.getenv(env) == null ? def : System.getenv(env);
    }

    protected void substituteStrategy(Map<ISKey, Map<Action, Double>> target,
                                      Map<ISKey, Map<Action, Double>> replacement,
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
                    assert oldvalue != null;
                    assert newvalue != null;
                    target.put(key, newvalue);
                }
            }

            for (Node n : curNode.getChildren().values()) {
                if ((n instanceof InnerNode)) q.addLast((InnerNode) n);
            }
        }
    }

    public Map<PublicState,List<GadgetInfoSet>> getTargetPsGadgetIs(Set<PublicState> targetPs) {
        Map<PublicState, List<GadgetInfoSet>> targetPsGadgetIs = new LinkedHashMap<>(); ;

        Set<InnerNode> targetPsNodes = new HashSet<>();
        targetPs.forEach(ps -> targetPsNodes.addAll(ps.getAllNodes()));


        targetPs.forEach(ps -> {
            Set<GadgetInfoSet> gadgetIs = ps.getSubgame().getGadgetInformationSets();
            ArrayList<GadgetInfoSet> gadgetIsAL = new ArrayList<>(gadgetIs);
            gadgetIsAL.sort((g1, g2) -> {
                int c = g1.getAllNodes().size() - g2.getAllNodes().size();
                if (c == 0) {
                    String g1s = g1.getAllNodes().stream().map(InnerNode::toString).reduce("", String::concat);
                    String g2s = g2.getAllNodes().stream().map(InnerNode::toString).reduce("", String::concat);
                    c = g1s.compareTo(g2s);
                }
                return c;
            });
            System.err.println(ps + ": " +
                            gadgetIsAL.stream().map(gis -> gis.getAllNodes().stream().map(in -> in + ", ").reduce("",
                                    String::concat) + " - ").reduce("", String::concat)
                              );
            targetPsGadgetIs.put(ps, gadgetIsAL);
        });

        return targetPsGadgetIs;
    }

    public Set<PublicState> getTargetPs(Game g, InnerNode rootNode, Player resolvingPlayer) {
        Set<PublicState> targetPs;
        switch (g.domain) {
            case "BRPS":
                targetPs = rootNode.getPublicState()
                        .getNextPublicStates(); // only one relevant public state
                break;
            case "IIGS":
                targetPs = rootNode.getPublicState()
                        .getNextPlayerPublicStates(resolvingPlayer)
                        .iterator().next()
                        .getNextPlayerPublicStates(resolvingPlayer);
                break;
            case "GP":
            case "LD":
                targetPs = new HashSet<>();
                rootNode.getPublicState() // chance players
                        .getNextPlayerPublicStates(resolvingPlayer) // take this player's pub states and all subsequent ps
                        .forEach(ps -> targetPs.addAll(ps.getNextPlayerPublicStates(resolvingPlayer)));
                break;
            case "PTTT":
                targetPs = rootNode.getPublicState().getNextPlayerPublicStates(resolvingPlayer);
                break;

            default:
                targetPs = rootNode.getPublicState()
                        .getNextPlayerPublicStates(resolvingPlayer)
                        .iterator().next()
                        .getNextPlayerPublicStates(resolvingPlayer);
        }
        return targetPs;
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
                    ", total=" + total() +
                    '}';
        }
    }

    private class Move {
        public String info;
        public int player;
        public String action;
        public double prob;
        public double[] p_dist;
        public int numSamplesDuringRun;
        public int numSamplesInCurrentIS;
        public int numSamplesInCurrentPS;
        public int numNodesTouchedDuringRun;
        public int numInfoSets;
        public int numNodes;


        public Move(int player,
                    String action,
                    double prob,
                    double[] p_dist,
                    int numSamplesDuringRun,
                    int numSamplesInCurrentIS,
                    int numSamplesInCurrentPS,
                    int numNodesTouchedDuringRun,
                    int numInfoSets,
                    int numNodes,
                    String info
                   ) {
            this.player = player;
            this.action = action;
            this.prob = prob;
            this.p_dist = p_dist;
            this.numSamplesDuringRun = numSamplesDuringRun;
            this.numSamplesInCurrentIS = numSamplesInCurrentIS;
            this.numSamplesInCurrentPS = numSamplesInCurrentPS;
            this.numNodesTouchedDuringRun = numNodesTouchedDuringRun;
            this.numInfoSets = numInfoSets;
            this.numNodes = numNodes;
            this.info = info;
        }

        @Override
        public String toString() {
            return "Move{" +
                    "player=" + player +
                    ", action='" + action + '\'' +
                    ", prob=" + prob +
                    ", numSamplesDuringRun=" + numSamplesDuringRun +
                    ", numSamplesInCurrentIS=" + numSamplesInCurrentIS +
                    ", numSamplesInCurrentPS=" + numSamplesInCurrentPS +
                    ", numNodesTouchedDuringRun=" + numNodesTouchedDuringRun +
                    ", p_dist=" + Arrays.toString(p_dist) +
                    ", numInfoSets=" + numInfoSets +
                    ", numNodes=" + numNodes +
//                    ", info=" + info +
                    '}';
        }

        public String pretty() {
            return "# Player: " + player +
                   " (action=" + action + ")\n" +
                   "prob=" + prob +
                   " numSamplesDuringRun=" + numSamplesDuringRun +
                   " numSamplesInCurrentIS=" + numSamplesInCurrentIS +
                   " numSamplesInCurrentPS=" + numSamplesInCurrentPS +
                   " numNodesTouchedDuringRun=" + numNodesTouchedDuringRun + "\n" +
                   " numInfoSets=" + numInfoSets+ "\n" +
                   " numNodes=" + numNodes + "\n" +
                   "p_dist=" + Arrays.toString(p_dist) + "\n" +
                   "info=\n" + info.replace(" ~ ", "\n");
        }
    }
}
