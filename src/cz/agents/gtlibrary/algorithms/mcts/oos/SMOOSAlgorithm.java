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
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;


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
    public boolean useMatchProbs = false;

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
        s = System.getProperty("INMATCHPROBS");
        if (s != null) useMatchProbs = Boolean.getBoolean(s);
    }

    double match_p1=1;
    double match_p2=1;
    double match_pc=1;
    
    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            iteration(rootNode, match_p1, match_p2, match_p1*match_p2*match_pc,rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            iteration(rootNode, match_p1, match_p2, match_p1*match_p2*match_pc,rootNode.getGameState().getAllPlayers()[1]);
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
            iteration(rootNode, 1, 1, 1, rootNode.getGameState().getAllPlayers()[0]);
            iteration(rootNode, 1, 1, 1, rootNode.getGameState().getAllPlayers()[1]);
        }
        if (!rootNode.getInformationSet().getPlayer().equals(searchingPlayer)) return null;
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }


    //additional iteration return values
    private double x = -1;
    private double l = -1;

    /**
     * The main function for OOS iteration.
     *
     * @param node      current node
     * @param pi        probability with which the searching player wants to reach the current node
     * @param pi_       probability with which the opponent of the searching player and chance want to reach the current node
     * @param us        probability that the unbiased sample reaches this node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, double pi, double pi_, double us, Player expPlayer) {
        if (node instanceof LeafNode) {
            x = 1;
            l = us;
            return ((LeafNode) node).getUtilities()[expPlayer.getId()];
        }
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode) node;
            Action a = cn.getRandomAction();
            double p = cn.getGameState().getProbabilityOfNatureFor(a);
            double u = iteration(cn.getChildFor(a), pi, p * pi_, p * us, expPlayer);
            x *= p;
            return u;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data;
        Action selectedA;
        double u = 0;
        int ai = -1;
        double pai = -1;
        if (is.getAlgorithmData() == null) {//this is a new Information Set
            data = new OOSAlgorithmData(in.getActions());
            is.setAlgorithmData(data);
            ai = rnd.nextInt(in.getActions().size());
            pai = 1.0 / in.getActions().size();
            selectedA = in.getActions().get(ai);
            Node child = in.getChildFor(selectedA);
            u = simulator.simulate(child.getGameState())[expPlayer.getId()];
            x = simulator.playersProb * (1.0 / in.getActions().size());
            l = us * simulator.playOutProb * (1.0 / in.getActions().size());
        } else {
            data = (OOSAlgorithmData) is.getAlgorithmData();
            data.getRMStrategy(rmProbs);

            if (is.getPlayer().equals(expPlayer)) {
                if (rnd.nextDouble() < epsilon) ai = rnd.nextInt(in.getActions().size());
                else ai = randomChoice(rmProbs, 1);
                pai = rmProbs[ai];
                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi * pai, pi_, us * ((1 - epsilon) * pai + (epsilon / in.getActions().size())), expPlayer);
            } else {
                ai = randomChoice(rmProbs, 1);
                pai = rmProbs[ai];
                u = iteration(in.getChildFor(in.getActions().get(ai)),
                        pi, pi_ * pai, us * pai, expPlayer);
            }
        }

        //regret/mean strategy update
        double s = us;
        double c = x;
        x *= pai;

        if (is.getPlayer().equals(expPlayer)) {
            data.updateRegret(ai, u * pi_ / l, c, x);
        } else {
            data.getRMStrategy(rmProbs);
            data.updateMeanStrategy(rmProbs, pi_ / s);
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
            assert !is.getAllNodes().isEmpty();
        }
        assert is.getAllNodes().size() == 1;
        Map<Action, Double> distribution;
        //update the match position probabilities
        if (useMatchProbs && gameState.getSequenceFor(gameState.getAllPlayers()[0]).size()>0){
            Action p1action = gameState.getSequenceFor(gameState.getAllPlayers()[0]).getLast();
            Action p2action = gameState.getSequenceFor(gameState.getAllPlayers()[1]).getLast();
            match_p1 *= Math.max(0.01,lastP1dist.get(p1action));
            match_p2 *= Math.max(0.01,lastP2dist.get(p2action));
        }
        //move to the right subtree
        rootNode = is.getAllNodes().iterator().next();
        rootNode.setParent(null);
        Action action = runMiliseconds(miliseconds);
        if (useMatchProbs){
            lastP1dist = (new MeanStratDist()).getDistributionFor(rootNode.getInformationSet().getAlgorithmData());
            lastP2dist = (new MeanStratDist()).getDistributionFor(((InnerNode)rootNode.getChildFor(rootNode.getActions().get(0))).getInformationSet().getAlgorithmData());
        }
        System.out.println("Mean leaf depth: " + StrategyCollector.meanLeafDepth(rootNode));
        if (gameState.getPlayerToMove().equals(searchingPlayer)) {
            clean(action);
            return action;
        } else {
            InnerNode child = (InnerNode) rootNode.getChildFor(rootNode.getActions().get(0));
            is = child.getInformationSet();
            distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());
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
