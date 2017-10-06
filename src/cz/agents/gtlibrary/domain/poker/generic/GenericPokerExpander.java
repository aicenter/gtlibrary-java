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
import java.util.Random;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GenericPokerExpander<I extends InformationSet> extends ExpanderImpl<I> {
	
	private static final long serialVersionUID = 5112190454182040130L;

	public GenericPokerExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}
	
	@Override
	public List<Action> getActions(GameState gameState) {
		GenericPokerGameState gpState = (GenericPokerGameState) gameState;
		List<Action> actions = new LinkedList<>();

		if (gpState.isPlayerToMoveNature()) {
			addActionsOfNature(gpState, actions, getAlgorithmConfig().getInformationSetFor(gameState));
			return actions;
		}
		addActionsOfRegularPlayer(gpState, actions, getAlgorithmConfig().getInformationSetFor(gameState));
		return actions;
	}

	protected void addActionsOfRegularPlayer(GenericPokerGameState gpState, List<Action> actions, I informationSet) {
		LinkedList<PokerAction> history = gpState.getSequenceForAllPlayers();

		if (!gpState.isGameEnd()) {
			if (history.isEmpty() || history.getLast().getActionType().equals("ch")) {
				addActionsAfterPasiveAction(gpState, actions, informationSet);
			} else if (history.getLast().getActionType().equals("b")) {
				addActionsAfterAggressiveActions(gpState, actions, history, informationSet);
			} else if (history.getLast().getActionType().equals("r")) {
				addActionsAfterAggressiveActions(gpState, actions, history, informationSet);
			} else if (history.getLast().getActionType().equals("c")) {
				addActionsAfterPasiveAction(gpState, actions, informationSet);
			} else if (!history.getLast().getActionType().equals("f")) {
				addActionsAfterPasiveAction(gpState, actions, informationSet);
			}
		}
	}

	protected void addActionsAfterPasiveAction(GenericPokerGameState gpState, List<Action> actions, I informationSet) {
		addBets(actions, gpState, informationSet);
		actions.add(createAction(gpState, 0, "ch", informationSet));
	}

	protected void addActionsAfterAggressiveActions(GenericPokerGameState gpState, List<Action> actions, LinkedList<PokerAction> history, I informationSet) {
		addCall(actions, gpState, history, informationSet);
		if (gpState.getContinuousRaiseCount() < GPGameInfo.MAX_RAISES_IN_ROW)
			addRaises(actions, gpState, informationSet);
		actions.add(createAction(gpState, 0, "f", informationSet));
	}

	protected void addCall(List<Action> actions, GenericPokerGameState gpState, LinkedList<PokerAction> history, I informationSet) {
		actions.add(createAction(gpState, 0, "c", informationSet));
	}

	protected int getValueOfCall(GenericPokerGameState state) {
		if (state.getPlayerToMove().equals(GPGameInfo.FIRST_PLAYER))
			return 2 * state.getGainForFirstPlayer() - state.getPot();
		return 2 * (state.getPot() - state.getGainForFirstPlayer()) - state.getPot();
	}

	protected void addRaises(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		if (gpState.getRound() == 1) {
			addFirstRoundRaises(actions, gpState, informationSet);
			return;
		}
		if (gpState.getRound() == 3) {
			addSecondRoundRaises(actions, gpState, informationSet);
		}
	}

	protected void addSecondRoundRaises(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int raiseValue : GPGameInfo.RAISES_SECOND_ROUND) {
			actions.add(createAction(gpState, raiseValue, "r", informationSet));
		}
	}

	protected void addFirstRoundRaises(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int raiseValue : GPGameInfo.RAISES_FIRST_ROUND) {
			actions.add(createAction(gpState, raiseValue, "r", informationSet));
		}
	}

	protected void addBets(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		if (gpState.getRound() == 1) {
			addFirstRoundBets(actions, gpState, informationSet);
			return;
		}
		if (gpState.getRound() == 3) {
			addSecondRoundBets(actions, gpState, informationSet);
		}
	}

	protected void addSecondRoundBets(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int betValue : GPGameInfo.BETS_SECOND_ROUND) {
			actions.add(createAction(gpState, betValue, "b", informationSet));
		}
	}

	protected void addFirstRoundBets(List<Action> actions, GenericPokerGameState gpState, I informationSet) {
		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			actions.add(createAction(gpState, betValue, "b", informationSet));
		}
	}

	protected void addActionsOfNature(GenericPokerGameState gpState, List<Action> actions, I informationSet) {
		for (int cardValue : GPGameInfo.CARD_TYPES) {
			if (isCardAvailableInState(cardValue, gpState)) {
				actions.add(createAction(gpState, cardValue, String.valueOf(cardValue), informationSet));
			}
		}
	}

	protected PokerAction createAction(GenericPokerGameState state, int action, String actionString, I informationSet) {
		return new GenericPokerAction(actionString, informationSet, state.getPlayerToMove(), action);
	}

	protected boolean isCardAvailableInState(int cardValue, GenericPokerGameState state) {
		int[] cardCount = getCardCount(state);

		return cardCount[cardValue] > 0;
	}

	protected int[] getCardCount(GenericPokerGameState state) {
		int[] cardCount = new int[GPGameInfo.CARD_TYPES.length];

		for (int cardValue : GPGameInfo.DECK) {
			cardCount[cardValue]++;
		}
		if (state.getPlayerCards()[0] != null)
			cardCount[Integer.parseInt(state.getPlayerCards()[0].getActionType())]--;
		if (state.getPlayerCards()[1] != null)
			cardCount[Integer.parseInt(state.getPlayerCards()[1].getActionType())]--;
		if (state.getTable() != null)
			cardCount[Integer.parseInt(state.getTable().getActionType())]--;
		return cardCount;
	}

}
