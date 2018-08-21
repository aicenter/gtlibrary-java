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

import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.InnerNode;
import cz.agents.gtlibrary.algorithms.mcts.nodes.interfaces.LeafNode;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;

public class LeafNodeImpl extends NodeImpl implements LeafNode {

    private final double[] utilities;
    public static double scalingConstant = 1.0;

    public LeafNodeImpl(InnerNode parent, GameState gameState, Action lastAction) {
        super(parent, lastAction, gameState);
        this.utilities = gameState.getUtilities();
    }

    public double[] getUtilities() {
        if(scalingConstant == 1.0) {
            return utilities;
        }

        double[] scaledUtilities = new double[utilities.length];
        for (int i = 0; i < utilities.length; i++) {
            scaledUtilities[i] = utilities[i] * scalingConstant;
        }
        return scaledUtilities;
    }

    public static double getScalingConstant() {
        return scalingConstant;
    }

    public static void setScalingConstant(double scalingConstant) {
        LeafNodeImpl.scalingConstant = scalingConstant;
    }
}
