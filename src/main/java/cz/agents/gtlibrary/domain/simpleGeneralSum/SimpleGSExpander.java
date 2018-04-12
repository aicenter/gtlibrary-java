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


package cz.agents.gtlibrary.domain.simpleGeneralSum;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kail
 * Date: 12/5/13
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleGSExpander extends ExpanderImpl {

    public SimpleGSExpander(AlgorithmConfig algConfig) {
        super(algConfig);
    }

    @Override
    public List<Action> getActions(GameState gameState) {
        List<Action> result = new ArrayList<Action>(SimpleGSInfo.MAX_ACTIONS);
        if (gameState.isGameEnd())
            return result;
        for (int i=0; i<SimpleGSInfo.MAX_ACTIONS; i++) {
            result.add(new SimpleGSAction(getAlgorithmConfig().getInformationSetFor(gameState), i, gameState.getPlayerToMove()));
        }
        return result;
    }
}
