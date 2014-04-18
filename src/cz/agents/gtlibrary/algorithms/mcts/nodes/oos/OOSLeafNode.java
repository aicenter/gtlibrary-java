/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.nodes.oos;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.LeafNode;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.OOSBackPropFactory;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

/**
 *
 * @author vilo
 */
public class OOSLeafNode extends LeafNode {
    public static OOSBackPropFactory fact;

    public OOSLeafNode(InnerNode parent, GameState gameState, Action lastAction) {
        super(parent, gameState, lastAction);
    }

    @Override
    public double[] simulate() {
        fact.x = 1;
        fact.l = fact.s;
        return super.simulate();
    }
}
