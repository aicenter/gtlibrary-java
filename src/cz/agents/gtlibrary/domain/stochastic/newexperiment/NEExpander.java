package cz.agents.gtlibrary.domain.stochastic.newexperiment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.agents.gtlibrary.domain.stochastic.StochasticExpander;
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
