package cz.agents.gtlibrary.domain.poker.generic;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GenericPokerExpanderDomain<I extends InformationSet> extends GenericPokerExpander<I> {
	
	public GenericPokerExpanderDomain(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	/**
	 * For player with card, which has value >= than highest card*ratio return aggressive move ordering {(r, c, f), (b, ch), ...}
	 * otherwise passive move ordering {(f, c, r), (ch, b), ...}
	 */
	private double ratio = 0.9;

	protected void addActionsAfterPasiveAction(GenericPokerGameState gpState, List<Action> actions, I informationSet) {
		if (isBetterToAddAggressiveFirst(gpState)) {
			addReversedBets(actions, gpState, informationSet);
			actions.add(createAction(gpState, 0, "ch", informationSet));
		} else {
			actions.add(createAction(gpState, 0, "ch", informationSet));
			addBets(actions, gpState, informationSet);
		}
	}

	private boolean isBetterToAddAggressiveFirst(GenericPokerGameState gpState) {
		return Integer.parseInt(gpState.getCardForActingPlayer().getActionType()) >= ((double)GPGameInfo.DECK[GPGameInfo.DECK.length - 1]) * ratio || hasPair(gpState);
	}

	private void addReversedBets(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		if (gpState.getRound() == 1) {
			addReversedFirstRoundBets(actions, gpState, informationSet);
			return;
		}
		if (gpState.getRound() == 3) {
			addReversedSecondRoundBets(actions, gpState, informationSet);
		}

	}

	private void addReversedFirstRoundBets(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int i = GPGameInfo.BETS_FIRST_ROUND.length - 1; i >= 0; i--) {
			actions.add(createAction(gpState, GPGameInfo.BETS_FIRST_ROUND[i], "b", informationSet));
		}
	}

	private void addReversedSecondRoundBets(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int i = GPGameInfo.BETS_SECOND_ROUND.length - 1; i >= 0; i--) {
			actions.add(createAction(gpState, GPGameInfo.BETS_SECOND_ROUND[i], "b", informationSet));
		}
	}

	protected void addActionsAfterAggressiveActions(GenericPokerGameState gpState, List<Action> actions, LinkedList<PokerAction> history, I informationSet) {
		if (isBetterToAddAggressiveFirst(gpState)) {
			if (gpState.getContinuousRaiseCount() < GPGameInfo.MAX_RAISES_IN_ROW)
				addReversedRaises(actions, gpState, informationSet);
			addCall(actions, gpState, history, informationSet);
			actions.add(createAction(gpState, 0, "f", informationSet));
		} else {
			addCall(actions, gpState, history, informationSet);
			actions.add(createAction(gpState, 0, "f", informationSet));
			if (gpState.getContinuousRaiseCount() < GPGameInfo.MAX_RAISES_IN_ROW)
				addRaises(actions, gpState, informationSet);
		}
	}

	private void addReversedRaises(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		if (gpState.getRound() == 1) {
			addReversedFirstRoundRaises(actions, gpState, informationSet);
			return;
		}
		if (gpState.getRound() == 3) {
			addReversedSecondRoundRaises(actions, gpState, informationSet);
		}
		
	}

	private void addReversedFirstRoundRaises(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int i = GPGameInfo.RAISES_FIRST_ROUND.length - 1; i >= 0; i--) {
			actions.add(createAction(gpState, GPGameInfo.RAISES_FIRST_ROUND[i], "r", informationSet));
		}
	}

	private void addReversedSecondRoundRaises(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int i = GPGameInfo.RAISES_SECOND_ROUND.length - 1; i >= 0; i--) {
			actions.add(createAction(gpState, GPGameInfo.RAISES_SECOND_ROUND[i], "r", informationSet));
		}
	}
	
	private boolean hasPair(GenericPokerGameState state) {
		return state.getCardForActingPlayer() != null && state.getCardForActingPlayer().equals(state.getTable());
	}

}
