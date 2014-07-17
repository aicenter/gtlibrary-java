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


package cz.agents.gtlibrary.domain.bpg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.bpg.AttackerAction.AttackerMovementType;
import cz.agents.gtlibrary.domain.bpg.data.BorderPatrollingGraph;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Node;

public class BPGExpander<I extends InformationSet> extends ExpanderImpl<I> {

	private static final long serialVersionUID = 3620429320502817997L;

	public BPGExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	public List<Action> getActions(GameState state) {
		BPGGameState gameState = (BPGGameState) state;

		if (gameState.getPlayerToMove().equals(BPGGameInfo.ATTACKER)) {
			return getActionsOfAttacker(gameState, getAlgorithmConfig().getInformationSetFor(gameState));
		}
		return getActionsForPatroller(gameState, getAlgorithmConfig().getInformationSetFor(gameState));
	}

	private List<Action> getActionsOfAttacker(BPGGameState gameState, I informationSet) {
		List<Action> result = new ArrayList<Action>();
		Node attackerPosition = gameState.getAttackerPosition();

		if (gameState.isSlowAttackerMovement()) {
			result.add(new AttackerAction(attackerPosition, attackerPosition, informationSet, AttackerMovementType.WAIT));
		} else {
			for (Edge edge : gameState.getGraph().getGraph().outgoingEdgesOf(attackerPosition)) {
				if (edge.getSource().equals(edge.getTarget()) && !edge.getSource().equals(gameState.getGraph().getOrigin()))
					continue;
				int idOfTarget = edge.getTarget().getIntID();
				AttackerAction quickAttackerAction = new AttackerAction(attackerPosition, edge.getTarget(), informationSet, AttackerMovementType.QUICK);

				result.add(quickAttackerAction);
				if (BPGGameInfo.SLOW_MOVES) {
					if (idOfTarget >= 2 && idOfTarget <= 6) {
						AttackerAction slowAttackerAction = new AttackerAction(attackerPosition, edge.getTarget(), informationSet, AttackerMovementType.SLOW);
						result.add(slowAttackerAction);
					}
				}

			}
		}
		return result;
	}

	private List<Action> getActionsForPatroller(BPGGameState gameState, I informationSet) {
		List<Action> result = new LinkedList<Action>();
		Node p1Position = gameState.getP1Position();
		Node p2Position = gameState.getP2Position();

		for (Node p1Target : getTargetsForP1(p1Position, gameState.getGraph())) {
			for (Node p2Target : getTargetsForP2(p2Position, gameState.getGraph())) {
				result.add(new PatrollerAction(p1Position, p2Position, p1Target, p2Target, informationSet, gameState.getFlaggedNodesObservedByPatroller()));
			}
		}
		return result;
	}

	private Iterable<Node> getTargetsForP1(Node position, BorderPatrollingGraph graph) {
		List<Node> nodes = new LinkedList<Node>();

		for (Edge edge : graph.getGraph().outgoingEdgesOf(position)) {
			if (edge.getTarget().equals(graph.getDestination()))
				continue;
			if (edge.getTarget().getIntID() >= 1 && edge.getTarget().getIntID() <= 6)
				nodes.add(edge.getTarget());
		}
		return nodes;
	}

	private Iterable<Node> getTargetsForP2(Node position, BorderPatrollingGraph graph) {
		List<Node> nodes = new LinkedList<Node>();

		for (Edge edge : graph.getGraph().outgoingEdgesOf(position)) {
			if (edge.getTarget().equals(graph.getDestination()))
				continue;
			if (edge.getTarget().getIntID() > 10)
				nodes.add(edge.getTarget());
		}
		return nodes;
	}

}
