/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.utils.Pair;

import java.io.Serializable;

/**
 * @author vilo
 */
public interface SMSelector extends Serializable, AlgorithmData {

    /**
     * Returns selected action index.
     */
    public Pair<Integer, Integer> select();

    /**
     * Updates the selector with action result
     */
    public void update(Pair<Integer, Integer> selection, double value);

    /**
     * Returns Algorithm for the second player information set in order to extract strategies.
     */
    public AlgorithmData getBottomData();
}
