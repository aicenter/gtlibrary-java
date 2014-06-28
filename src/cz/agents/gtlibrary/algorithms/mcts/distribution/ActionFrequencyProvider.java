/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 *
 * @author vilo
 */
public interface ActionFrequencyProvider {
    public double[] getActionFreq();
    public List<Action> getActions();
}
