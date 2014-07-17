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


package cz.agents.gtlibrary.domain.randomgame;

import cz.agents.gtlibrary.experimental.rpoptimization.ActionComparator;
import cz.agents.gtlibrary.interfaces.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RandomGameExpanderWithMoveOrdering<I extends InformationSet> extends RandomGameExpander<I> {

	private static final long serialVersionUID = 3616797799524952539L;

    private int[] order;

	public RandomGameExpanderWithMoveOrdering(AlgorithmConfig<I> algConfig, int[] ordering) {
		super(algConfig);
        this.order = ordering;
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		List<Action> actions = super.getActions(gameState);
		Action[] reorderedActions = new Action[actions.size()];

        assert (order.length == actions.size());

        for (int i=0; i<actions.size(); i++) {
            reorderedActions[order[i]] = actions.get(i);
        }

        return Arrays.asList(reorderedActions);
	}

}
