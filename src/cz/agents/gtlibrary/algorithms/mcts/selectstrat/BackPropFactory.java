/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.interfaces.Action;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * @author Vilo
 */
public interface BackPropFactory extends Serializable {
    Selector createSelector(List<Action> actions);
    Selector createSelector(int N);
    public Random getRandom();
}
