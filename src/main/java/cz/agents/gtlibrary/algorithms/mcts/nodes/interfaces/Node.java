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


package cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces;


import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;

import java.io.Serializable;

public interface Node extends Serializable {
    GameState getGameState();

    int getDepth();

    Expander<MCTSInformationSet> getExpander();

    MCTSConfig getAlgConfig();

    InnerNode getParent();

    void setParent(InnerNode parent);

    Action getLastAction();

    void setLastAction(Action lastAction);

    AlgorithmData getAlgorithmData();

    void setAlgorithmData(AlgorithmData algorithmData);

    default Player[] getAllPlayers() {
        return getGameState().getAllPlayers();
    }

    default double getProbabilityOfNatureFor(Action action) {
        return getGameState().getProbabilityOfNatureFor(action);
    }

    boolean isGameEnd();
}
