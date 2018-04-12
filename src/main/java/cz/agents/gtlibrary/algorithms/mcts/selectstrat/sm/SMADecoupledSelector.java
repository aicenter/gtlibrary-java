/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BasicStats;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.Pair;
import java.util.List;

/**
 *
 * @author vilo
 */
public class SMADecoupledSelector extends SMDecoupledSelector {
    BasicStats[][] stats;
    
    public SMADecoupledSelector(List<Action> actions1, List<Action> actions2, Selector p1selector, Selector p2selector) {
        super(actions1, actions2, p1selector, p2selector);
        stats = new BasicStats[actions1.size()][actions2.size()];
        for (int i=0; i<actions1.size(); i++){
            for (int j=0; j<actions2.size(); j++){
                stats[i][j] = new BasicStats();
            }
        }
    }

    @Override
    public void update(Pair<Integer, Integer> selection, double value) {
        final BasicStats s = stats[selection.getLeft()][selection.getRight()];
        s.onBackPropagate(value);
        super.update(selection, s.getEV());
    }
}
