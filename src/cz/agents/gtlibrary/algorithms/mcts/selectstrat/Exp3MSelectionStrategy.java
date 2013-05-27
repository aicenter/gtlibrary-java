/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author vilo
 */
public class Exp3MSelectionStrategy extends Exp3SelectionStrategy {
    static Action lastP1Action = null;
    public Exp3MSelectionStrategy(Exp3BackPropFactory fact, MCTSInformationSet infSet){
        super(fact, infSet);
    }

    @Override
    public double onBackPropagate(InnerNode node, Action action, double value) {
        double newValue;
        Node child = node.getChildOrNull(action);
        if (child == null) return value;
        
        if (infSet.getAllNodes().size() > 1){
            lastP1Action = action;
        } else {
            if (lastP1Action == null) return value;
            child = ((InnerNode)child).getChildOrNull(lastP1Action);
            if (child == null) return value;
            lastP1Action = null;
        }
        newValue = child.getEV()[infSet.getPlayer().getId()];
        
        super.onBackPropagate(node, action, newValue);
        return value;
    }
}
