/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;

/**
 *
 * @author vilo
 */
public class Exp3MRemBackPropFactory extends Exp3BackPropFactory {

    public Exp3MRemBackPropFactory(double minUtility, double maxUtility, double gamma) {
        super(minUtility, maxUtility, gamma);
    }
    
    @Override
    public SelectionStrategy createForIS(MCTSInformationSet infSet) {
        return new Exp3MRemSelector(this, infSet);
    }
}
