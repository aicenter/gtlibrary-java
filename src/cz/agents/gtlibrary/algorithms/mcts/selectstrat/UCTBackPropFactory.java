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
import cz.agents.gtlibrary.utils.HighQualityRandom;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class UCTBackPropFactory implements BackPropFactory {
    public double C;
    public Random rnd = new HighQualityRandom();
    
    public UCTBackPropFactory(double C) {
        this.C = C;
    }
    
    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        //return new UCTMAXSelectionStrategy(this, infSet);
        return new UCTSelectionStrategy(this, infSet);
        //return new ConfidenceMAXSelector(this, infSet);
    }

    @Override
    public SelectionStrategy createForNode(Node node) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
