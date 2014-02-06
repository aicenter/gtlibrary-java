/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import java.io.Serializable;

/**
 *
 * @author Vilo
 */
public interface BackPropFactory extends Serializable {
        SelectionStrategy createForIS(MCTSInformationSet infSet);
        SelectionStrategy createForNode(Node node);
}
