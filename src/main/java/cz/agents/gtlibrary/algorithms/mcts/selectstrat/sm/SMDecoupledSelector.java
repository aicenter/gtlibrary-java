/*
Copyright 2014 Faculty of Electrical Engineering at CTU in Prague

This file is part of Game Theoretic Library.

Game Theoretic Library is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Game Theoretic Library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with Game Theoretic Library.  If not, see <http://www.gnu.org/licenses/>.*/


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
public class SMDecoupledSelector implements SMSelector, MeanStrategyProvider {
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
    public List<Action> getActions() {
        return actions1;
    }
    
    @Override
    public double[] getMp() {
        return ((MeanStrategyProvider)p1selector).getMp();
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
