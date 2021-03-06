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

import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.interfaces.Action;
import java.util.List;
import java.util.Random;

/**
 * @author vilo
 */
public class SMDecoupledFactory implements SMBackPropFactory {
    BackPropFactory fact;
    boolean useAverageForUpdates;

    public SMDecoupledFactory(BackPropFactory fact) {
        this(fact, false);
    }
    
    public SMDecoupledFactory(BackPropFactory fact, boolean useAverageForUpdates) {
        this.fact = fact;
        this.useAverageForUpdates = useAverageForUpdates;
    }

    public String getFactoryInfo(){
        return fact.getClass().getSimpleName();
    }

    @Override
    public SMSelector createSlector(List<Action> actions1, List<Action> actions2) {
        if (useAverageForUpdates)
            return new SMADecoupledSelector(actions1, actions2, fact.createSelector(actions1), fact.createSelector(actions2));
        else
            return new SMDecoupledSelector(actions1, actions2, fact.createSelector(actions1), fact.createSelector(actions2));
    }

    @Override
    public Random getRandom() {
        return fact.getRandom();
    }
}
