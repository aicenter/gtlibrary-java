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

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;

public class FastImprovedExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = -2839907519424628882L;

	public FastImprovedExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	@Override
	public List<Action> getActions(GameState gameState) {
		if (gameState.getPlayerToMove().equals(PursuitGameInfo.EVADER))
			return getEvaderActions((PursuitGameState) gameState);
		return getPatrollerActions((PursuitGameState) gameState);
	}

	private List<Action> getEvaderActions(PursuitGameState state) {
		LinkedList<Action> actions = new LinkedList<Action>();
		int biggestDistance = Integer.MIN_VALUE;
		Node nodeWithBiggestDistance = null;

		for (Edge edge : state.getGraph().getEdgesOf(state.getEvaderPosition())) {
			if (edge.getSource().equals(state.getEvaderPosition())) {
				int distance = Math.min(getDistance(state.getP2Position(), edge.getTarget(), state.getGraph().getAllNodes().size()), getDistance(state.getP1Position(), edge.getTarget(), state.getGraph().getAllNodes().size()));

				if (distance > biggestDistance) {
					if (nodeWithBiggestDistance != null)
						actions.addFirst(new EvaderPursuitAction(state.getEvaderPosition(), nodeWithBiggestDistance, getAlgorithmConfig().getInformationSetFor(state)));
					biggestDistance = distance;
					nodeWithBiggestDistance = edge.getTarget();
				} else {
					actions.add(new EvaderPursuitAction(state.getEvaderPosition(), edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
				}
			}
		}
		if (!PursuitGameInfo.forceMoves) {
			actions.add(new EvaderPursuitAction(state.getEvaderPosition(), state.getEvaderPosition(), getAlgorithmConfig().getInformationSetFor(state)));
		}
		actions.addFirst(new EvaderPursuitAction(state.getEvaderPosition(), nodeWithBiggestDistance, getAlgorithmConfig().getInformationSetFor(state)));
		return actions;
	}

	private List<Action> getPatrollerActions(PursuitGameState state) {
		LinkedList<Action> actions = new LinkedList<Action>();
		int smallestDistanceSum = Integer.MAX_VALUE;
		int smallestDistanceMax = Integer.MAX_VALUE;
		Node nodeWithSmallestDistance1 = null;
		Node nodeWithSmallestDistance2 = null;

		for (Edge p1Edge : state.getGraph().getEdgesOf(state.getP1Position())) {
			for (Edge p2Edge : state.getGraph().getEdgesOf(state.getP2Position())) {
				if (p1Edge.getSource().equals(state.getP1Position()) && p2Edge.getSource().equals(state.getP2Position())) {
					int p1Distance = getDistance(state.getEvaderPosition(), p1Edge.getTarget(), state.getGraph().getAllNodes().size());
					int p2Distance = getDistance(state.getEvaderPosition(), p2Edge.getTarget(), state.getGraph().getAllNodes().size());

					if (p1Distance + p2Distance < smallestDistanceSum && Math.max(p1Distance, p2Distance) < smallestDistanceMax) {
						if (nodeWithSmallestDistance1 != null)
							actions.addFirst(new PatrollerPursuitAction(state.getP1Position(), nodeWithSmallestDistance1, state.getP2Position(), nodeWithSmallestDistance2, getAlgorithmConfig().getInformationSetFor(state)));
						smallestDistanceSum = p1Distance + p2Distance;
						smallestDistanceMax = Math.max(p1Distance, p2Distance);
						nodeWithSmallestDistance1 = p1Edge.getTarget();
						nodeWithSmallestDistance2 = p2Edge.getTarget();
					} else {
						actions.add(new PatrollerPursuitAction(state.getP1Position(), p1Edge.getTarget(), state.getP2Position(), p2Edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
					}
				}
				if (!PursuitGameInfo.forceMoves) {
					actions.add(new PatrollerPursuitAction(state.getP1Position(), state.getP1Position(), state.getP2Position(), p2Edge.getTarget(), getAlgorithmConfig().getInformationSetFor(state)));
					actions.add(new PatrollerPursuitAction(state.getP1Position(), p1Edge.getTarget(), state.getP2Position(), state.getP2Position(), getAlgorithmConfig().getInformationSetFor(state)));
				}
			}
		}
		if (!PursuitGameInfo.forceMoves) {
			actions.add(new PatrollerPursuitAction(state.getP1Position(), state.getP1Position(), state.getP2Position(), state.getP2Position(), getAlgorithmConfig().getInformationSetFor(state)));
		}
		actions.addFirst(new PatrollerPursuitAction(state.getP1Position(), nodeWithSmallestDistance1, state.getP2Position(), nodeWithSmallestDistance2, getAlgorithmConfig().getInformationSetFor(state)));
		return actions;
	}

	private int getDistance(Node position, Node target, int nodeCount) {
		int rowCount = (int) Math.sqrt(nodeCount);
		int positionX = ((position.getIntID()-1) % rowCount);
		int positionY = (int) Math.floor((position.getIntID()-1) / rowCount);
		int targetX = ((target.getIntID()-1) % rowCount);
		int targetY = (int) Math.floor((target.getIntID()-1) / rowCount);

		return Math.abs(positionX - targetX) + Math.abs(positionY - targetY);
	}

}
