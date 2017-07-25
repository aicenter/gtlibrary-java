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

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.algorithms.mcts.selectstrat.BackPropFactory;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.List;

/**
 * @author vilo
 */
public class SMConjectureFactory extends SMDecoupledFactory {

    public SMConjectureFactory(BackPropFactory fact) {
        super(fact);
    }

    public SMConjectureFactory(BackPropFactory fact, boolean useAverageForUpdates) {
        super(fact, useAverageForUpdates);
    }
    
    @Override
    public SMSelector createSlector(List<Action> actions1, List<Action> actions2) {
        if (((MCTSInformationSet)actions1.get(0).getInformationSet()).getPlayersHistory().size()==0)
            return new SMConjuctureSelector(super.createSlector(actions1, actions2), actions1.size(), actions2.size());
        else
            return super.createSlector(actions1, actions2);
    }
}
