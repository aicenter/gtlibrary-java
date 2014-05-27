/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanValueProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MostFrequentAction;
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
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;

    
/**
 *
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
    public Action runMiliseconds(int miliseconds){
        int iters=0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (;(threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds;) {
            InnerNode n = curISArray[rnd.nextInt(curISArray.length)];
            iteration(n);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        if (curISArray[0].getGameState().isPlayerToMoveNature()) return null;
        MCTSInformationSet is = curISArray[0].getInformationSet();
        Map<Action, Double> distribution = (new MostFrequentAction()).getDistributionFor(is.getAlgorithmData());
        return Strategy.selectAction(distribution, rnd);
    }
    
    public Action runIterations(int iterations){
        for (int i=0;i<iterations;i++) {
            InnerNode n = curISArray[rnd.nextInt(curISArray.length)];
            iteration(n);
        }
        if (curISArray[0].getGameState().isPlayerToMoveNature()) return null;
        MCTSInformationSet is = curISArray[0].getInformationSet();
        //Map<Action, Double> distribution = (new MostFrequentAction()).getDistributionFor(is.getAlgorithmData());
        //return Strategy.selectAction(distribution, rnd);
        return null;
    }
    
    protected double iteration(Node node){
        if (node instanceof LeafNode) return ((LeafNode)node).getUtilities()[searchingPlayer.getId()];
        else {
            InnerNode n = (InnerNode) node;
            int sgn = (n.getGameState().getPlayerToMove().equals(searchingPlayer) ? 1 : -1);
            double retValue=0;
            int selActionIdx=0;
            Action selAction = null;
            Selector selector = null;
            
            if (node instanceof ChanceNode){
                selAction=((ChanceNode)node).getRandomAction();
                assert !returnMeanValue || ((ChanceNode)node).getActions().size()==1;
            } else {
                List<Action> actions = n.getActions();
                selector = (Selector) n.getInformationSet().getAlgorithmData();
                selActionIdx = selector.select();
                selAction = actions.get(selActionIdx);
            }
            
            Node child = n.getChildOrNull(selAction);
            if (child != null){
                retValue = iteration(child);
            } else {
                child = n.getChildFor(selAction);
                if (child instanceof InnerNode) {
                    InnerNode iChild = (InnerNode)child;
                    if (!iChild.getGameState().isPlayerToMoveNature())
                        iChild.getInformationSet().setAlgorithmData(fact.createSelector(iChild.getActions()));
                }
                retValue = simulator.simulate(n.getGameState())[searchingPlayer.getId()];
            }
            if (selector != null) selector.update(selActionIdx, sgn*retValue);
            if (returnMeanValue && selector != null && n.getInformationSet().getAllNodes().size()==1) return sgn*((MeanValueProvider)selector).getMeanValue();
            else return retValue;
        }
    }
    
    @Override
    public void setCurrentIS(InformationSet curIS) {
        MCTSInformationSet currentIS = (MCTSInformationSet) curIS;
        curISArray = currentIS.getAllNodes().toArray(new InnerNode[currentIS.getAllNodes().size()]);
    }
    
    public InnerNode getRootNode() {
        return rootNode;
    }
}
