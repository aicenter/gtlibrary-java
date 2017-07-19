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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cz.agents.gtlibrary.experimental.stochastic.StochasticExpander;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.Pair;
import cz.agents.gtlibrary.utils.graph.Edge;
import cz.agents.gtlibrary.utils.graph.Graph;
import cz.agents.gtlibrary.utils.graph.Node;

public class ExperimentExpander implements StochasticExpander {

	@Override
	public List<Action> getActions(GameState gameState) {
		ExperimentGameState state = (ExperimentGameState) gameState;

		if (state.getPlayerToMove().equals(ExperimentInfo.ATTACKER))
			return getAttackerActions(state);
		if (state.getPlayerToMove().equals(ExperimentInfo.PATROLLER))
			return getPatrollerActions(state);
		return getNatureActions(state);
	}

	private List<Action> getNatureActions(ExperimentGameState state) {
		if (state.getAttackedNode().isEmptyNode())
			return getNatureActionsWithoutAttack(state);
		return getNatureActionsAfterAttack(state);
	}

	private List<Action> getNatureActionsAfterAttack(ExperimentGameState state) {
		List<Action> actions = new ArrayList<Action>();

		actions.add(new NatureAction(state.getAttackedNode()));
		actions.add(new NatureAction(Node.EMPTY_NODE));
		return actions;
	}

	private List<Action> getNatureActionsWithoutAttack(ExperimentGameState state) {
		if (state.isSecondMoveOfNature())
			return getSecondNatureActions(state.getLastCommitment());
		return getFirstNatureActions(state);
	}

	private List<Action> getSecondNatureActions(Commitment commitment) {
		if (commitment.isIncorrectlyDiscretised()) {
			List<Action> actions = new ArrayList<Action>(createActionsForNature(commitment, commitment.copy()));

			for (Action action : actions) {
				((NormalizationNatrueAction) action).setProbability(1. / actions.size());
			}
			assert !actions.isEmpty();
			return actions;
		}
		List<Action> actions = new ArrayList<Action>();
		NormalizationNatrueAction action = new NormalizationNatrueAction(commitment);

		action.setProbability(1);
		actions.add(action);
		return actions;
	}

	private Set<Action> createActionsForNature(Commitment currentCommitment, Commitment commitment) {
		return createActionsForNature(new LinkedHashSet<Action>(), currentCommitment, commitment, 1);
	}

	private Set<Action> createActionsForNature(Set<Action> actions, Commitment currentCommitment, Commitment commitment, double sum) {
		if (!currentCommitment.isIncorrectlyDiscretised()) {
			if (!commitment.isIncorrectlyDiscretised())
				actions.add(new NormalizationNatrueAction(commitment));
			return actions;
		}
		for (Map<Integer, Integer> distribution : getFixedDistributions(currentCommitment, sum)) {
			Commitment commitmentCopy = commitment.copy();

			commitmentCopy.setDistributionTo(currentCommitment, toArray(distribution, commitment.nodeCount));
			for (int i = 0; i < commitmentCopy.children.length; i++) {
				if (commitmentCopy.distribution[i] > 1e-8)
					createActionsForNature(actions, commitmentCopy.children[i], commitmentCopy, commitmentCopy.getCorrectProbability(commitmentCopy.distribution[i]));
			}
		}
		return actions;
	}

	private int[] toArray(Map<Integer, Integer> distribution, int nodeCount) {
		int[] array = new int[nodeCount];

		for (Entry<Integer, Integer> entry : distribution.entrySet()) {
			array[entry.getKey()] = entry.getValue();
		}
		return array;
	}

	private Set<Map<Integer, Integer>> getFixedDistributions(Commitment commitment, double sum) {
		LinkedList<Entry<Integer, double[]>> possibleValues = getPossibleValues(commitment);
		Set<Map<Integer, Double>> fixedDistributions = new LinkedHashSet<Map<Integer, Double>>();

		createFixedDistributions(fixedDistributions, possibleValues, new LinkedHashMap<Integer, Double>(), sum);
		return convertToInnerRepr(fixedDistributions);
	}

	private Set<Map<Integer, Integer>> convertToInnerRepr(Set<Map<Integer, Double>> fixedDistributions) {
		Set<Map<Integer, Integer>> innerRepr = new LinkedHashSet<Map<Integer, Integer>>();

		for (Map<Integer, Double> distribution : fixedDistributions) {
			innerRepr.add(convertToInnerRepr(distribution));
		}
		return innerRepr;
	}

	private Map<Integer, Integer> convertToInnerRepr(Map<Integer, Double> distribution) {
		Map<Integer, Integer> innerRepr = new LinkedHashMap<Integer, Integer>();

		for (Entry<Integer, Double> entry : distribution.entrySet()) {
			innerRepr.put(entry.getKey(), recalculate(entry.getValue()));
		}
		return innerRepr;
	}

	private Integer recalculate(Double value) {
		return (int) Math.round(value / ExperimentInfo.epsilon);
	}

	private void createFixedDistributions(Set<Map<Integer, Double>> fixedDistributions, LinkedList<Entry<Integer, double[]>> possibleValues, Map<Integer, Double> currentDistribution, double sum) {
		if (possibleValues.isEmpty()) {
			if (isDistribution(currentDistribution, sum))
				fixedDistributions.add(currentDistribution);
			return;
		}
		Entry<Integer, double[]> possibleValue = possibleValues.removeFirst();

		for (double probability : possibleValue.getValue()) {
			Map<Integer, Double> currentDistributionCopy = new LinkedHashMap<Integer, Double>(currentDistribution);

			currentDistributionCopy.put(possibleValue.getKey(), probability);
			createFixedDistributions(fixedDistributions, possibleValues, currentDistributionCopy, sum);
		}
		possibleValues.addFirst(possibleValue);
	}

	private boolean isDistribution(Map<Integer, Double> currentDistribution, double targetedSum) {
		double sum = 0;

		for (Double value : currentDistribution.values()) {
			sum += value;
		}
		return Math.abs(sum - targetedSum) < 1e-8;
	}

	private LinkedList<Entry<Integer, double[]>> getPossibleValues(Commitment commitment) {
		Map<Integer, double[]> possibleValues = new LinkedHashMap<Integer, double[]>();

		for (int i = 0; i < commitment.children.length; i++) {
			if (commitment.children[i] != null) {
				double probability = commitment.getCurrentProbability(i);

				possibleValues.put(i, getPossibilities(probability));
			}
		}
		return new LinkedList<Entry<Integer, double[]>>(possibleValues.entrySet());
	}

	private double[] getPossibilities(double probability) {
		double currentProbability = 0;

		for (int i = 1; i <= 1 / ExperimentInfo.epsilon; i++) {
			if (Math.abs(currentProbability - probability) < 1e-8)
				return new double[] { currentProbability };
			if (currentProbability > probability)
				return new double[] { currentProbability - ExperimentInfo.epsilon, currentProbability };
			currentProbability += ExperimentInfo.epsilon;
		}
		return new double[] { probability };
	}

	private List<Action> getFirstNatureActions(ExperimentGameState state) {
		List<Action> actions = new ArrayList<Action>();

		for (int i = 0; i < state.getLastCommitment().distribution.length; i++) {
			if (state.getLastCommitment().distribution[i] > 0)
				actions.add(new NatureAction(state.getGraph().getAllNodes().get("ID" + i)));
		}
		return actions;
	}

	private List<Action> getPatrollerActions(ExperimentGameState state) {
		List<Action> actions = new ArrayList<Action>();
		Commitment lastCommitment = state.getLastCommitment();

		if (lastCommitment == null)
			return createInitialCommitments(ExperimentInfo.commitmentDepth, state);
		LinkedList<Pair<Commitment, Integer>> endingPoints = lastCommitment.getEndingPoints((int) (1 / ExperimentInfo.epsilon));

		createActions(state, endingPoints, actions, lastCommitment);

		for (Action action : actions) {
			assert !((PatrollerAction) action).getCommitment().isInconsistent(ExperimentInfo.commitmentDepth, 1);
		}
		return actions;
	}

	private List<Action> createInitialCommitments(int h, ExperimentGameState state) {
		Commitment commitment = new Commitment(state.getPatrollerNode().getIntID(), 1, state.getGraph().getAllNodes().size());
		List<Commitment> commitments = new ArrayList<Commitment>();

		commitments.add(commitment);
		for (int i = 0; i < h; i++) {
			List<Commitment> nextCommitments = new ArrayList<Commitment>();

			for (Commitment lastCommitment : commitments) {
				createCommitments(state, lastCommitment.getEndingPoints((int) Math.round(1 / ExperimentInfo.epsilon)), nextCommitments, lastCommitment, i);
			}
			commitments = nextCommitments;
			for (Commitment commitment2 : nextCommitments) {
				assert !commitment2.isInconsistent(i, 1);
			}
		}
		for (Commitment commitment2 : commitments) {
			assert !commitment2.isInconsistent(ExperimentInfo.commitmentDepth, 1);
		}
		return wrap(commitments, state);
	}

	private void createCommitments(ExperimentGameState state, LinkedList<Pair<Commitment, Integer>> endingPoints, List<Commitment> actions, Commitment commitment, int depth) {
		if (endingPoints.isEmpty()) {
			actions.add(commitment);
			return;
		}
		Pair<Commitment, Integer> currentEndingPoint = endingPoints.removeFirst();

		for (Commitment expandedCommitment : getExpandedCommitments(state.getGraph(), currentEndingPoint, commitment, depth)) {
			createCommitments(state, endingPoints, actions, expandedCommitment, depth);
		}
		endingPoints.addFirst(currentEndingPoint);
	}

	private List<Action> wrap(List<Commitment> commitments, ExperimentGameState state) {
		List<Action> actions = new ArrayList<Action>();

		for (Commitment commitment : commitments) {
			actions.add(new PatrollerAction(commitment));
		}
		return actions;
	}

	private void createActions(ExperimentGameState state, LinkedList<Pair<Commitment, Integer>> endingPoints, List<Action> actions, Commitment commitment) {
		if (endingPoints.isEmpty()) {
			actions.add(new PatrollerAction(commitment));
			return;
		}
		Pair<Commitment, Integer> currentEndingPoint = endingPoints.removeFirst();

		for (Commitment expandedCommitment : getExpandedCommitments(state.getGraph(), currentEndingPoint, commitment, ExperimentInfo.commitmentDepth - 1)) {
			createActions(state, endingPoints, actions, expandedCommitment);
		}
		endingPoints.addFirst(currentEndingPoint);
	}

//	private Iterable<CommitmentGenerator> getExpandedCommitments(Graph graph, CommitmentGenerator currentEndingPoint, CommitmentGenerator commitment) {
//		List<CommitmentGenerator> commitments = new ArrayList<CommitmentGenerator>();
//
//		for (Edge edge1 : graph.getEdgesOf(currentEndingPoint.node)) {
//			if(edge1.getTarget().equals(currentEndingPoint.node))
//				continue;
//			CommitmentGenerator commitmentCopy = commitment.copy();
//			Map<Node, Double> distribution = new LinkedHashMap<Node, Double>();
//
//			for (Edge edge2 : graph.getEdgesOf(currentEndingPoint.node)) {
//				if(edge2.getTarget().equals(currentEndingPoint.node))
//					continue;
//				distribution.put(edge2.getTarget(), 0d);
//			}
//			distribution.put(edge1.getTarget(), 1d);
//			commitmentCopy.setDistriutionToLeaf(currentEndingPoint.getNode(), distribution);
//			commitments.add(commitmentCopy);
//		}
//		return commitments;
//	}

	private Iterable<Commitment> getExpandedCommitments(Graph graph, Pair<Commitment, Integer> currentEndingPoint, Commitment commitment, int depth) {
		List<Commitment> commitments = new ArrayList<Commitment>();

		for (Map<Node, Integer> distribution : getDistributions(getReachableNodes(graph, currentEndingPoint.getLeft().nodeIdx), currentEndingPoint.getRight())) {
			Commitment commitmentCopy = commitment.copy();

			commitmentCopy.setDistriutionToLeaf(currentEndingPoint.getLeft().nodeIdx, toArrayNode(distribution, graph.getAllNodes().size()), depth);
			commitments.add(commitmentCopy);
		}
		return commitments;
	}

	private int[] toArrayNode(Map<Node, Integer> distribution, int size) {
		int[] array = new int[size];

		for (Entry<Node, Integer> entry : distribution.entrySet()) {
			array[entry.getKey().getIntID()] = entry.getValue();
		}
		return array;
	}

	private LinkedList<Node> getReachableNodes(Graph graph, int nodeIdx) {
		LinkedList<Node> reachableNodes = new LinkedList<Node>();
		Node node = graph.getAllNodes().get("ID" + nodeIdx);

		for (Edge edge : graph.getEdgesOf(node)) {
			if (edge.getSource().equals(node))
				reachableNodes.add(edge.getTarget());
		}
		return reachableNodes;
	}

	private Set<Map<Node, Integer>> getDistributions(LinkedList<Node> nodes, int count) {
//		Map<Node, Integer> distribution = new LinkedHashMap<Node, Integer>();
		
//		for (Node node : nodes) {
//			distribution.put(node, 0);
//		}
		return recursiveDistributions(nodes, new HashSet<Map<Node,Integer>>(), count, new LinkedHashMap<Node, Integer>());
	}
//	
//	private Set<Map<Node, Integer>> recursiveDistributions(List<Node> nodes, Set<Map<Node, Integer>> distributions, int depth, Map<Node, Integer> distribution) {
//		if(depth == 0) {
//			distributions.add(new LinkedHashMap<Node, Integer>(distribution));
//			return distributions;
//		}
//		for (Node node : nodes) {
//			distribution.put(node, distribution.get(node) + 1);
//			recursiveDistributions(nodes, distributions, depth - 1, distribution);
//			distribution.put(node, distribution.get(node) - 1);
//		}
//		return distributions;
//	}
	
	
	private Set<Map<Node, Integer>> recursiveDistributions(LinkedList<Node> nodes, Set<Map<Node, Integer>> distributions, int sum, Map<Node, Integer> distribution) {
		if(nodes.size() == 1) {
			distribution.put(nodes.getFirst(), sum);
			distributions.add(new LinkedHashMap<Node, Integer>(distribution));
			distribution.remove(nodes.getFirst());
			return distributions;
		}
		Node node = nodes.removeFirst();
		
		for (int i = 0; i <= sum; i++) {
			distribution.put(node, i);
			recursiveDistributions(nodes, distributions, sum - i, distribution);
			distribution.remove(node);
		}
		nodes.addFirst(node);
//		for (Node node : nodes) {
//			distribution.put(node, distribution.get(node) + 1);
//			recursiveDistributions(nodes, distributions, depth - 1, distribution);
//			distribution.put(node, distribution.get(node) - 1);
//		}
		return distributions;
	}

	private Map<Node, Integer> getInitialDistribution(List<Node> nodes, int count) {
		Map<Node, Integer> distribution = new LinkedHashMap<Node, Integer>();

		for (Node node : nodes) {
			distribution.put(node, 0);
		}
		distribution.put(nodes.get(0), count);
		return distribution;
	}

//	private List<Map<Node, Double>> getDistributions(List<Map<Node, Double>> distributions, Map<Node, Double> distribution) {
//		Map<Node, Double> substractedDistribution = getSubstractedDistribution(distribution);
//		int firstNonZeroIndex = getFirstNonZeroIndex(distribution);
//		int index = 0;
//
//		for (Entry<Node, Double> entry : distribution.entrySet()) {
//			if (index > firstNonZeroIndex) {
//				Map<Node, Double> nextDistribution = new LinkedHashMap<Node, Double>(substractedDistribution);
//
//				nextDistribution.put(entry.getKey(), entry.getValue() + ExperimentInfo.epsilon);
//				distributions.add(nextDistribution);
//				getDistributions(distributions, nextDistribution);
//			}
//			index++;
//		}
//		return distributions;
//	}

//	private int getFirstNonZeroIndex(Map<Node, Double> distribution) {
//		int index = 0;
//
//		for (Double reward : distribution.values()) {
//			if (reward > 0)
//				return index;
//			index++;
//		}
//		return Integer.MAX_VALUE;
//	}

//	private Map<Node, Double> getSubstractedDistribution(Map<Node, Double> distribution) {
//		Map<Node, Double> substractedDistribution = new LinkedHashMap<Node, Double>(distribution);
//
//		for (Entry<Node, Double> entry : distribution.entrySet()) {
//			if (entry.getValue() > 0) {
//				substractedDistribution.put(entry.getKey(), entry.getValue() - ExperimentInfo.epsilon);
//				return distribution;
//			}
//		}
//		return null;
//	}

	public List<Action> getAttackerActions(ExperimentGameState state) {
		List<Action> actions = new ArrayList<Action>(state.getGraph().getAllNodes().size() + 1);

		for (Node node : state.getGraph().getAllNodes().values()) {
			actions.add(new AttackerAction(node));
		}
		actions.add(new AttackerAction(Node.EMPTY_NODE));
		return actions;
	}

}
