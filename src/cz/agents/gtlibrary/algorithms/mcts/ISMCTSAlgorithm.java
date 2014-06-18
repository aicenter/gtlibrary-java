/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStratDist;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanValueProvider;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.strategy.Strategy;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;


/**
 * @author vilo
 */
public class ISMCTSAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected Simulator simulator;
    protected BackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;

    private InnerNode[] curISArray;
    private HighQualityRandom rnd = new HighQualityRandom();

    public boolean returnMeanValue = false;

    public ISMCTSAlgorithm(Player searchingPlayer, Simulator simulator, BackPropFactory fact, GameState rootState, Expander expander) {
        this.searchingPlayer = searchingPlayer;
        this.simulator = simulator;
        this.fact = fact;
        if (rootState.isPlayerToMoveNature()) this.rootNode = new ChanceNode(expander, rootState);
        else this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
        curISArray = new InnerNode[]{rootNode};
        rootNode.getInformationSet().setAlgorithmData(fact.createSelector(rootNode.getActions()));
    }

    @Override
    public Action runMiliseconds(int miliseconds) {
        int iters = 0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (; (threadBean.getCurrentThreadCpuTime() - start) / 1e6 < miliseconds; ) {
            InnerNode n = curISArray[rnd.nextInt(curISArray.length)];
            iteration(n);
            iters++;
        }
        System.out.println();
        System.out.println("ISMCTS Iters: " + iters);
        if (curISArray[0].getGameState().isPlayerToMoveNature()) return null;
        MCTSInformationSet is = curISArray[0].getInformationSet();
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }

    public Action runIterations(int iterations) {
        for (int i = 0; i < iterations; i++) {
            InnerNode n = curISArray[rnd.nextInt(curISArray.length)];
            iteration(n);
        }
        if (curISArray[0].getGameState().isPlayerToMoveNature()) return null;
        MCTSInformationSet is = curISArray[0].getInformationSet();
        Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
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

    @Override
    public void setCurrentIS(InformationSet curIS) {
        MCTSInformationSet currentIS = (MCTSInformationSet) curIS;
        curISArray = currentIS.getAllNodes().toArray(new InnerNode[currentIS.getAllNodes().size()]);
        rootNode = curISArray[0];
        for (InnerNode n : curISArray) n.setParent(null);
    }

    public InnerNode getRootNode() {
        return rootNode;
    }

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        MCTSInformationSet is = rootNode.getAlgConfig().getInformationSetFor(gameState);

        if (is.getAllNodes().isEmpty()) {
            InnerNode in = curISArray[0];
            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[0]).getLast());
            in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[1]).getLast());
            if (in.getGameState().isPlayerToMoveNature()) {
                in = (InnerNode) in.getChildFor(gameState.getSequenceFor(gameState.getAllPlayers()[2]).getLast());
            }
            is = rootNode.getAlgConfig().getInformationSetFor(gameState);
            is.setAlgorithmData(fact.createSelector(in.getActions()));
        }
        setCurrentIS(is);
        Action action = runMiliseconds(miliseconds);

        if (gameState.getPlayerToMove().equals(searchingPlayer)) {
            clean(action);
            return action;
        } else {
            InnerNode child = (InnerNode) curISArray[0].getChildren().values().iterator().next();
            is = child.getInformationSet();
            Map<Action, Double> distribution = (new MeanStratDist()).getDistributionFor(is.getAlgorithmData());
            action = Strategy.selectAction(distribution, rnd);
            clean(action);
            return action;
        }
    }

    private void clean(Action action) {
//        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
//        long oldUsedMemory = memoryBean.getHeapMemoryUsage().getUsed();
//        int isCount = rootNode.getAlgConfig().getAllInformationSets().size();
//        long finPending = memoryBean.getObjectPendingFinalizationCount();

        cleanUnnecessaryPartsOfTree(rootNode, action);
//        System.err.println("pending change before gc: " + (finPending - memoryBean.getObjectPendingFinalizationCount()));
//        System.gc();
//        System.err.println("pending change after gc: " + (finPending - memoryBean.getObjectPendingFinalizationCount()));
//        System.err.println("saved memory: " + (oldUsedMemory - memoryBean.getHeapMemoryUsage().getUsed())/1e6);
//        System.err.println("total memory: " + memoryBean.getHeapMemoryUsage().getUsed()/1e6);
//        System.err.println("deleted IS: " + (isCount - rootNode.getAlgConfig().getAllInformationSets().size()));

    }

    private void cleanUnnecessaryPartsOfTree(InnerNode rootNode, Action action) {
        for (InnerNode innerNode : curISArray) {
            innerNode.setParent(null);
            innerNode.setLastAction(null);
        }
        rootNode.setParent(null);
        rootNode.setLastAction(null);
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
    }
}
