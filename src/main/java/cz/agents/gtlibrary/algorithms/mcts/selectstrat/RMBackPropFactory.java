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
public class RMBackPropFactory implements BackPropFactory  {
    double gamma = 0.2;
    Random random = new HighQualityRandom();

    private double minUtility=-1;
    private double maxUtility=1;

    public RMBackPropFactory(double minUtility, double maxUtility, double gamma) {
        this(gamma);
        this.minUtility = minUtility;
        this.maxUtility = maxUtility;
    }
    
    public RMBackPropFactory(double minUtility, double maxUtility, double gamma, Random random) {
        this(minUtility,maxUtility,gamma);
        this.random = random;
    }
    
    public RMBackPropFactory(double gamma) {
        this.gamma = gamma;
    }
    
    public double normalizeValue(double value) {       
        assert minUtility <= value + 1e-5 && value <= maxUtility + 1e-5;
        return (value - minUtility) / (maxUtility - minUtility);
//        assert minUtility == 0 && maxUtility == 1;
//        return reward;
    }
    
    @Override
    public Selector createSelector(List<Action> actions) {
        return new RMSelector(actions, this);
    }

    @Override
    public Selector createSelector(int N) {
        return new RMSelector(N, this);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
