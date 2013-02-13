package cz.agents.gtlibrary.domain.poker.generic;

import java.util.LinkedList;
import java.util.List;

import cz.agents.gtlibrary.domain.poker.PokerAction;
import cz.agents.gtlibrary.iinodes.ExpanderImpl;
import cz.agents.gtlibrary.interfaces.Action;
import cz.agents.gtlibrary.interfaces.AlgorithmConfig;
import cz.agents.gtlibrary.interfaces.GameState;
import cz.agents.gtlibrary.interfaces.InformationSet;

public class GenericPokerExpander<I extends InformationSet> extends ExpanderImpl<I> {
	
	public GenericPokerExpander(AlgorithmConfig<I> algConfig) {
		super(algConfig);
	}
	
	@Override
	public List<Action> getActions(GameState gameState) {
		GenericPokerGameState gpState = (GenericPokerGameState) gameState;
		List<Action> actions = new LinkedList<Action>();

		if (gpState.isPlayerToMoveNature()) {
			addActionsOfNature(gameState, gpState, actions);
			return actions;
		}
		addActionsOfRegularPlayer(gpState, actions);
		return actions;
	}

	protected void addActionsOfRegularPlayer(GenericPokerGameState gpState, List<Action> actions) {
		LinkedList<PokerAction> history = gpState.getSequenceForAllPlayers();

		if (!gpState.isGameEnd()) {
			if (history.isEmpty() || history.getLast().getActionType().equals("ch")) {
				addActionsAfterPasiveAction(gpState, actions);
			} else if (history.getLast().getActionType().equals("b")) {
				addActionsAfterAggressiveActions(gpState, actions, history);
			} else if (history.getLast().getActionType().equals("r")) {
				addActionsAfterAggressiveActions(gpState, actions, history);
			} else if (history.getLast().getActionType().equals("c")) {
				addActionsAfterPasiveAction(gpState, actions);
			} else if (!history.getLast().getActionType().equals("f")) {
				addActionsAfterPasiveAction(gpState, actions);
			}
		}
	}

	protected void addActionsAfterPasiveAction(GenericPokerGameState gpState, List<Action> actions) {
		addBets(actions, gpState);
		actions.add(createAction(gpState, 0, "ch"));
	}

	protected void addActionsAfterAggressiveActions(GenericPokerGameState gpState, List<Action> actions, LinkedList<PokerAction> history) {
		addCall(actions, gpState, history);
		if (gpState.getContinuousRaiseCount() < GPGameInfo.MAX_RAISES_IN_ROW)
			addRaises(actions, gpState);
		actions.add(createAction(gpState, 0, "f"));
	}

	protected void addCall(List<Action> actions, GenericPokerGameState gpState, LinkedList<PokerAction> history) {
		actions.add(createAction(gpState, getValueOfCall(gpState), "c"));
	}

	protected int getValueOfCall(GenericPokerGameState state) {
		if (state.getPlayerToMove().equals(GPGameInfo.FIRST_PLAYER))
			return 2 * state.getGainForFirstPlayer() - state.getPot();
		return 2 * (state.getPot() - state.getGainForFirstPlayer()) - state.getPot();
	}

	protected void addRaises(List<Action> actions, GenericPokerGameState gpState) {
		if (gpState.getRound() == 1) {
			addFirstRoundRaises(actions, gpState);
			return;
		}
		if (gpState.getRound() == 3) {
			addSecondRoundRaises(actions, gpState);
		}
	}

	protected void addSecondRoundRaises(List<Action> actions, GenericPokerGameState gpState) {
		for (int raiseValue : GPGameInfo.RAISES_SECOND_ROUND) {
			actions.add(createAction(gpState, raiseValue, "r"));
		}
	}

	protected void addFirstRoundRaises(List<Action> actions, GenericPokerGameState gpState) {
		for (int raiseValue : GPGameInfo.RAISES_FIRST_ROUND) {
			actions.add(createAction(gpState, raiseValue, "r"));
		}
	}

	protected void addBets(List<Action> actions, GenericPokerGameState gpState) {
		if (gpState.getRound() == 1) {
			addFirstRoundBets(actions, gpState);
			return;
		}
		if (gpState.getRound() == 3) {
			addSecondRoundBets(actions, gpState);
		}
	}

	protected void addSecondRoundBets(List<Action> actions, GenericPokerGameState gpState) {
		for (int betValue : GPGameInfo.BETS_SECOND_ROUND) {
			actions.add(createAction(gpState, betValue, "b"));
		}
	}

	protected void addFirstRoundBets(List<Action> actions, GenericPokerGameState gpState) {
		for (int betValue : GPGameInfo.BETS_FIRST_ROUND) {
			actions.add(createAction(gpState, betValue, "b"));
		}
	}

	protected void addActionsOfNature(GameState gameState, GenericPokerGameState gpState, List<Action> actions) {
		for (int cardValue : GPGameInfo.CARD_TYPES) {
			if (isCardAvailableInState(cardValue, (GenericPokerGameState) gameState)) {
				actions.add(createAction(gpState, cardValue, String.valueOf(cardValue)));
			}
		}
	}

	protected PokerAction createAction(GenericPokerGameState state, int action, String actionString) {
		return new GenericPokerAction(actionString, getAlgorithmConfig().getInformationSetFor(state), state.getPlayerToMove(), action);
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

	@Override
	public List<Action> getActions(InformationSet informationSet) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

}
