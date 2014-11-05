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
package cz.agents.gtlibrary.algorithms.cfr;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.ChanceNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
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
            iteration(rootNode,1,1,rootNode.getGameState().getAllPlayers()[0]);
            iters++;
            iteration(rootNode,1,1,rootNode.getGameState().getAllPlayers()[1]);
            iters++;
        }
        System.out.println();
        System.out.println("Iters: " + iters);
        return null;
    }

    public Action runIterations(int iterations){
        for (int i = 0; i < iterations; i++) {
            iteration(rootNode,1,1,rootNode.getGameState().getAllPlayers()[0]);
            iteration(rootNode,1,1,rootNode.getGameState().getAllPlayers()[1]);
        }
        return null;
    }
    
    /** 
     * The main function for CFR iteration. Implementation based on Algorithm 1 in M. Lanctot PhD thesis.
     * @param node current node
     * @param pi1 probability with which the opponent of the searching player and chance want to reach the current node
     * @param expPlayer the exploring player for this iteration
     * @return iteration game value is actually returned. Other return values are in global x and l
     */
    protected double iteration(Node node, double pi1, double pi2, Player expPlayer){
        if (pi1==0 && pi2==0) return 0;
        if (node instanceof LeafNode) {
            return ((LeafNode)node).getUtilities()[expPlayer.getId()];
        } 
        if (node instanceof ChanceNode) {
            ChanceNode cn = (ChanceNode)node;
            double ev=0;
            for (Action ai : cn.getActions()){
                final double p = cn.getGameState().getProbabilityOfNatureFor(ai);
                double new_p1 = expPlayer.getId()==1 ? pi1 * p : pi1;
                double new_p2 = expPlayer.getId()==0 ? pi2 * p : pi2;
                ev += p*iteration(cn.getChildFor(ai), new_p1, new_p2, expPlayer);
            }
            return ev;
        }
        InnerNode in = (InnerNode) node;
        MCTSInformationSet is = in.getInformationSet();
        OOSAlgorithmData data = (OOSAlgorithmData) is.getAlgorithmData();

        double[] rmProbs = data.getRMStrategy();
        double[] tmpV = new double[rmProbs.length];
        double ev=0;
        
        int i=-1;
        for (Action ai : in.getActions()){
            i++;
            if (is.getPlayer().getId()==0){
                tmpV[i]=iteration(in.getChildFor(ai), pi1 * rmProbs[i], pi2, expPlayer);
            }  else {
                tmpV[i]=iteration(in.getChildFor(ai), pi1, rmProbs[i]*pi2, expPlayer);
            }
            ev += rmProbs[i]*tmpV[i];
        }
        if (is.getPlayer().equals(expPlayer)){
            data.updateAllRegrets(tmpV, ev, (expPlayer.getId()==0 ? pi2 : pi1));
            data.updateMeanStrategy(rmProbs, (expPlayer.getId()==0 ? pi1 : pi2));
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

    @Override
    public Action runMiliseconds(int miliseconds, GameState gameState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
