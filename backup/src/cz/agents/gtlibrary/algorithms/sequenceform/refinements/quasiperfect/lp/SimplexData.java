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


package cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.lp;

import cz.agents.gtlibrary.algorithms.sequenceform.refinements.quasiperfect.numbers.EpsilonReal;

import java.util.Map;

public class SimplexData {
    private LPDictionary<EpsilonReal> simplex;
    private Map<Object, Integer> watchedPrimalVars;
    private Map<Object, Integer> watchedDualVars;

    public SimplexData(LPDictionary<EpsilonReal> simplex, Map<Object, Integer> watchedPrimalVars, Map<Object, Integer> watchedDualVars) {
        this.simplex = simplex;
        this.watchedPrimalVars = watchedPrimalVars;
        this.watchedDualVars = watchedDualVars;
    }

    public LPDictionary<EpsilonReal> getSimplex() {
        return simplex;
    }

    public Map<Object, Integer> getWatchedDualVars() {
        return watchedDualVars;
    }

    public Map<Object, Integer> getWatchedPrimalVars() {
        return watchedPrimalVars;
    }
}
