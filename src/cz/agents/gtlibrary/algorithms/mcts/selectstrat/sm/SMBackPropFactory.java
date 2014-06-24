/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.interfaces.Action;

import java.util.List;
import java.util.Random;

/**
 * @author vilo
 */
public interface SMBackPropFactory {
    SMSelector createSlector(List<Action> actions1, List<Action> actions2);
    public Random getRandom();
}
