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


package cz.agents.gtlibrary.experimental.stochastic.newexperiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.Pair;

public class NEExpander implements StochasticExpander {

	private Map<String, Map<Pair<NEAction, NEAction>, NEGameState>> connections;

	public NEExpander() {
		connections = new HashMap<String, Map<Pair<NEAction, NEAction>, NEGameState>>();

		Map<Pair<NEAction, NEAction>, NEGameState> aConnections = new HashMap<Pair<NEAction, NEAction>, NEGameState>();

		aConnections.put(new Pair<NEAction, NEAction>(new NEAction(0, NEGameInfo.p1), new NEAction(2, NEGameInfo.p2)), NEGameInfo.gameStates.get("B"));
		aConnections.put(new Pair<NEAction, NEAction>(new NEAction(0, NEGameInfo.p1), new NEAction(3, NEGameInfo.p2)), NEGameInfo.gameStates.get("C"));
		aConnections.put(new Pair<NEAction, NEAction>(new NEAction(1, NEGameInfo.p1), new NEAction(2, NEGameInfo.p2)), NEGameInfo.gameStates.get("B"));
		aConnections.put(new Pair<NEAction, NEAction>(new NEAction(1, NEGameInfo.p1), new NEAction(3, NEGameInfo.p2)), NEGameInfo.gameStates.get("C"));
		connections.put("A", aConnections);

		Map<Pair<NEAction, NEAction>, NEGameState> bConnections = new HashMap<Pair<NEAction, NEAction>, NEGameState>();

		bConnections.put(new Pair<NEAction, NEAction>(new NEAction(0, NEGameInfo.p1), new NEAction(2, NEGameInfo.p2)), NEGameInfo.gameStates.get("C"));
		bConnections.put(new Pair<NEAction, NEAction>(new NEAction(0, NEGameInfo.p1), new NEAction(3, NEGameInfo.p2)), NEGameInfo.gameStates.get("E"));
		bConnections.put(new Pair<NEAction, NEAction>(new NEAction(1, NEGameInfo.p1), new NEAction(2, NEGameInfo.p2)), NEGameInfo.gameStates.get("D"));
		bConnections.put(new Pair<NEAction, NEAction>(new NEAction(1, NEGameInfo.p1), new NEAction(3, NEGameInfo.p2)), NEGameInfo.gameStates.get("C"));
		connections.put("B", bConnections);

		Map<Pair<NEAction, NEAction>, NEGameState> cConnections = new HashMap<Pair<NEAction, NEAction>, NEGameState>();

		cConnections.put(new Pair<NEAction, NEAction>(new NEAction(0, NEGameInfo.p1), new NEAction(2, NEGameInfo.p2)), NEGameInfo.gameStates.get("F"));
		cConnections.put(new Pair<NEAction, NEAction>(new NEAction(0, NEGameInfo.p1), new NEAction(3, NEGameInfo.p2)), NEGameInfo.gameStates.get("A"));
		cConnections.put(new Pair<NEAction, NEAction>(new NEAction(1, NEGameInfo.p1), new NEAction(2, NEGameInfo.p2)), NEGameInfo.gameStates.get("G"));
		cConnections.put(new Pair<NEAction, NEAction>(new NEAction(1, NEGameInfo.p1), new NEAction(3, NEGameInfo.p2)), NEGameInfo.gameStates.get("H"));
		connections.put("C", cConnections);
	}

	public List<Action> getActions(GameState state) {
		if (state.getPlayerToMove().equals(NEGameInfo.p1))
			return getPatrollerActions();
		if (state.getPlayerToMove().equals(NEGameInfo.p2))
			return getAttackerActions();
		return getNatureActions(state);
	}

	public List<Action> getNatureActions(GameState state) {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new NatureAction(getFollowingState(state), NEGameInfo.nature));
		return actions;
	}

	private NEGameState getFollowingState(GameState state) {
		NEGameState neState = (NEGameState) state;

		return connections.get(neState.getId()).get(new Pair<NEAction, NEAction>(neState.getP1Action(), neState.getP2Action())).copy();
	}

	public List<Action> getAttackerActions() {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new NEAction(2, NEGameInfo.p2));
		actions.add(new NEAction(3, NEGameInfo.p2));
		return actions;
	}

	public List<Action> getPatrollerActions() {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new NEAction(0, NEGameInfo.p1));
		actions.add(new NEAction(1, NEGameInfo.p1));
		return actions;
	}
}
