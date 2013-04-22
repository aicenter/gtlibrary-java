/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class RMBackPropFactory implements BackPropFactory  {
    double gamma = 0.05;
    Random random = new Random();

    public RMBackPropFactory(double gamma) {
        this.gamma = gamma;
    }

    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        if (infSet.getPlayer().getId() > 1) return null;
        return new RMSelector(this, infSet);
    }

    @Override
    public SelectionStrategy createForNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
