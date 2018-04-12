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


package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanStrategyProvider;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 *
 * @author vilo
 */
public class ASelector implements Selector, MeanStrategyProvider {
    Selector coreSelector;
    private BasicStats[] stats;
    
    public ASelector(Selector coreSelector, int N) {
        this.coreSelector = coreSelector;
        stats = new BasicStats[N];
        for (int i = 0; i < N; i++) {
            stats[i] = new BasicStats();
        }
    }


    @Override
    public void update(int ai, double value) {
        stats[ai].onBackPropagate(value);
        coreSelector.update(ai, stats[ai].getEV());
    }    

    @Override
    public int select() {
        return coreSelector.select();
    }

    @Override
    public List<Action> getActions() {
        return ((MeanStrategyProvider)coreSelector).getActions();
    }

    @Override
    public double[] getMp() {
        return ((MeanStrategyProvider)coreSelector).getMp();
    }
    
    
}
