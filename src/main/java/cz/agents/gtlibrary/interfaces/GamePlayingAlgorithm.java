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
package cz.agents.gtlibrary.interfaces;

import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;

/**
 *
 * @author vilo
 */
public interface GamePlayingAlgorithm {
    public Action runMiliseconds(int miliseconds, GameState gameState);
    public Action runMiliseconds(int miliseconds);
    public void setCurrentIS(InformationSet currentIS);
    public InnerNode getRootNode();
    default boolean hasGivenUp() {
        return false;
    }
    default Double actionChosenWithProb() {
        return 1.;
    }
}
