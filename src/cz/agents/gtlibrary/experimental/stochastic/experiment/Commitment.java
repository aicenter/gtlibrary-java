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
import java.util.LinkedList;

import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.graph.Node;

public class Commitment {

	protected double ratio;
	protected int nodeIdx;
	protected int[] distribution;
	protected Commitment[] children;
	protected int nodeCount;

	public Commitment(Node node, double ratio, int nodeCount) {
		this.ratio = ratio;
		this.nodeIdx = node.getIntID();
		this.nodeCount = nodeCount;
//		distribution = new int[nodeCount];
//		children = new CommitmentGenerator[nodeCount];
	}

	public Commitment(int nodeIdx, double ratio, int nodeCount) {
		this.ratio = ratio;
		this.nodeIdx = nodeIdx;
		this.nodeCount = nodeCount;
//		distribution = new int[nodeCount];
//		children = new CommitmentGenerator[nodeCount];
	}

	public Commitment(Commitment commitment) {
		this.ratio = commitment.ratio;
		this.nodeIdx = commitment.nodeIdx;
		this.nodeCount = commitment.nodeCount;
		if (commitment.distribution != null) {
			this.distribution = Arrays.copyOf(commitment.distribution, commitment.distribution.length);
			children = new Commitment[commitment.children.length];

			for (int i = 0; i < commitment.children.length; i++) {
				if (commitment.children[i] != null)
					children[i] = commitment.children[i].copy();
			}
		}
	}

	public boolean setDistriutionToLeaf(Node node, int[] distribution, int depth) {
		return setDistriutionToLeaf(node.getIntID(), distribution, depth);
	}

	public boolean setDistriutionToLeaf(int nodeIdx, int[] distribution, int depth) {
		if (depth == 0) {
			if (this.distribution == null) {
				if (this.nodeIdx == nodeIdx) {
					for (int i = 0; i < distribution.length; i++) {
						createChild(i, distribution[i]);
					}
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			if (children != null)
				for (int i = 0; i < children.length; i++) {
					if (children[i] != null && this.distribution[i] > 1e-8)
						if (children[i].setDistriutionToLeaf(nodeIdx, distribution, depth - 1))
							return true;
				}
			return false;
		}
	}

	public boolean isInconsistent(int h, double sum) {
		if (children == null && h > 0)
			return true;
		boolean inconsistency = false;
		double sum1 = 0;

		if (distribution != null)
			for (int i = 0; i < distribution.length; i++) {
				if (distribution[i] > 1e-8) {
					double probability = getCorrectProbability(distribution[i]);

					inconsistency |= children[i].isInconsistent(h - 1, probability);
					sum1 += probability;
				}
			}
		if (distribution != null)
			inconsistency |= Math.abs(sum1 - sum) > 1e-8;
		return inconsistency;
	}

	public int nonZeroChildrenCount() {
		int count = 0;

		for (int probability : distribution) {
			if (probability > 0)
				count++;
		}
		return count;
	}

	private Commitment createChild(int index, int probability) {
		if (probability < 1e-8)
			return null;
		if (distribution == null) {
			distribution = new int[nodeCount];
			children = new Commitment[nodeCount];
		}
		Commitment childCommitment = new Commitment(index, 1, nodeCount);

		distribution[index] = probability;
		children[index] = childCommitment;
		return childCommitment;
	}

	public double getProbability(int nodeIdx) {
		double probability = 0;

		if (children != null)
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null)
					if (children[i].nodeIdx == nodeIdx)
						probability += getCurrentProbability(nodeIdx);
					else
						probability += children[i].getProbability(nodeIdx);
			}
		return probability;
	}

	public double getProbability(Node node) {
		return getProbability(node.getIntID());
	}

	public double getCurrentProbability(int nodeIdx) {
		return getCorrectProbability(distribution[nodeIdx]);
	}

	public double getCurrentProbability(Node node) {
		return getCurrentProbability(node.getIntID());
	}

	public void normalize(double targetSum) {
		ratio = targetSum;

		if (children != null)
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null)
					children[i].normalize(ratio);
			}

//		if (distribution != null) {
//			double sum = 0;
//			for (Entry<Node, Integer> entry : distribution.entrySet()) {
//				assert getCorrectProbability(entry.getValue()) <= 1;
//				sum += getCorrectProbability(entry.getValue());
//			}
//			assert sum > 0;
//		}
	}

	double getSum() {
		return getSum(distribution);
	}

	double getCorrectProbability(int probability) {
		return ratio * ExperimentInfo.epsilon * probability;
	}

	public boolean isNormalized() {
		return getSum() == 1;
	}

	public Commitment getChild(Node node) {
		return children[node.getIntID()];
	}

//	public Iterable<Node> getChildrenNodes() {
//		return children.keySet();
//	}

	public int getNodeIdx() {
		return nodeIdx;
	}

	public LinkedList<Pair<Commitment, Integer>> getEndingPoints(int probability) {
		LinkedList<Pair<Commitment, Integer>> endingPoints = new LinkedList<Pair<Commitment, Integer>>();

		if (children == null) {
			endingPoints.add(new Pair<Commitment, Integer>(this, probability));
			return endingPoints;
		}
		for (int i = 0; i < distribution.length; i++) {
			if (distribution[i] > 0)
				endingPoints.addAll(children[i].getEndingPoints(distribution[i]));
		}
		return endingPoints;
	}

	public Commitment copy() {
		return new Commitment(this);
	}

	public int[] getDistribution() {
		return distribution;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Node: " + nodeIdx + ", ");
		builder.append(Arrays.toString(distribution));
		builder.append(Arrays.toString(children));
		return builder.toString();
	}

	public boolean isIncorrectlyDiscretised() {
		boolean incorrect = false;

		if (distribution != null)
			for (int i = 0; i < distribution.length; i++) {
				if (Math.abs((Math.round(getCorrectProbability(distribution[i]) / ExperimentInfo.epsilon)) - getCorrectProbability(distribution[i]) / ExperimentInfo.epsilon) > 1e-8)
					return true;
			}
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null)
					incorrect |= children[i].isIncorrectlyDiscretised();
			}
		return incorrect;
	}

//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + ((children == null) ? 0 : children.hashCode());
//		result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
//		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
//		CommitmentGenerator other = (CommitmentGenerator) obj;
//		if (children == null) {
//			if (other.children != null)
//				return false;
//		} else if (!children.equals(other.children))
//			return false;
//		if (distribution == null) {
//			if (other.distribution != null)
//				return false;
//		} else if (!distribution.equals(other.distribution))
//			return false;
//		if (node == null) {
//			if (other.node != null)
//				return false;
//		} else if (!node.equals(other.node))
//			return false;
//		return true;
//	}

	public boolean isExtensionOf(Commitment commitment, int depth) {
		if (depth == 0)
			return true;
		boolean equality = Arrays.equals(distribution, commitment.distribution);

		for (int i = 0; i < children.length; i++) {
			if (children[i] != null)
				equality &= children[i].isExtensionOf(commitment.children[i], depth - 1);
		}
		return equality;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(children);
		result = prime * result + Arrays.hashCode(distribution);
		result = prime * result + nodeIdx;
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
		Commitment other = (Commitment) obj;
		if (!Arrays.equals(children, other.children))
			return false;
		if (!Arrays.equals(distribution, other.distribution))
			return false;
		if (nodeIdx != other.nodeIdx)
			return false;
		return true;
	}

	public void setDistributionTo(Commitment currentCommitment, int[] distribution) {
		if (this.equals(currentCommitment)) {
			this.distribution = distribution;
			ratio = 1;
			return;
//			updateChildren();
		}
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null)
					children[i].setDistributionTo(currentCommitment, distribution);
			}
	}

	private double getSum(int[] distribution) {
		double sum = 0;

		for (int i = 0; i < distribution.length; i++) {
			sum += getCorrectProbability(distribution[i]);
		}
		return sum;
	}

//	private void updateChildren() {
//		for (Entry<Node, CommitmentGenerator> entry : children.entrySet()) {
//			entry.getValue().ratio = getCurrentProbability(entry.getKey()) / (entry.getValue().getIntSum(entry.getValue().distribution) * ExperimentInfo.epsilon);
//			entry.getValue().updateChildren();
//		}
//	}

	public void maintenance(double ratio) {
		if (distribution != null)
			for (int i = 0; i < distribution.length; i++) {
				distribution[i] = (int) Math.round(getCorrectProbability(distribution[i]) / ExperimentInfo.epsilon);
			}
		this.ratio = ratio;
		if (distribution != null)
			for (int i = 0; i < distribution.length; i++) {
				assert getCorrectProbability(distribution[i]) <= 1;
			}
		if (children != null)
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null)
					children[i].maintenance(1);
			}
	}
}
