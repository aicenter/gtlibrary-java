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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.undominated;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.LPTable;

public class UndominatedTable extends LPTable {


    public void addToObjective(Object varKey, double value) {
        Double oldValue = objective.get(varKey);

        if (oldValue == null)
            objective.put(varKey, value);
        else
            objective.put(varKey, oldValue + value);
        updateVariableIndices(varKey);
    }

    public void clearObjective() {
        objective.clear();
    }
}
