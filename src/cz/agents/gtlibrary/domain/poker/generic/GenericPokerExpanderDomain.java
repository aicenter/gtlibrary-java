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


package cz.agents.gtlibrary.domain.poker.generic;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GenericPokerExpanderDomain<I extends InformationSet> extends GenericPokerExpander<I> {
	
	private static final long serialVersionUID = 7535153881793134220L;

	public GenericPokerExpanderDomain(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}

	/**
	 * For player with card, which has reward >= than highest card*ratio return aggressive move ordering {(r, c, f), (b, ch), ...}
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
