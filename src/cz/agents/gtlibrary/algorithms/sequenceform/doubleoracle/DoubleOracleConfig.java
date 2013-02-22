package cz.agents.gtlibrary.algorithms.sequenceform.doubleoracle;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cz.agents.gtlibrary.algorithms.sequenceform.SQFBestResponseAlgorithm;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceFormConfig;
import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
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
public class DoubleOracleConfig<I extends SequenceInformationSet> extends SequenceFormConfig<I> {

	private GameState rootState;
	private GameInfo gameInfo;	

	private Map<Player, Set<Sequence>> fullBRSequences = new HashMap<Player, Set<Sequence>>();
	private Set<GameState> temporaryLeafs = new HashSet<GameState>();
	
	public DoubleOracleConfig(GameState rootState, GameInfo gameInfo) {
		this.rootState = rootState;
		this.gameInfo = gameInfo;
	}
	
	@Override
	public I getInformationSetFor(GameState gameState) {		
		return super.getInformationSetFor(gameState);		
	}
	
	public void addFullBRSequneces(Player player, Set<Sequence> newBRSequences) {
		Set<Sequence> currentPlayerFullBRSet = fullBRSequences.get(player);
		if (currentPlayerFullBRSet == null) {
			currentPlayerFullBRSet = new HashSet<Sequence>();
		}
		currentPlayerFullBRSet.addAll(newBRSequences);
	}
	
	public void createValidRestrictedGame(Player addingPlayer, HashSet<Sequence> newBRSequences, SQFBestResponseAlgorithm addingPlayerBR, Expander<DoubleOracleInformationSet> expander) {
		
		Player searchingPlayer = addingPlayer;
		Player opponentPlayer = gameInfo.getOpponent(addingPlayer);

		LinkedList<Pair<GameState, Map<Player, Set<Sequence>>>> queue = new LinkedList<Pair<GameState,Map<Player,Set<Sequence>>>>();
		Map<Player, Set<Sequence>> tmpMap = new HashMap<Player, Set<Sequence>>();
		tmpMap.put(searchingPlayer, newBRSequences);
		tmpMap.put(opponentPlayer, fullBRSequences.get(opponentPlayer));
		queue.add(new Pair<GameState, Map<Player, Set<Sequence>>>(rootState, tmpMap));
		
		while (!queue.isEmpty()) {
			Pair<GameState, Map<Player, Set<Sequence>>> currentTuple = queue.pollFirst();
			GameState currentState = currentTuple.getLeft();

			// terminal state			
			if (currentState.isGameEnd()) {
				Double utRes = getActualNonzeroUtilityValues(currentState);
				if (utRes == null) {					
					setUtility(currentState); // utility caching
				} else assert (utRes == currentState.getNatureProbability() * currentState.getUtilities()[0]);
				continue;
			}
			
			Map<Player, Set<Sequence>> currentSequences = currentTuple.getRight();

			// TODO the following test could probably be removed
			boolean isSomeUncheckedSequenceForThisState = false;
			for (Sequence s : newBRSequences) {
				if (currentState.getHistory().getSequenceOf(searchingPlayer).isPrefixOf(s)) {
					isSomeUncheckedSequenceForThisState = true;
				}
			}			
			if (!isSomeUncheckedSequenceForThisState) {				
				continue;
			}
			// TODO 	

			if (currentState.isPlayerToMoveNature()) { // nature moving
				List<Action> natureMoves = expander.getActions(currentState);
				for (Action action : natureMoves) {
					GameState natureTempState = currentState.performAction(action);

					Map<Player, Set<Sequence>> newCurrentSequences = new FixedSizeMap<Player, Set<Sequence>>(2);
					newCurrentSequences.put(searchingPlayer, (HashSet<Sequence>)((HashSet<Sequence>)currentSequences.get(searchingPlayer)).clone());
					newCurrentSequences.put(opponentPlayer, (HashSet<Sequence>)((HashSet<Sequence>)currentSequences.get(opponentPlayer)).clone());

					queue.add(new Pair<GameState, Map<Player, Set<Sequence>>>(natureTempState, newCurrentSequences));
				}
			} else if (currentState.getPlayerToMove().equals(searchingPlayer) || currentState.getPlayerToMove().equals(opponentPlayer)) {
				Player movingPlayer = currentState.getPlayerToMove();
				Player otherPlayer = gameInfo.getOpponent(movingPlayer);

				Map<GameState, Map<Player, Set<Sequence>>> tmpNewStatesMap = new HashMap<GameState, Map<Player,Set<Sequence>>>();

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

						removeUtility(currentState);
						addStateToSequenceForm(newState);

					} // if the sequence is not consistent, we simply do not continue ...
				}

				if (!tmpNewStatesMap.isEmpty()) {
					for (Entry<GameState, Map<Player, Set<Sequence>>> e : tmpNewStatesMap.entrySet()) {
						queue.add(new Pair<GameState, Map<Player, Set<Sequence>>>(e.getKey(), e.getValue()));
					}
				} else {
					assert (movingPlayer.equals(opponentPlayer));
					if ((temporaryLeafs.contains(currentState)) || // it is already a temporary leaf
							(getInformationSetFor(currentState) != null && getInformationSetFor(currentState).getOutgoingSequences() != null && !getInformationSetFor(currentState).getOutgoingSequences().isEmpty())){ // it is a inner node in RG 
						// do nothing -- the node is already included in the restricted game
					} else { // otherwise create a new temporary leaf
						Double exactValue = addingPlayerBR.getCachedValueForState(currentState);						
						if (exactValue == null) {
						  exactValue = addingPlayerBR.calculateBRNoClear(currentState);
						  if (searchingPlayer.getId()  != 0) exactValue *= -1; // we are storing the utility value for the first player
						}
						temporaryLeafs.add(currentState);
						setUtility(currentState, exactValue);
					}
				}

			} else
				assert false;
		}
	}
	
}
