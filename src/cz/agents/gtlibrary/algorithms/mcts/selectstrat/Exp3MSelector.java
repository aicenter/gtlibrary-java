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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.gtlibrary.algorithms.mcts.selectstrat;

import cz.agents.gtlibrary.algorithms.mcts.distribution.MeanValueProvider;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;

/**
 *
 * @author vilo
 */
public class Exp3MSelector extends Exp3Selector implements MeanValueProvider {
    private BasicStats stats = new BasicStats();
    
    public Exp3MSelector(int N, Exp3BackPropFactory fact) {
        super(N, fact);
    }

    public Exp3MSelector(List<Action> actions, Exp3BackPropFactory fact) {
        super(actions, fact);
    }

    @Override
    public void update(int ai, double value) {
        super.update(ai, value);
        stats.onBackPropagate(value);
    }

    @Override
    public double getMeanValue() {
        return stats.getEV();
    }
    
    
    
}
