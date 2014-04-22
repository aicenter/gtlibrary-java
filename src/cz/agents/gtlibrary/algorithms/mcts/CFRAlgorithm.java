/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.CFRAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.interfaces.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

    
/**
 *
 * @author vilo
 */
public class CFRAlgorithm implements GamePlayingAlgorithm {
    protected Player searchingPlayer;
    protected BackPropFactory fact;
    protected InnerNode rootNode;
    protected ThreadMXBean threadBean;

    
    public CFRAlgorithm(Player searchingPlayer, GameState rootState, Expander expander) {
        this.searchingPlayer = searchingPlayer;
        if (rootState.isPlayerToMoveNature()) this.rootNode = new ChanceNode(expander, rootState);
        else this.rootNode = new InnerNode(expander, rootState);
        threadBean = ManagementFactory.getThreadMXBean();
    }
    
    @Override
    public Action runMiliseconds(int miliseconds){
        int iters=0;
        long start = threadBean.getCurrentThreadCpuTime();
        for (;(threadBean.getCurrentThreadCpuTime()-start)/1e6 < miliseconds;) {
            iteration(rootNode,1,rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            iteration(rootNode,1,rootNode.getGameState().getAllPlayers()[1]);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }
    
    /** 
     * The main function for CFR iteration.
     * @param node current node
     * @param pi_ probability with which the opponent of the searching player and chance want to reach the current node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, double pi_, Player expPlayer){
        if (node instanceof LeafNode) {
            return ((LeafNode)node).getUtilities()[searchingPlayer.getId()];
        } 
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode)node;
            double ev=0;
            for (Action ai : cn.getActions()){
                final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
                ev += p*iteration(cn.getChildFor(ai), p*pi_, expPlayer);
            }
            return ev;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        CFRAlgorithmData data = (CFRAlgorithmData) is.getAlgorithmData();

        double[] rmProbs = data.getRMStrategy();
        
        double ev=0;
        if (is.getPlayer().equals(expPlayer)){
            double[] tmpV = new double[rmProbs.length];
            int i=-1;
            for (Action ai : in.getActions()){
                i++;
                tmpV[i]=(expPlayer.equals(searchingPlayer)?1:-1)*iteration(in.getChildFor(ai), pi_, expPlayer);
                ev += rmProbs[i]*tmpV[i];
                
            }
            data.updateAllRegrets(tmpV, ev);
            if (!expPlayer.equals(searchingPlayer)) ev*=-1;
            
        } else {
            int i=-1;
            for (Action ai : in.getActions()){
                i++;
                ev += rmProbs[i]*iteration(in.getChildFor(ai), rmProbs[i]*pi_, expPlayer);
            }
            data.updateMeanStrategy(rmProbs, 1);
        }
        return ev;
    }
    

    @Override
    public void setCurrentIS(InformationSet is){
        throw new NotImplementedException();
    }   
    
    public InnerNode getRootNode() {
        return rootNode;
    }
}
