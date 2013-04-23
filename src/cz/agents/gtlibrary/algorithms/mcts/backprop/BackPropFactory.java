/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.backprop;

import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 *
 * @author Vilo
 */
public interface BackPropFactory {
        BPStrategy createForIS(InformationSet infSet);
        BPStrategy createForISAction(InformationSet infSet, Action a);
        BPStrategy createForNode(Node node);
        BPStrategy createForNodeAction(Node node, Action action);
}
