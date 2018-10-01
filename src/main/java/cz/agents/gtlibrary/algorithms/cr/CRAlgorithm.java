package cz.agents.gtlibrary.algorithms.cr;

import cz.agents.gtlibrary.NotImplementedException;
import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInfoSet;
import cz.agents.gtlibrary.algorithms.cr.gadgettree.GadgetInnerNode;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.domain.goofspiel.IIGoofSpielGameState;
import cz.agents.gtlibrary.domain.liarsdice.LiarsDiceGameState;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.interfaces.*;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.stream.Collectors;

import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.collectCFRResolvingData;
import static cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm.updateCFRResolvingData;
import static cz.agents.gtlibrary.algorithms.cr.CRExperiments.buildCompleteTree;
import static cz.agents.gtlibrary.algorithms.cr.ResolvingMethod.RESOLVE_MCCFR;
//import static cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm.gadgetActionChoices;

// Continual Resolving algorithm
public class CRAlgorithm implements GamePlayingAlgorithm {
    public static double totalTimeResolving = 0.;
    public static double totalTimeRoot = 0.;
    private final GameState rootState;
    private final Expander<MCTSInformationSet> expander;
    private final InnerNode statefulRootNode;
    private final ThreadMXBean threadBean;
    private final MCTSConfig config;
    public ResolvingMethod defaultResolvingMethod = RESOLVE_MCCFR;
    public ResolvingMethod defaultRootMethod = RESOLVE_MCCFR;
    private Node statefulCurNode;
    private Random rnd;
    private double epsilonExploration = 0.6;
    private boolean resetData = true;
    public CFRData rootCfrData;
    public CFRData gadgetCfrData;
    public boolean gadgetIterationsCountFollow = false;

    public CRAlgorithm(GameState rootState, Expander<MCTSInformationSet> expander) {
        this(rootState, expander, 0.6);
    }
    public CRAlgorithm(GameState rootState, Expander<MCTSInformationSet> expander, double epsilonExploration) {
        this.rootState = rootState;
        this.expander = expander;
        this.config = ((MCTSConfig) expander.getAlgorithmConfig());
        this.rnd = config.getRandom();

        this.statefulRootNode = buildRootNode();
        this.statefulCurNode = statefulRootNode;
        this.epsilonExploration = epsilonExploration;
        OOSAlgorithmData.useEpsilonRM = false;
        threadBean = ManagementFactory.getThreadMXBean();
    }
    public CRAlgorithm(InnerNode rootNode, Expander expander) {
        this(rootNode, expander, 0.6);
    }
    public CRAlgorithm(InnerNode rootNode, Expander<MCTSInformationSet> expander, double epsilonExploration) {
        this.rootState = rootNode.getGameState();
        this.expander = expander;
        this.config = ((MCTSConfig) expander.getAlgorithmConfig());
        this.rnd = config.getRandom();
        this.statefulRootNode = rootNode;
        this.statefulCurNode = rootNode;
        this.epsilonExploration = epsilonExploration;
        OOSAlgorithmData.useEpsilonRM = false;
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public Expander<MCTSInformationSet> getExpander() {
        return expander;
    }

    public MCTSConfig getConfig() {
        return config;
    }

    private InnerNode buildRootNode() {
        InnerNode rootNode;
        if (rootState.isPlayerToMoveNature()) {
            rootNode = new ChanceNodeImpl(expander, rootState, this.rnd);
        } else {
            rootNode = new InnerNodeImpl(expander, rootState);
        }
        return rootNode;
    }

//    public double[] runIterations(Player resolvingPlayer, int iterationsInRoot, int iterationsPerGadgetGame) {
//        System.err.println("Using " +
//                "iterationsInRoot=" + iterationsInRoot + " " +
//                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
//                "epsilonExploration=" + epsilonExploration + " " +
//                "player=" + resolvingPlayer.getId() + " ");
//
//        Node curNode = buildRootNode();
//        // root MCCFR
//        runRoot(resolvingPlayer, (InnerNode) curNode, iterationsInRoot);
//
//        // continual resolving
//        while (!(curNode instanceof LeafNode)) {
//            Action action = runStep(resolvingPlayer, curNode, iterationsPerGadgetGame, iterationsInRoot);
//            curNode = ((InnerNode) curNode).getChildFor(action);
//        }
//
//        return ((LeafNode) curNode).getUtilities();
//        //StrategyCollector.getStrategyFor(getRootNode(), getRootNode().getAllPlayers()[0], new MeanStratDist());
//    }

    public void runRoot(Player resolvingPlayer, InnerNode rootNode, int iterationsInRoot) {
        runRoot(defaultRootMethod, resolvingPlayer, rootNode, iterationsInRoot);
    }

    public void runRoot(ResolvingMethod rootResolveMethod,
                        Player resolvingPlayer,
                        InnerNode rootNode,
                        int iterationsInRoot) {
        long start = threadBean.getCurrentThreadCpuTime();

        switch (rootResolveMethod) {
            case RESOLVE_MCCFR:
                runRootMCCFR(resolvingPlayer, rootNode, iterationsInRoot);
                break;
            case RESOLVE_CFR:
                runRootCFR(resolvingPlayer, rootNode, iterationsInRoot);
        }

        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        totalTimeRoot += diff;
        System.err.println("root iters in " + diff + " ms");

        if (rootNode.getPlayerToMove().equals(resolvingPlayer)) {
            rootNode.getPublicState().incrResolvingIterations(iterationsInRoot);
            rootNode.getPublicState().setResolvingMethod(rootResolveMethod);
        }
        rootNode.getPublicState().getNextPlayerPublicStates(resolvingPlayer).forEach(nextPs -> {
            nextPs.incrResolvingIterations(iterationsInRoot);
            nextPs.setResolvingMethod(rootResolveMethod);
        });
    }

    public Action runStepStateful(Player resolvingPlayer, int iterationsPerGadgetGame, int iterationsInRoot) {
        if (statefulCurNode instanceof LeafNode) {
            return null;
        }

        if (statefulCurNode.equals(statefulRootNode)) {
            // root MCCFR
            runRoot(resolvingPlayer, statefulRootNode, iterationsPerGadgetGame);
        }

        Action action = runStep(resolvingPlayer, statefulCurNode, iterationsPerGadgetGame);
        statefulCurNode = ((InnerNode) statefulCurNode).getChildFor(action);
        return action;
    }

    public Action runStep(Player resolvingPlayer, Node curNode, int iterationsPerGadgetGame) {
        return runStep(resolvingPlayer, curNode, defaultResolvingMethod, iterationsPerGadgetGame);
    }

    public Action runStep(Player resolvingPlayer,
                          Node curNode,
                          ResolvingMethod resolvingMethod,
                          int iterationsPerGadgetGame) {
//        if (iterationsPerGadgetGame < 2 || iterationsInRoot < 2) {
//            throw new RuntimeException("Cannot resolve with small number of samples!");
//        }

        if (curNode instanceof LeafNode) {
            return null;
        }

        Action action;
        if (curNode instanceof ChanceNode) {
            ChanceNode currentChance = (ChanceNode) curNode;
            action = currentChance.getRandomAction();

            // this builds tree until next public states
            buildTreeExpandChanceNodes(currentChance);
            return action;
        }

        assert curNode instanceof InnerNode;
        InnerNode curIn = ((InnerNode) curNode);

        if (!curIn.getPlayerToMove().equals(resolvingPlayer)) {
            throw new RuntimeException("Cannot resolve in public state that does not belong to the player!");
        }

        MCTSInformationSet curIS = curIn.getInformationSet();
        PublicState curPS = curIS.getPublicState();
        PublicState parentPS = curPS.getPlayerParentPublicState();

        System.err.println("--------------------------");
        System.err.println("Resolving " +
                "\n\tPS: " + curPS + " (parent " + parentPS + ") " +
                "\n\tIS: " + curIS + " " +
                "\n\tN: " + curNode + " " +
                "\n\t" + curPS.getAllNodes().size() + " nodes, " +
                curPS.getAllInformationSets().size() + " infosets in public state");

        // weird optimization to speed up resolvings in nice games
        // most public states are at the end of the public tree, but if they have only
        // one action and it is one round before the end of the game it is senseless to resolve here
        // so we can speed up the entire resolving about 2x => faster experiments!
        int maxNumActionsAtPs = curPS.getAllInformationSets().stream().map(
                is -> is.getActions().size()).max(Integer::compareTo).get();
        if (maxNumActionsAtPs == 1 && isNiceGame(curNode.getGameState())) {
            System.err.println("Only one action possible, skipping resolving");
            action = curIS.getActions().iterator().next();
        } else {
            Map<Action, Double> distributionBefore = getDistributionFor(curIS.getAlgorithmData());

            // Resolve!
            resolveGadgetGame(resolvingPlayer, curPS, resolvingMethod, iterationsPerGadgetGame);

            Map<Action, Double> distributionAfter = getDistributionFor(curIS.getAlgorithmData());
            Map<Action, Double> diff = distributionBefore.keySet().stream().collect(Collectors.toMap(
                    entry -> entry,
                    distAction -> distributionBefore.get(distAction) - distributionAfter.get(distAction)));

            System.err.println("Before: " + distributionToString(curIS.getActions(), distributionBefore));
            System.err.println("After:  " + distributionToString(curIS.getActions(), distributionAfter));
            System.err.println("Diff:   " + distributionToString(curIS.getActions(), diff));
            action = randomChoice(distributionAfter);
        } // else
        updatePlayerRp(curPS);

        assert curIS.getActions().contains(action);
        return action;
    }

    public InnerNode solveEntireGame(Player resolvingPlayer, int iterationsInRoot, int iterationsPerGadgetGame) {
        System.err.println("Using " +
                "iterationsInRoot=" + iterationsInRoot + " " +
                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
                "epsilonExploration=" + epsilonExploration + " " +
                "resetData=" + resetData + " " +
                "player=" + resolvingPlayer.getId() + " ");

        InnerNode solvingRoot;
        if (rootState.isPlayerToMoveNature()) {
            solvingRoot = new ChanceNodeImpl(expander, rootState, this.rnd);
        } else {
            solvingRoot = new InnerNodeImpl(expander, rootState);
        }

        if (iterationsInRoot < 2) {
            System.err.println("Skipping root initialization.");
        } else {
            runRoot(resolvingPlayer, solvingRoot, iterationsInRoot);
        }

        if (iterationsPerGadgetGame < 2) {
            System.err.println("Skipping resolving.");
        } else {
            ArrayDeque<PublicState> q = new ArrayDeque<>();
            q.add(getRootNode().getPublicState());
            while (!q.isEmpty()) {
                PublicState s = q.removeFirst();

                if(!s.isReachable(resolvingPlayer)) {
                    // If public state is not reachable by our player, we can leave whatever strategy was there.
                    System.err.println("Skipping resolving public state "+s+" - not reachable.");
                    continue;
                }

                // NextPlayerPublicStates builds up the tree incrementally until
                // next public states, so everything is properly defined between
                // resolvings
                q.addAll(s.getNextPlayerPublicStates(resolvingPlayer));

                InnerNode n = s.getAllNodes().iterator().next();
                runStep(resolvingPlayer, n, iterationsPerGadgetGame);
            }
        }

        return solvingRoot;
    }

    private void updatePlayerRp(PublicState ps) {
        // Top-down update of reach probabilities.
        //
        // From current public state until next public states of the same player,
        // update reach probabilities of each node.
        //
        // Basically, we know that between the public states the current player
        // will play the resolved average strategy.
        System.err.println("Updating reach probabilities");
        Player updatingPlayer = ps.getPlayer();

        Set<InnerNode> nextPsNodesBarrier = new HashSet<>();
        ps.getNextPlayerPublicStates(updatingPlayer).stream()
                .map(PublicState::getAllNodes)
                .forEach(nextPsNodesBarrier::addAll);

        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.addAll(ps.getAllNodes());

        while (!q.isEmpty()) {
            InnerNode curNode = q.removeFirst();

            Map<Action, Double> avgStrategy = null;
            if (curNode.isPlayerMoving(updatingPlayer)) {
                avgStrategy = getDistributionFor(curNode.getInformationSet().getAlgorithmData());
            }

            for (Action action : curNode.getActions()) {
                Node nextNode = curNode.getChildFor(action);
                if (nextNode instanceof LeafNode) continue;

                Double pA = 1.0; // action probability if the *opponent* is moving in curNode
                if (avgStrategy != null) {
                    pA = avgStrategy.get(action);
                }

                InnerNode nextInner = (InnerNode) nextNode;
                nextInner.setReachPrByPlayer(updatingPlayer, curNode.getReachPrByPlayer(updatingPlayer) * pA);
                if (!nextPsNodesBarrier.contains(nextInner)) {
                    q.add(nextInner);
                }
            }
        }
    }

    private void resolveGadgetGame(Player resolvingPlayer,
                                   PublicState publicState,
                                   ResolvingMethod resolvingMethod,
                                   int iterationsPerGadgetGame) {
        // todo: check redundancy of incrmeentally building tree
        System.err.println("Incrementally building tree");
        publicState.getNextPlayerPublicStates(); // build all the nodes until next public states

        System.err.println("Building gadget");
        Subgame subgame = publicState.getSubgame();
        GadgetChanceNode gadgetRootNode = subgame.getGadgetRoot();

        if (resetData && !isPublicTreeRootKeeping(publicState)) {
            System.err.println("Resetting data");
            publicState.resetData(false);
            publicState.setDataKeeping(false);
        } else {
            System.err.println("Keeping data");
            publicState.setDataKeeping(true);
        }

//        new GambitEFG().write(
//                expander.getClass().getSimpleName() + "_PS_" + publicState.getPSKey().getHash() + ".gbt",
//                gadgetRootNode);

//        subgame.getGadgetInformationSets().forEach(gis -> {
//            System.out.println(gis + " " + gis.getIsCFV(publicState.getResolvingIterations()));
//        });

        runGadget(resolvingMethod, resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
    }

    private void runGadget(ResolvingMethod resolvingMethod,
                           Player resolvingPlayer,
                           PublicState publicState,
                           GadgetChanceNode gadgetRootNode,
                           int iterationsPerGadgetGame) {

        System.err.println("Using " + resolvingMethod + " for resolving gadget " +
                        "with " + iterationsPerGadgetGame + " iterations");

        long start = threadBean.getCurrentThreadCpuTime();
        int iterations = iterationsPerGadgetGame;
        switch (resolvingMethod) {
            case RESOLVE_CFR:
                runGadgetCFR(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
                break;
            case RESOLVE_MCCFR:
                iterations = runGadgetMCCFR(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
                break;
            case RESOLVE_UNIFORM:
                runGadgetUniform(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
                break;
        }
        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        System.err.println("resolved in " + diff + " ms");
        totalTimeResolving += diff;

//         todo: fix bug root MCCFR number of iterations?!
        // update resolving iterations
//        PublicState parentPs = publicState.getPlayerParentPublicState();
//        final Integer totalIterations = (parentPs != null // propagate # of iterations down if applicable
//                && parentPs.isDataKeeping()
//                && parentPs.getResolvingMethod() == RESOLVE_MCCFR)
//                ? iterations + parentPs.getResolvingIterations()
//                : iterations;
        final Integer totalIterations = iterations;

        publicState.getNextPlayerPublicStates().forEach(ps -> {
            ps.incrResolvingIterations(totalIterations);
            ps.setResolvingMethod(resolvingMethod);
        });
    }

    private boolean isPublicTreeRootKeeping(PublicState publicState) {
        InnerNode aNode = publicState.getAllNodes().iterator().next();

        // todo: chance is in the root, but it's outcomes are hidden, therefore PS spans the entire level
        return publicState.getAllNodes().size() == 1
                && aNode.getReachPr() == 1.
                && (aNode.getParent() == null
                || aNode.getParent() instanceof ChanceNode // there is a trivial chance node with probability 1
        );
    }

    public void runRootMCCFR(Player resolvingPlayer, InnerNode rootNode, int iterations) {
        System.err.println("Calculating initial strategy from root using MCCFR in "+iterations+" iterations");
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, rootNode, epsilonExploration);
        alg.setRnd(rnd);

        alg.runIterations(iterations);

        // debug
//        assert debugDepthSamplingAssert(rootNode, alg) == iterations / 2;
    }

    public void runRootCFR(Player resolvingPlayer, InnerNode rootNode, int iterations) {
        System.err.println("Calculating initial strategy from root using CFR in "+iterations+" iterations");
        CFRAlgorithm alg = new CFRAlgorithm(rootNode);

        // todo: check if should build or not!
//        buildCompleteTree(rootNode);
        alg.runIterations(iterations);

        PublicState publicState = rootNode.getPublicState();

        // get values needed for next resolving
        if(publicState.getPlayer().equals(resolvingPlayer)){
            rootCfrData = collectCFRResolvingData(publicState);
        } else {
            rootCfrData = collectCFRResolvingData(publicState.getNextPlayerPublicStates(resolvingPlayer));
        }

        if (publicState.getPlayer().equals(resolvingPlayer)) {
            updateCFRResolvingData(publicState, rootCfrData.reachProbs, rootCfrData.historyExpValues);
        }
        publicState.getNextPlayerPublicStates(resolvingPlayer).forEach(ps -> {
            updateCFRResolvingData(ps, rootCfrData.reachProbs, rootCfrData.historyExpValues);
        });
    }

    private int runGadgetMCCFR(Player resolvingPlayer,
                                PublicState publicState,
                                GadgetChanceNode gadgetRoot,
                                int iterations) {
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, gadgetRoot, epsilonExploration);
        alg.setRnd(rnd);

        int iters = alg.runIterations(iterations, gadgetIterationsCountFollow);

//        System.out.println("follow: " + alg.gadgetActionChoices[0][0]+" "+alg.gadgetActionChoices[1][0]);
//        System.out.println("terminate: " +alg.gadgetActionChoices[0][1]+" "+alg.gadgetActionChoices[1][1]);

        return iters;
    }

    private void runGadgetCFR(Player resolvingPlayer,
                              PublicState publicState,
                              GadgetChanceNode gadgetRootNode,
                              int iterations) {
        buildCompleteTree(gadgetRootNode);

        CFRAlgorithm alg = new CFRAlgorithm(gadgetRootNode);
        alg.runIterations(iterations);

        // update values needed for next resolving
        gadgetCfrData = collectCFRResolvingData(publicState);
        publicState.getNextPlayerPublicStates().forEach(ps -> {
            updateCFRResolvingData(ps, gadgetCfrData.reachProbs, gadgetCfrData.historyExpValues);
        });
    }

    /**
     * Not intended for general use. Overwrite strategy by random numbers.
     */
    private void runGadgetUniform(Player resolvingPlayer,
                                  PublicState publicState,
                                  GadgetChanceNode gadgetRootNode,
                                  int iterationsPerGadgetGame) {
        buildCompleteTree(gadgetRootNode);

        Random rnd = new Random(123456);
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.addAll(publicState.getAllNodes());
        while (!q.isEmpty()) {
            InnerNode n = q.removeFirst();
            if (!(n instanceof ChanceNode)) {
                MCTSInformationSet is = n.getInformationSet();
                OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

                double[] mp = data.getMp();
                for (int i = 0; i < mp.length; i++) {
                    mp[i] = rnd.nextDouble();
                }

            } else { // expand chance nodes
                for (Action a : n.getActions()) {
                    Node ch = n.getChildFor(a);
                    if (ch instanceof InnerNode) {
                        q.add((InnerNode) ch);
                    }
                }
            }
        }



    }

//    private void printCFVs(Stream<MCTSInformationSet> stream, int iterations) {
//        stream
//                .forEach(is -> {
//                    OOSAlgorithmData data = ((OOSAlgorithmData) is.getAlgorithmData());
//                    System.out.println(
//                            "CFV:" + is.toString() + "," + ((int) data.getIsVisitsCnt()) + "," + data.getIsCFV(
//                                    iterations));
//
//                    Map<Action, Double> distribution = getDistributionFor(data);
//
//                    System.out.println("Strat:" + distributionToString(is.getActions(), distribution));
//
//                    double[][] optimal = {
//                            {0.010244883284174892, 0.3322823662715796, 0.6574727504442456},
//                            {0.3342004650475451, 0.009645618665963192, 0.6561539162864918}
//                    };
//
//
//                    for (int i = 0; i < is.getActions().size(); i++) {
//                        Action a = is.getActions().get(i);
//                        distribution.put(a, optimal[is.getPlayer().getId()][i] - distribution.get(a));
//                    }
//
//                    System.out.println("Diff:" + distributionToString(is.getActions(), distribution));
//
//                    double[] rmstrat = data.getRMStrategy();
//                    double[] cfvas = data.getActionCFV();
//
//                    System.out.println("RMSStrat:" + rmstrat[0] + "," + rmstrat[1] + "," + rmstrat[2] + ",");
//                    System.out.println("CFVAs:" + cfvas[0] + "," + cfvas[1] + "," + cfvas[2] + ",");
//                    System.out.println("---");
//
//                });
//    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        throw new NotImplementedException();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        throw new NotImplementedException();
    }

    @Override
    public void setCurrentIS(InformationSet currentIS) {
        throw new NotImplementedException();
    }

    @Override
    public InnerNode getRootNode() {
        return statefulRootNode;
    }

    private boolean isNiceGame(GameState gameState) {
        return gameState instanceof IIGoofSpielGameState
                || gameState instanceof LiarsDiceGameState
                || gameState instanceof GenericPokerGameState;
    }

    private Action randomChoice(Map<Action, Double> distribution) {
        double r = rnd.nextDouble();
        for (Map.Entry<Action, Double> entry : distribution.entrySet()) {
            double pA = entry.getValue();
            Action action = entry.getKey();
            if (r <= pA) return action;
            r -= pA;
        }

        return null;
    }

    public void setDoResetData(boolean resetData) {
        this.resetData = resetData;
    }

    private void buildTreeExpandChanceNodes(InnerNode startNode) {
        // basically expand all chance actions in the root, before the first players can act
        System.err.println("Building tree until first public states.");
        int nodes = 0, infosets = 0;
        ArrayDeque<InnerNode> q = new ArrayDeque<>();

        q.add(startNode);
        while (!q.isEmpty()) {
            nodes++;
            InnerNode n = q.removeFirst();
            if (!(n instanceof ChanceNode)) {
                MCTSInformationSet is = n.getInformationSet();
                if (is.getAlgorithmData() == null) {
                    infosets++;
                    is.setAlgorithmData(new OOSAlgorithmData(n.getActions()));
                }
            } else { // expand chance nodes
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

    private String distributionToString(List<Action> actions, Map<Action, Double> distribution) {
        StringBuilder s = new StringBuilder("{");
        for (Action a : actions) {
            s.append(a);
            s.append(" = ");
            s.append(String.format("% 1.6f", distribution.get(a)));
            s.append(",\t");
        }
        s.append("}");
        return s.toString();
    }

    public void printDomainStatistics() {
        MCTSConfig config = getRootNode().getAlgConfig();

        Integer inners = config.getAllInformationSets().values()
                .stream().map(MCTSInformationSet::getAllNodes)
                .map(Set::size).reduce(0, Integer::sum);
        Long leafs = config.getAllInformationSets().values().stream()
                .map(MCTSInformationSet::getAllNodes)
                .map(setIN -> setIN.stream()
                        .map(InnerNode::getChildren)
                        .map(Map::values)
                        .map(mapCh -> mapCh.stream().filter(Node::isGameEnd).count())
                        .reduce(0L, Long::sum))
                .reduce(0L, Long::sum);

        PublicState deepestPS = config.getAllPublicStates().stream()
                .sorted((ps1, ps2) -> Integer.compare(ps2.getDepth(), ps1.getDepth()))
                .findFirst().get();
        int numC = 0;
        PublicState currentPs = deepestPS;
        while (currentPs.getParentPublicState() != null) {
            currentPs = currentPs.getParentPublicState();
            if (currentPs.getAllNodes().stream().anyMatch(n -> (n instanceof ChanceNode))) {
                numC++;
            }
        }
        Integer maxPTdepth = deepestPS.getDepth() - numC + 1;

        int augIs = config.getAllPublicStates().stream()
                .filter(ps->ps.getPlayer().getId() <= 1) // exclude chance
                .map(ps -> ps.getSubgame().getGadgetInformationSets().size())
                .reduce(0, Integer::sum);

        System.err.println("Game has: \n" +
                "public states & info sets & aug info sets & inner nodes & leaf nodes & max PT depth");
        System.err.println(config.getAllPublicStates().size() + " & " +
                config.getAllInformationSets().size() + " & " +
                augIs + " & " +
                inners + " & " +
                leafs + " & " +
                (maxPTdepth));
//        System.err.println(
//                "F:" + gadgetActionChoices[0] + " " +
//                "T:" + gadgetActionChoices[1] + " " +
//                "ratio: " + ((double) gadgetActionChoices[0] / (gadgetActionChoices[0] + gadgetActionChoices[1])) + " " +
//                "total: " + (gadgetActionChoices[0] + gadgetActionChoices[1]));
    }

    private Map<Action, Double> getDistributionFor(AlgorithmData algorithmData) {
        return (new MeanStratDist()).getDistributionFor(algorithmData);
    }


    private void debugClearISVisitCounts(InnerNode rootNode) {
        // clear out IS counts
        ArrayDeque<Node> qn = new ArrayDeque<>();
        qn.add(rootNode);
        while (!qn.isEmpty()) {
            InnerNode in = (InnerNode) qn.removeFirst();
            if (in.getInformationSet() != null) {
                in.getInformationSet().setVisitsCnt(0);
                if (in.getInformationSet() instanceof GadgetInfoSet) {
                    ((GadgetInfoSet) in.getInformationSet()).setFollowCnt(0);
                    ((GadgetInfoSet) in.getInformationSet()).setTerminateCnt(0);
                }
                if (in instanceof GadgetInnerNode) {
                    ((GadgetInnerNode) in).setFollowCnt(0, 0);
                    ((GadgetInnerNode) in).setFollowCnt(0, 1);
                    ((GadgetInnerNode) in).setTerminateCnt(0, 0);
                    ((GadgetInnerNode) in).setTerminateCnt(0, 1);
                }
            }
            for (Node next : in.getChildren().values()) {
                if (next instanceof InnerNode) {
                    qn.add(next);
                }
            }
        }
    }

    private int debugDepthSamplingAssert(InnerNode rootNode, OOSAlgorithm alg) {
        Map<Integer, Integer> depthTest = new HashMap<>();
        Set<InformationSet> processed = new HashSet<>();
        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.add(rootNode);

        // at each depth level, we should get the same number of samples
        // (applies for goofspiel and liar's dice)
        while (!q.isEmpty()) {
            InnerNode n = q.removeFirst();

            if (n.getInformationSet() != null) {
                if (!processed.contains(n.getInformationSet())) {
                    int defValue = n.getDepth() == 1 ? 0 : alg.gadgetActionChoices[n.getPlayerToMove().getId()][1];
                    depthTest.put(n.getDepth(),
                            depthTest.getOrDefault(n.getDepth(), defValue) + n.getInformationSet().getVisitsCnt());
                    processed.add(n.getInformationSet());
                }
            }

            for (Node next : n.getChildren().values()) {
                if (next instanceof InnerNode) {
                    q.add((InnerNode) next);
                }
            }
        }
        Integer numVisits = null;
        for (Map.Entry<Integer, Integer> entry : depthTest.entrySet()) {
            if (numVisits == null) numVisits = entry.getValue();
            assert numVisits == (int) entry.getValue();
        }

        // the number of expPlayer taking "follow" should be the same as original IS regret update visits
        GadgetChanceNode gadgetRootNode = (GadgetChanceNode) rootNode;
        Map<MCTSInformationSet, Integer> originalIsVisitCnts = new HashMap<>();
        Map<MCTSInformationSet, Integer> followIsVisitCnts = new HashMap<>();
        gadgetRootNode.getResolvingInnerNodes().values().stream()
                .map(GadgetInnerNode::getOriginalNode)
                .map(InnerNode::getInformationSet)
                .forEach(origIs -> originalIsVisitCnts.put(origIs, origIs.getVisitsCnt()));
        gadgetRootNode.getResolvingInnerNodes().values()
                .forEach(gadgetNode -> {
                    MCTSInformationSet origIs = gadgetNode.getOriginalNode().getInformationSet();
                    followIsVisitCnts.put(origIs, followIsVisitCnts.getOrDefault(origIs, 0)
                            + gadgetNode.getFollowCnt(origIs.getPlayer().getId()));
                });
        originalIsVisitCnts.keySet().forEach(is -> {
            assert (int) originalIsVisitCnts.get(is) == (int) followIsVisitCnts.get(is);
        });

//
//        Map<Action, Double> chanceProbabilities = gadgetRootNode.getChanceProbabilities();
//        Map<Action, GadgetInnerNode> resolvingInnerNodes = gadgetRootNode.getResolvingInnerNodes();
//        for(Action a: chanceProbabilities.keySet()) {
//            double p = chanceProbabilities.get(a);
//            int orig_cnt = (resolvingInnerNodes.get(a).getOriginalNode().getInformationSet()).getVisitsCnt();
//            int gadget_cnt = resolvingInnerNodes.get(a).getInformationSet().getVisitsCnt();
//            GadgetInfoSet g_is = ((GadgetInfoSet) resolvingInnerNodes.get(a).getInformationSet());
////            assert g_is.getTerminateCnt()+g_is.getFollowCnt() == numVisits;
//            System.err.println(a + "," + String.format("%.10f", p) + ","+orig_cnt+","+gadget_cnt+","+g_is.getFollowCnt()+","+g_is.getTerminateCnt());
//        }

        debugClearISVisitCounts(rootNode);

        return numVisits;
    }
}
