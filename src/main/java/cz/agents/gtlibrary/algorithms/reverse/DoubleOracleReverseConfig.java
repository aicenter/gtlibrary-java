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


package cz.agents.gtlibrary.algorithms.reverse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle.DoubleOracleInformationSet;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;
import cz.agents.gtlibrary.interfaces.GameInfo;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;
import cz.agents.gtlibrary.utils.FixedSizeMap;
import cz.agents.gtlibrary.utils.Pair;

/**
 * 
 * The restricted game is defined with playerSequences.
 * 
 * @author bosansky
 * 
 */
public class DoubleOracleReverseConfig<I extends DoubleOracleInformationSet> extends DoubleOracleConfig<I> {

	public DoubleOracleReverseConfig(GameState rootState, GameInfo gameInfo) {
		super(rootState,gameInfo);
	}

	@Override
	public I getInformationSetFor(GameState gameState) {
		I infoSet = allInformationSets.get(gameState.getISKeyForPlayerToMove());
		if (infoSet == null) {
			infoSet = (I) new DoubleOracleReverseInformationSet(gameState);
		}
		return infoSet;
	}
	

	
	@Override
	public void initializeRG(Map<Player, Set<Sequence>> sequences, SQFBestResponseAlgorithm[] bestResponseAlgorithms, Expander<? extends DoubleOracleInformationSet> expander) {

		assert (sequences.keySet().size() == 2);

		Player searchingPlayer = sequences.keySet().iterator().next();
		Player opponentPlayer = gameInfo.getOpponent(searchingPlayer);

		//LinkedList<Pair<GameState, Map<Player, Set<Sequence>>>> queue = new LinkedList<Pair<GameState, Map<Player, Set<Sequence>>>>();
		Map<Player, Set<Sequence>> tmpMap = new HashMap<Player, Set<Sequence>>();
		tmpMap.put(searchingPlayer, sequences.get(searchingPlayer));
		tmpMap.put(opponentPlayer, sequences.get(opponentPlayer));
		//queue.add(new Pair<GameState, Map<Player, Set<Sequence>>>(rootState, tmpMap));

		//while (!queue.isEmpty()) {
			Map<Player, Set<Sequence>> currentSequences = createRG(
					bestResponseAlgorithms, expander, searchingPlayer,
					opponentPlayer, new Pair<GameState, Map<Player, Set<Sequence>>>(rootState, tmpMap), null);
		//}
		//System.out.println("TLF: " + temporaryLeafs);
	}

	private Map<Player, Set<Sequence>> createRG(
			SQFBestResponseAlgorithm[] bestResponseAlgorithms,
			Expander<? extends DoubleOracleInformationSet> expander,
			Player searchingPlayer, Player opponentPlayer,
			Pair<GameState, Map<Player, Set<Sequence>>> currentTuple, Map<Player, Set<Sequence>> currentSequences) {
		//Pair<GameState, Map<Player, Set<Sequence>>> currentTuple = queue.pollFirst();
		GameState currentState = currentTuple.getLeft();

		// terminal state			
		if (currentState.isGameEnd()) {
			Double utRes = getActualNonzeroUtilityValues(currentState);
			if (utRes == null) {
				setUtility(currentState); // utility caching
			} else
				assert (utRes == currentState.getNatureProbability() * currentState.getUtilities()[0]);
			return currentSequences;
		}

		currentSequences = currentTuple.getRight();	

		if (currentState.isPlayerToMoveNature()) { // nature moving
			//GameState natureTempState = currentState.copy();
			List<Action> natureMoves = expander.getActions(currentState);
			for (Action action : natureMoves) {
				GameState natureTempState = currentState.copy();
				//natureTempState = currentState.performAction(action);
				natureTempState.performActionModifyingThisState(action);

				Map<Player, Set<Sequence>> newCurrentSequences = new FixedSizeMap<Player, Set<Sequence>>(2);
				newCurrentSequences.put(searchingPlayer, new HashSet<Sequence>(currentSequences.get(searchingPlayer)));
				newCurrentSequences.put(opponentPlayer, new HashSet<Sequence>(currentSequences.get(opponentPlayer)));

				createRG(bestResponseAlgorithms, expander, searchingPlayer,
						opponentPlayer, new Pair<GameState, Map<Player, Set<Sequence>>>(natureTempState, newCurrentSequences), currentSequences);
				//natureTempState.reverseAction();
			}
		} else if (currentState.getPlayerToMove().equals(searchingPlayer) || currentState.getPlayerToMove().equals(opponentPlayer)) {
			Player movingPlayer = currentState.getPlayerToMove();
			Player otherPlayer = gameInfo.getOpponent(movingPlayer);
			GameState newState = currentState;
			Map<GameState, Map<Player, Set<Sequence>>> tmpNewStatesMap = new HashMap<GameState, Map<Player, Set<Sequence>>>();
			//GameState
			for (Sequence s : currentSequences.get(movingPlayer)) {
				if (s.size() == 0)
					continue;
				Action action = s.getFirst();
				//System.out.println("Outcheck");
				if (currentState.checkConsistency(action)) {
					newState = currentState.performAction(action);
					// newState = currentState;
					//newState.performActionModifyingThisState(action);
					Map<Player, Set<Sequence>> tmpNewUsefulSequences = tmpNewStatesMap.get(newState);
					if (tmpNewUsefulSequences == null) {
						tmpNewUsefulSequences = new FixedSizeMap<Player, Set<Sequence>>(2);
					}

					Set<Sequence> newSequencesForMovingPlayer = tmpNewUsefulSequences.get(movingPlayer);
					if (newSequencesForMovingPlayer == null)
						newSequencesForMovingPlayer = new HashSet<Sequence>();
					newSequencesForMovingPlayer.add(s.getSubSequence(1, s.size() - 1));
					tmpNewUsefulSequences.put(movingPlayer, newSequencesForMovingPlayer);

					Set<Sequence> newSequencesForOpponentPlayer = tmpNewUsefulSequences.get(otherPlayer);
					if (newSequencesForOpponentPlayer == null) {
						newSequencesForOpponentPlayer = new HashSet<Sequence>();
						newSequencesForOpponentPlayer.addAll(currentSequences.get(otherPlayer));
					}
					tmpNewUsefulSequences.put(otherPlayer, newSequencesForOpponentPlayer);

					tmpNewStatesMap.put(newState, tmpNewUsefulSequences);

					removeUtility(currentState);

					if (!getAllSequences().contains(newState.getSequenceFor(movingPlayer))) {
						newSequences.add(newState.getSequenceFor(movingPlayer));
					}

					addStateToSequenceForm(currentState);
					addStateToSequenceForm(newState);
					
					//newState.reverseAction();

				} // if the sequence is not consistent, we simply do not continue ...
				else {
//						System.err.print("");
//						currentState.checkConsistency(action);
				}
			}

			if (!tmpNewStatesMap.isEmpty()) {
				for (Entry<GameState, Map<Player, Set<Sequence>>> e : tmpNewStatesMap.entrySet()) {
					createRG(bestResponseAlgorithms, expander, searchingPlayer,
							opponentPlayer,new Pair<GameState, Map<Player, Set<Sequence>>>(e.getKey(), e.getValue()),currentSequences);
				}
			} else {
				if ((temporaryLeafs.contains(currentState)) || // it is already a temporary leaf
				(getInformationSetFor(currentState) != null && getInformationSetFor(currentState).getOutgoingSequences() != null && !getInformationSetFor(currentState).getOutgoingSequences().isEmpty())) { // it is a inner node in RG 
					// do nothing -- the node is already included in the restricted game
				} else { // otherwise create a new temporary leaf
					addStateToSequenceForm(currentState);
					int brPlayerIdx = gameInfo.getOpponent(currentState.getPlayerToMove()).getId();
					Double exactValue = bestResponseAlgorithms[brPlayerIdx].getCachedValueForState(currentState);
					if (exactValue == null) {
						System.out.println("IS null :/");
						exactValue = bestResponseAlgorithms[brPlayerIdx].calculateBRNoClear(currentState);
					}
					if (brPlayerIdx != 0)
						exactValue *= -1; // we are storing the utility reward for the first player
					Double oppRP = bestResponseAlgorithms[brPlayerIdx].getOpponentRealizationPlan().get(currentState.getHistory().getSequenceOf(currentState.getPlayerToMove()));
					if (oppRP != null && oppRP > 0)
						exactValue = exactValue / oppRP;
					temporaryLeafs.add(currentState);
					addToTempLeafsForSeqComb(currentState);
					/* if (exactValue != 0) */setUtility(currentState, exactValue);
				}
			}

		} else
			assert false;
		return currentSequences;
	}
	
	@Override
	public void validateRestrictedGameStructure(Expander<? extends DoubleOracleInformationSet> expander, SQFBestResponseAlgorithm[] bestResponseAlgorithms) {
		Player player1 = rootState.getAllPlayers()[0];
		Player player2 = rootState.getAllPlayers()[1];

		HashSet<GameState> visitedStates = new HashSet<GameState>();
		HashSet<DoubleOracleInformationSet> visitedISs = new HashSet<DoubleOracleInformationSet>();

		LinkedList<Pair<GameState, Map<Player, Set<Sequence>>>> queue = new LinkedList<Pair<GameState, Map<Player, Set<Sequence>>>>();
		Map<Player, Set<Sequence>> tmpMap = new HashMap<Player, Set<Sequence>>();
		tmpMap.put(player1, fullBRSequences.get(player1));
		tmpMap.put(player2, fullBRSequences.get(player2));
		queue.add(new Pair<GameState, Map<Player, Set<Sequence>>>(rootState, tmpMap));

		while (!queue.isEmpty()) {
			Pair<GameState, Map<Player, Set<Sequence>>> currentTuple = queue.pollFirst();
			GameState currentState = currentTuple.getLeft();

			if (!currentState.isPlayerToMoveNature()) {
				visitedStates.add(currentState);
				assert (getAllInformationSets().containsKey(currentState.getISKeyForPlayerToMove()));
				visitedISs.add(getAllInformationSets().get(currentState.getISKeyForPlayerToMove()));
			}

			// terminal state
			if (currentState.isGameEnd()) {
				assert (getInformationSetFor(currentState).getOutgoingSequences().size() == 0);
				Double utRes = getActualNonzeroUtilityValues(currentState);
				if (utRes == null) {
					assert ((currentState.getNatureProbability() * currentState.getUtilities()[0]) == 0);
				} else
					assert (utRes == currentState.getNatureProbability() * currentState.getUtilities()[0]);
				continue;
			}

			Map<Player, Set<Sequence>> currentSequences = currentTuple.getRight();

			if (currentState.isPlayerToMoveNature()) { // nature moving
				assert (!temporaryLeafs.contains(currentState));
				List<Action> natureMoves = expander.getActions(currentState);
				for (Action action : natureMoves) {
					GameState natureTempState = currentState.performAction(action);

					Map<Player, Set<Sequence>> newCurrentSequences = new FixedSizeMap<Player, Set<Sequence>>(2);
					newCurrentSequences.put(player1, (HashSet<Sequence>) ((HashSet<Sequence>) currentSequences.get(player1)).clone());
					newCurrentSequences.put(player2, (HashSet<Sequence>) ((HashSet<Sequence>) currentSequences.get(player2)).clone());

					queue.add(new Pair<GameState, Map<Player, Set<Sequence>>>(natureTempState, newCurrentSequences));
				}
			} else if (currentState.getPlayerToMove().equals(player1) || currentState.getPlayerToMove().equals(player2)) {
				Player movingPlayer = currentState.getPlayerToMove();
				Player otherPlayer = gameInfo.getOpponent(movingPlayer);

				Map<GameState, Map<Player, Set<Sequence>>> tmpNewStatesMap = new HashMap<GameState, Map<Player, Set<Sequence>>>();

				for (Sequence s : currentSequences.get(movingPlayer)) {
					if (s.size() == 0)
						continue;
					Action action = s.getFirst();
					if (currentState.checkConsistency(action)) {
						GameState newState = currentState.performAction(action);
						Map<Player, Set<Sequence>> tmpNewUsefulSequences = tmpNewStatesMap.get(newState);
						if (tmpNewUsefulSequences == null) {
							tmpNewUsefulSequences = new FixedSizeMap<Player, Set<Sequence>>(2);
						}

						Set<Sequence> newSequencesForMovingPlayer = tmpNewUsefulSequences.get(movingPlayer);
						if (newSequencesForMovingPlayer == null)
							newSequencesForMovingPlayer = new HashSet<Sequence>();
						newSequencesForMovingPlayer.add(s.getSubSequence(1, s.size() - 1));
						tmpNewUsefulSequences.put(movingPlayer, newSequencesForMovingPlayer);

						Set<Sequence> newSequencesForOpponentPlayer = tmpNewUsefulSequences.get(otherPlayer);
						if (newSequencesForOpponentPlayer == null) {
							newSequencesForOpponentPlayer = new HashSet<Sequence>();
							newSequencesForOpponentPlayer.addAll(currentSequences.get(otherPlayer));
						}
						tmpNewUsefulSequences.put(otherPlayer, newSequencesForOpponentPlayer);

						tmpNewStatesMap.put(newState, tmpNewUsefulSequences);
						assert (!temporaryLeafs.contains(currentState));
						assert (getActualNonzeroUtilityValues(currentState) == null);
						assert (getAllSequences().contains(currentState.getSequenceFor(movingPlayer)));
						assert (getAllSequences().contains(currentState.getSequenceFor(otherPlayer)));
						assert (getAllInformationSets().containsKey(currentState.getISKeyForPlayerToMove()));
						assert (getAllInformationSets().get(currentState.getISKeyForPlayerToMove()).getAllStates().contains(currentState));
						assert (getInformationSetFor(currentState).getOutgoingSequences().size() > 0);

					} // if the sequence is not consistent, we simply do not continue ...
					else {
						System.err.print("");
						currentState.checkConsistency(action);
					}
				}
				if (!tmpNewStatesMap.isEmpty()) {
					for (Entry<GameState, Map<Player, Set<Sequence>>> e : tmpNewStatesMap.entrySet()) {
						queue.add(new Pair<GameState, Map<Player, Set<Sequence>>>(e.getKey(), e.getValue()));
					}
				} else {
					assert (temporaryLeafs.contains(currentState));
					double exactValue = bestResponseAlgorithms[gameInfo.getOpponent(currentState.getPlayerToMove()).getId()].calculateBRNoClear(currentState);
					Double oppRP = bestResponseAlgorithms[gameInfo.getOpponent(currentState.getPlayerToMove()).getId()].getOpponentRealizationPlan().get(currentState.getHistory().getSequenceOf(currentState.getPlayerToMove()));
					if (oppRP != null && oppRP > 0)
						exactValue = exactValue / oppRP;
					if (gameInfo.getOpponent(currentState.getPlayerToMove()).getId() != 0)
						exactValue *= -1; // we are storing the utility reward for the first player
					if (exactValue == 0) {
						assert (getActualNonzeroUtilityValues(currentState) == null);
					} else {
						bestResponseAlgorithms[gameInfo.getOpponent(currentState.getPlayerToMove()).getId()].calculateBRNoClear(currentState);
//                        assert (Math.abs(getActualNonzeroUtilityValues(currentState) - exactValue) < 0.00001);
					}
					assert (getInformationSetFor(currentState).getOutgoingSequences().size() == 0);
				}
			}
		}
		assert (visitedISs.size() == getAllInformationSets().values().size());

		HashSet<Sequence> rqSequences = new HashSet<Sequence>();
		for (GameState state : visitedStates) {
			rqSequences.add(state.getSequenceFor(player1));
			rqSequences.add(state.getSequenceFor(player2));
		}
		assert (getAllSequences().size() == rqSequences.size());

	}

}
