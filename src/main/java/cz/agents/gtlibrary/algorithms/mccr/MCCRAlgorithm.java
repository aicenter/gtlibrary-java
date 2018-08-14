package cz.agents.gtlibrary.algorithms.mccr;

import cz.agents.gtlibrary.algorithms.mccr.gadgettree.GadgetChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNodeImpl;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithm;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.io.GambitEFG;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

// Monte Carlo Continual Resolving algorithm
public class MCCRAlgorithm implements GamePlayingAlgorithm {
    private final GameState rootState;
    private final InnerNode rootNode;
    private final Expander expander;
    private Random rnd;
    private double epsilonExploration = 0.6;
    private Node statefulCurNode;
    private double pi_c = 1.0;

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

    public Strategy runIterations(int iterationsInRoot, int iterationsPerGadgetGame) {
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

        return StrategyCollector.getStrategyFor(getRootNode(), getRootNode().getAllPlayers()[0], new MeanStratDist());
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
            try {
                System.err.println("--------------------------");
                System.err.println(
                        "Resolving " + curIS + "  --  current " + curNode + " -- " + curIS.getPublicState().getAllNodes().size() + " nodes, " + curIS.getPublicState().getAllInformationSets().size() + " infosets in public state");

                if (curIS.getActions().size() == 1) {
                    System.err.println("Only one action possible, skipping resolving");
                    action = curIS.getActions().iterator().next();
                } else {
                    Map<Action, Double> distributionBefore = getDistributionFor(curIS.getAlgorithmData());
                    Map<Action, Double> distribution = resolveGadgetGame(curIS, iterationsPerGadgetGame);
                    Map<Action, Double> diff = distributionBefore.keySet().stream()
                            .collect(Collectors.toMap(
                                    entry -> entry,
                                    distAction -> distributionBefore.get(distAction) - distribution.get(distAction)
                                                     ));

                    System.err.println("Before: " + distributionToString(curIS.getActions(), distributionBefore));
                    System.err.println("After:  " + distributionToString(curIS.getActions(), distribution));
                    System.err.println("Diff:   " + distributionToString(curIS.getActions(), diff));
                    action = randomChoice(distribution);
                }

                System.err.println("Updating reach probabilities");
                // this builds tree until next public states
                for (InnerNode node : curIS.getPublicState().getAllNodes()) {
                    updateRp_newAvgStrategyFound(curIS.getPlayer(), node.getReachPr(), node);
                }

                assert curIS.getActions().contains(action);
            } finally {
//            } catch (Throwable e){
                // debug
                new GambitEFG().write(expander.getClass().getSimpleName()+"_RP_node_"+curNode+".gbt", curNode);
                new GambitEFG().write(expander.getClass().getSimpleName()+"_RP_root_"+curNode+".gbt", getRootNode());
//                throw e;
            }
        }
        return action;
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
                "epsilonExploration=" + epsilonExploration + " ");

        Node curNode = getRootNode();
        runRootMCCFR(iterationsInRoot);

        ArrayDeque<PublicState> q = new ArrayDeque<>();
        q.add(getRootNode().getPublicState());
        while (!q.isEmpty()) {
            PublicState s = q.removeFirst();

            Node n = s.getAllNodes().iterator().next();
            runStep(n, iterationsPerGadgetGame);

            q.addAll(s.getNextPublicStates());

//            // check if this part of the tree is reachable
//            // if not, we can keep any kind of strategy (also default)
//            double rootReach = s.getAllNodes().stream()
//                    .map(InnerNode::getReachPr)
//                    .reduce(0.0, Double::sum);
//            if(rootReach > 0){
//                runStep(n, iterationsPerGadgetGame);
//                q.addAll(s.getNextPublicStates());
//            }
        }

        MCTSConfig config = getRootNode().getAlgConfig();
        System.err.println("Game has: " + config.getAllPublicStates().size() + " public states, "
                + config.getAllInformationSets().size() + " info sets. ");
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
                continue;
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


    private Map<Action, Double> getDistributionFor(AlgorithmData algorithmData) {
        return (new MeanStratDist()).getDistributionFor(algorithmData);
    }

    private void updateRp_newAvgStrategyFound(Player updatingPlayer, double updatePr, InnerNode node) {
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

            if (nextNode instanceof InnerNode
                    && !(nextNode instanceof ChanceNode)
                    && ((InnerNode) nextNode).getInformationSet().getAlgorithmData() == null) {
                InnerNode inNode = ((InnerNode) nextNode);
                inNode.getInformationSet().setAlgorithmData(new OOSAlgorithmData(inNode.getActions()));
            }

            Double pA = 1.0;
            if (node.isPlayerMoving(updatingPlayer)) {
                pA = avgStrategy.get(action);
            }

            InnerNode nextInner = (InnerNode) nextNode;
            if (nextInner.isPlayerMoving(updatingPlayer)) {
                nextInner.setPlayerReachPr(updatePr * pA); // we are done, no more recursion
            } else {
                updateRp_newAvgStrategyFound(updatingPlayer, updatePr * pA, nextInner);
            }
        }

    }

    private Map<Action, Double> resolveGadgetGame(MCTSInformationSet playerIS, int iterationsPerGadgetGame) {
        InnerNode aNode = playerIS.getAllNodes().iterator().next();

        PublicState publicState = playerIS.getPublicState();
        publicState.getNextPublicStates(); // build all the nodes until next public states


        Subgame subgame = new SubgameImpl(
                publicState,
                aNode.getAlgConfig(),
                aNode.getExpander());
        GadgetChanceNode gadgetRootNode = subgame.getGadgetRoot();

        new GambitEFG().write(expander.getClass().getSimpleName()+"_RP_gadget_"+gadgetRootNode+".gbt", gadgetRootNode);

        subgame.resetData();

        return runGadgetMCCFR(gadgetRootNode, playerIS, iterationsPerGadgetGame);
    }

    private void runRootMCCFR(int iterations) {
        System.err.println("Calculating initial strategy from root...");
        OOSAlgorithm alg = new OOSAlgorithm(rootState.getAllPlayers()[0], rootNode, epsilonExploration);
        alg.setRnd(rnd);
        alg.runIterations(iterations);

//        printCFVs();
    }

    private Map<Action, Double> runGadgetMCCFR(GadgetChanceNode gadgetRoot,
                                               MCTSInformationSet playerIS,
                                               int iterations) {

        OOSAlgorithm alg = new OOSAlgorithm(playerIS.getPlayer(), gadgetRoot, epsilonExploration);
        alg.setRnd(rnd);
        alg.runIterations(iterations);

        printCFVs();

        return getDistributionFor(playerIS.getAlgorithmData());
    }

    private void printCFVs() {
        ((MCTSConfig) expander.getAlgorithmConfig()).getAllInformationSets().values().stream().filter(
                is -> ((OOSAlgorithmData) is.getAlgorithmData()).track).forEach(is -> {
            OOSAlgorithmData data = ((OOSAlgorithmData) is.getAlgorithmData());
            System.out.println(is.toString()+","+((int) data.getIsVisitsCnt())+","+data.getIsCFV());
        });
    }

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
}
