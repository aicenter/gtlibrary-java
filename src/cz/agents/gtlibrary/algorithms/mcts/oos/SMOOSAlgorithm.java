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
package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Random;


/**
 * @author vilo
 */
public class SMOOSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected OOSSimulator simulator;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;
    protected MCTSConfig config;
    protected Expander expander;
    private double epsilon = 0.6;
    public boolean dropTree = false;

    private Random rnd;

    public SMOOSAlgorithm(Player searchingPlayer, OOSSimulator simulator, GameState rootState, Expander expander, double epsilon, Random random) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.epsilon = epsilon;
        if (rootState.isPlayerToMoveNature())
            this.rootNode = new ChanceNode(expander, rootState, random);
        else
            this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
        config = rootNode.getAlgConfig();
        this.rnd = random;
        this.expander = expander;
        String s = System.getProperty("DROPTREE");
        if (s != null) dropTree = Boolean.getBoolean(s);
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            iteration(rootNode, rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            iteration(rootNode, rootNode.getGameState().getAllPlayers()[1]);
            iters++;
        }
        System.out.println();
        System.out.println("OOS Iters: " + iters);
        if (!rootNode.getGameState().getPlayerToMove().equals(searchingPlayer))
            return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());

        return Strategy.selectAction(distribution, rnd);
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations / 2; i++) {
            iteration(rootNode, rootNode.getGameState().getAllPlayers()[0]);
            iteration(rootNode, rootNode.getGameState().getAllPlayers()[1]);
        }
        if (!rootNode.getInformationSet().getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }


    //additional iteration return values
    private double x = -1;
    private double q = -1;

    /**
     * The main function for OOS iteration.
     *
     * @param node      current node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, Player expPlayer) {
        if (node instanceof LeafNode) {
            x = 1;
            q = 1;
            return ((LeafNode) node).getUtilities()[expPlayer.getId()];
        }
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            Action a = cn.getRandomAction();
            double p = cn.getGameState().getProbabilityOfNatureFor(a);
            double u = iteration(cn.getChildFor(a), expPlayer);
            x *= p; q *= p;
            return u;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data1,data2;
        Action selectedA;
        double u = 0;
        int a1 = -1,a2 = -1;
        double pa1 = -1,pa2 = -1;
        if (is.getAlgorithmData() == null) {//this is a new Information Set
            data1 = new OOSAlgorithmData(in.getActions());
            is.setAlgorithmData(data1);
            a1 = rnd.nextInt(in.getActions().size());
            pa1 = 1.0 / in.getActions().size();
            selectedA = in.getActions().get(a1);
            InnerNode in2 = (InnerNode) in.getChildFor(selectedA);
            is = in2.getInformationSet();
            data2 = new OOSAlgorithmData(in2.getActions());
            is.setAlgorithmData(data2);
            a2 = rnd.nextInt(in2.getActions().size());
            pa2 = 1.0 / in2.getActions().size();
            selectedA = in2.getActions().get(a2);
            
            Node child = in2.getChildFor(selectedA);
            u = simulator.simulate(child.getGameState())[expPlayer.getId()];
            x = simulator.playersProb * pa1 * pa2;
            q = x;
        } else {
            if (expPlayer.getId()==0) {//first player is exploring
                data1 = (OOSAlgorithmData) is.getAlgorithmData();
                data1.getRMStrategy(rmProbs);
                if (rnd.nextDouble() < epsilon) a1 = rnd.nextInt(in.getActions().size());
                else a1 = randomChoice(rmProbs, 1);
                pa1 = rmProbs[a1];
                selectedA = in.getActions().get(a1);
                InnerNode in2 = (InnerNode) in.getChildFor(selectedA);
                is = in2.getInformationSet();
                data2 = (OOSAlgorithmData) is.getAlgorithmData();
                data2.getRMStrategy(rmProbs);
                a2 = randomChoice(rmProbs, 1);
                pa2 = rmProbs[a2];
                u = iteration(in2.getChildFor(in2.getActions().get(a2)), expPlayer);
                q *= ((1 - epsilon) * pa1 + (epsilon / in.getActions().size()))*pa2;
                
                double c = x*pa2;
                x = c*pa1;
                data1.updateRegret(a1, u/q, c, x);
                data2.getRMStrategy(rmProbs);
                data2.updateMeanStrategy(rmProbs, 1);
                
            } else {
                data1 = (OOSAlgorithmData) is.getAlgorithmData();
                data1.getRMStrategy(rmProbs);
                a1 = randomChoice(rmProbs, 1);
                pa1 = rmProbs[a1];
                selectedA = in.getActions().get(a1);
                InnerNode in2 = (InnerNode) in.getChildFor(selectedA);
                is = in2.getInformationSet();
                data2 = (OOSAlgorithmData) is.getAlgorithmData();
                data2.getRMStrategy(rmProbs);
                if (rnd.nextDouble() < epsilon) a2 = rnd.nextInt(in2.getActions().size());
                else a2 = randomChoice(rmProbs, 1);
                pa2 = rmProbs[a2];
                u = iteration(in2.getChildFor(in2.getActions().get(a2)), expPlayer);
                q *= ((1 - epsilon) * pa2 + (epsilon / in2.getActions().size()))*pa1;
                
                double c = x;
                x = c*pa2;
                data2.updateRegret(a2, pa1*u/q, c, x);
                data1.getRMStrategy(rmProbs);
                data1.updateMeanStrategy(rmProbs, 1);
                x *= pa1;
            }
        }
        return u;
    }


    private double[] rmProbs = new double[1000];

    private int randomChoice(double[] dArray, double sum) {
        double r = rnd.nextDouble() * sum;
        for (int i = 0; i < dArray.length; i++) {
            if (r <= dArray[i]) return i;
            r -= dArray[i];
        }
        return -1;
    }


    @Override
    public void setCurrentIS(InformationSet is) {
        throw new UnsupportedOperationException();
    }

    private Player getOpponent() {
        return rootNode.getGameState().getAllPlayers()[1 - searchingPlayer.getId()];
    }

    private Player getNaturePlayer() {
        return rootNode.getGameState().getAllPlayers()[2];
    }


    public InnerNode getRootNode() {
        return rootNode;
    }

    Map<Action, Double> lastP1dist;
    Map<Action, Double> lastP2dist;
    
    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        if (dropTree) config.cleanSetsNotContaining(null, 0, null, 0);
        MCTSInformationSet is = config.getInformationSetFor(gameState);
        if (is.getAllNodes().isEmpty()) {
//            InnerNode in = rootNode;
//            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[0]).getLast());
//            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[1]).getLast());
//            if (in.getGameState().isPlayerToMoveNature()) {
//                in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[2]).getLast());
//            }
//            is = config.getInformationSetFor(gameState);
//            is.setAlgorithmData(new OOSAlgorithmData(in.getActions()));
            InnerNode in = new InnerNode(expander, gameState);

            is = in.getInformationSet();
            is.setAlgorithmData(new OOSAlgorithmData(in.getActions()));
            InnerNode child = (InnerNode) in.getChildFor(in.getActions().get(0));
            child.getInformationSet().setAlgorithmData(new OOSAlgorithmData(child.getActions()));
            assert !is.getAllNodes().isEmpty();
        }
        assert is.getAllNodes().size() == 1;
        Map<Action, Double> distribution;

        //move to the right subtree
        rootNode = is.getAllNodes().iterator().next();
        rootNode.setParent(null);
        Action action = runMiliseconds(miliseconds);
        System.out.println("Mean leaf depth: " + StrategyCollector.meanLeafDepth(rootNode));
        if (gameState.getPlayerToMove().equals(searchingPlayer)) {
            System.out.println("OOS: " + (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData()));
            clean(action);
            return action;
        } else {
            InnerNode child = (InnerNode) rootNode.getChildFor(rootNode.getActions().get(0));
            is = child.getInformationSet();
            distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());
            System.out.println("OOS: " + distribution);
            action = Strategy.selectAction(distribution, rnd);
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

    private void cleanUnnecessaryPartsOfTree(Action action) {
        if (rootNode.getChildren() != null)
            for (Node node : rootNode.getChildren().values()) {
                node.setParent(null);
                if (node instanceof InnerNode) {

                    if(!((InnerNode)node).getLastAction().equals(action)) {
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
}
