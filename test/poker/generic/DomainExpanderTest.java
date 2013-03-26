package poker.generic;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import cz.agents.gtlibrary.algorithms.sequenceform.SequenceInformationSet;
import cz.agents.gtlibrary.domain.poker.generic.GPGameInfo;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerAction;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerExpanderDomain;
import cz.agents.gtlibrary.domain.poker.generic.GenericPokerGameState;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.Expander;

public class DomainExpanderTest {

	@Test
	public void beginningOfTheGameTest() {

		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		for (int cardValue : GPGameInfo.CARD_TYPES) {
			expectedActions.add(new GenericPokerAction(String.valueOf(cardValue), new SequenceInformationSet(state), GPGameInfo.NATURE, cardValue));
		}

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterCardsTest() {

		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		
		expectedActions.add(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			expectedActions.add(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));
		}

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterBetInFirstRoundTest() {

		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			GenericPokerGameState state = new GenericPokerGameState();
			Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
			List<Action> expectedActions = new LinkedList<Action>();

			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));

			expectedActions.add(new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, betValue));
			expectedActions.add(new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));

			if (GPGameInfo.MAX_RAISES_IN_ROW > 0)
				for (int raiseValue : GPGameInfo.RAISES_FIRST_ROUND) {
					expectedActions.add(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, raiseValue));
				}

			assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
		}
	}

	@Test
	public void afterCheckInFirstRoundTest() {

		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

		expectedActions.add(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
		
		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			expectedActions.add(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, betValue));
		}

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterRaiseInFirstRoundTest() {

		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			for (int raiseValue : GPGameInfo.RAISES_FIRST_ROUND) {

				GenericPokerGameState state = new GenericPokerGameState();
				Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
				List<Action> expectedActions = new LinkedList<Action>();

				state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
				state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
				state.performActionModifyingThisState(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));
				state.performActionModifyingThisState(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, raiseValue));

				expectedActions.add(new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, raiseValue));
				expectedActions.add(new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

				if (GPGameInfo.MAX_RAISES_IN_ROW > 1) {
					for (int raiseValue1 : GPGameInfo.RAISES_FIRST_ROUND) {
						expectedActions.add(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, raiseValue1));
					}
				}

				assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
			}
		}
	}

	@Test
	public void afterFlopTest() {

		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
		state.performActionModifyingThisState(new GenericPokerAction("1", null, GPGameInfo.NATURE, 0));

		expectedActions.add(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));
		
		for (int betValue : GPGameInfo.BETS_SECOND_ROUND) {
			expectedActions.add(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));
		}

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}
	
	@Test
	public void afterFlopTestWithPair() {

		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));

		for (int i = GPGameInfo.BETS_SECOND_ROUND.length - 1; i >= 0; i--) {
			expectedActions.add(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_SECOND_ROUND[i]));
		}
		expectedActions.add(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterFlopBetTest() {
		for (int betValue : GPGameInfo.BETS_SECOND_ROUND) {
			GenericPokerGameState state = new GenericPokerGameState();
			Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
			List<Action> expectedActions = new LinkedList<Action>();

			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));
			state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
			state.performActionModifyingThisState(new GenericPokerAction("1", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));
			
			expectedActions.add(new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, betValue));
			expectedActions.add(new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
			
			if (GPGameInfo.MAX_RAISES_IN_ROW > 0)
				for (int raiseValue : GPGameInfo.RAISES_SECOND_ROUND) {
					expectedActions.add(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, raiseValue));
				}

			assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
		}
	}
	
	@Test
	public void afterFlopBetTestWithPair() {
		for (int betValue : GPGameInfo.BETS_SECOND_ROUND) {
			GenericPokerGameState state = new GenericPokerGameState();
			Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
			List<Action> expectedActions = new LinkedList<Action>();

			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));
			state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));
			
			if (GPGameInfo.MAX_RAISES_IN_ROW > 0)
				for (int i = GPGameInfo.RAISES_SECOND_ROUND.length - 1; i >= 0; i--) {
					expectedActions.add(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_SECOND_ROUND[i]));
				}
			expectedActions.add(new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, betValue));
			expectedActions.add(new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));

			assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
		}
	}
	
	@Test
	public void beginningOfTheGameTestReversed() {
		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		for (int cardValue : GPGameInfo.CARD_TYPES) {
			expectedActions.add(new GenericPokerAction(String.valueOf(cardValue), new SequenceInformationSet(state), GPGameInfo.NATURE, cardValue));
		}

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterCardsTestReversed() {
		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));

		for (int i = GPGameInfo.BETS_FIRST_ROUND.length - 1; i >= 0; i--) {
			expectedActions.add(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]));
		}

		expectedActions.add(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterBetInFirstRoundTestReversed() {

		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			GenericPokerGameState state = new GenericPokerGameState();
			Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
			List<Action> expectedActions = new LinkedList<Action>();

			state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));

			if (GPGameInfo.MAX_RAISES_IN_ROW > 0)
				for (int i = GPGameInfo.RAISES_FIRST_ROUND.length - 1; i >= 0; i--) {
					expectedActions.add(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[i]));
				}
			
			expectedActions.add(new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, betValue));
			expectedActions.add(new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));

			assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
		}
	}

	@Test
	public void afterCheckInFirstRoundTestReversed() {
		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

		for (int i = GPGameInfo.BETS_FIRST_ROUND.length - 1; i >= 0; i--) {
			expectedActions.add(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.BETS_FIRST_ROUND[i]));
		}

		expectedActions.add(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterRaiseInFirstRoundTestReversed() {

		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			for (int raiseValue : GPGameInfo.RAISES_FIRST_ROUND) {
				GenericPokerGameState state = new GenericPokerGameState();
				Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
				List<Action> expectedActions = new LinkedList<Action>();

				state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
				state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
				state.performActionModifyingThisState(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));
				state.performActionModifyingThisState(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, raiseValue));
				
				if (GPGameInfo.MAX_RAISES_IN_ROW > 1) {
					for (int i = GPGameInfo.RAISES_FIRST_ROUND.length - 1; i >= 0; i--) {
						expectedActions.add(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.RAISES_FIRST_ROUND[i]));
					}
				}

				expectedActions.add(new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, raiseValue));
				expectedActions.add(new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

				assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
			}
		}
	}

	@Test
	public void afterFlopTestReversed() {

		GenericPokerGameState state = new GenericPokerGameState();
		Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
		List<Action> expectedActions = new LinkedList<Action>();

		state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));
		state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
		state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));

		for (int i = GPGameInfo.BETS_SECOND_ROUND.length - 1; i >= 0; i--) {
			expectedActions.add(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, GPGameInfo.BETS_SECOND_ROUND[i]));
		}

		expectedActions.add(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));

		assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
	}

	@Test
	public void afterFlopBetTestReversed() {

		for (int betValue : GPGameInfo.BETS_SECOND_ROUND) {

			GenericPokerGameState state = new GenericPokerGameState();
			Expander<SequenceInformationSet> expander = new GenericPokerExpanderDomain<SequenceInformationSet>(null);
			List<Action> expectedActions = new LinkedList<Action>();

			state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction(String.valueOf(GPGameInfo.DECK[GPGameInfo.DECK.length - 1]), null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, 0));
			state.performActionModifyingThisState(new GenericPokerAction("ch", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));
			state.performActionModifyingThisState(new GenericPokerAction("0", null, GPGameInfo.NATURE, 0));
			state.performActionModifyingThisState(new GenericPokerAction("b", new SequenceInformationSet(state), GPGameInfo.FIRST_PLAYER, betValue));

			if (GPGameInfo.MAX_RAISES_IN_ROW > 0)
				for (int i = GPGameInfo.RAISES_SECOND_ROUND.length - 1; i >= 0; i--) {
					expectedActions.add(new GenericPokerAction("r", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, GPGameInfo.RAISES_SECOND_ROUND[i]));
				}
			
			expectedActions.add(new GenericPokerAction("c", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, betValue));
			expectedActions.add(new GenericPokerAction("f", new SequenceInformationSet(state), GPGameInfo.SECOND_PLAYER, 0));

			assertEquals(expectedActions, expander.getActions(new SequenceInformationSet(state)));
		}
	}

}
