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


package cz.agents.gtlibrary.algorithms.mcts.distribution;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.oos.OOSAlgorithmData;
import cz.agents.gtlibrary.interfaces.Action;

import java.util.HashMap;
import java.util.Map;

public class CurrentStratDist implements Distribution {
    @Override
    public Map<Action, Double> getDistributionFor(AlgorithmData data) {
        OOSAlgorithmData stat = (OOSAlgorithmData) data;
        if (stat == null) return null;

        final double[] strat = stat.getRMStrategy();
        Map<Action, Double> distribution = new HashMap<>(stat.getActions().size());

        int i = 0;
        for (Action a : stat.getActions()) distribution.put(a, strat[i++]);
        assert Math.abs(1 - distribution.values().stream().reduce(0., Double::sum)) < 1e-6;
        return distribution;
    }
}
