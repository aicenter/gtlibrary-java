/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMBackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm.SMSelector;
import cz.agents.gtlibrary.interfaces.*;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import cz.agents.gtlibrary.utils.Pair;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

    
/**
 *
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
    public Action runMiliseconds(int miliseconds){
        int iters=0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (;(threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds;) {
            iteration(rootNode);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
        //TODO finish implementation of the online case
//        if (curISArray[0].getGameState().isPlayerToMoveNature()) return null;
//        MCTSInformationSet is = curISArray[0].getInformationSet();
//        Map<Action, Double> distribution = (new MostFrequentAction()).getDistributionFor(is.getAlgorithmData());
//        return Strategy.selectAction(distribution, rnd);
    }
    
    public Action runIterations(int iterations){
        for (int i=0;i<iterations;i++) {
            iteration(rootNode);
        }
        return null;
        //TODO: as above
    }
    
    protected double iteration(Node node){
        if (node instanceof LeafNode) return ((LeafNode)node).getUtilities()[searchingPlayer.getId()];
        else {
            InnerNode n = (InnerNode) node;
            double retValue=0;
            Pair<Integer,Integer> selActionIdxs;
            Action selAction = null;
            SMSelector selector = null;
            
            if (node instanceof ChanceNode){
                selAction=((ChanceNode)node).getRandomAction();
                Node child = n.getChildFor(selAction);
                return iteration(child);
            }
            
            assert n.getInformationSet().getAllNodes().size()==1;
            selector = (SMSelector) n.getInformationSet().getAlgorithmData();
            if (selector != null){
                selActionIdxs = selector.select();
                selAction = n.getActions().get(selActionIdxs.getLeft());
                InnerNode bottom = (InnerNode) n.getChildOrNull(selAction);
                Node child = bottom.getChildFor(bottom.getActions().get(selActionIdxs.getRight()));
                retValue = iteration(child);
            } else {
                expandNode(n);
                selector = (SMSelector) n.getInformationSet().getAlgorithmData();
                selActionIdxs = selector.select();
                selAction = n.getActions().get(selActionIdxs.getLeft());
                InnerNode bottom = (InnerNode) n.getChildOrNull(selAction);
                Node child = bottom.getChildFor(bottom.getActions().get(selActionIdxs.getRight()));
                retValue = simulator.simulate(child.getGameState())[searchingPlayer.getId()];
            }
            selector.update(selActionIdxs, retValue);
            return retValue;
        }
    }
    
    private void expandNode(InnerNode n){
        InnerNode bottom=null;
        for (Action a : n.getActions()){
            bottom = (InnerNode) n.getChildFor(a);
            for (Action b : bottom.getActions()){
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
        assert curIS.getAllStates().size()==1;//only the top node of the simultaneous move can be given here
        MCTSInformationSet currentIS = (MCTSInformationSet) curIS;
        rootNode = currentIS.getAllNodes().iterator().next();
    }
    
    @Override
    public InnerNode getRootNode() {
        return rootNode;
    }
}
