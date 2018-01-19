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


package cz.agents.gtlibrary.domain.pursuit;

import java.util.ArrayList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Edge;

public class PursuitExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = 3736551160185044265L;

	public PursuitExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if (gameState.getPlayerToMove().equals(PursuitGameInfo.EVADER))
			return getEvaderActions((PursuitGameState) gameState);
		return getPatrollerActions((PursuitGameState) gameState);
	}

	private List<Action> getEvaderActions(PursuitGameState state) {
		List<Action> actions = new ArrayList<Action>();

		for (Edge edge : state.getGraph().getEdgesOf(state.getEvaderPosition())) {
			if (edge.getSource().equals(state.getEvaderPosition()))
				actions.add(new EvaderPursuitAction(state.getEvaderPosition(), edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
		}
		if (!PursuitGameInfo.forceMoves) {
			actions.add(new EvaderPursuitAction(state.getEvaderPosition(), state.getEvaderPosition(), getAlgorithmConfig().getInformationSetFor(state)));
		}
		return actions;
	}

	private List<Action> getPatrollerActions(PursuitGameState state) {
		List<Action> actions = new ArrayList<Action>();

		for (Edge p1Edge : state.getGraph().getEdgesOf(state.getP1Position())) {
			for (Edge p2Edge : state.getGraph().getEdgesOf(state.getP2Position())) {
				if (p1Edge.getSource().equals(state.getP1Position()) && p2Edge.getSource().equals(state.getP2Position()))
					actions.add(new PatrollerPursuitAction(state.getP1Position(), p1Edge.getTarget(), state.getP2Position(), p2Edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
				if (!PursuitGameInfo.forceMoves) {
					actions.add(new PatrollerPursuitAction(state.getP1Position(), state.getP1Position(), state.getP2Position(), p2Edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
					actions.add(new PatrollerPursuitAction(state.getP1Position(), p1Edge.getTarget(), state.getP2Position(), state.getP2Position(), getAlgorithmConfig().getInformationSetFor(state)));
				}
			}
		}
//		if (!PursuitGameInfo.forceMoves) {
//			actions.add(new PatrollerPursuitAction(state.getP1Position(), state.getP1Position(), state.getP2Position(), state.getP2Position(), getAlgorithmConfig().getInformationSetFor(state)));
//		}
		return actions;
	}

}
