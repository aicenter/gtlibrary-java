/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.oos;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.InformationSet;

/**
 *
 * @author vilo
 */
public interface OOSTargeting {
    public boolean isAllowedAction(InnerNode node, Action action);
    public void update(InformationSet curIS);
    public double getSampleProbMultiplayer();
}
