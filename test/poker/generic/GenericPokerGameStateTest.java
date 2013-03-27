package poker.generic;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerAction;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.iinodes.LinkedListSequenceImpl;
import cz.agents.gtlibrary.interfaces.Player;
import cz.agents.gtlibrary.interfaces.Sequence;

public class GenericPokerGameStateTest {

	@Test
	public void executeActionsOnThisStateAfterBetTest() {

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {

			GenericPokerGameState state = new GenericPokerGameState();
			GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
			GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
			Map<Player, Sequence> history = new HashMap<Player, Sequence>();
			Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
			Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
			Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

			
			natureSequence.addLast(firstPlayerCard);
			natureSequence.addLast(secondPlayerCard);

			history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
			history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
			history.put(GPGameInfo.NATURE, natureSequence);

			state.performActionModifyingThisState(firstPlayerCard);
			state.performActionModifyingThisState(secondPlayerCard);
			
			GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);
			
			state.performActionModifyingThisState(action1);
			firstPlayerSequence.addLast(action1);

			assertEquals(2 * GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i], state.getPot());
			assertEquals(GPGameInfo.ANTE, state.getGainForFirstPlayer());
			assertEquals(1, state.getRound());
			assertEquals(false, state.isGameEnd());
			assertEquals(GPGameInfo.SECOND_PLAYER, state.getPlayerToMove());
			assertEquals(0, state.getContinuousRaiseCount());
			assertEquals(history, state.getHistory().getSequencesOfPlayers());
			assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
		}
	}

	@Test
	public void executeActionsOnThisStateAfterBetCallTest() {

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {

			GenericPokerGameState state = new GenericPokerGameState();
			GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
			GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
			Map<Player, Sequence> history = new HashMap<Player, Sequence>();
			Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
			Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
			Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

			natureSequence.addLast(firstPlayerCard);
			natureSequence.addLast(secondPlayerCard);

			history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
			history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
			history.put(GPGameInfo.NATURE, natureSequence);

			state.performActionModifyingThisState(firstPlayerCard);
			state.performActionModifyingThisState(secondPlayerCard);
			
			GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);
			
			state.performActionModifyingThisState(action1);
			
			GenericPokerAction action2 = new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);
			
			state.performActionModifyingThisState(action2);
			firstPlayerSequence.addLast(action1);
			secondPlayerSequence.addLast(action2);

			assertEquals(2 * GPGameInfo.ANTE + 2 * GPGameInfo.BETS_FIRST_ROUND[i], state.getPot());
			assertEquals(GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i], state.getGainForFirstPlayer());
			assertEquals(2, state.getRound());
			assertEquals(false, state.isGameEnd());
			assertEquals(GPGameInfo.NATURE, state.getPlayerToMove());
			assertEquals(0, state.getContinuousRaiseCount());
			assertEquals(history, state.getHistory().getSequencesOfPlayers());
			assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
		}
	}

	@Test
	public void executeActionsOnThisStateAfterBetRaiseTest() {

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
			for (int j = 0; j < GPGameInfo.RAISES_FIRST_ROUND.length; j++) {

				GenericPokerGameState state = new GenericPokerGameState();
				GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
				GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
				Map<Player, Sequence> history = new HashMap<Player, Sequence>();
				Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
				Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
				Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

				natureSequence.addLast(firstPlayerCard);
				natureSequence.addLast(secondPlayerCard);

				history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
				history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
				history.put(GPGameInfo.NATURE, natureSequence);

				state.performActionModifyingThisState(firstPlayerCard);
				state.performActionModifyingThisState(secondPlayerCard);
				
				GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);
				
				state.performActionModifyingThisState(action1);
				
				GenericPokerAction action2 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[j]);
				
				state.performActionModifyingThisState(action2);
				firstPlayerSequence.addLast(action1);
				secondPlayerSequence.addLast(action2);

				assertEquals(2 * GPGameInfo.ANTE + 2 * GPGameInfo.BETS_FIRST_ROUND[i] + GPGameInfo.RAISES_FIRST_ROUND[j], state.getPot());
				assertEquals(GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i] + GPGameInfo.RAISES_FIRST_ROUND[j], state.getGainForFirstPlayer());
				assertEquals(1, state.getRound());
				assertEquals(false, state.isGameEnd());
				assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
				assertEquals(1, state.getContinuousRaiseCount());
				assertEquals(history, state.getHistory().getSequencesOfPlayers());
				assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
			}
		}
	}

	@Test
	public void executeActionsOnThisStateAfterBetCallCardTest() {

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
			for (int j = 0; j < GPGameInfo.CARD_TYPES.length; j++) {

				GenericPokerGameState state = new GenericPokerGameState();
				GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
				GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
				GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
				Map<Player, Sequence> history = new HashMap<Player, Sequence>();
				Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
				Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
				Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

				natureSequence.addLast(firstPlayerCard);
				natureSequence.addLast(secondPlayerCard);
				natureSequence.addLast(tableCard);

				history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
				history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
				history.put(GPGameInfo.NATURE, natureSequence);

				state.performActionModifyingThisState(firstPlayerCard);
				state.performActionModifyingThisState(secondPlayerCard);
				
				GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);
				
				state.performActionModifyingThisState(action1);
				
				GenericPokerAction action2 = new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);
				
				state.performActionModifyingThisState(action2);
				state.performActionModifyingThisState(tableCard);
				firstPlayerSequence.addLast(action1);
				secondPlayerSequence.addLast(action2);

				assertEquals(2 * GPGameInfo.ANTE + 2 * GPGameInfo.BETS_FIRST_ROUND[i], state.getPot());
				assertEquals(GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i], state.getGainForFirstPlayer());
				assertEquals(3, state.getRound());
				assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[0]), state.getTable().getActionType());
				assertEquals(false, state.isGameEnd());
				assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
				assertEquals(0, state.getContinuousRaiseCount());
				assertEquals(history, state.getHistory().getSequencesOfPlayers());
				assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
				assertEquals(0, state.getProbabilityOfNatureFor(firstPlayerCard), 0.0000000001);
				assertEquals(1./3, state.getProbabilityOfNatureFor(secondPlayerCard), 0.0000000001);
				assertEquals(2./3, state.getProbabilityOfNatureFor(new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[2]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[2])), 0.0000000001);
			}
		}
	}

	@Test
	public void executeActionsOnThisStateAfterBetRaiseCallCardTest() {

		if (GPGameInfo.MAX_RAISES_IN_ROW < 1) {
			return;
		}

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
			for (int j = 0; j < GPGameInfo.CARD_TYPES.length; j++) {
				for (int k = 0; k < GPGameInfo.RAISES_FIRST_ROUND.length; k++) {

					GenericPokerGameState state = new GenericPokerGameState();
					GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
					GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
					GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[j]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[j]);
					Map<Player, Sequence> history = new HashMap<Player, Sequence>();
					Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
					Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
					Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

					natureSequence.addLast(firstPlayerCard);
					natureSequence.addLast(secondPlayerCard);
					natureSequence.addLast(tableCard);

					history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
					history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
					history.put(GPGameInfo.NATURE, natureSequence);

					state.performActionModifyingThisState(firstPlayerCard);
					state.performActionModifyingThisState(secondPlayerCard);
					
					GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);
					
					state.performActionModifyingThisState(action1);
					
					GenericPokerAction action2 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[k]);
					
					state.performActionModifyingThisState(action2);
					
					GenericPokerAction action3 = new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[k]);
					
					state.performActionModifyingThisState(action3);
					state.performActionModifyingThisState(tableCard);
					firstPlayerSequence.addLast(action1);
					firstPlayerSequence.addLast(action3);
					secondPlayerSequence.addLast(action2);

					assertEquals(2 * GPGameInfo.ANTE + 2 * GPGameInfo.BETS_FIRST_ROUND[i] + 2 * GPGameInfo.RAISES_FIRST_ROUND[k], state.getPot());
					assertEquals(GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i] + GPGameInfo.RAISES_FIRST_ROUND[k], state.getGainForFirstPlayer());
					assertEquals(3, state.getRound());
					assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[j]), state.getTable().getActionType());
					assertEquals(false, state.isGameEnd());
					assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
					assertEquals(0, state.getContinuousRaiseCount());
					assertEquals(history, state.getHistory().getSequencesOfPlayers());
					assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
				}
			}
		}
	}

	@Test
	public void executeActionsOnThisStateAfterBetRaiseRaiseCallCardTest() {

		if (GPGameInfo.MAX_RAISES_IN_ROW < 2) {
			return;
		}

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
			for (int j = 0; j < GPGameInfo.CARD_TYPES.length; j++) {
				for (int k = 0; k < GPGameInfo.RAISES_FIRST_ROUND.length; k++) {
					for (int l = 0; l < GPGameInfo.RAISES_FIRST_ROUND.length; l++) {

						GenericPokerGameState state = new GenericPokerGameState();
						GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
						GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
						GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[j]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[j]);
						Map<Player, Sequence> history = new HashMap<Player, Sequence>();
						Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
						Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
						Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

						natureSequence.addLast(firstPlayerCard);
						natureSequence.addLast(secondPlayerCard);
						natureSequence.addLast(tableCard);

						history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
						history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
						history.put(GPGameInfo.NATURE, natureSequence);

						state.performActionModifyingThisState(firstPlayerCard);
						state.performActionModifyingThisState(secondPlayerCard);
						GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);

						state.performActionModifyingThisState(action1);
						GenericPokerAction action2 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[k]);

						state.performActionModifyingThisState(action2);
						GenericPokerAction action3 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[l]);

						state.performActionModifyingThisState(action3);
						GenericPokerAction action4 = new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[l]);
						
						state.performActionModifyingThisState(action4);
						state.performActionModifyingThisState(tableCard);
						firstPlayerSequence.addLast(action1);
						firstPlayerSequence.addLast(action3);
						secondPlayerSequence.addLast(action2);
						secondPlayerSequence.addLast(action4);

						assertEquals(2 * GPGameInfo.ANTE + 2 * GPGameInfo.BETS_FIRST_ROUND[i] + 2 * GPGameInfo.RAISES_FIRST_ROUND[k] + 2 * GPGameInfo.RAISES_FIRST_ROUND[l], state.getPot());
						assertEquals(GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i] + GPGameInfo.RAISES_FIRST_ROUND[k] + GPGameInfo.RAISES_FIRST_ROUND[l], state.getGainForFirstPlayer());
						assertEquals(3, state.getRound());
						assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[j]), state.getTable().getActionType());
						assertEquals(false, state.isGameEnd());
						assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
						assertEquals(0, state.getContinuousRaiseCount());
						assertEquals(history, state.getHistory().getSequencesOfPlayers());
						assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
					}
				}
			}
		}
	}

	@Test
	public void executeActionsOnThisStateAfterBetRaiseRaiseCallTest() {

		if (GPGameInfo.MAX_RAISES_IN_ROW < 2) {
			return;
		}

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
			for (int j = 0; j < GPGameInfo.CARD_TYPES.length; j++) {
				for (int k = 0; k < GPGameInfo.RAISES_FIRST_ROUND.length; k++) {
					for (int l = 0; l < GPGameInfo.RAISES_FIRST_ROUND.length; l++) {

						GenericPokerGameState state = new GenericPokerGameState();
						GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
						GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
						Map<Player, Sequence> history = new HashMap<Player, Sequence>();
						Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
						Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
						Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

						natureSequence.addLast(firstPlayerCard);
						natureSequence.addLast(secondPlayerCard);

						history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
						history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
						history.put(GPGameInfo.NATURE, natureSequence);

						state.performActionModifyingThisState(firstPlayerCard);
						state.performActionModifyingThisState(secondPlayerCard);
						GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);

						state.performActionModifyingThisState(action1);
						GenericPokerAction action2 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[k]);
						
						state.performActionModifyingThisState(action2);
						GenericPokerAction action3 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[l]);
						
						state.performActionModifyingThisState(action3);
						firstPlayerSequence.addLast(action1);
						firstPlayerSequence.addLast(action3);
						secondPlayerSequence.addLast(action2);

						assertEquals(2 * GPGameInfo.ANTE + 2 * GPGameInfo.BETS_FIRST_ROUND[i] + 2 * GPGameInfo.RAISES_FIRST_ROUND[k] + GPGameInfo.RAISES_FIRST_ROUND[l], state.getPot());
						assertEquals(GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i] + GPGameInfo.RAISES_FIRST_ROUND[k], state.getGainForFirstPlayer());
						assertEquals(1, state.getRound());
						assertEquals(false, state.isGameEnd());
						assertEquals(GPGameInfo.SECOND_PLAYER, state.getPlayerToMove());
						assertEquals(2, state.getContinuousRaiseCount());
						assertEquals(history, state.getHistory().getSequencesOfPlayers());
						assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
					}
				}
			}
		}
	}

	@Test
	public void executeActionsOnThisStateAfterBetRaiseRaiseRaiseCallCardTest() {

		if (GPGameInfo.MAX_RAISES_IN_ROW < 3) {
			return;
		}

		for (int i = 0; i < GPGameInfo.BETS_FIRST_ROUND.length; i++) {
			for (int j = 0; j < GPGameInfo.CARD_TYPES.length; j++) {
				for (int k = 0; k < GPGameInfo.RAISES_FIRST_ROUND.length; k++) {
					for (int l = 0; l < GPGameInfo.RAISES_FIRST_ROUND.length; l++) {
						for (int m = 0; m < GPGameInfo.RAISES_FIRST_ROUND.length; m++) {

							GenericPokerGameState state = new GenericPokerGameState();
							GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
							GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
							GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[j]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[j]);
							Map<Player, Sequence> history = new HashMap<Player, Sequence>();
							Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
							Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
							Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

							natureSequence.addLast(firstPlayerCard);
							natureSequence.addLast(secondPlayerCard);
							natureSequence.addLast(tableCard);

							history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
							history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
							history.put(GPGameInfo.NATURE, natureSequence);

							state.performActionModifyingThisState(firstPlayerCard);
							state.performActionModifyingThisState(secondPlayerCard);
							GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]);

							state.performActionModifyingThisState(action1);
							GenericPokerAction action2 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[k]);

							state.performActionModifyingThisState(action2);
							GenericPokerAction action3 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[l]);

							state.performActionModifyingThisState(action3);
							GenericPokerAction action4 = new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[m]);
							
							state.performActionModifyingThisState(action4);
							GenericPokerAction action5 = new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[m]);
							
							state.performActionModifyingThisState(action5);
							state.performActionModifyingThisState(tableCard);
							firstPlayerSequence.addLast(action1);
							firstPlayerSequence.addLast(action3);
							firstPlayerSequence.addLast(action5);
							secondPlayerSequence.addLast(action2);
							secondPlayerSequence.addLast(action4);

							assertEquals(2 * GPGameInfo.ANTE + 2 * GPGameInfo.BETS_FIRST_ROUND[i] + 2 * GPGameInfo.RAISES_FIRST_ROUND[k] + 2 * GPGameInfo.RAISES_FIRST_ROUND[l] + 2 * GPGameInfo.RAISES_FIRST_ROUND[m], state.getPot());
							assertEquals(GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[i] + GPGameInfo.RAISES_FIRST_ROUND[k] + GPGameInfo.RAISES_FIRST_ROUND[l] + GPGameInfo.RAISES_FIRST_ROUND[m], state.getGainForFirstPlayer());
							assertEquals(3, state.getRound());
							assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[j]), state.getTable().getActionType());
							assertEquals(false, state.isGameEnd());
							assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
							assertEquals(0, state.getContinuousRaiseCount());
							assertEquals(history, state.getHistory().getSequencesOfPlayers());
							assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
						}
					}
				}
			}
		}
	}

	@Test
	public void executeActionsOnThisStateAfterCheckCheckCardCheckCheckTest() {

		GenericPokerGameState state = new GenericPokerGameState();
		GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
		GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		Map<Player, Sequence> history = new HashMap<Player, Sequence>();
		Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
		Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
		Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

		natureSequence.addLast(firstPlayerCard);
		natureSequence.addLast(secondPlayerCard);
		natureSequence.addLast(tableCard);

		history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
		history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
		history.put(GPGameInfo.NATURE, natureSequence);

		state.performActionModifyingThisState(firstPlayerCard);
		state.performActionModifyingThisState(secondPlayerCard);
		GenericPokerAction action1 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);

		state.performActionModifyingThisState(action1);
		GenericPokerAction action2 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);

		state.performActionModifyingThisState(action2);
		state.performActionModifyingThisState(tableCard);
		GenericPokerAction action3 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);
		
		state.performActionModifyingThisState(action3);
		GenericPokerAction action4 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);
		
		state.performActionModifyingThisState(action4);
		firstPlayerSequence.addLast(action1);
		firstPlayerSequence.addLast(action3);
		secondPlayerSequence.addLast(action2);
		secondPlayerSequence.addLast(action4);

		assertEquals(2 * GPGameInfo.ANTE, state.getPot());
		assertEquals(GPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(4, state.getRound());
		assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[0]), state.getTable().getActionType());
		assertEquals(true, state.isGameEnd());
		assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(0, state.getContinuousRaiseCount());
		assertEquals(history, state.getHistory().getSequencesOfPlayers());
		assertArrayEquals(new double[] { GPGameInfo.ANTE, -GPGameInfo.ANTE, 0 }, state.getUtilities(), 0.00001);
	}

	@Test
	public void executeActionsOnThisStateAfterCheckCheckCardCheckCheckTestWithSwitchedCards() {

		GenericPokerGameState state = new GenericPokerGameState();
		GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
		GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		Map<Player, Sequence> history = new HashMap<Player, Sequence>();
		Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
		Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
		Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

		natureSequence.addLast(firstPlayerCard);
		natureSequence.addLast(secondPlayerCard);
		natureSequence.addLast(tableCard);

		history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
		history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
		history.put(GPGameInfo.NATURE, natureSequence);

		state.performActionModifyingThisState(firstPlayerCard);
		state.performActionModifyingThisState(secondPlayerCard);
		GenericPokerAction action1 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);

		state.performActionModifyingThisState(action1);
		GenericPokerAction action2 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);

		state.performActionModifyingThisState(action2);
		state.performActionModifyingThisState(tableCard);
		GenericPokerAction action3 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);

		state.performActionModifyingThisState(action3);
		GenericPokerAction action4 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);
		
		state.performActionModifyingThisState(action4);
		firstPlayerSequence.addLast(action1);
		firstPlayerSequence.addLast(action3);
		secondPlayerSequence.addLast(action2);
		secondPlayerSequence.addLast(action4);

		assertEquals(2 * GPGameInfo.ANTE, state.getPot());
		assertEquals(GPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(4, state.getRound());
		assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[0]), state.getTable().getActionType());
		assertEquals(true, state.isGameEnd());
		assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(0, state.getContinuousRaiseCount());
		assertEquals(history, state.getHistory().getSequencesOfPlayers());
		assertArrayEquals(new double[] { -GPGameInfo.ANTE, GPGameInfo.ANTE, 0 }, state.getUtilities(), 0.00001);
	}

	@Test
	public void executeActionsOnThisStateAfterBetFoldTest() {

		GenericPokerGameState state = new GenericPokerGameState();
		GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
		GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		Map<Player, Sequence> history = new HashMap<Player, Sequence>();
		Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
		Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
		Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);
;
		natureSequence.addLast(firstPlayerCard);
		natureSequence.addLast(secondPlayerCard);

		history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
		history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
		history.put(GPGameInfo.NATURE, natureSequence);

		state.performActionModifyingThisState(firstPlayerCard);
		state.performActionModifyingThisState(secondPlayerCard);
		GenericPokerAction action1 = new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[0]);

		state.performActionModifyingThisState(action1);
		GenericPokerAction action2 = new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);
		
		state.performActionModifyingThisState(action2);
		firstPlayerSequence.addLast(action1);
		secondPlayerSequence.addLast(action2);

		assertEquals(2 * GPGameInfo.ANTE + GPGameInfo.BETS_FIRST_ROUND[0], state.getPot());
		assertEquals(GPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(4, state.getRound());
		assertEquals(true, state.isGameEnd());
		assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(0, state.getContinuousRaiseCount());
		assertEquals(history, state.getHistory().getSequencesOfPlayers());
		assertArrayEquals(new double[] { GPGameInfo.ANTE, -GPGameInfo.ANTE, 0 }, state.getUtilities(), 0.00001);
	}

	@Test
	public void executeActionsOnThisStateAfterCheckCheckCardCheckCheckTestWithSameCards() {

		GenericPokerGameState state = new GenericPokerGameState();
		GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		Map<Player, Sequence> history = new HashMap<Player, Sequence>();
		Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
		Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
		Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

		natureSequence.addLast(firstPlayerCard);
		natureSequence.addLast(secondPlayerCard);
		natureSequence.addLast(tableCard);

		history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
		history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
		history.put(GPGameInfo.NATURE, natureSequence);

		state.performActionModifyingThisState(firstPlayerCard);
		state.performActionModifyingThisState(secondPlayerCard);
		GenericPokerAction action1 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);

		state.performActionModifyingThisState(action1);
		GenericPokerAction action2 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);

		state.performActionModifyingThisState(action2);
		state.performActionModifyingThisState(tableCard);
		GenericPokerAction action3 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);

		state.performActionModifyingThisState(action3);
		GenericPokerAction action4 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);
		
		state.performActionModifyingThisState(action4);
		firstPlayerSequence.addLast(action1);
		firstPlayerSequence.addLast(action3);
		secondPlayerSequence.addLast(action2);
		secondPlayerSequence.addLast(action4);

		assertEquals(2 * GPGameInfo.ANTE, state.getPot());
		assertEquals(GPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(4, state.getRound());
		assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[1]), state.getTable().getActionType());
		assertEquals(true, state.isGameEnd());
		assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(0, state.getContinuousRaiseCount());
		assertEquals(history, state.getHistory().getSequencesOfPlayers());
		assertArrayEquals(new double[] { 0, 0, 0 }, state.getUtilities(), 0.00001);
	}

	@Test
	public void executeActionsOnThisStateAfterCheckCheckCardCheckCheckTestWithSameCardsAndDifferentTable() {

		GenericPokerGameState state = new GenericPokerGameState();
		GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[0]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[0]);
		GenericPokerAction tableCard = new GenericPokerAction(String.valueOf(GPGameInfo.CARD_TYPES[1]), null, GPGameInfo.NATURE, GPGameInfo.CARD_TYPES[1]);
		Map<Player, Sequence> history = new HashMap<Player, Sequence>();
		Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
		Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
		Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

		natureSequence.addLast(firstPlayerCard);
		natureSequence.addLast(secondPlayerCard);
		natureSequence.addLast(tableCard);

		history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
		history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
		history.put(GPGameInfo.NATURE, natureSequence);

		state.performActionModifyingThisState(firstPlayerCard);
		state.performActionModifyingThisState(secondPlayerCard);
		GenericPokerAction action1 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);

		state.performActionModifyingThisState(action1);
		GenericPokerAction action2 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);

		state.performActionModifyingThisState(action2);
		state.performActionModifyingThisState(tableCard);
		GenericPokerAction action3 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);

		state.performActionModifyingThisState(action3);
		GenericPokerAction action4 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);
		
		state.performActionModifyingThisState(action4);
		firstPlayerSequence.addLast(action1);
		firstPlayerSequence.addLast(action3);
		secondPlayerSequence.addLast(action2);
		secondPlayerSequence.addLast(action4);

		assertEquals(2 * GPGameInfo.ANTE, state.getPot());
		assertEquals(GPGameInfo.ANTE, state.getGainForFirstPlayer());
		assertEquals(4, state.getRound());
		assertEquals(String.valueOf(GPGameInfo.CARD_TYPES[1]), state.getTable().getActionType());
		assertEquals(true, state.isGameEnd());
		assertEquals(GPGameInfo.FIRST_PLAYER, state.getPlayerToMove());
		assertEquals(0, state.getContinuousRaiseCount());
		assertEquals(history, state.getHistory().getSequencesOfPlayers());
		assertArrayEquals(new double[] { 0, 0, 0 }, state.getUtilities(), 0.00001);
	}

	@Test
	public void executeActionsOnThisStateAfterCheckCheck() {

		for (int i = 0; i < GPGameInfo.DECK.length; i++) {
			for (int j = 0; j < GPGameInfo.DECK.length; j++) {
				if (i != j) {
					GenericPokerGameState state = new GenericPokerGameState();
					GenericPokerAction firstPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.DECK[j]), null, GPGameInfo.NATURE, GPGameInfo.DECK[j]);
					GenericPokerAction secondPlayerCard = new GenericPokerAction(String.valueOf(GPGameInfo.DECK[i]), null, GPGameInfo.NATURE, GPGameInfo.DECK[i]);
					Map<Player, Sequence> history = new HashMap<Player, Sequence>();
					Sequence firstPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.FIRST_PLAYER);
					Sequence secondPlayerSequence = new LinkedListSequenceImpl(GPGameInfo.SECOND_PLAYER);
					Sequence natureSequence = new LinkedListSequenceImpl(GPGameInfo.NATURE);

					natureSequence.addLast(firstPlayerCard);
					natureSequence.addLast(secondPlayerCard);

					history.put(GPGameInfo.FIRST_PLAYER, firstPlayerSequence);
					history.put(GPGameInfo.SECOND_PLAYER, secondPlayerSequence);
					history.put(GPGameInfo.NATURE, natureSequence);

					state.performActionModifyingThisState(firstPlayerCard);
					state.performActionModifyingThisState(secondPlayerCard);
					GenericPokerAction action1 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0);
					
					state.performActionModifyingThisState(action1);
					GenericPokerAction action2 = new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0);
					
					state.performActionModifyingThisState(action2);
					firstPlayerSequence.addLast(action1);
					secondPlayerSequence.addLast(action2);

					assertEquals(2 * GPGameInfo.ANTE, state.getPot());
					assertEquals(GPGameInfo.ANTE, state.getGainForFirstPlayer());
					assertEquals(2, state.getRound());
					assertEquals(false, state.isGameEnd());
					assertEquals(GPGameInfo.NATURE, state.getPlayerToMove());
					assertEquals(0, state.getContinuousRaiseCount());
					assertEquals(history, state.getHistory().getSequencesOfPlayers());
					assertArrayEquals(new double[] { 0 }, state.getUtilities(), 0.00001);
				}
			}
		}

	}
	
}
