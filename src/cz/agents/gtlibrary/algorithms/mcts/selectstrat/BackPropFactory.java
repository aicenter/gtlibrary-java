/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;

/**
 *
 * @author Vilo
 */
public interface BackPropFactory {
        SelectionStrategy createForIS(MCTSInformationSet infSet);
        SelectionStrategy createForNode(Node node);
}
