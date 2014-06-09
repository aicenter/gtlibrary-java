/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.interfaces;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;

/**
 *
 * @author vilo
 */
public interface GamePlayingAlgorithm {
    public Action runMiliseconds(int miliseconds, GameState gameState);
    public Action runMiliseconds(int miliseconds);
    public void setCurrentIS(InformationSet currentIS);
    public InnerNode getRootNode();
}
