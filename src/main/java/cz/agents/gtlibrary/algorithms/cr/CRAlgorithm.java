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
import static cz.agents.gtlibrary.algorithms.cr.MCCR_CFV_Experiments.buildCompleteTree;
import static cz.agents.gtlibrary.algorithms.cr.ResolvingMethod.RESOLVE_MCCFR;
//import static cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm.gadgetActionChoices;

// Continual Resolving algorithm
public class CRAlgorithm implements GamePlayingAlgorithm {
    public static double totalTimeResolving = 0.;
    public static double totalTimeRoot = 0.;
    private final GameState rootState;
    private final Expander expander;
    private final InnerNode statefulRootNode;
    private final ThreadMXBean threadBean;
    ResolvingMethod defaultResolvingMethod = RESOLVE_MCCFR;
    ResolvingMethod defaultRootMethod = RESOLVE_MCCFR;
    private Node statefulCurNode;
    private Random rnd;
    private double epsilonExploration = 0.6;
    private double pi_c = 1.0;
    private boolean resetData = true;
    public CFRData rootCfrData;
    public CFRData gadgetCfrData;

    public CRAlgorithm(GameState rootState, Expander expander, double epsilonExploration) {
        this.rootState = rootState;
        this.expander = expander;
        this.rnd = ((MCTSConfig) expander.getAlgorithmConfig()).getRandom();

        this.statefulRootNode = buildRootNode();
        this.statefulCurNode = statefulRootNode;
        this.epsilonExploration = epsilonExploration;
        // use epsRM so that traversal into all parts of the public tree is well defined
        // otherwise we may get 0-prob of some actions which prohibit visiting some parts of the tree
        OOSAlgorithmData.useEpsilonRM = true;
        OOSAlgorithmData.epsilon = 0.00001f;
        threadBean = ManagementFactory.getThreadMXBean();
    }

    public CRAlgorithm(InnerNode rootNode, Expander expander, double epsilonExploration) {
        this.rootState = rootNode.getGameState();
        this.expander = expander;
        this.rnd = ((MCTSConfig) expander.getAlgorithmConfig()).getRandom();
        this.statefulRootNode = rootNode;
        this.statefulCurNode = rootNode;
        this.epsilonExploration = epsilonExploration;
        // use epsRM so that traversal into all parts of the public tree is well defined
        // otherwise we may get 0-prob of some actions which prohibit visiting some parts of the tree
        OOSAlgorithmData.useEpsilonRM = true;
        OOSAlgorithmData.epsilon = 0.00001f;
        threadBean = ManagementFactory.getThreadMXBean();
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

    public double[] runIterations(Player resolvingPlayer, int iterationsInRoot, int iterationsPerGadgetGame) {
        System.err.println("Using " +
                "iterationsInRoot=" + iterationsInRoot + " " +
                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
                "epsilonExploration=" + epsilonExploration + " " +
                "player=" + resolvingPlayer.getId() + " ");

        Node curNode = buildRootNode();
        // root MCCFR
        runRoot(resolvingPlayer, (InnerNode) curNode, iterationsInRoot);

        // continual resolving
        while (!(curNode instanceof LeafNode)) {
            Action action = runStep(resolvingPlayer, curNode, iterationsPerGadgetGame, iterationsInRoot);
            curNode = ((InnerNode) curNode).getChildFor(action);
        }

        return ((LeafNode) curNode).getUtilities();
        //StrategyCollector.getStrategyFor(getRootNode(), getRootNode().getAllPlayers()[0], new MeanStratDist());
    }

    public void runRoot(Player resolvingPlayer, InnerNode rootNode, int iterationsInRoot) {
        runRoot(defaultRootMethod, resolvingPlayer, rootNode, iterationsInRoot);
    }

    public void runRoot(ResolvingMethod rootResolveMethod,
                        Player resolvingPlayer,
                        InnerNode rootNode,
                        int iterationsInRoot) {
        long start = threadBean.getCurrentThreadCpuTime();

        int samplesSkipped = 0;
        switch (rootResolveMethod) {
            case RESOLVE_MCCFR:
                samplesSkipped = runRootMCCFR(resolvingPlayer, rootNode, iterationsInRoot);
                break;
            case RESOLVE_CFR:
                runRootCFR(resolvingPlayer, rootNode, iterationsInRoot);
        }

        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        totalTimeRoot += diff;
        System.err.println("root iters in " + diff + " ms");

        int totalIterations = iterationsInRoot - samplesSkipped;

        if (rootNode.getPlayerToMove().equals(resolvingPlayer)) {
            rootNode.getPublicState().incrResolvingIterations(totalIterations);
            rootNode.getPublicState().setResolvingMethod(rootResolveMethod);
        }
        rootNode.getPublicState().getNextPlayerPublicStates(resolvingPlayer).forEach(ps -> {
            ps.incrResolvingIterations(totalIterations);
            ps.setResolvingMethod(rootResolveMethod);
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

        Action action = runStep(resolvingPlayer, statefulCurNode, iterationsPerGadgetGame, iterationsInRoot);
        statefulCurNode = ((InnerNode) statefulCurNode).getChildFor(action);
        return action;
    }

    public Action runStep(Player resolvingPlayer, Node curNode, int iterationsPerGadgetGame, int iterationsInRoot) {
        return runStep(resolvingPlayer, curNode, defaultResolvingMethod, iterationsPerGadgetGame, iterationsInRoot);
    }

//    public void solveEntireGame(int iterationsInRoot, int iterationsPerGadgetGame) {
//        System.err.println("Using " +
//                "iterationsInRoot=" + iterationsInRoot + " " +
//                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
//                "epsilonExploration=" + epsilonExploration + " " +
//                "resetData=" + resetData + " ");
//
////        buildCompleteTree(rootNode);
//
//        InnerNode curNode = getRootNode();
//        if (iterationsInRoot < 2) {
//            System.err.println("Skipping root MCCFR.");
//        } else {
//            runRootMCCFR(iterationsInRoot);
//        }
//
//        if (iterationsPerGadgetGame < 2) {
//            System.err.println("Skipping resolving.");
//        } else {
//            ArrayDeque<PublicState> q = new ArrayDeque<>();
//            q.add(getRootNode().getPublicState());
//            while (!q.isEmpty()) {
//                PublicState s = q.removeFirst();
//
//                InnerNode n = s.getAllNodes().iterator().next();
//                runStep(n, iterationsPerGadgetGame, iterationsInRoot);
//
//                q.addAll(s.getNextPublicStates());
//            }
//        }
//
//        // make sure we have everything needed for domain stats
////        buildCompleteTree(rootNode);
//        printDomainStatistics();
//    }

    public Action runStep(Player resolvingPlayer,
                          Node curNode,
                          ResolvingMethod resolvingMethod,
                          int iterationsPerGadgetGame,
                          int iterationsInRoot) {
        if (iterationsPerGadgetGame < 2 || iterationsInRoot < 2) {
            throw new RuntimeException("Cannot resolve with small number of samples!");
        }

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

        if (curIS.getActions().size() == 1 && isNiceGame(curNode.getGameState())) {
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

        System.err.println("Updating reach probabilities");
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

                InnerNode n = s.getAllNodes().iterator().next();
                runStep(resolvingPlayer, n, iterationsPerGadgetGame, iterationsInRoot);

                q.addAll(s.getNextPlayerPublicStates(resolvingPlayer));
            }
        }

        // make sure we have everything needed for domain stats
        buildCompleteTree(solvingRoot);
        printDomainStatistics();

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
        Player updatingPlayer = ps.getPlayer();

        Set<InnerNode> nextPsNodesBarrier = new HashSet<>();
        ps.getNextPlayerPublicStates(updatingPlayer).stream()
                .map(PublicState::getAllNodes)
                .forEach(nextPsNodesBarrier::addAll);

        ArrayDeque<InnerNode> q = new ArrayDeque<>();
        q.addAll(ps.getAllNodes());

        while (!q.isEmpty()) {
            InnerNode node = q.removeFirst();

            Map<Action, Double> avgStrategy = null;
            if (node.isPlayerMoving(updatingPlayer)) {
                avgStrategy = getDistributionFor(node.getInformationSet().getAlgorithmData());
            }

            for (Action action : node.getActions()) {
                Node nextNode = node.getChildFor(action);
                if (nextNode instanceof LeafNode) continue;

                Double pA = 1.0;
                if (avgStrategy != null) {
                    pA = avgStrategy.get(action);
                }

                InnerNode nextInner = (InnerNode) nextNode;
//                assert nextInner.getReachPrByPlayer(updatingPlayer) == 1.; // not updated yet
                nextInner.setReachPrByPlayer(updatingPlayer, node.getReachPrByPlayer(updatingPlayer) * pA);
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
        InnerNode aNode = publicState.getAllNodes().iterator().next();

        System.err.println("Incrementally building tree");
        publicState.getNextPlayerPublicStates(); // build all the nodes until next public states

        System.err.println("Building gadget");
        GadgetChanceNode gadgetRootNode = publicState.getSubgame().getGadgetRoot();

        if (resetData && !isPublicTreeRootKeeping(publicState)) {
            System.err.println("Resetting data");
            publicState.resetData();
            publicState.setDataKeeping(false);
        } else {
            System.err.println("Keeping data");
            publicState.setDataKeeping(true);
        }

//        new GambitEFG().write(
//                expander.getClass().getSimpleName() + "_PS_" + publicState.getPSKey().getHash() + ".gbt",
//                gadgetRootNode);

        runGadget(resolvingMethod, resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
    }

    private void runGadget(ResolvingMethod resolvingMethod,
                           Player resolvingPlayer,
                           PublicState publicState,
                           GadgetChanceNode gadgetRootNode,
                           int iterationsPerGadgetGame) {

        System.err.println(
                "Using " + resolvingMethod + " for resolving gadget " +
                        "with " + iterationsPerGadgetGame + " iterations");

        long start = threadBean.getCurrentThreadCpuTime();
        int samplesSkipped = 0;
        switch (resolvingMethod) {
            case RESOLVE_CFR:
                runGadgetCFR(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);
                break;
            case RESOLVE_MCCFR:
                samplesSkipped = runGadgetMCCFR(resolvingPlayer, publicState, gadgetRootNode, iterationsPerGadgetGame);

        }
        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        System.err.println("resolved in " + diff + " ms");
        totalTimeResolving += diff;

        publicState.setResolvingMethod(RESOLVE_MCCFR);

        // update resolving iterations
        PublicState parentPs = publicState.getPlayerParentPublicState();
        final Integer totalIterations = (parentPs != null // propagate # of iterations down if applicable
                && parentPs.isDataKeeping()
                && parentPs.getResolvingMethod() == RESOLVE_MCCFR)
                ? iterationsPerGadgetGame - samplesSkipped + parentPs.getResolvingIterations()
                : iterationsPerGadgetGame - samplesSkipped;

        publicState.getNextPlayerPublicStates().forEach(ps -> {
            ps.incrResolvingIterations(totalIterations);
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

    public int runRootMCCFR(Player resolvingPlayer, InnerNode rootNode, int iterations) {
        System.err.println("Calculating initial strategy from root using MCCFR in "+iterations+" iterations");
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, rootNode, epsilonExploration);
        alg.setRnd(rnd);

        alg.runIterations(iterations);

        // debug
        assert debugDepthSamplingAssert(rootNode, alg) == iterations / 2;
        return alg.samplesSkipped;
    }

    public void runRootCFR(Player resolvingPlayer, InnerNode rootNode, int iterations) {
        System.err.println("Calculating initial strategy from root using CFR in "+iterations+" iterations");
        CFRAlgorithm alg = new CFRAlgorithm(resolvingPlayer, rootState, expander);

        buildCompleteTree(rootNode);
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
        alg.runIterations(iterations);

        assert debugDepthSamplingAssert(gadgetRoot, alg) == iterations / 2; // debug
        return alg.samplesSkipped;
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
            updateCFRResolvingData(ps, rootCfrData.reachProbs, rootCfrData.historyExpValues);
        });
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

    private void printDomainStatistics() {
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

        System.err.println("Game has: \n" +
                "public states & info sets & inner nodes & leaf nodes & max PT depth");
        System.err.println(config.getAllPublicStates().size() + " & " +
                config.getAllInformationSets().size() + " & " +
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
