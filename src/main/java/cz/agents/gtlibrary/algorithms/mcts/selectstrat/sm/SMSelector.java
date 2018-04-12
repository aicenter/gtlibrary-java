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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cz.agents.gtlibrary.algorithms.mcts.selectstrat.sm;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.utils.Pair;

import java.io.Serializable;

/**
 * @author vilo
 */
public interface SMSelector extends Serializable, AlgorithmData {

    /**
     * Returns selected action index.
     */
    public Pair<Integer, Integer> select();

    /**
     * Updates the selector with action result
     */
    public void update(Pair<Integer, Integer> selection, double value);

    /**
     * Returns Algorithm for the second player information set in order to extract strategies.
     */
    public AlgorithmData getBottomData();
}
