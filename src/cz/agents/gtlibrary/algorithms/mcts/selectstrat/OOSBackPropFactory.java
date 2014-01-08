/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.ArrayDeque;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class OOSBackPropFactory implements BackPropFactory  {
    InnerNode root;
    double gamma = 0.05;
    Random random = new HighQualityRandom(123452);
    /** Each player's contribution to the probability of being in the current IS. */
    public double pi[];
    public ArrayDeque<Double> pis[] = new ArrayDeque[]{new ArrayDeque(),  new ArrayDeque()};
    public double s=1.0;
    public double x,l;


    public OOSBackPropFactory(double gamma) {
        this.gamma = gamma;
        pi = new double[getRealPlayersNum()];
        for (int i=0; i<getRealPlayersNum(); i++) pi[i]=1.0;
    }

    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        if (root==null) root = infSet.getAllNodes().iterator().next();
        if (infSet.getPlayer().getId() > 1) return null;
        return new OOSSelector(this, infSet);
    }

    @Override
    public SelectionStrategy createForNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private int getRealPlayersNum(){
        return 2;
    }
    
    public void putPIs(){
        pis[0].add(pi[0]);
        pis[1].add(pi[1]);
    }
    
    public void popPIs(){
        pi[0] = pis[0].removeLast();
        pi[1] = pis[1].removeLast();
    }
}
