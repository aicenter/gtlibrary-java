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
package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.utils.HighQualityRandom;

import java.util.List;
import java.util.Random;

/**
 * @author vilo
 */
public class SMRMBackPropFactory implements SMBackPropFactory {
    Random random;
    double gamma = 0.2;
    private double minUtility = 0;
    private double maxUtility = 1;

    public SMRMBackPropFactory(double gamma) {
        this(gamma, new HighQualityRandom());
    }

    public SMRMBackPropFactory(double gamma, Random random) {
        this.gamma = gamma;
        this.random = random;
    }

    @Override
    public SMSelector createSlector(List<Action> actions1, List<Action> actions2) {
        return new SMRMSelector(this, actions1, actions2);
    }

    @Override
    public Random getRandom() {
        return random;
    }
}
