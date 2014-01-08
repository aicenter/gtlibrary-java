/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

/**
 *
 * @author vilo
 */
public interface Simulator {

    double[] simulate(GameState gameState, Expander<MCTSInformationSet> expander);
    
}
