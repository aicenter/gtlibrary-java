package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.cfr.CFRAlgorithm;
import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNodeImpl;
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
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.*;
import java.util.stream.Collectors;

import static cz.agents.gtlibrary.algorithms.mccr.MCCR_CFV_Experiments.buildCompleteTree;
import static cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm.gadgetActionChoices;

// Monte Carlo Continual Resolving algorithm
public class MCCRAlgorithm implements GamePlayingAlgorithm {
    private final GameState rootState;
    private final InnerNode rootNode;
    private final Expander expander;
    private Random rnd;
    private double epsilonExploration = 0.6;
    private Node statefulCurNode;
    private double pi_c = 1.0;

    private boolean resetData = true;

    public static boolean updateCRstatistics = true;
    public boolean gadgetMCCFR = true;

    public MCCRAlgorithm(GameState rootState, Expander expander, double epsilonExploration) {
        this.rootState = rootState;
        this.expander = expander;
        this.rnd = ((MCTSConfig) expander.getAlgorithmConfig()).getRandom();

        if (rootState.isPlayerToMoveNature()) {
            this.rootNode = new ChanceNodeImpl(expander, rootState, this.rnd);
        } else {
            this.rootNode = new InnerNodeImpl(expander, rootState);
        }

        this.statefulCurNode = rootNode;
        this.epsilonExploration = epsilonExploration;
        // use epsRM so that traversal into all parts of the public tree is well defined
        // otherwise we may get 0-prob of some actions which prohibit visiting some parts of the tree
        OOSAlgorithmData.useEpsilonRM = true;
        OOSAlgorithmData.epsilon = 0.00001f;
    }

    public MCCRAlgorithm(InnerNode rootNode, Expander expander, double epsilonExploration) {
        this.rootState = rootNode.getGameState();
        this.expander = expander;
        this.rnd = ((MCTSConfig) expander.getAlgorithmConfig()).getRandom();
        this.rootNode = rootNode;

        this.statefulCurNode = rootNode;
        this.epsilonExploration = epsilonExploration;
        // use epsRM so that traversal into all parts of the public tree is well defined
        // otherwise we may get 0-prob of some actions which prohibit visiting some parts of the tree
        OOSAlgorithmData.useEpsilonRM = true;
        OOSAlgorithmData.epsilon = 0.00001f;
    }

    public double[] runIterations(int iterationsInRoot, int iterationsPerGadgetGame) {
        System.err.println("Using " +
                "iterationsInRoot=" + iterationsInRoot + " " +
                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
                "epsilonExploration=" + epsilonExploration + " ");

        Node curNode = rootNode;
        // root MCCFR
        runRootMCCFR(iterationsPerGadgetGame);

        // continual resolving
        while (!(curNode instanceof LeafNode)) {
            Action action = runStep(curNode, iterationsPerGadgetGame);
            curNode = ((InnerNode) curNode).getChildFor(action);
        }

        return ((LeafNode) curNode).getUtilities();
        //StrategyCollector.getStrategyFor(getRootNode(), getRootNode().getAllPlayers()[0], new MeanStratDist());
    }

    public Action runStepStateful(int iterationsPerGadgetGame) {
        if (statefulCurNode instanceof LeafNode) {
            return null;
        }

        if (statefulCurNode.equals(rootNode)) {
            // root MCCFR
            runRootMCCFR(iterationsPerGadgetGame);
        }

        Action action = runStep(statefulCurNode, iterationsPerGadgetGame);
        statefulCurNode = ((InnerNode) statefulCurNode).getChildFor(action);
        return action;
    }

    public Action runStep(Node curNode, int iterationsPerGadgetGame) {
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

            try {
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
                    resolveGadgetGame(curPS, iterationsPerGadgetGame);
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
                // this builds tree until next public states
                for (InnerNode node : curPS.getAllNodes()) {
                    updateRp_newAvgStrategyFound(curIS.getPlayer(), curIS.getOpponent(), node.getReachPr(), node);
                }

                assert curIS.getActions().contains(action);
            } finally {
//            } catch (Throwable e){
                // debug
//                new GambitEFG().write(expander.getClass().getSimpleName() + "_RP_node_" + curNode + ".gbt", curNode);
//                new GambitEFG().write(expander.getClass().getSimpleName() + "_RP_root_" + curNode + ".gbt", getRootNode());
//                throw e;
            }
        }
        return action;
    }

    private boolean isChancePS(PublicState ps) {
        return ps.getAllNodes().iterator().next() instanceof ChanceNode;
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

    public void solveEntireGame(int iterationsInRoot, int iterationsPerGadgetGame) {
        System.err.println("Using " +
                "iterationsInRoot=" + iterationsInRoot + " " +
                "iterationsPerGadgetGame=" + iterationsPerGadgetGame + " " +
                "epsilonExploration=" + epsilonExploration + " " +
                "resetData=" + resetData + " ");

        InnerNode curNode = getRootNode();
        if (iterationsInRoot < 2) {
            System.err.println("Skipping root MCCFR.");
        } else {
            runRootMCCFR(iterationsInRoot);
        }

        if (iterationsPerGadgetGame < 2) {
            System.err.println("Skipping resolving.");
        } else {
            ArrayDeque<PublicState> q = new ArrayDeque<>();
            q.add(getRootNode().getPublicState());
            while (!q.isEmpty()) {
                PublicState s = q.removeFirst();

                InnerNode n = s.getAllNodes().iterator().next();
                runStep(n, iterationsPerGadgetGame);

                q.addAll(s.getNextPlayerPublicStates());
            }
        }
        printDomainStatistics();
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
        System.err.println("Game has: public states, info sets, inner nodes, leaf nodes: ");
        System.err.println(config.getAllPublicStates().size() + " & " +
                config.getAllInformationSets().size() + " & " +
                inners + " & " +
                leafs);
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
            } else {
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

    private void updateRp_newAvgStrategyFound(Player updatingPlayer, Player opponentPlayer,
                                              double updatePr, InnerNode node) {
        // Top-down update of reach probabilities.
        //
        // From current public state until next public states of the same player,
        // update reach probabilities of each node.
        //
        // Basically, we know that between the public states the current player
        // will play the resolved average strategy.

        Map<Action, Double> avgStrategy = null;
        if (node.isPlayerMoving(updatingPlayer)) {
            avgStrategy = getDistributionFor(node.getInformationSet().getAlgorithmData());
        }

        // todo: dont ignore multi-level IS, and chance player in public state
        for (Action action : node.getActions()) {
            Node nextNode = node.getChildFor(action);
            if (nextNode instanceof LeafNode) continue;

            Double pA = 1.0;
            if (node.isPlayerMoving(updatingPlayer)) {
                pA = avgStrategy.get(action);
            }

            InnerNode nextInner = (InnerNode) nextNode;
            if (nextInner.isPlayerMoving(updatingPlayer)) {
                nextInner.setPlayerReachPr(updatePr * pA); // we are done, no more recursion
            } else {
                updateRp_newAvgStrategyFound(updatingPlayer, opponentPlayer, updatePr * pA, nextInner);
            }
        }

    }

    private void resolveGadgetGame(PublicState publicState, int iterationsPerGadgetGame) {
        InnerNode aNode = publicState.getAllNodes().iterator().next();

        publicState.getNextPlayerPublicStates(); // build all the nodes until next public states

        Subgame subgame = new SubgameImpl(
                publicState,
                aNode.getAlgConfig(),
                aNode.getExpander(),
                iterationsPerGadgetGame);
        GadgetChanceNode gadgetRootNode = subgame.getGadgetRoot();


//        new GambitEFG().write(expander.getClass().getSimpleName() + "_RP_gadget_" + gadgetRootNode + ".gbt",
//                gadgetRootNode);

//        System.err.println(playerIS + " isCFV: "+ ((OOSAlgorithmData) playerIS.getAlgorithmData()).getIsCFV(0));

//        Optional<double[]> utils = gadgetRootNode.getChildren().values().stream()
//                .filter(gadgetInnerNode -> ((GadgetInnerNode) gadgetInnerNode).getOriginalNode().getInformationSet().equals(
//                        playerIS))
//                .findFirst().map(gadgetInnerNode -> ((GadgetInnerNode) gadgetInnerNode).getTerminateNode())
//                .map(GadgetLeafNode::getUtilities);
//        System.err.println(playerIS + " gadget util: "+ utils.get()[0]);

        if (resetData) {
            subgame.resetData();
        }

//        new GambitEFG().write(expander.getClass().getSimpleName() + "_PS_"+publicState.getPSKey().getHash()+".gbt", gadgetRootNode);

        if(gadgetMCCFR) {
            runGadgetMCCFR(gadgetRootNode, iterationsPerGadgetGame);
        } else {
            runGadgetCFR(gadgetRootNode, iterationsPerGadgetGame);
        }
    }


    private void runRootMCCFR(int iterations) {
        System.err.println("Calculating initial strategy from root...");
        OOSAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], rootNode, epsilonExploration);
        alg.setRnd(rnd);
        alg.runIterations(iterations);

//        printCFVs(((MCTSConfig) expander.getAlgorithmConfig()).getAllInformationSets().values().stream()
//                .filter(is -> is.getAlgorithmData() != null), iterations);
    }

    private void runGadgetCFR(GadgetChanceNode gadgetRootNode, int iterations) {
        System.err.println("using CFR for resolving gadget!");
        buildCompleteTree(gadgetRootNode);
        CFRAlgorithm alg = new CFRAlgorithm(gadgetRootNode);
        alg.runIterations(iterations);

//        printCFVs(((MCTSConfig) expander.getAlgorithmConfig()).getAllInformationSets().values().stream()
//                        .filter(is -> is.getAlgorithmData() != null),
//                iterations);

//        alg.printStrategies(gadgetRootNode);
    }

    private void runGadgetMCCFR(GadgetChanceNode gadgetRoot, int iterations) {
        OOSAlgorithm alg = new OOSAlgorithm(gadgetRoot, epsilonExploration);
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
        return rootNode;
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
