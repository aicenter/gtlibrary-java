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


package cz.agents.gtlibrary.experimental.stochastic.experiment;

import java.util.Arrays;

import cz.agents.gtlibrary.experimental.stochastic.StochasticGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

public class ExperimentGameState extends StochasticGameState {

	private static final long serialVersionUID = -2023436546409105452L;

	private int currentPlayer;
	private Commitment lastCommitment;
	private Node attackedNode;
	private Node defenderNode;
	private Graph graph;
	private boolean secondMoveOfNature;

	public ExperimentGameState() {
		super(ExperimentInfo.ALL_PLAYERS);
		this.currentPlayer = 0;
		attackedNode = Node.EMPTY_NODE;
		defenderNode = Node.EMPTY_NODE;
		graph = new ExperimentGraph(ExperimentInfo.graphFile);
		this.secondMoveOfNature = false;
	}

	public ExperimentGameState(ExperimentGameState state) {
		super(state);
		this.currentPlayer = state.currentPlayer;
		if (state.lastCommitment != null)
			this.lastCommitment = state.lastCommitment.copy();
		this.attackedNode = state.attackedNode;
		this.defenderNode = state.defenderNode;
		this.graph = state.graph;
		this.secondMoveOfNature = state.secondMoveOfNature;
	}

	public void setLastCommitment(Commitment commitment) {
		lastCommitment = commitment;
		assert !lastCommitment.isInconsistent(ExperimentInfo.commitmentDepth, 1);
		currentPlayer = 1;
	}

	public void attackNode(Node node) {
		this.attackedNode = node;
		currentPlayer = 2;
	}

	public void commitTo(Node node) {
		if (!attackedNode.isEmptyNode()) {
			currentPlayer = 0;
			defenderNode = node;
			return;
		}
		currentPlayer = 2;
		secondMoveOfNature = true;
		double probability = lastCommitment.getCurrentProbability(node);

		assert !lastCommitment.isInconsistent(ExperimentInfo.commitmentDepth, 1);
		lastCommitment = lastCommitment.getChild(node);
		lastCommitment.normalize(1. / probability);
		assert !lastCommitment.isInconsistent(ExperimentInfo.commitmentDepth - 1, 1);
		assert Math.abs(lastCommitment.getSum() - 1) < 1e-8;
	}

	public void normalizeCommitment(Commitment normalizedCommitment) {
		currentPlayer = 0;
		secondMoveOfNature = false;
		lastCommitment = normalizedCommitment;
		assert !lastCommitment.isInconsistent(ExperimentInfo.commitmentDepth - 1, 1);
		lastCommitment.maintenance(1);
		assert !lastCommitment.isInconsistent(ExperimentInfo.commitmentDepth - 1, 1);
	}

	public boolean isSecondMoveOfNature() {
		return secondMoveOfNature;
	}

	public Graph getGraph() {
		return graph;
	}

	public Commitment getLastCommitment() {
		return lastCommitment;
	}

	public Node getAttackedNode() {
		return attackedNode;
	}

	@Override
	public double getProbabilityOfNatureFor(Action action) {
		if (isPlayerToMoveNature()) {
			if (attackedNode.isEmptyNode()) {
				if (isSecondMoveOfNature())
					return ((NormalizationNatrueAction) action).getProbability();
				return lastCommitment.getCurrentProbability(((NatureAction) action).getNode());
			} else {
				double probability = lastCommitment.getProbability(attackedNode);
				return attackedNode.equals(((NatureAction) action).getNode()) ? probability : (1 - probability);
			}
		}
		return 0;
	}

	@Override
	public Player getPlayerToMove() {
		return ExperimentInfo.ALL_PLAYERS[currentPlayer];
	}

	public Node getPatrollerNode() {
		if (lastCommitment == null)
			return graph.getAllNodes().get("ID" + ExperimentInfo.patrollerStartId);
		return graph.getAllNodes().get("ID" + lastCommitment.getNodeIdx());
	}

	@Override
	public ExperimentGameState copy() {
		return new ExperimentGameState(this);
	}

	@Override
	public double[] getUtilities() {
		if (!isGameEnd())
			return new double[] { 0, 0, 0 };
		if (defenderNode.isEmptyNode())
			return new double[] { -ExperimentInfo.UTILITY, ExperimentInfo.UTILITY, 0 };
		return new double[] { 0, -ExperimentInfo.UTILITY, 0 };
	}

	@Override
	public boolean isGameEnd() {
		return !attackedNode.isEmptyNode() && currentPlayer == 0;
	}

	@Override
	public boolean isPlayerToMoveNature() {
		return currentPlayer == 2;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("[");
		builder.append(lastCommitment == null ? "root" : lastCommitment.toString());
		builder.append(", AN: " + attackedNode);
		builder.append(", DN: " + defenderNode);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		if(isGameEnd()) {
			return Arrays.hashCode(getUtilities());
		}
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attackedNode == null) ? 0 : attackedNode.hashCode());
		result = prime * result + currentPlayer;
		result = prime * result + ((defenderNode == null) ? 0 : defenderNode.hashCode());
		result = prime * result + ((lastCommitment == null) ? 0 : lastCommitment.hashCode());
		result = prime * result + (secondMoveOfNature ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExperimentGameState other = (ExperimentGameState) obj;
		
		if(isGameEnd())
			return Arrays.equals(getUtilities(), other.getUtilities());
		if (attackedNode == null) {
			if (other.attackedNode != null)
				return false;
		} else if (!attackedNode.equals(other.attackedNode))
			return false;
		if (currentPlayer != other.currentPlayer)
			return false;
		if (defenderNode == null) {
			if (other.defenderNode != null)
				return false;
		} else if (!defenderNode.equals(other.defenderNode))
			return false;
		if (lastCommitment == null) {
			if (other.lastCommitment != null)
				return false;
		} else if (!lastCommitment.equals(other.lastCommitment))
			return false;
		if (secondMoveOfNature != other.secondMoveOfNature)
			return false;
		return true;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//
//		result = prime * result + ((attackedNode == null) ? 0 : attackedNode.hashCode());
//		result = prime * result + ((defenderNode == null) ? 0 : defenderNode.hashCode());
//		result = prime * result + currentPlayer;
//		result = prime * result + ((lastCommitment == null) ? 0 : lastCommitment.hashCode());
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		ExperimentGameState other = (ExperimentGameState) obj;
//
//		if (attackedNode == null) {
//			if (other.attackedNode != null)
//				return false;
//		} else if (!attackedNode.equals(other.attackedNode))
//			return false;
//		if (defenderNode == null) {
//			if (other.defenderNode != null)
//				return false;
//		} else if (!defenderNode.equals(other.defenderNode))
//			return false;
//		if (currentPlayer != other.currentPlayer)
//			return false;
//		if (lastCommitment == null) {
//			if (other.lastCommitment != null)
//				return false;
//		} else if (!lastCommitment.equals(other.lastCommitment))
//			return false;
//		return true;
//	}
}
