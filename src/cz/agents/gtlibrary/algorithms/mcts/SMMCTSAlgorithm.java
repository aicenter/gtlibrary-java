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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMSelector;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.Pair;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;


/**
 * @author vilo
 */
public class SMMCTSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected Simulator simulator;
    protected SMBackPropFactory fact;
    protected InnerNode rootNode;
    protected MCTSConfig config;
    protected ThreadMXBean threadBean;
    protected Expander<MCTSInformationSet> expander;

    public SMMCTSAlgorithm(Player searchingPlayer, Simulator simulator, SMBackPropFactory fact, GameState rootState, Expander expander) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.fact = fact;
        this.expander = expander;
        if (rootState.isPlayerToMoveNature())
            this.rootNode = new ChanceNode(expander, rootState, fact.getRandom());
        else
            this.rootNode = new InnerNode(expander, rootState);
        config = rootNode.getAlgConfig();
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

        if (rootNode == null || rootNode.getInformationSet() == null)
            return null;

        if (rootNode.getInformationSet().getPlayer().equals(searchingPlayer)) {
            distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
        } else {
            distribution = (new MeanStratDist()).getDistributionFor(((InnerNode) rootNode.getChildren().values().iterator().next()).getInformationSet().getAlgorithmData());
        }
        return Strategy.selectAction(distribution, fact.getRandom());
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            iteration(rootNode);
        }
        if (!rootNode.getGameState().getPlayerToMove().equals(searchingPlayer))
            return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());

        return Strategy.selectAction(distribution, fact.getRandom());
    }

    protected double iteration(Node node) {
        if (node instanceof LeafNode) {
            return ((LeafNode) node).getUtilities()[0];
        } else {
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
                expandNode(n);
                selector = (SMSelector) n.getInformationSet().getAlgorithmData();
                selActionIdxs = selector.select();
                selAction = n.getActions().get(selActionIdxs.getLeft());
                InnerNode bottom = (InnerNode) n.getChildFor(selAction);
                Node child = bottom.getChildFor(bottom.getActions().get(selActionIdxs.getRight()));
                retValue = simulator.simulate(child.getGameState())[0];
            }
            selector.update(selActionIdxs, retValue);
            return retValue;
        }
    }

    private void expandNode(InnerNode n) {
        InnerNode bottom = (InnerNode) n.getChildFor(n.getActions().get(0));
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
        MCTSInformationSet is = config.getInformationSetFor(gameState);

        if (is.getAllNodes().isEmpty()) {
//            InnerNode in = rootNode;
//
//            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[0]).getLast());
//            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[1]).getLast());
//            if (in.getGameState().isPlayerToMoveNature()) {
//                in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[2]).getLast());
//            }
//            is = config.getInformationSetFor(gameState);
            InnerNode in = new InnerNode(expander, gameState);

            is = in.getInformationSet();
            assert !is.getAllNodes().isEmpty();
            expandNode(in);
        }
        assert is.getAllNodes().size() == 1;
        rootNode = is.getAllNodes().iterator().next();
        rootNode.setParent(null);
        Action action = runMiliseconds(miliseconds);
        System.out.println("Mean SMMCTS leaf depth: " + StrategyCollector.meanLeafDepth(rootNode));
        //Pair<Double,Double> supportSize = StrategyCollector.meanSupportSize(rootNode, new MeanStratDist());
        //System.out.println("Mean SMMCTS support size : " + supportSize.getLeft() + ", mean num of actions: " + supportSize.getRight());
        System.out.println("Mean SMMCTS support size: " + StrategyCollector.meanSupportSize(StrategyCollector.getStrategyFor(rootNode, searchingPlayer, new MeanStratDist())));
        
        System.out.println("SMMCTS p1: " + (new MeanStratDist()).getDistributionFor(is.getAlgorithmData()));
        System.out.println("SMMCTS p2: " + (new MeanStratDist()).getDistributionFor(((InnerNode) (rootNode.getChildren().values().iterator().next())).getInformationSet().getAlgorithmData()));
        if (gameState.getPlayerToMove().equals(searchingPlayer)) {
            clean(action);
            
            return action;
        } else {
            InnerNode child = (InnerNode) rootNode.getChildFor(rootNode.getActions().get(0));
            is = child.getInformationSet();
            Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());
            action = Strategy.selectAction(distribution, fact.getRandom());
            clean(action);
            return action;
        }
    }

    private void clean(Action action) {
//        System.gc();
//        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
//        long oldUsedMemory = memoryBean.getHeapMemoryUsage().getUsed();
//        int isCount = config.getAllInformationSets().size();
//        long finPending = memoryBean.getObjectPendingFinalizationCount();
//
//
//        System.err.println("is count: " + isCount);
//        new Scanner(System.in).next();
        cleanUnnecessaryPartsOfTree(action);
//        System.err.println("pending change before gc: " + (finPending - memoryBean.getObjectPendingFinalizationCount()));
        System.gc();
//        new Scanner(System.in).next();
//        System.err.println("pending change after gc: " + (finPending - memoryBean.getObjectPendingFinalizationCount()));
//        System.err.println("saved memory: " + (oldUsedMemory - memoryBean.getHeapMemoryUsage().getUsed()) / 1e6);
//        System.err.println("total memory: " + memoryBean.getHeapMemoryUsage().getUsed() / 1e6);
//        System.err.println("deleted IS: " + (isCount - config.getAllInformationSets().size()));

    }

//    private void clean(GameState state) {
//        System.gc();
//        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
//        long oldUsedMemory = memoryBean.getHeapMemoryUsage().getUsed();
//        int isCount = rootNode.getAlgConfig().getAllInformationSets().size();
//        long finPending = memoryBean.getObjectPendingFinalizationCount();
//        System.err.println("----------------------------");
//        System.err.println("is count: " + isCount);
//        cleanUnnecessaryPartsOfTree(state);
//        System.err.println("pending change before gc: " + (finPending - memoryBean.getObjectPendingFinalizationCount()));
//        System.gc();
//        System.err.println("pending change after gc: " + (finPending - memoryBean.getObjectPendingFinalizationCount()));
//        System.err.println("saved memory: " + (oldUsedMemory - memoryBean.getHeapMemoryUsage().getUsed()) / 1e6);
//        System.err.println("total memory: " + memoryBean.getHeapMemoryUsage().getUsed() / 1e6);
//        System.err.println("deleted IS: " + (isCount - rootNode.getAlgConfig().getAllInformationSets().size()));
//
//    }

    private void cleanUnnecessaryPartsOfTree(Action action) {
        if (rootNode.getChildren() != null)
            for (Node node : rootNode.getChildren().values()) {
                node.setParent(null);
                if (node instanceof InnerNode) {

                    if(!node.getLastAction().equals(action)) {
                        ((InnerNode)node).getInformationSet().setAlgorithmData(null);
                        ((InnerNode)node).setInformationSet(null);
                        ((InnerNode)node).setAlgorithmData(null);
                        ((InnerNode)node).setChildren(null);
                        ((InnerNode)node).setActions(null);
                    }
                    ((InnerNode) node).setLastAction(null);
                }
            }
        rootNode.setParent(null);
        rootNode.setLastAction(null);
        rootNode.setChildren(null);
        rootNode.setActions(null);
        GameState state = rootNode.getGameState();
        Sequence p1Sequence = state.getSequenceFor(state.getAllPlayers()[0]);
        Sequence p2Sequence = state.getSequenceFor(state.getAllPlayers()[1]);

        if (searchingPlayer.getId() == 0) {
            if (p2Sequence.size() > 0)
                rootNode.getAlgConfig().cleanSetsNotContaining(action, p1Sequence.size(), p2Sequence.getLast(), p2Sequence.size() - 1);
            else
                rootNode.getAlgConfig().cleanSetsNotContaining(action, p1Sequence.size(), null, -1);
        } else {
            if (p1Sequence.size() > 0)
                rootNode.getAlgConfig().cleanSetsNotContaining(p1Sequence.getLast(), p1Sequence.size() - 1, action, p2Sequence.size());
            else
                rootNode.getAlgConfig().cleanSetsNotContaining(null, -1, action, p2Sequence.size());
        }
        if (rootNode.getInformationSet() != null)
            rootNode.getInformationSet().setAlgorithmData(null);
        rootNode.setInformationSet(null);
        rootNode = null;
    }

//    private void cleanUnnecessaryPartsOfTree(GameState state) {
//        rootNode.setParent(null);
//        rootNode.setLastAction(null);
//        Sequence p1Sequence = state.getSequenceFor(state.getAllPlayers()[0]);
//        Sequence p2Sequence = state.getSequenceFor(state.getAllPlayers()[1]);
//
//        if(p1Sequence.size() == 0 && p2Sequence.size() == 0)
//            return;
//        if(p1Sequence.size() == 0)
//            rootNode.getAlgConfig().cleanSetsNotContaining(null, -1, p2Sequence.getLast(), p2Sequence.size() - 1);
//        else if(p2Sequence.size() == 0)
//            rootNode.getAlgConfig().cleanSetsNotContaining(p1Sequence.getLast(), p1Sequence.size() - 1, null, -1);
//        else
//            rootNode.getAlgConfig().cleanSetsNotContaining(p1Sequence.getLast(), p1Sequence.size() - 1, p2Sequence.getLast(), p2Sequence.size() - 1);
//
//    }
}
