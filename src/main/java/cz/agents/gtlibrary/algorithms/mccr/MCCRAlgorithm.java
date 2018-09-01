package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetChanceNode;
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

import static cz.agents.gtlibrary.algorithms.mccr.MCCR_CFV_Experiments.buildCompleteTree;
import static cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm.gadgetActionChoices;

// Monte Carlo Continual Resolving algorithm
public class MCCRAlgorithm implements GamePlayingAlgorithm {
    public static double totalTimeResolving = 0.;
    private final GameState rootState;
//    private final InnerNode rootNode;
    private final Expander expander;
    private final ThreadMXBean threadBean;
    private final InnerNode statefulRootNode;
    private Random rnd;
    private double epsilonExploration = 0.6;
    private Node statefulCurNode;
    private double pi_c = 1.0;

    private boolean resetData = true;

    public static boolean updateCRstatistics = true;
    public boolean resolveUsingMCCFR = true;

    public MCCRAlgorithm(GameState rootState, Expander expander, double epsilonExploration) {
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

    public MCCRAlgorithm(InnerNode rootNode, Expander expander, double epsilonExploration) {
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
        runRootMCCFR(resolvingPlayer, (InnerNode) curNode, iterationsPerGadgetGame);

        // continual resolving
        while (!(curNode instanceof LeafNode)) {
            Action action = runStep(resolvingPlayer, curNode, iterationsPerGadgetGame, iterationsInRoot);
            curNode = ((InnerNode) curNode).getChildFor(action);
        }

        return ((LeafNode) curNode).getUtilities();
        //StrategyCollector.getStrategyFor(getRootNode(), getRootNode().getAllPlayers()[0], new MeanStratDist());
    }

    public Action runStepStateful(Player resolvingPlayer, int iterationsPerGadgetGame, int iterationsInRoot) {
        if (statefulCurNode instanceof LeafNode) {
            return null;
        }

        if (statefulCurNode.equals(statefulRootNode)) {
            // root MCCFR
            runRootMCCFR(resolvingPlayer, statefulRootNode, iterationsPerGadgetGame);
        }

        Action action = runStep(resolvingPlayer, statefulCurNode, iterationsPerGadgetGame, iterationsInRoot);
        statefulCurNode = ((InnerNode) statefulCurNode).getChildFor(action);
        return action;
    }

    public Action runStep(Player resolvingPlayer, Node curNode, int iterationsPerGadgetGame, int iterationsInRoot) {
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
        } else {
            assert curNode instanceof InnerNode;

            MCTSInformationSet curIS = ((InnerNode) curNode).getInformationSet();
            PublicState curPS = curIS.getPublicState();
            PublicState parentPS = curPS.getParentPublicState();

            if(parentPS != null) {
                while (isChancePS(parentPS)) {
                    if(parentPS.getParentPublicState() == null) break;
                    parentPS = parentPS.getParentPublicState();
                }
            }

            System.err.println("--------------------------");
            System.err.println("Resolving " +
                    "\n\tPS: " + curPS + " (parent "+ parentPS +") " +
                    "\n\tIS: " + curIS + " " +
                    "\n\tN: " + curNode + " " +
                    "\n\t" + curPS.getAllNodes().size() + " nodes, " +
                    curPS.getAllInformationSets().size() + " infosets in public state");

            if (curIS.getActions().size() == 1 &&
                    (curNode.getGameState() instanceof IIGoofSpielGameState
                            || curNode.getGameState() instanceof LiarsDiceGameState
                            || curNode.getGameState() instanceof GenericPokerGameState)) {
                System.err.println("Only one action possible, skipping resolving");
                action = curIS.getActions().iterator().next();
            } else {
                Map<Action, Double> distributionBefore = getDistributionFor(curIS.getAlgorithmData());
                resolveGadgetGame(resolvingPlayer, curPS, iterationsPerGadgetGame, iterationsInRoot);
                Map<Action, Double> distributionAfter = getDistributionFor(curIS.getAlgorithmData());

                Map<Action, Double> diff = distributionBefore.keySet().stream()
                        .collect(Collectors.toMap(
                                entry -> entry,
                                distAction -> distributionBefore.get(distAction) - distributionAfter.get(distAction)
                                                 ));

                System.err.println("Before: " + distributionToString(curIS.getActions(), distributionBefore));
                System.err.println("After:  " + distributionToString(curIS.getActions(), distributionAfter));
                System.err.println("Diff:   " + distributionToString(curIS.getActions(), diff));
                action = randomChoice(distributionAfter);
            } // else

            System.err.println("Updating reach probabilities");
            updatePlayerRp(curPS);
            assert curIS.getActions().contains(action);
        }
        return action;
    }

    private boolean isChancePS(PublicState ps) {
        return ps.getPlayer().getId() == 2;
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
            System.err.println("Skipping root MCCFR.");
        } else {
            runRootMCCFR(resolvingPlayer, solvingRoot, iterationsInRoot);
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
        while(currentPs.getParentPublicState() != null) {
            currentPs = currentPs.getParentPublicState();
            if(currentPs.getAllNodes().stream().anyMatch(n -> (n instanceof ChanceNode))) {
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
        System.err.println(
                "F:" + gadgetActionChoices[0] + " " +
                "T:" + gadgetActionChoices[1] + " " +
                "ratio: " + ((double) gadgetActionChoices[0] / (gadgetActionChoices[0] + gadgetActionChoices[1])) + " " +
                "total: " + (gadgetActionChoices[0] + gadgetActionChoices[1]));
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

    private Map<Action, Double> getDistributionFor(AlgorithmData algorithmData) {
        return (new MeanStratDist()).getDistributionFor(algorithmData);
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

        while(!q.isEmpty()) {
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
                if(!nextPsNodesBarrier.contains(nextInner)) {
                    q.add(nextInner);
                }
            }
        }
    }

    private void resolveGadgetGame(Player resolvingPlayer, PublicState publicState, int iterationsPerGadgetGame, int iterationsInRoot) {
        InnerNode aNode = publicState.getAllNodes().iterator().next();

        System.err.println("Incrementally building tree");
        publicState.getNextPlayerPublicStates(); // build all the nodes until next public states

        boolean isFirstResolving = publicState.getPlayerParentPublicState() == null;
        boolean isRootKeeping = publicState.getAllNodes().size() == 1
                && aNode.getReachPr() == 1.
                && (aNode.getParent() == null || aNode.getParent() instanceof ChanceNode);

        boolean isAfterRootKeeping = false;
        if(publicState.getPlayerParentPublicState() != null) {
            PublicState parPs = publicState.getPlayerParentPublicState();
            InnerNode parNode = parPs.getAllNodes().iterator().next();
            isAfterRootKeeping = parPs.getAllNodes().size() == 1
                    && parNode.getReachPr() == 1.
                    && (parNode.getParent() == null || parNode.getParent() instanceof ChanceNode);
        }

        int expUtilityIterations = isFirstResolving
                ? iterationsInRoot
                : (isAfterRootKeeping
                    ? iterationsPerGadgetGame + iterationsInRoot
                    : iterationsPerGadgetGame);
        expUtilityIterations /= 2;

        System.err.println("Building gadget");
        Subgame subgame = new SubgameImpl(
                publicState,
                aNode.getAlgConfig(),
                aNode.getExpander(),
                expUtilityIterations);
        GadgetChanceNode gadgetRootNode = subgame.getGadgetRoot();

//        new GambitEFG().write(expander.getClass().getSimpleName() + "_RP_gadget_" + gadgetRootNode + ".gbt",
//                gadgetRootNode);


        if (resetData) {
//            if(!isRootKeeping) {
                System.err.println("Resetting data");
                subgame.resetData();
        }

//        new GambitEFG().write(expander.getClass().getSimpleName() + "_PS_"+publicState.getPSKey().getHash()+".gbt", gadgetRootNode);

        System.err.println("Resolving");
        long start = threadBean.getCurrentThreadCpuTime();
        if(resolveUsingMCCFR) {
            runGadgetMCCFR(resolvingPlayer, gadgetRootNode, iterationsPerGadgetGame);
        } else {
            runGadgetCFR(gadgetRootNode, iterationsPerGadgetGame);
        }
        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        System.err.println("resolved in "+diff+ " ms");
        totalTimeResolving += diff;
    }


    private void runRootMCCFR(Player resolvingPlayer, InnerNode rootNode, int iterations) {
        System.err.println("Calculating initial strategy from root...");
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, rootNode, epsilonExploration);
        alg.setRnd(rnd);

        long start = threadBean.getCurrentThreadCpuTime();
        alg.runIterations(iterations);
        double diff = (threadBean.getCurrentThreadCpuTime() - start) / 1e6;
        System.err.println("root iters in "+diff+ " ms");

//        printCFVs(((MCTSConfig) expander.getAlgorithmConfig()).getAllInformationSets().values().stream()
//                .filter(is -> is.getAlgorithmData() != null), iterations);
    }

    private void runGadgetCFR(GadgetChanceNode gadgetRootNode, int iterations) {
        System.err.println("using CFR for resolving gadget!");
        buildCompleteTree(gadgetRootNode);

//        InnerNode aRootNode = ((GadgetInnerNode) gadgetRootNode.getChildren().values().iterator().next()).getOriginalNode();
//        CFRAlgorithm alg = new CFRAlgorithm(aRootNode);
        CFRAlgorithm alg = new CFRAlgorithm(gadgetRootNode);
        alg.runIterations(iterations);

//        printCFVs(((MCTSConfig) expander.getAlgorithmConfig()).getAllInformationSets().values().stream()
//                        .filter(is -> is.getAlgorithmData() != null),
//                iterations);

//        alg.printStrategies(gadgetRootNode);
    }

    private void runGadgetMCCFR(Player resolvingPlayer, GadgetChanceNode gadgetRoot, int iterations) {
        OOSAlgorithm alg = new OOSAlgorithm(resolvingPlayer, gadgetRoot, epsilonExploration);
        alg.setRnd(rnd);
        alg.runIterations(iterations);

//        printCFVs(((MCTSConfig) expander.getAlgorithmConfig()).getAllInformationSets().values().stream()
//                .filter(is -> is.getAlgorithmData() != null),
//                iterations);
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
        return null;
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        return null;
    }

    @Override
    public void setCurrentIS(InformationSet currentIS) {

    }

    @Override
    public InnerNode getRootNode() {
        return statefulRootNode;
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

    public void setResetData(boolean resetData) {
        this.resetData = resetData;
    }
}
