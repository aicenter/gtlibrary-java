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


package cz.agents.gtlibrary.algorithms.mcts;

import cz.agents.gtlibrary.algorithms.mcts.nodes.InnerNode;
import cz.agents.gtlibrary.iinodes.InformationSetImpl;
import cz.agents.gtlibrary.interfaces.GameState;

import java.util.HashSet;
import java.util.Set;

public class MCTSInformationSet extends InformationSetImpl {

    private Set<InnerNode> allNodes;
    private AlgorithmData algorithmData;

    public MCTSInformationSet(GameState state) {
        super(state);
        allNodes = new HashSet<InnerNode>();
    }

    public void addNode(InnerNode node) {
        allNodes.add(node);
    }

    public Set<InnerNode> getAllNodes() {
        return allNodes;
    }

    public AlgorithmData getAlgorithmData() {
        return algorithmData;
    }

    public void setAlgorithmData(AlgorithmData algorithmData) {
        this.algorithmData = algorithmData;
    }

}
