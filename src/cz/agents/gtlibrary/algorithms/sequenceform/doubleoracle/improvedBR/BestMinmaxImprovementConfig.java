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


package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.improvedBR;

import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 5/27/13
 * Time: 1:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class BestMinmaxImprovementConfig extends DoubleOracleConfig{


    public BestMinmaxImprovementConfig(GameState rootState, GameInfo gameInfo) {
        super(rootState, gameInfo);
    }

}
