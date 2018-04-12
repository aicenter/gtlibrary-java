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


package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.Distribution;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanValueProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MostFrequentAction;
import cz.agents.gtlibrary.algorithms.mcts.distribution.StrategyCollector;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelector;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.commons.lang3.ArrayUtils;


/**
 * @author vilo
 */
public class ISMCTSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected Simulator simulator;
    protected BackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;
    protected MCTSConfig config;
    protected Expander<MCTSInformationSet> expander;

    private InnerNode[] curISArray;
    double[] belief = new double[]{1};
    public boolean useBelief = false;

    public boolean returnMeanValue = false;
    public boolean useUCTMax = true;
    
    public ISMCTSAlgorithm(Player searchingPlayer, Simulator simulator, BackPropFactory fact, GameState rootState, Expander expander) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.fact = fact;
        if (rootState.isPlayerToMoveNature())
            this.rootNode = new ChanceNode(expander, rootState, fact.getRandom());
        else
            this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
        curISArray = new InnerNode[]{rootNode};
        config = rootNode.getAlgConfig();
        this.expander = expander;
        if (!rootState.isPlayerToMoveNature())
            rootNode.getInformationSet().setAlgorithmData(fact.createSelector(rootNode.getActions()));
        
        String s = System.getProperty("USEBELIEF");
        if (s != null) useBelief = Boolean.parseBoolean(s);
        s = System.getProperty("P1USEBELIEF");
        if (s != null && searchingPlayer.getId()==0) useBelief = Boolean.parseBoolean(s);
        s = System.getProperty("P2USEBELIEF");
        if (s != null && searchingPlayer.getId()==1) useBelief = Boolean.parseBoolean(s);
    }

    private Distribution meanDist = new MeanStratDist();
    @Override
    public Action runMiliseconds(int miliseconds) {
        if (giveUp) return null;
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            final int rndNum = fact.getRandom().nextInt(curISArray.length);
            if (useBelief){
                for (int i=0; i<1+100*belief[rndNum]; i++){
                    InnerNode n = curISArray[rndNum];
                    iteration(n);
                    iters++;
                }
            } else {
                InnerNode n = curISArray[rndNum];
                iteration(n);
                iters++;
            }
        }
//        System.out.println();
        System.out.println("ISMCTS Iters: " + iters);
        System.out.println("Mean leaf depth: " + StrategyCollector.meanLeafDepth(rootNode));
//        System.out.println("CurIS size: " + curISArray.length);
        if (curISArray[0].getGameState().isPlayerToMoveNature()) return null;
        MCTSInformationSet is = curISArray[0].getInformationSet();
        Map<Action, Double> distribution = is.getAlgorithmData() instanceof UCTSelector && useUCTMax
                ? (new MostFrequentAction()).getDistributionFor(is.getAlgorithmData()) 
                : meanDist.getDistributionFor(is.getAlgorithmData());
//        System.out.println("Strategy: " + (new MeanStratDist()).getDistributionFor(is.getAlgorithmData()));
        return Strategy.selectAction(distribution, fact.getRandom());
    }

    public Action runIterations(int iterations) {
        assert useBelief==false;//not imlpemented
        for (int i = 0; i < iterations; i++) {
            InnerNode n = curISArray[fact.getRandom().nextInt(curISArray.length)];
            iteration(n);
        }
        if (curISArray[0].getGameState().isPlayerToMoveNature()) return null;
        MCTSInformationSet is = curISArray[0].getInformationSet();
        Map<Action, Double> distribution = meanDist.getDistributionFor(is.getAlgorithmData());
        return Strategy.selectAction(distribution, fact.getRandom());
    }

    protected double iteration(Node node) {
        if (node instanceof LeafNode) {
            return ((LeafNode) node).getUtilities()[searchingPlayer.getId()];
        } else {
            InnerNode n = (InnerNode) node;
            Action selAction = null;
            Selector selector = null;
            double retValue = 0;
            int sgn = (n.getGameState().getPlayerToMove().equals(searchingPlayer) ? 1 : -1);
            int selActionIdx = 0;

            if (node instanceof ChanceNode) {
                selAction = ((ChanceNode) node).getRandomAction();
                assert !returnMeanValue || ((ChanceNode) node).getActions().size() == 1;
            } else {
                List<Action> actions = n.getActions();

                selector = (Selector) n.getInformationSet().getAlgorithmData();
                selActionIdx = selector.select();
                selAction = actions.get(selActionIdx);
            }
            Node child = n.getChildOrNull(selAction);

            if (child != null) {
                retValue = iteration(child);
            } else {
                child = n.getChildFor(selAction);
                if (child instanceof InnerNode) {
                    InnerNode iChild = (InnerNode) child;

                    if (!iChild.getGameState().isPlayerToMoveNature())
                        iChild.getInformationSet().setAlgorithmData(fact.createSelector(iChild.getActions()));
                }
                retValue = simulator.simulate(child.getGameState())[searchingPlayer.getId()];
            }
            if (selector != null)
                selector.update(selActionIdx, sgn * retValue);
            if (returnMeanValue && selector != null && n.getInformationSet().getAllNodes().size() == 1)
                return sgn * ((MeanValueProvider) selector).getMeanValue();
            else return retValue;
        }
    }

    private boolean giveUp=false;
    @Override
    public void setCurrentIS(InformationSet newCurIS) {
        MCTSInformationSet newCurrentIS = (MCTSInformationSet) newCurIS;
        if (newCurrentIS.getAllNodes().isEmpty()){
            giveUp=true;
            return;
        }

        if (useBelief){
            System.out.println("Belief based tree progress.");
            InnerNode[] oldArray = curISArray;
            double[] oldBelief = belief;
            curISArray = new InnerNode[newCurrentIS.getAllNodes().size()];
            belief = new double[curISArray.length];
            int next=0;
            for (int i=0; i<oldArray.length; i++){
                next = fillBelief(oldArray[i], newCurrentIS, oldBelief[i], next);
            }
            assert next==belief.length;
            //normalize belief
            double sum=0;
            for (double d : belief) sum +=d;
            for (int i=0;i<belief.length;i++) belief[i] /= sum;
        } else {
            curISArray = newCurrentIS.getAllNodes().toArray(new InnerNode[newCurrentIS.getAllNodes().size()]);
        }
        
        for (InnerNode n : curISArray)
            n.setParent(null);
        rootNode = curISArray[0];
    }
    
    private int fillBelief(InnerNode n, MCTSInformationSet curIS, double p, int nextPos){
        if (curIS.equals(n.getInformationSet())) {
            curISArray[nextPos]=n;
            belief[nextPos]=p;
            return nextPos+1;
        }
        if (n.getGameState().getPlayerToMove()==curIS.getPlayer()){//searching player's node
            if (n.getInformationSet().getPlayersHistory().size() != curIS.getPlayersHistory().size()-1) return nextPos; //this is after opponent's move out of current IS
            Node child = n.getChildOrNull(curIS.getPlayersHistory().getLast());
            if (child == null) return nextPos;//running out of the tree in memory, alternativly, we could add the nodes to the tree
            if (child instanceof LeafNode) return nextPos;//if based on unknown information the last players action leads to a leaf
            return fillBelief((InnerNode) child, curIS, p, nextPos);
        } else if (n.getGameState().isPlayerToMoveNature()){
            int i=nextPos;
            for (Map.Entry<Action, Node> en : n.getChildren().entrySet()){
                if (en.getValue() instanceof InnerNode)
                    i = fillBelief((InnerNode)en.getValue(), curIS, p*n.getGameState().getProbabilityOfNatureFor(en.getKey()), i);
            }
            return i;
        } else {//opponent's move
            Map<Action, Double> distribution = meanDist.getDistributionFor(n.getInformationSet().getAlgorithmData());
            int i=nextPos;
            for (Map.Entry<Action, Double> en : distribution.entrySet()){
                Node child = n.getChildOrNull(en.getKey());
                if (child != null && child instanceof InnerNode)
                    i = fillBelief((InnerNode)child, curIS, p*en.getValue(), i);
            }
            return i;
        }
    }

    public InnerNode getRootNode() {
        return rootNode;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        MCTSInformationSet is = config.getInformationSetFor(gameState);

        if (is.getAllNodes().isEmpty()) {
//            InnerNode in = curISArray[0];
//            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[0]).getLast());
//            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[1]).getLast());
//            if (in.getGameState().isPlayerToMoveNature()) {
//                in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[2]).getLast());
//            }
//            is = config.getInformationSetFor(gameState);
            InnerNode in = new InnerNode(expander, gameState);

            is = in.getInformationSet();
            is.setAlgorithmData(fact.createSelector(expander.getActions(gameState)));
            assert !is.getAllNodes().isEmpty();
        }
        setCurrentIS(is);
        Action action = runMiliseconds(miliseconds);
        System.out.println("Mean ISMCTS leaf depth: " + StrategyCollector.meanLeafDepth(rootNode));
        System.out.println("Mean ISMCTS support size: " + StrategyCollector.meanSupportSize(StrategyCollector.getStrategyFor(rootNode, searchingPlayer, new MeanStratDist())));
        
        System.out.println("ISMCTS p1: " + (new MeanStratDist()).getDistributionFor(is.getAlgorithmData()));
        System.out.println("ISMCTS p2: " + (new MeanStratDist()).getDistributionFor(((InnerNode) (rootNode.getChildren().values().iterator().next())).getInformationSet().getAlgorithmData()));
        
        if (gameState.getPlayerToMove().equals(searchingPlayer)) {
            clean(action);
            return action;
        } else {
            InnerNode child = (InnerNode) curISArray[0].getChildren().values().iterator().next();
            is = child.getInformationSet();
            Map<Action, Double> distribution = is.getAlgorithmData() instanceof UCTSelector && useUCTMax
                ? (new MostFrequentAction()).getDistributionFor(is.getAlgorithmData()) 
                : meanDist.getDistributionFor(is.getAlgorithmData());
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
        for (InnerNode innerNode : curISArray) {
            for (Node node : innerNode.getChildren().values()) {
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
            innerNode.setParent(null);
            innerNode.setLastAction(null);
            innerNode.setChildren(null);

            innerNode.setActions(null);
            innerNode.getInformationSet().setAlgorithmData(null);
            innerNode.setInformationSet(null);
            innerNode.setAlgorithmData(null);
        }
        curISArray = null;
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
}
