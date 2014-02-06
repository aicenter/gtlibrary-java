/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import java.util.ArrayDeque;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class SMOOSBackPropFactory implements BackPropFactory  {
    InnerNode root;
    double gamma = 0.05;
    Random random = new Random();
    /** Each player's contribution to the probability of being in current IS. */
    public double pi = 1.0;
    public ArrayDeque<Double> pis = new ArrayDeque<Double>();


    public SMOOSBackPropFactory(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        if (root==null) root = infSet.getAllNodes().iterator().next();
        if (infSet.getPlayer().getId() > 1) return null;
        return new SMOOSSelector(this, infSet);
    }

    @Override
    public SelectionStrategy createForNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
