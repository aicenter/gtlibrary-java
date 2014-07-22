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


package cz.agents.gtlibrary.experimental.rpoptimization;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class RPMerger {

	private GameState rootState;
	private Expander<? extends InformationSet> expander;

	public RPMerger(GameState rootState, Expander<? extends InformationSet> expander) {
		this.rootState = rootState;
		this.expander = expander;
	}

	public Map<Player, Map<Sequence, Double>> mergePlans(Map<Player, Map<Sequence, Double>> realPlan1, Map<Player, Map<Sequence, Double>> realPlan2) {
		Map<Player, Map<Sequence, Double>> realPlan = new HashMap<Player, Map<Sequence, Double>>();
		LinkedList<GameState> states = new LinkedList<GameState>();

		realPlan.put(rootState.getAllPlayers()[0], new HashMap<Sequence, Double>());
		realPlan.put(rootState.getAllPlayers()[1], new HashMap<Sequence, Double>());
		states.addLast(rootState);

		while (!states.isEmpty()) {
			GameState state = states.removeFirst();
			if(state.isGameEnd())
				continue;

			if (!state.isPlayerToMoveNature())
				addContinuation(state, realPlan, realPlan1, realPlan2);
			for (Action action : expander.getActionsForUnknownIS(state)) {
				states.add(state.performAction(action));
			}
		}
		return realPlan;
	}

	private void addContinuation(GameState state, Map<Player, Map<Sequence, Double>> realPlan, Map<Player, Map<Sequence, Double>> realPlan1, Map<Player, Map<Sequence, Double>> realPlan2) {
		boolean added = addContinuationFrom(state, realPlan, realPlan1);

		if (!added) {
			addContinuationFrom(state, realPlan, realPlan2);
		}
	}

	private boolean addContinuationFrom(GameState state, Map<Player, Map<Sequence, Double>> realPlan, Map<Player, Map<Sequence, Double>> realPlan1) {
		boolean added = false;

		Sequence sequence = state.getSequenceForPlayerToMove();
		for (Action action : expander.getActionsForUnknownIS(state)) {
			Sequence contSequence = new LinkedListSequenceImpl(sequence);

			contSequence.addLast(action);

			Double value = realPlan1.get(state.getPlayerToMove()).get(contSequence);

			if (value != null) {
				realPlan.get(state.getPlayerToMove()).put(contSequence, value);
				added = true;
			}
		}
		return added;
	}

}
