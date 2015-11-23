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

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.List;
import java.util.Random;

/**
 *
 * @author vilo
 */
public class UCTBackPropFactory implements BackPropFactory {
    public double C;
    public Random random;

    public UCTBackPropFactory(double C) {
        this(C, new HighQualityRandom());
    }
    
    public UCTBackPropFactory(double C, Random random) {
        this.C = C;
        this.random = random;
    }

    @Override
    public Selector createSelector(List<Action> actions) {
        return new UCTSelector(actions, this);
    }
    
    @Override
    public Selector createSelector(int N) {
        return new UCTSelector(N, this);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
