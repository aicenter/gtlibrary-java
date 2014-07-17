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

import cz.agents.gtlibrary.algorithms.mcts.MCTSInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import java.util.Random;

public class ChanceNode extends InnerNode {
	private Random random;

	public ChanceNode(InnerNode parent, GameState gameState, Action lastAction) {
		this(parent, gameState, lastAction, new Random());
	}
	
	public ChanceNode(Expander<MCTSInformationSet> expander, GameState gameState) {
		this(expander, gameState, new Random());
	}

    public ChanceNode(InnerNode parent, GameState gameState, Action lastAction, long seed) {
        this(parent, gameState, lastAction, new Random(seed));
    }

    public ChanceNode(Expander<MCTSInformationSet> expander, GameState gameState, long seed) {
        this(expander, gameState, new Random(seed));
    }

    public ChanceNode(InnerNode parent, GameState gameState, Action lastAction, Random random) {
        super(parent, gameState, lastAction);
        this.random = random;
    }

    public ChanceNode(Expander<MCTSInformationSet> expander, GameState gameState, Random random) {
        super(expander, gameState);
        this.random = random;
    }

	public Action getRandomAction() {
		double move = random.nextDouble();
		
		for (Action action : actions) {
			move -= gameState.getProbabilityOfNatureFor(action);
			if (move < 0) {
				return action;
			}
		}
		return actions.get(actions.size() - 1);
	}
}
