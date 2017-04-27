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


package cz.agents.gtlibrary.algorithms.mcts.nodes;

import cz.agents.gtlibrary.algorithms.mcts.AlgorithmData;
import cz.agents.gtlibrary.algorithms.mcts.MCTSConfig;
import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;

public abstract class NodeImpl implements Node {

    protected InnerNode parent;
    protected GameState gameState;
    protected Action lastAction;
    protected Expander<MCTSInformationSet> expander;
    protected int depth;
    protected AlgorithmData algorithmData;
    
    // Very useful for debugging. testSumS should be approximatly the same everywhere
    //public double testSumS=0;
    //public int visits=0;
    
    public NodeImpl(InnerNode parent, Action lastAction, GameState gameState) {
        this.parent = parent;
        this.lastAction = lastAction;
        this.gameState = gameState;
        this.expander = parent.expander;
        depth = parent.depth + 1;
    }

    public NodeImpl(Expander<MCTSInformationSet> expander, GameState gameState) {
        this.expander = expander;
        this.gameState = gameState;
        depth = 0;
    }

    @Override
    public InnerNode getParent() {
        return parent;
    }

    @Override
    public int getDepth() {
        return depth;
    }

    @Override
    public void setParent(InnerNode parent) {
        this.parent = parent;
    }

    @Override
    public Action getLastAction() {
        return lastAction;
    }

    public void setLastAction(Action lastAction) {
        this.lastAction = lastAction;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public String toString() {
        return "Node: " + gameState;
    }

    public MCTSConfig getAlgConfig() {
        return (MCTSConfig) expander.getAlgorithmConfig();
    }

    public Expander<MCTSInformationSet> getExpander() {
        return expander;
    }

    public AlgorithmData getAlgorithmData() {
        return algorithmData;
    }

    public void setAlgorithmData(AlgorithmData algorithmData) {
        this.algorithmData = algorithmData;
    }
}
