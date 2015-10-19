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


package cz.agents.gtlibrary.nfg.MDP.implementations.pureStrategyOracle;

import cz.agents.gtlibrary.nfg.Utility;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStateActionMarginal;
import cz.agents.gtlibrary.nfg.MDP.implementations.MDPStrategy;
import cz.agents.gtlibrary.nfg.MDP.interfaces.MDPConfig;

/**
 * Created with IntelliJ IDEA.
 * User: bosansky
 * Date: 7/24/13
 * Time: 10:04 AM
 * To change this template use File | Settings | File Templates.
 */

public class MDPUtilityComputer extends Utility<MDPStrategy, MDPStrategy> {

    MDPConfig config;

    public MDPUtilityComputer(MDPConfig config) {
        this.config = config;
    }

    @Override
    public double getUtility(MDPStrategy s1, MDPStrategy s2) {
        double result = 0d;
        for (MDPStateActionMarginal m1 : s1.getAllMarginalsInStrategy()) {
            result += s1.getStrategyProbability(m1) * s1.getUtility(m1, s2);
        }
        return result;
    }

}
