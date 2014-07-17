/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.algorithms.mcts.distribution.NbSamplesProvider;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.Selector;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.Pair;
import java.util.List;

/**
 *
 * @author vilo
 */
public class SMDecoupledSelector implements SMSelector {
    List<Action> actions1;
    List<Action> actions2;
    Selector p1selector;
    Selector p2selector;

    public SMDecoupledSelector(List<Action> actions1, List<Action> actions2, Selector p1selector, Selector p2selector) {
        this.actions1 = actions1;
        this.actions2 = actions2;
        this.p1selector = p1selector;
        this.p2selector = p2selector;
    }
    
    @Override
    public Pair<Integer, Integer> select() {
        return new Pair<>(p1selector.select(), p2selector.select());
    }

    @Override
    public void update(Pair<Integer, Integer> selection, double value) {
        p1selector.update(selection.getLeft(), value);
        p2selector.update(selection.getRight(), -value);
    }

    @Override
    public AlgorithmData getBottomData() {
        return new DummyBottom(this);
    }
    
    private class DummyBottom implements AlgorithmData, MeanStrategyProvider, NbSamplesProvider {
        SMDecoupledSelector top;

        public DummyBottom(SMDecoupledSelector top) {
            this.top = top;
        }

        @Override
        public List<Action> getActions() {
            return top.actions2;
        }

        @Override
        public double[] getMp() {
            return ((MeanStrategyProvider)top.p2selector).getMp();
        }

        @Override
        public int getNbSamples() {
            return ((NbSamplesProvider)top.p2selector).getNbSamples();
        }


    }
    
}
