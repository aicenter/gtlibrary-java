/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.SelectionStrategy;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.UCTSelectionStrategy;

/**
 *
 * @author vilo
 */
public class UCTBackPropFactory implements BackPropFactory {
    public double C;
    
    public UCTBackPropFactory(double C) {
        this.C = C;
    }
    
    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        return new UCTSelectionStrategy(this, infSet);
    }

    @Override
    public SelectionStrategy createForNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
