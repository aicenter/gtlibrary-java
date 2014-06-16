/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMSelector;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;


/**
 * @author vilo
 */
public class SMMCTSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected Simulator simulator;
    protected SMBackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;

    private HighQualityRandom rnd = new HighQualityRandom();

    public SMMCTSAlgorithm(Player searchingPlayer, Simulator simulator, SMBackPropFactory fact, GameState rootState, Expander expander) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.fact = fact;
        if (rootState.isPlayerToMoveNature()) this.rootNode = new ChanceNode(expander, rootState);
        else this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            iteration(rootNode);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        Map<Action, Double> distribution;
        if (rootNode.getInformationSet().getPlayer().equals(searchingPlayer)) {
            distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
        } else {
            distribution = (new MeanStratDist()).getDistributionFor(((InnerNode) rootNode.getChildren().values().iterator().next()).getInformationSet().getAlgorithmData());
        }
        return Strategy.selectAction(distribution, rnd);
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            iteration(rootNode);
        }
        if (!rootNode.getInformationSet().getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }

    protected double iteration(Node node) {
        if (node instanceof LeafNode) return ((LeafNode) node).getUtilities()[searchingPlayer.getId()];
        else {
            InnerNode n = (InnerNode) node;
            double retValue = 0;
            Pair<Integer, Integer> selActionIdxs;
            Action selAction = null;
            SMSelector selector = null;

            if (node instanceof ChanceNode) {
                selAction = ((ChanceNode) node).getRandomAction();
                Node child = n.getChildFor(selAction);
                return iteration(child);
            }

            assert n.getInformationSet().getAllNodes().size() == 1;
            selector = (SMSelector) n.getInformationSet().getAlgorithmData();
            if (selector != null) {
                selActionIdxs = selector.select();
                selAction = n.getActions().get(selActionIdxs.getLeft());
                InnerNode bottom = (InnerNode) n.getChildFor(selAction);
                Node child = bottom.getChildFor(bottom.getActions().get(selActionIdxs.getRight()));
                retValue = iteration(child);
            } else {
//                expandNode(n);
                createSelectors(n);
                selector = (SMSelector) n.getInformationSet().getAlgorithmData();
                selActionIdxs = selector.select();
                selAction = n.getActions().get(selActionIdxs.getLeft());
                InnerNode bottom = (InnerNode) n.getChildFor(selAction);
                Node child = bottom.getChildFor(bottom.getActions().get(selActionIdxs.getRight()));
                retValue = simulator.simulate(child.getGameState())[searchingPlayer.getId()];
            }
            selector.update(selActionIdxs, retValue);
            return retValue;
        }
    }

    private void createSelectors(InnerNode node) {
        List<Action> childActions = null;

        for (Node child : node.getChildren().values()) {
            if (child instanceof InnerNode) {
                childActions = ((InnerNode) child).getActions();
                break;
            }
        }
        InnerNode child = null;

        if (childActions == null) {
            child = ((InnerNode) node.getChildFor(node.getActions().get(0)));
            childActions = child.getActions();
        }
        SMSelector selector = fact.createSlector(node.getActions(), childActions);
        node.getInformationSet().setAlgorithmData(selector);
        if (child != null)
            child.getInformationSet().setAlgorithmData(selector.getBottomData());
    }

    private void expandNode(InnerNode n) {
        InnerNode bottom = null;
        for (Action a : n.getActions()) {
            bottom = (InnerNode) n.getChildFor(a);
            for (Action b : bottom.getActions()) {
                bottom.getChildFor(b);
            }
        }
        assert n.getActions().size() == bottom.getInformationSet().getAllNodes().size();
        SMSelector selector = fact.createSlector(n.getActions(), bottom.getActions());
        n.getInformationSet().setAlgorithmData(selector);
        bottom.getInformationSet().setAlgorithmData(selector.getBottomData());
    }

    @Override
    public void setCurrentIS(InformationSet curIS) {
        assert curIS.getAllStates().size() == 1;//only the top node of the simultaneous move can be given here
        MCTSInformationSet currentIS = (MCTSInformationSet) curIS;
        rootNode = currentIS.getAllNodes().iterator().next();
    }

    @Override
    public InnerNode getRootNode() {
        return rootNode;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        MCTSInformationSet is = rootNode.getAlgConfig().getInformationSetFor(gameState);
        if (is.getAllNodes().isEmpty()) {
            InnerNode in = rootNode;
            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[0]).getLast());
            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[1]).getLast());
            if (in.getGameState().isPlayerToMoveNature()) {
                in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[2]).getLast());
            }
            is = rootNode.getAlgConfig().getInformationSetFor(gameState);
            expandNode(in);
        }
        assert is.getAllNodes().size() == 1;
        rootNode = is.getAllNodes().iterator().next();
        rootNode.setParent(null);
        Action a = runMiliseconds(miliseconds);
        if (gameState.getPlayerToMove().equals(searchingPlayer)) {
            System.out.println("returning " + a);
            return a;
        } else {
            InnerNode child = (InnerNode) rootNode.getChildFor(rootNode.getActions().get(0));
            is = child.getInformationSet();
            Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());

            System.out.println("distr  " + distribution);
            return Strategy.selectAction(distribution, rnd);
        }
    }
}
