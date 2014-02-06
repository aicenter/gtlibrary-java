/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.nodes.oos;

import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.Node;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

/**
 *
 * @author vilo
 */
public class OOSInnerNode extends InnerNode {

    public OOSInnerNode(Expander<MCTSInformationSet> expander, MCTSConfig config, GameState gameState) {
        super(expander, config, gameState);
    }

    public OOSInnerNode(InnerNode parent, GameState gameState, Action lastAction) {
        super(parent, gameState, lastAction);
    }
    
    @Override
    public Node getNewChildAfter(Action action) {
            GameState nextState = gameState.performAction(action);

            if (nextState.isGameEnd()) {
                    return new OOSLeafNode(this, nextState, action);
            }
            if (nextState.isPlayerToMoveNature()) {
                    return new OOSChanceNode(this, nextState, action);
            }
            return new OOSInnerNode(this, nextState, action);
    }
}
