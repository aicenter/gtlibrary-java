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
public class DefaultBackPropFactory implements BackPropFactory {

    @Override
    public BPStrategy createForIS(InformationSet infSet) {
        return new BPStrategy();
    }

    @Override
    public BPStrategy createForISAction(InformationSet infSet, Action a) {
        return new BPStrategy();
    }

    @Override
    public BPStrategy createForNode(Node node) {
        return new BPStrategy();
    }

    @Override
    public BPStrategy createForNodeAction(Node node, Action action) {
        return new BPStrategy();
    }

}
