/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.oos.OOSInnerNode;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

/**
 *
 * @author vilo
 */
public class OOSMCTSRunner extends MCTSRunner {

    public OOSMCTSRunner(MCTSConfig algConfig, GameState gameState, Expander<MCTSInformationSet> expander) {
        super(algConfig, gameState, expander);
    }
    
    @Override
    protected InnerNode createRootNode(GameState gameState, Expander<MCTSInformationSet> expander, MCTSConfig algConfig) {
            if (gameState.isPlayerToMoveNature()){
                    assert false;
                    return null;
                    //return new ChanceNode(gameState, expander, algConfig, opponentRealizationPlan, opponent, 1);
            }
            return new OOSInnerNode(expander, algConfig, gameState);
    }
}
