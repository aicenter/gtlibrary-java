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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.utils.graph.Node;

public class SanityCheck {

	public static void main(String[] args) {
		check(new ExperimentGameState(), new ExperimentExpander());
	}

	public static void check(ExperimentGameState root, ExperimentExpander expander) {
		LinkedList<ExperimentGameState> queue = new LinkedList<ExperimentGameState>();
		Set<ExperimentGameState> blackList = new HashSet<ExperimentGameState>();

		queue.add(root);
		blackList.add(root);
		while (!queue.isEmpty()) {
			ExperimentGameState state = queue.removeFirst();

			if (!state.isGameEnd())
				for (Action action : expander.getActions(state)) {
					Commitment commitment = ((PatrollerAction) action).getCommitment();

					assert !commitment.isInconsistent(ExperimentInfo.commitmentDepth, 1);
					if (state.getLastCommitment() != null) {
						assert !state.getLastCommitment().isInconsistent(ExperimentInfo.commitmentDepth - 1, 1);
						assert commitment.isExtensionOf(state.getLastCommitment(), ExperimentInfo.commitmentDepth - 1);
					}
					ExperimentGameState attackerState = (ExperimentGameState) state.performAction(action);

//					assert expander.getActions(attackerState).size() == 4;
					for (Action action1 : expander.getActions(attackerState)) {
						ExperimentGameState natureState = (ExperimentGameState) attackerState.performAction(action1);
						double probabilitySum = 0;

						assert ((AttackerAction) action1).getNode().equals(natureState.getAttackedNode());
						if (natureState.getAttackedNode().isEmptyNode())
							assert expander.getActions(natureState).size() == natureState.getLastCommitment().nonZeroChildrenCount();
						else
							assert expander.getActions(natureState).size() == 2;

						for (Action action2 : expander.getActions(natureState)) {
							ExperimentGameState nextState = (ExperimentGameState) natureState.performAction(action2);
							probabilitySum += natureState.getProbabilityOfNatureFor(action2);
							if (nextState.isGameEnd()) {
								if (!blackList.contains(nextState)) {
									queue.addLast(nextState);
									blackList.add(nextState);
								}
							} else {
								double probabilitySum1 = 0;

								for (Action action3 : expander.getActions(nextState)) {
									ExperimentGameState nextState1 = (ExperimentGameState) nextState.performAction(action3);
									assert !natureState.getAttackedNode().isEmptyNode() || natureState.getProbabilityOfNatureFor(action2) == natureState.getLastCommitment().getCurrentProbability(((NatureAction) action2).getNode());
									assert natureState.getProbabilityOfNatureFor(action2) <= 1;
									probabilitySum1 += nextState.getProbabilityOfNatureFor(action3);
									if (!blackList.contains(nextState1)) {
										queue.addLast(nextState1);
										blackList.add(nextState1);
									}
								}
								assert probabilitySum1 == 1;
							}
						}
						assert Math.abs(probabilitySum - 1) < 1e-8;
					}
				}
		}
		System.out.println(blackList.size());
	}

	private static double getSum(ExperimentGameState state, Commitment commitment) {
		double sum = 0;
		for (Node node : state.getGraph().getAllNodes().values()) {
			sum += commitment.getProbability(node);
		}
		return sum;
	}

	public static void checkInitial(Map<GameState, Double> values, ExperimentExpander expander) {
		LinkedList<ExperimentGameState> queue = getQueue(values);

		while (!queue.isEmpty()) {
			ExperimentGameState state = queue.removeFirst();

			if (!state.isGameEnd())
				for (Action action : expander.getActions(state)) {
					Commitment commitment = ((PatrollerAction) action).getCommitment();

					assert !commitment.isInconsistent(ExperimentInfo.commitmentDepth, 1);
					if (state.getLastCommitment() != null) {
						assert !state.getLastCommitment().isInconsistent(ExperimentInfo.commitmentDepth - 1, 1);
						assert commitment.isExtensionOf(state.getLastCommitment(), ExperimentInfo.commitmentDepth - 1);
					}
					ExperimentGameState attackerState = (ExperimentGameState) state.performAction(action);

//					assert expander.getActions(attackerState).size() == 4;
					for (Action action1 : expander.getActions(attackerState)) {
						ExperimentGameState natureState = (ExperimentGameState) attackerState.performAction(action1);

						double probabilitySum = 0;

						assert ((AttackerAction) action1).getNode().equals(natureState.getAttackedNode());
						if (natureState.getAttackedNode().isEmptyNode())
							assert expander.getActions(natureState).size() == natureState.getLastCommitment().nonZeroChildrenCount();
						else
							assert expander.getActions(natureState).size() == 2;

						for (Action action2 : expander.getActions(natureState)) {
							ExperimentGameState nextState = (ExperimentGameState) natureState.performAction(action2);
							probabilitySum += natureState.getProbabilityOfNatureFor(action2);
							if (nextState.isGameEnd()) {
								assert values.get(nextState) == nextState.getUtilities()[0];
							} else {
								double probabilitySum1 = 0;

								for (Action action3 : expander.getActions(nextState)) {
									ExperimentGameState nextState1 = (ExperimentGameState) nextState.performAction(action3);
									assert !natureState.getAttackedNode().isEmptyNode() || natureState.getProbabilityOfNatureFor(action2) == natureState.getLastCommitment().getCurrentProbability(((NatureAction) action2).getNode());
									assert natureState.getProbabilityOfNatureFor(action2) <= 1 + 1e-8;
									probabilitySum1 += nextState.getProbabilityOfNatureFor(action3);
									assert values.get(nextState1) == nextState1.getUtilities()[0];
								}
								assert Math.abs(probabilitySum1 - 1) < 1e-8;
							}

						}
						assert Math.abs(probabilitySum - 1) < 1e-8;
//						double probabilitySum = 0;
//
//						assert ((AttackerAction) action1).getNode().equals(natureState.getAttackedNode());
////						if (natureState.getAttackedNode().isEmptyNode())
////							assert expander.getActions(natureState).size() == natureState.getLastCommitment().nonZeroChildrenCount();
////						else
////							assert expander.getActions(natureState).size() == 2;
//						for (Action action2 : expander.getActions(natureState)) {
//							ExperimentGameState nextState = (ExperimentGameState) natureState.performAction(action2);
//
////							assert !natureState.getAttackedNode().isEmptyNode() || natureState.getProbabilityOfNatureFor(action2) == natureState.getLastCommitment().getCurrentProbability(((NatureAction) action2).getNode());
////							assert natureState.getProbabilityOfNatureFor(action2) <= 1;
////							probabilitySum += natureState.getProbabilityOfNatureFor(action2);
////							assert values.get(nextState) == nextState.getUtilities()[0];
//						}
//						assert probabilitySum == 1;
					}
				}
		}
	}

	public static void check(Map<GameState, Double> values, ExperimentExpander expander) {
		LinkedList<ExperimentGameState> queue = getQueue(values);

		while (!queue.isEmpty()) {
			ExperimentGameState state = queue.removeFirst();

			if (!state.isGameEnd())
				for (Action action : expander.getActions(state)) {
					Commitment commitment = ((PatrollerAction) action).getCommitment();

					assert !commitment.isInconsistent(ExperimentInfo.commitmentDepth, 1);
					if (state.getLastCommitment() != null) {
						assert !state.getLastCommitment().isInconsistent(ExperimentInfo.commitmentDepth - 1, 1);
						assert commitment.isExtensionOf(state.getLastCommitment(), ExperimentInfo.commitmentDepth - 1);
					}
					ExperimentGameState attackerState = (ExperimentGameState) state.performAction(action);

//					assert expander.getActions(attackerState).size() == 4;
					for (Action action1 : expander.getActions(attackerState)) {
						ExperimentGameState natureState = (ExperimentGameState) attackerState.performAction(action1);

						double probabilitySum = 0;

						assert ((AttackerAction) action1).getNode().equals(natureState.getAttackedNode());
						if (natureState.getAttackedNode().isEmptyNode())
							assert expander.getActions(natureState).size() == natureState.getLastCommitment().nonZeroChildrenCount();
						else
							assert expander.getActions(natureState).size() == 2;
						for (Action action2 : expander.getActions(natureState)) {
							ExperimentGameState nextState = (ExperimentGameState) natureState.performAction(action2);
							probabilitySum += natureState.getProbabilityOfNatureFor(action2);
							if (nextState.isGameEnd()) {
								assert values.get(nextState) == nextState.getUtilities()[0];
							} else {
								double probabilitySum1 = 0;

								for (Action action3 : expander.getActions(nextState)) {
									ExperimentGameState nextState1 = (ExperimentGameState) nextState.performAction(action3);
									assert !natureState.getAttackedNode().isEmptyNode() || natureState.getProbabilityOfNatureFor(action2) == natureState.getLastCommitment().getCurrentProbability(((NatureAction) action2).getNode());
									assert natureState.getProbabilityOfNatureFor(action2) <= 1 + 1e-8;
									probabilitySum1 += nextState.getProbabilityOfNatureFor(action3);
								}
								assert Math.abs(probabilitySum1 - 1) < 1e-8;
							}

						}
						assert Math.abs(probabilitySum - 1) < 1e-8;
					}
				}
		}
	}

	private static LinkedList<ExperimentGameState> getQueue(Map<GameState, Double> values) {
		LinkedList<ExperimentGameState> queue = new LinkedList<ExperimentGameState>();

		for (GameState gameState : values.keySet()) {
			queue.add((ExperimentGameState) gameState);
		}
		return queue;
	}

}
